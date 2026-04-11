package com.dogecoding.android_components.cloud

import android.accounts.Account
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.dogecoding.android_components.cloud.storage.CloudStorageProvider
import com.dogecoding.android_components.cloud.storage.GDriveStorageProvider
import com.dogecoding.android_components.cloud.storage.OneDriveAccessTokenProvider
import com.dogecoding.android_components.cloud.storage.OneDriveStorageProvider
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.microsoft.graph.serviceclient.GraphServiceClient
import com.microsoft.identity.client.AcquireTokenParameters
import com.microsoft.identity.client.AcquireTokenSilentParameters
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.IPublicClientApplication
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.Prompt
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.SignInParameters
import com.microsoft.identity.client.SilentAuthenticationCallback
import com.microsoft.identity.client.exception.MsalException
import com.microsoft.kiota.authentication.BaseBearerTokenAuthenticationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.coroutines.resume

object CloudManager {

    private const val TAG = "CloudManager"

    private val _activeProvider = MutableStateFlow<CloudStorageProvider?>(null)
    val activeProvider = _activeProvider.asStateFlow()

    private val msalMutex = Mutex()
    private var msalApp: ISingleAccountPublicClientApplication? = null

    suspend fun getMsalApp(context: Context): ISingleAccountPublicClientApplication? {
        msalApp?.let { return it }

        return msalMutex.withLock {
            // Double-check after acquiring lock
            msalApp?.let { return@withLock it }

            suspendCancellableCoroutine<ISingleAccountPublicClientApplication?> { continuation ->
                val resId = getResourceId(context, "auth_config_single_account", "raw")
                if (resId == 0) {
                    Log.e(TAG, "Resource 'auth_config_single_account' not found in raw folder")
                    if (continuation.isActive) continuation.resume(null)
                    return@suspendCancellableCoroutine
                }

                PublicClientApplication.createSingleAccountPublicClientApplication(
                    context.applicationContext,
                    resId,
                    object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                        override fun onCreated(application: ISingleAccountPublicClientApplication) {
                            Log.d(TAG, "MSAL Application created successfully")
                            msalApp = application
                            if (continuation.isActive) continuation.resume(application)
                        }

                        override fun onError(exception: MsalException) {
                            Log.e(TAG, "MSAL Initialization error: ${exception.message}", exception)
                            if (continuation.isActive) continuation.resume(null)
                        }
                    }
                )
            }
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun getResourceId(context: Context, name: String, defType: String): Int {
        return context.resources.getIdentifier(name, defType, context.packageName)
    }

    fun getCredentialManager(context: Context): CredentialManager {
        return CredentialManager.create(context)
    }

    fun getGoogleIdOption(context: Context): GetGoogleIdOption {
        val clientId = try {
            val jsonString =
                context.assets.open("google-services.json").bufferedReader().use { it.readText() }
            val json = JSONObject(jsonString)
            // Try "web" first (required for ID tokens), then fall back to "installed"
            if (json.has("web")) {
                json.getJSONObject("web").getString("client_id")
            } else {
                json.getJSONObject("installed").getString("client_id")
            }
        } catch (e: Exception) {
            throw e
        }

        return GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(true)
            .setServerClientId(clientId)
            .build()
    }

    fun getCredentialRequest(context: Context): GetCredentialRequest {
        return GetCredentialRequest.Builder()
            .addCredentialOption(getGoogleIdOption(context))
            .build()
    }

    fun getAccountFromCredential(result: GetCredentialResponse): Account? {
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            return Account(googleIdTokenCredential.id, "com.google")
        }
        return null
    }

