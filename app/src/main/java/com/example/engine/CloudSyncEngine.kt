package com.example.engine

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.model.CloudProvider
import com.example.model.CloudRemoteFile
import com.example.model.CloudSyncProgress
import com.example.model.CloudSyncState
import com.example.model.FileCategory
import com.example.model.ScannableItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class CloudSyncEngine(private val context: Context) {

    private val _syncProgress = MutableStateFlow(CloudSyncProgress())
    val syncProgress: StateFlow<CloudSyncProgress> = _syncProgress.asStateFlow()

    private val _cloudFiles = MutableStateFlow<List<CloudRemoteFile>>(
        listOf(
            CloudRemoteFile(
                id = "cloud_photo_1",
                name = "Google_Drive_Photo_Archive_1.jpg",
                sizeBytes = 4820100L,
                category = FileCategory.PHOTOS,
                uploadedTimestamp = System.currentTimeMillis() - 7200000L,
                provider = CloudProvider.GOOGLE_DRIVE
            ),
            CloudRemoteFile(
                id = "cloud_vault_2",
                name = "Encrypted_Vault_Decrypted_Backups.zip",
                sizeBytes = 148201000L,
                category = FileCategory.VAULT,
                uploadedTimestamp = System.currentTimeMillis() - 14400000L,
                provider = CloudProvider.GOOGLE_DRIVE
            ),
            CloudRemoteFile(
                id = "cloud_audio_3",
                name = "Voice_Recordings_Sync.m4a",
                sizeBytes = 8420100L,
                category = FileCategory.MUSIC,
                uploadedTimestamp = System.currentTimeMillis() - 28800000L,
                provider = CloudProvider.GOOGLE_DRIVE
            )
        )
    )
    val cloudFiles: StateFlow<List<CloudRemoteFile>> = _cloudFiles.asStateFlow()

    fun setProvider(provider: CloudProvider) {
        _syncProgress.value = _syncProgress.value.copy(activeProvider = provider)
    }

    fun toggleAccountConnection(provider: CloudProvider) {
        val currentAccounts = _syncProgress.value.linkedAccounts.map { acc ->
            if (acc.provider == provider) {
                val newStatus = !acc.isConnected
                acc.copy(
                    isConnected = newStatus,
                    lastSyncTime = if (newStatus) System.currentTimeMillis() else acc.lastSyncTime
                )
            } else {
                acc
            }
        }
        val isNowConnected = currentAccounts.find { it.provider == _syncProgress.value.activeProvider }?.isConnected ?: true
        _syncProgress.value = _syncProgress.value.copy(
            linkedAccounts = currentAccounts,
            isConnected = isNowConnected
        )
    }

    fun updateAccountEmail(provider: CloudProvider, newEmail: String) {
        if (newEmail.isBlank()) return
        val currentAccounts = _syncProgress.value.linkedAccounts.map { acc ->
            if (acc.provider == provider) {
                acc.copy(accountEmail = newEmail, isConnected = true)
            } else {
                acc
            }
        }
        val currentEmail = if (provider == _syncProgress.value.activeProvider) newEmail else _syncProgress.value.userAccountEmail
        _syncProgress.value = _syncProgress.value.copy(
            linkedAccounts = currentAccounts,
            userAccountEmail = currentEmail
        )
    }

    suspend fun syncFilesToCloud(
        itemsToSync: List<ScannableItem>,
        onProgress: (Int, Int, String, Float) -> Unit
    ) {
        if (itemsToSync.isEmpty()) return

        val total = itemsToSync.size
        _syncProgress.value = _syncProgress.value.copy(
            state = CloudSyncState.SYNCING,
            totalFilesToSync = total,
            syncedFilesCount = 0,
            progressPercent = 0f
        )

        val newUploadedFiles = mutableListOf<CloudRemoteFile>()

        for ((index, item) in itemsToSync.withIndex()) {
            val progressPercent = (index + 1).toFloat() / total
            _syncProgress.value = _syncProgress.value.copy(
                syncedFilesCount = index + 1,
                currentFileName = item.name,
                progressPercent = progressPercent,
                uploadSpeedKbps = (1200..3400).random()
            )
            onProgress(index + 1, total, item.name, progressPercent)
            delay(140)

            newUploadedFiles.add(
                CloudRemoteFile(
                    id = "cloud_sync_${System.currentTimeMillis()}_$index",
                    name = item.name,
                    sizeBytes = item.sizeBytes,
                    category = item.category,
                    uploadedTimestamp = System.currentTimeMillis(),
                    provider = _syncProgress.value.activeProvider
                )
            )
        }

        _cloudFiles.value = newUploadedFiles + _cloudFiles.value
        _syncProgress.value = _syncProgress.value.copy(
            state = CloudSyncState.COMPLETED,
            progressPercent = 1.0f,
            lastSyncTime = System.currentTimeMillis()
        )
    }

    // Share/Upload directly to Google Drive or installed cloud app via Android Intent
    fun shareRecoveredFilesToCloudApp(filePaths: List<String>) {
        if (filePaths.isEmpty()) return
        try {
            val uris = ArrayList<Uri>()
            for (p in filePaths) {
                val f = File(p)
                if (f.exists()) {
                    val uri = try {
                        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", f)
                    } catch (e: Exception) {
                        Uri.fromFile(f)
                    }
                    uris.add(uri)
                }
            }

            val intent = if (uris.size == 1) {
                Intent(Intent.ACTION_SEND).apply {
                    type = "*/*"
                    putExtra(Intent.EXTRA_STREAM, uris[0])
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } else {
                Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "*/*"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }

            val chooser = Intent.createChooser(intent, "مزامنة ورفع إلى Google Drive أو التخزين السحابي")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Throwable) {
            // Handled
        }
    }
}
