package com.dogecoding.android_components.cloud

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dogecoding.android_components.cloud.preferences.CloudPreferences
import com.dogecoding.android_components.cloud.storage.CloudStorageProvider
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.exception.MsalException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

abstract class CloudStorageViewModel(application: Application) : AndroidViewModel(application) {
    protected val cloudPreferences = CloudPreferences(application)
    private val setupMutex = Mutex()

    val activeProvider: StateFlow<CloudStorageProvider?> = CloudManager.activeProvider

    private val _cloudProvider = MutableStateFlow(cloudPreferences.getCloudProvider())
    val cloudProvider: StateFlow<String> = _cloudProvider.asStateFlow()

    private val _cloudUsername = MutableStateFlow(cloudPreferences.getCloudUsername())
    val cloudUsername: StateFlow<String?> = _cloudUsername.asStateFlow()

    private val _cloudPath = MutableStateFlow(cloudPreferences.getCloudPath())
    val cloudPath: StateFlow<String?> = _cloudPath.asStateFlow()

    private val _errorEvents = MutableSharedFlow<String>()
    val errorEvents: SharedFlow<String> = _errorEvents.asSharedFlow()

    private val _successEvents = MutableSharedFlow<String>()
    val successEvents: SharedFlow<String> = _successEvents.asSharedFlow()

    private val scopes = listOf("Files.ReadWrite")

    private var isLoggingIn = false

    abstract fun getRootName(): String
    abstract fun getSubPath(): String

    init {
        tryAutoLogin()
    }

    private fun tryAutoLogin() {
        val savedProvider = cloudPreferences.getCloudProvider()
        val savedUsername = cloudPreferences.getCloudUsername()
        
        Log.i("CloudStorageViewModel", "tryAutoLogin: savedProvider=$savedProvider, savedUsername=$savedUsername")
        
        if (savedProvider == CloudPreferences.PROVIDER_GOOGLE && savedUsername != null) {
            if (activeProvider.value == null) {
                CloudManager.initGoogleDrive(getApplication(), savedUsername)
                setupDefaultFolder()
            }
        } else {
            viewModelScope.launch {
                CloudManager.restoreOneDriveSession(
                    getApplication(),
                    scopes,
                    onSuccess = { result ->
                        val email = result.account.username
                        _cloudUsername.value = email
                        _cloudProvider.value = CloudPreferences.PROVIDER_ONEDRIVE
                        cloudPreferences.saveCloudProvider(CloudPreferences.PROVIDER_ONEDRIVE)
                        cloudPreferences.saveCloudUsername(email)
                        setupDefaultFolder()
                    },
                    onError = { exception ->
                        val isTerminal = exception.errorCode == "invalid_grant" || 
                                       exception.errorCode == "interaction_required" ||
                                       exception.message?.contains("interaction", ignoreCase = true) == true
                        
                        if (isTerminal && savedProvider == CloudPreferences.PROVIDER_ONEDRIVE) {
                            logout()
                        }
                    }
                )
            }
        }
    }

    fun loginGoogle(context: Context) {
        viewModelScope.launch {
            try {
                val credentialManager = CloudManager.getCredentialManager(context)
                val result = credentialManager.getCredential(
                    context,
                    CloudManager.getCredentialRequest(context)
                )

                val account = CloudManager.getAccountFromCredential(result)
                val email = CloudManager.getEmailFromCredential(result)

                if (account != null) {
                    CloudManager.initGoogleDrive(context, account)
                    cloudPreferences.saveCloudProvider(CloudPreferences.PROVIDER_GOOGLE)
                    cloudPreferences.saveCloudUsername(email)

                    _cloudProvider.value = CloudPreferences.PROVIDER_GOOGLE
                    _cloudUsername.value = email

                    setupDefaultFolder()
                    _successEvents.emit("Signed into Google Drive")
                }
            } catch (e: Exception) {
                _errorEvents.emit("Sign-in failed: ${e.message}")
            }
        }
    }

