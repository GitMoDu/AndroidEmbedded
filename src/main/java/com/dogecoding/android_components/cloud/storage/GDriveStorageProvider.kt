package com.dogecoding.android_components.cloud.storage

import com.google.api.client.http.InputStreamContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

class GDriveStorageProvider(private val driveService: Drive) : CloudStorageProvider {

    override suspend fun listFiles(folderPath: String): List<CloudFile> = withContext(Dispatchers.IO) {
        val folderId = resolveFolderId(folderPath) ?: return@withContext emptyList()
        
        val result = driveService.files().list()
            .setQ("'$folderId' in parents and trashed = false")
            .setFields("files(id, name, mimeType, size, modifiedTime, trashed)")
            .execute()

        result.files.map { file ->
            CloudFile(
                name = file.name,
                id = file.id,
                isFolder = file.mimeType == "application/vnd.google-apps.folder",
                mimeType = file.mimeType,
                size = file.getSize(),
                modifiedTime = file.modifiedTime?.value
            )
        }
    }

    override suspend fun readFile(filePath: String, outputStream: OutputStream) = withContext(Dispatchers.IO) {
        val fileId = resolveFileId(filePath) ?: throw Exception("File not found: $filePath")
        driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
    }

    override suspend fun writeFile(filePath: String, inputStream: InputStream, mimeType: String) = withContext(Dispatchers.IO) {
        val fileName = filePath.substringAfterLast('/')
        val parentPath = if (filePath.contains('/')) filePath.substringBeforeLast('/') else ""
        val parentId = if (parentPath.isNotEmpty()) {
            resolveFolderId(parentPath, createIfMissing = true)
        } else {
            "root"
        }

        val existingFileId = resolveFileId(filePath)
        val contentStream = InputStreamContent(mimeType, inputStream)

        if (existingFileId != null) {
            // Update existing file
            driveService.files().update(existingFileId, null, contentStream).execute()
        } else {
            // Create new file
            val fileMetadata = File().apply {
                name = fileName
                parents = listOf(parentId)
            }
            driveService.files().create(fileMetadata, contentStream).execute()
        }
        Unit
    }

    override suspend fun deleteFile(filePath: String) = withContext(Dispatchers.IO) {
        val fileId = resolveFileId(filePath) ?: return@withContext
        driveService.files().delete(fileId).execute()
        Unit
    }

    override suspend fun createFolder(folderPath: String) = withContext(Dispatchers.IO) {
        resolveFolderId(folderPath, createIfMissing = true)
        Unit
    }

    private fun resolveFolderId(path: String, createIfMissing: Boolean = false): String? {
        if (path == "" || path == "/" || path == "root") return "root"
        
        val parts = path.trim('/').split('/')
        var currentParentId = "root"

        for (part in parts) {
            val query = "name = '$part' and '$currentParentId' in parents and mimeType = 'application/vnd.google-apps.folder' and trashed = false"
            val result = driveService.files().list()
                .setQ(query)
                .setFields("files(id)")
                .execute()

            val folder = result.files.firstOrNull()
            if (folder != null) {
                currentParentId = folder.id
            } else if (createIfMissing) {
                val newFolderMetadata = File().apply {
                    name = part
                    mimeType = "application/vnd.google-apps.folder"
                    parents = listOf(currentParentId)
                }
                val newFolder = driveService.files().create(newFolderMetadata)
                    .setFields("id")
                    .execute()
                currentParentId = newFolder.id
            } else {
                return null
            }
        }
        return currentParentId
    }

    private fun resolveFileId(path: String): String? {
        val fileName = path.substringAfterLast('/')
        val parentPath = if (path.contains('/')) path.substringBeforeLast('/') else ""
        val parentId = resolveFolderId(parentPath) ?: return null

        val query = "name = '$fileName' and '$parentId' in parents and trashed = false"
        val result = driveService.files().list()
            .setQ(query)
            .setFields("files(id)")
            .execute()

        return result.files.firstOrNull()?.id
    }
}
