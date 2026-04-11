package com.dogecoding.android_components.cloud.storage

import com.microsoft.graph.models.DriveItem
import com.microsoft.graph.models.Folder
import com.microsoft.graph.serviceclient.GraphServiceClient
import com.microsoft.identity.client.AcquireTokenSilentParameters
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.kiota.authentication.AccessTokenProvider
import com.microsoft.kiota.authentication.AllowedHostsValidator
import android.util.Log
import com.microsoft.kiota.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.URI

class OneDriveStorageProvider(private val graphClient: GraphServiceClient) : CloudStorageProvider {

    private var cachedDriveId: String? = null

    private suspend fun getDriveId(): String {
        cachedDriveId?.let { return it }
        return withContext(Dispatchers.IO) {
            val driveId = graphClient.me().drive().get()?.id ?: "root"
            cachedDriveId = driveId
            driveId
        }
    }

    private fun normalizePath(path: String): String {
        return path.trim('/').replace("\\", "/")
    }

    override suspend fun listFiles(folderPath: String): List<CloudFile> =
        withContext(Dispatchers.IO) {
            val driveId = getDriveId()
            val normalized = normalizePath(folderPath)
            val driveItems = if (normalized.isEmpty() || normalized == "root") {
                graphClient.drives().byDriveId(driveId).items().byDriveItemId("root").children().get()
            } else {
                graphClient.drives().byDriveId(driveId).items().byDriveItemId("root:/$normalized:").children().get()
            }

            driveItems?.value?.map { item ->
                CloudFile(
                    name = item.name ?: "",
                    id = item.id ?: "",
                    isFolder = item.folder != null,
                    mimeType = item.file?.mimeType ?: "application/octet-stream",
                    size = item.size ?: 0L,
                    modifiedTime = item.lastModifiedDateTime?.toInstant()?.toEpochMilli()
                )
            } ?: emptyList()
        }

    override suspend fun readFile(filePath: String, outputStream: OutputStream) {
        withContext(Dispatchers.IO) {
            val driveId = getDriveId()
            val path = normalizePath(filePath)
            val inputStream = graphClient.drives().byDriveId(driveId).items().byDriveItemId("root:/$path:").content().get()
            inputStream?.use { it.copyTo(outputStream) }
        }
    }

    override suspend fun writeFile(filePath: String, inputStream: InputStream, mimeType: String) {
        withContext(Dispatchers.IO) {
            val driveId = getDriveId()
            val path = normalizePath(filePath)
            // Microsoft Graph SDK v6 put() takes an InputStream
            graphClient.drives().byDriveId(driveId).items().byDriveItemId("root:/$path:").content().put(inputStream)
        }
    }

    override suspend fun deleteFile(filePath: String) {
        withContext(Dispatchers.IO) {
            val driveId = getDriveId()
            val path = normalizePath(filePath)
            graphClient.drives().byDriveId(driveId).items().byDriveItemId("root:/$path:").delete()
        }
    }

    override suspend fun createFolder(folderPath: String) {
        withContext(Dispatchers.IO) {
            val driveId = getDriveId()
            val normalized = normalizePath(folderPath)
            if (normalized.isEmpty()) return@withContext

            val parts = normalized.split('/')
            var currentPath = ""

            for (part in parts) {
                val nextPath = if (currentPath.isEmpty()) part else "$currentPath/$part"
                try {
                    // Use a more efficient check if possible, or just catch 404
                    graphClient.drives().byDriveId(driveId).items().byDriveItemId("root:/$nextPath:").get()
                } catch (e: ApiException) {
                    if (e.responseStatusCode == 404) {
                        val folderItem = DriveItem().apply {
                            name = part
                            folder = Folder()
                        }

                        if (currentPath.isEmpty()) {
                            graphClient.drives().byDriveId(driveId).items().byDriveItemId("root").children().post(folderItem)
                        } else {
                            graphClient.drives().byDriveId(driveId).items().byDriveItemId("root:/$currentPath:").children().post(folderItem)
                        }
                    } else {
                        throw e
                    }
                } catch (e: Exception) {
                    throw e
                }
                currentPath = nextPath
            }
        }
    }
}

class OneDriveAccessTokenProvider(
    private val msalApp: ISingleAccountPublicClientApplication,
    private val scopes: List<String>
) : AccessTokenProvider {
    private val TAG = "OneDriveAuth"

    override fun getAuthorizationToken(
        uri: URI,
        additionalAuthenticationContext: Map<String, Any>?
    ): String {
        return try {
            Log.d(TAG, "getAuthorizationToken called for: $uri")
            val currentAccountResult = msalApp.currentAccount
            val account = currentAccountResult.currentAccount

            if (account == null) {
                Log.e(TAG, "No account found in getCurrentAccount(). Account result changed: ${currentAccountResult.didAccountChange()}")
                return ""
            }

            Log.d(TAG, "Account found: ${account.username}, authority: ${account.authority}")

            // Ensure authority is not null or empty
            val authority = if (!account.authority.isNullOrBlank()) {
                account.authority
            } else {
                Log.w(TAG, "Account authority is null/blank. Falling back to 'common' authority.")
                "https://login.microsoftonline.com/common"
            }

            val builder = AcquireTokenSilentParameters.Builder()
                .withScopes(scopes)
                .forAccount(account)
                .fromAuthority(authority)
                .forceRefresh(false) // Use cached token if possible

            Log.d(TAG, "Acquiring token silently for scopes: $scopes from authority: $authority")
            val result = msalApp.acquireTokenSilent(builder.build())

            Log.d(TAG, "Successfully acquired token silently. Token length: ${result.accessToken.length}")
            result.accessToken
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get auth token silently: ${e.message}")
            ""
        }
    }

    override fun getAllowedHostsValidator(): AllowedHostsValidator {
        return AllowedHostsValidator("graph.microsoft.com")
    }
}
