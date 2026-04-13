package com.dogecoding.android_components.cloud.storage

import java.io.InputStream
import java.io.OutputStream

interface CloudStorageProvider {
    suspend fun listFiles(folderPath: String): List<CloudFile>
    suspend fun readFile(filePath: String, outputStream: OutputStream)
    suspend fun writeFile(filePath: String, inputStream: InputStream, mimeType: String = "application/octet-stream")
    suspend fun deleteFile(filePath: String)
    suspend fun createFolder(folderPath: String)
}

data class CloudFile(
    val name: String,
    val id: String,
    val isFolder: Boolean,
    val mimeType: String,
    val size: Long? = null,
    val modifiedTime: Long? = null
)