    fun loginOneDrive(activity: Activity) {
        if (isLoggingIn) return
        isLoggingIn = true
        
        viewModelScope.launch {
            CloudManager.signInOneDrive(
                activity,
                scopes,
                onSuccess = { result ->
                    isLoggingIn = false
                    val email = result.account.username
                    cloudPreferences.saveCloudProvider(CloudPreferences.PROVIDER_ONEDRIVE)
                    cloudPreferences.saveCloudUsername(email)
                    _cloudProvider.value = CloudPreferences.PROVIDER_ONEDRIVE
                    _cloudUsername.value = email
                    setupDefaultFolder()
                    viewModelScope.launch { _successEvents.emit("Signed into OneDrive") }
                },
                onError = { exception ->
                    isLoggingIn = false
                    viewModelScope.launch { _errorEvents.emit("OneDrive sign-in failed: ${exception.message}") }
                },
                onCancel = {
                    isLoggingIn = false
                    viewModelScope.launch { _errorEvents.emit("OneDrive sign-in cancelled") }
                }
            )
        }
    }

    fun logoutCloud(context: Context) {
        viewModelScope.launch {
            if (cloudProvider.value == CloudPreferences.PROVIDER_GOOGLE) {
                CloudManager.signOut(context)
            } else if (cloudProvider.value == CloudPreferences.PROVIDER_ONEDRIVE) {
                val msalApp = CloudManager.getMsalApp(context)
                msalApp?.signOut(object : ISingleAccountPublicClientApplication.SignOutCallback {
                    override fun onSignOut() { Log.d("CloudStorageViewModel", "OneDrive signed out") }
                    override fun onError(exception: MsalException) { Log.e("CloudStorageViewModel", "OneDrive sign out error", exception) }
                })
            }
            logout()
        }
    }

    private fun setupDefaultFolder() {
        val root = getRootName()
        val sub = getSubPath()
        val fullPath = if (sub.isEmpty()) root else "$root/$sub"
        setupFolderByName(fullPath)
    }

    fun setupFolderByName(folderName: String) {
        viewModelScope.launch {
            setupMutex.withLock {
                if (cloudPath.value == folderName || cloudProvider.value == CloudPreferences.PROVIDER_NONE) return@withLock

                val provider = withTimeoutOrNull(15000) {
                    activeProvider.first { it != null }
                }

                if (provider != null) {
                    try {
                        provider.createFolder(folderName)
                        updateCloudPath(folderName)
                    } catch (e: Exception) {
                        handleCloudError(e, "folder setup ($folderName)")
                    }
                }
            }
        }
    }

    protected open fun handleCloudError(e: Exception, operation: String) {
        Log.e("CloudStorageViewModel", "Error during $operation: ${e.message}", e)
        val message = e.message?.lowercase() ?: ""

        val isTerminalError = message.contains("auth") || message.contains("token") ||
                             message.contains("sign in") || message.contains("unauthorized") ||
                             message.contains("forbidden") || message.contains("msal") ||
                             message.contains("expired") || message.contains("invalid_grant") ||
                             message.contains("interaction_required") || message.contains("401") ||
                             message.contains("403")

        if (isTerminalError) {
            logout()
        }
    }

    fun updateCloudPath(path: String?) {
        cloudPreferences.saveCloudPath(path)
        _cloudPath.value = path
    }

    fun logout() {
        CloudManager.logout()
        cloudPreferences.clearCloudSession()

        _cloudProvider.value = CloudPreferences.PROVIDER_NONE
        _cloudUsername.value = null
        _cloudPath.value = null

        viewModelScope.launch {
            try {
                val msalApp = CloudManager.getMsalApp(getApplication())
                msalApp?.signOut(object : ISingleAccountPublicClientApplication.SignOutCallback {
                    override fun onSignOut() {}
                    override fun onError(exception: MsalException) {}
                })
            } catch (e: Exception) {}
        }
    }
}