    fun getEmailFromCredential(result: GetCredentialResponse): String? {
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            return googleIdTokenCredential.id
        }
        return null
    }

    suspend fun signOut(context: Context) {
        val credentialManager = getCredentialManager(context)
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
        logout()
    }

    fun initGoogleDrive(context: Context, accountName: String) {
        val account = Account(accountName, "com.google")
        initGoogleDrive(context, account)
    }

    fun initGoogleDrive(context: Context, account: Account) {
        val credential = GoogleAccountCredential.usingOAuth2(
            context.applicationContext, listOf(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account

        val driveService = Drive.Builder(
            NetHttpTransport(),
            GsonFactory(),
            credential
        )
            .setApplicationName(
                context.applicationInfo.loadLabel(context.packageManager).toString()
            )
            .build()

        _activeProvider.value = GDriveStorageProvider(driveService)
    }

    fun initOneDrive(graphClient: GraphServiceClient) {
        _activeProvider.value = OneDriveStorageProvider(graphClient)
    }

    suspend fun restoreOneDriveSession(
        context: Context,
        scopes: List<String>,
        onSuccess: (IAuthenticationResult) -> Unit,
        onError: (MsalException) -> Unit
    ) {
        val msalApp = getMsalApp(context) ?: return

        val currentAccountResult = withContext(Dispatchers.IO) { msalApp.currentAccount }
        val account = currentAccountResult.currentAccount ?: return

        val authority = account.authority.ifBlank {
            "https://login.microsoftonline.com/common"
        }

        val silentParameters = AcquireTokenSilentParameters.Builder()
            .withScopes(scopes)
            .forAccount(account)
            .fromAuthority(authority)
            .withCallback(object : SilentAuthenticationCallback {
                override fun onSuccess(authenticationResult: IAuthenticationResult) {
                    val graphClient = createGraphClient(msalApp, scopes)
                    initOneDrive(graphClient)
                    onSuccess(authenticationResult)
                }

                override fun onError(exception: MsalException) {
                    onError(exception)
                }
            })
            .build()

        msalApp.acquireTokenSilentAsync(silentParameters)
    }

    suspend fun signInOneDrive(
        activity: Activity,
        scopes: List<String>,
        onSuccess: (IAuthenticationResult) -> Unit,
        onError: (MsalException) -> Unit,
        onCancel: () -> Unit
    ) {
        val msalApp = getMsalApp(activity) ?: return

        // Force sign-out for clean state
        msalApp.signOut(object : ISingleAccountPublicClientApplication.SignOutCallback {
            override fun onSignOut() {
                performSignIn(activity, msalApp, scopes, onSuccess, onError, onCancel)
            }

            override fun onError(exception: MsalException) {
                performSignIn(activity, msalApp, scopes, onSuccess, onError, onCancel)
            }
        })
    }

    private fun performSignIn(
        activity: Activity,
        msalApp: ISingleAccountPublicClientApplication,
        scopes: List<String>,
        onSuccess: (IAuthenticationResult) -> Unit,
        onError: (MsalException) -> Unit,
        onCancel: () -> Unit
    ) {
        val params = SignInParameters.builder()
            .withActivity(activity)
            .withScopes(scopes)
            .withPrompt(Prompt.SELECT_ACCOUNT)
            .withCallback(object : AuthenticationCallback {
                override fun onSuccess(result: IAuthenticationResult) {
                    val graphClient = createGraphClient(msalApp, scopes)
                    initOneDrive(graphClient)
                    onSuccess(result)
                }

                override fun onError(exception: MsalException) {
                    if (exception.errorCode == "already_signed_in" || exception.message?.contains(
                            "mismatch",
                            true
                        ) == true
                    ) {
                        performFallbackSignIn(
                            activity,
                            msalApp,
                            scopes,
                            onSuccess,
                            onError,
                            onCancel
                        )
                    } else {
                        onError(exception)
                    }
                }

                override fun onCancel() = onCancel()
            })
            .build()
        msalApp.signIn(params)
    }

    private fun performFallbackSignIn(
        activity: Activity,
        msalApp: ISingleAccountPublicClientApplication,
        scopes: List<String>,
        onSuccess: (IAuthenticationResult) -> Unit,
        onError: (MsalException) -> Unit,
        onCancel: () -> Unit
    ) {
        val params = AcquireTokenParameters.Builder()
            .startAuthorizationFromActivity(activity)
            .withScopes(scopes)
            .withPrompt(Prompt.SELECT_ACCOUNT)
            .withCallback(object : AuthenticationCallback {
                override fun onSuccess(result: IAuthenticationResult) {
                    val graphClient = createGraphClient(msalApp, scopes)
                    initOneDrive(graphClient)
                    onSuccess(result)
                }

                override fun onError(exception: MsalException) = onError(exception)
                override fun onCancel() = onCancel()
            })
            .build()
        msalApp.acquireToken(params)
    }

    fun createGraphClient(
        msalApp: ISingleAccountPublicClientApplication,
        scopes: List<String>
    ): GraphServiceClient {
        val tokenProvider = OneDriveAccessTokenProvider(msalApp, scopes)
        val authProvider = BaseBearerTokenAuthenticationProvider(tokenProvider)
        return GraphServiceClient(authProvider)
    }

    fun logout() {
        _activeProvider.value = null
    }
}
