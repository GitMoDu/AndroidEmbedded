package com.dogecoding.android_embedded.inertia.components.log

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.dogecoding.android_components.cloud.CloudStorageViewModel
import com.dogecoding.android_components.cloud.preferences.CloudPreferences
import com.dogecoding.android_embedded.R
import com.dogecoding.android_embedded.inertia.components.log.database.LogDatabase
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.iterator

class LogCloudViewModel(application: Application) : CloudStorageViewModel(application) {

    private val logDao = LogDatabase.getDatabase(application).logDao()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncCount = MutableStateFlow(0)
    val syncCount: StateFlow<Int> = _syncCount.asStateFlow()

    private val _syncFinishedEvent = MutableSharedFlow<Boolean>()
    val syncFinishedEvent: SharedFlow<Boolean> = _syncFinishedEvent.asSharedFlow()

    override fun getRootName(): String {
        return getApplication<Application>().getString(R.string.app_name)
    }

    override fun getSubPath(): String {
        return "Log"
    }

    fun syncLogs() {
        viewModelScope.launch {
            if (_isSyncing.value) return@launch

            val currentProviderType = cloudProvider.value
            if (currentProviderType == CloudPreferences.Companion.PROVIDER_NONE) return@launch

            val provider = withTimeoutOrNull(5000) {
                activeProvider.first { it != null }
            }

            if (provider == null) {
                Log.w("LogCloudViewModel", "Sync aborted: No active cloud provider")
                return@launch
            }

            var allSuccess = true
            try {
                // 1. SET UPLOADING STATE FIRST
                _isSyncing.value = true

                // 2. Wait for database writes to finish (important!)
                delay(1000)

                // 3. Query logs
                val unsynced = withContext(Dispatchers.IO) {
                    logDao.getUnsyncedLogs()
                }

                if (unsynced.isEmpty()) {
                    Log.d("LogCloudViewModel", "No unsynced logs found after delay.")
                    _syncCount.value = 0
                    allSuccess = true // Nothing to sync is technically success
                    return@launch
                }

                _syncCount.value = unsynced.size
                Log.d("LogCloudViewModel", "Starting sync of ${unsynced.size} logs")

                var path = cloudPath.value
                if (path == null) {
                    val expectedPath = getRootName() + "/" + getSubPath()
                    setupFolderByName(expectedPath)
                    path = withTimeoutOrNull(10000) { cloudPath.first { it != null } }
                }

                if (path == null) {
                    Log.e("LogCloudViewModel", "Failed to resolve cloud path")
                    allSuccess = false
                    return@launch
                }

                val existingFiles = try {
                    provider.listFiles(path)
                } catch (e: Exception) {
                    handleCloudError(e, "listing files")
                    allSuccess = false
                    return@launch
                }

                val sessions = unsynced.groupBy { it.bootId }
                val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

                for ((bootId, sessionLogs) in sessions) {
                    if (!_isSyncing.value) {
                        allSuccess = false
                        break
                    }

                    // Sort logs by recordId to find continuous batches
                    val sortedLogs = sessionLogs.sortedBy { it.recordId }
                    val batches = mutableListOf<List<LogDbRecord>>()

                    if (sortedLogs.isNotEmpty()) {
                        var currentBatch = mutableListOf<LogDbRecord>()
                        currentBatch.add(sortedLogs[0])

                        for (i in 1 until sortedLogs.size) {
                            val prev = sortedLogs[i - 1]
                            val curr = sortedLogs[i]

                            // Check if ID is continuous (difference is 1)
                            if (curr.recordId == prev.recordId + 1) {
                                currentBatch.add(curr)
                            } else {
                                batches.add(currentBatch)
                                currentBatch = mutableListOf(curr)
                            }
                        }
                        batches.add(currentBatch)
                    }

                    for (logs in batches) {
                        val firstLog = logs.minByOrNull { it.getInferredUtc() } ?: logs.first()
                        val dateStr = dateFormatter.format(Date(firstLog.getInferredUtc()))
                        val fileName = "${dateStr}_boot_$bootId.csv"
                        val filePath = "$path/$fileName"

                        try {
                            val existingFile = existingFiles.find { it.name == fileName }
                            val contentToWrite: String

                            if (existingFile != null) {
                                val outputStream = ByteArrayOutputStream()
                                provider.readFile(filePath, outputStream)
                                val existingContent = outputStream.toString(Charsets.UTF_8.name())
                                val newRows = buildCsv(logs, includeHeader = false)
                                contentToWrite =
                                    if (existingContent.endsWith("\n") || existingContent.isEmpty()) {
                                        existingContent + newRows
                                    } else {
                                        existingContent + "\n" + newRows
                                    }
                            } else {
                                contentToWrite = buildCsv(logs, includeHeader = true)
                            }

                            val inputStream =
                                ByteArrayInputStream(contentToWrite.toByteArray(Charsets.UTF_8))
                            provider.writeFile(filePath, inputStream, "text/csv")

                            withContext(Dispatchers.IO) {
                                logs.forEach { logDao.markAsSynced(it.id) }
                            }

                            _syncCount.value = (_syncCount.value - logs.size).coerceAtLeast(0)
                        } catch (e: Exception) {
                            handleCloudError(
                                e,
                                "syncing boot $bootId batch starting at ${logs.firstOrNull()?.recordId}"
                            )
                            allSuccess = false
                            break // Stop on first error for this session
                        }
                    }
                    if (!allSuccess) break // Stop entire sync if a batch failed
                }
            } catch (e: Exception) {
                Log.e("LogCloudViewModel", "Critical sync error: ${e.message}")
                allSuccess = false
            } finally {
                _isSyncing.value = false
                _syncCount.value = 0
                _syncFinishedEvent.emit(allSuccess)
            }
        }
    }

    override fun handleCloudError(e: Exception, operation: String) {
        Log.e("LogCloudViewModel", "Cloud Error during $operation", e)
        val message = e.message?.lowercase() ?: ""
        val exceptionType = e.javaClass.simpleName

        Log.d("LogCloudViewModel", "Error Details - Type: $exceptionType, Message: ${e.message}")

        // Comprehensive check for authentication or terminal session errors
        val isTerminalError = message.contains("auth") ||
                message.contains("token") ||
                message.contains("sign in") ||
                message.contains("unauthorized") ||
                message.contains("forbidden") ||
                message.contains("msal") ||
                message.contains("expired") ||
                message.contains("invalid_grant") ||
                exceptionType.contains("Msal", ignoreCase = true)

        if (isTerminalError) {
            Log.w("LogCloudViewModel", "Terminal cloud error detected ($operation). Logging out.")
            logout()
            _isSyncing.value = false
        }
    }

    private fun buildCsv(logs: List<LogDbRecord>, includeHeader: Boolean = true): String {
        val sb = StringBuilder()
        if (includeHeader) {
            sb.append("bootId,recordId,crc,dumpUtc,dumpUptime,uptimeMillis,overflows,inferredUtc,tag,instance,code,value,type\n")
        }
        for (log in logs) {
            sb.append("${log.bootId},${log.recordId},${log.crc},${log.dumpUtc},${log.dumpUptime},${log.uptimeMillis},${log.overflows},${log.getInferredUtc()},${log.tag},${log.instance},${log.code},${log.value},${log.type}\n")
        }
        return sb.toString()
    }
}
