package com.example.model

enum class CloudProvider(val id: String, val displayName: String, val iconEmoji: String, val defaultQuota: String) {
    GOOGLE_DRIVE("google_drive", "Google Drive", "📁", "15 GB مجاني / 100 GB سحابي"),
    DROPBOX("dropbox", "Dropbox", "📦", "2 GB مجاني / 2 TB سحابي"),
    ONEDRIVE("onedrive", "Microsoft OneDrive", "☁️", "5 GB مجاني / 1 TB سحابي"),
    PRIVATE_VAULT("private_vault", "الخزنة السحابية المشفرة", "🛡️", "تخزين مشفر آمن غير محدود")
}

enum class CloudSyncState {
    IDLE,
    CONNECTING,
    SYNCING,
    COMPLETED,
    FAILED
}

data class CloudConnectionAccount(
    val provider: CloudProvider,
    val accountEmail: String,
    val isConnected: Boolean,
    val lastSyncTime: Long? = null,
    val storageUsageText: String = ""
)

data class CloudSyncProgress(
    val state: CloudSyncState = CloudSyncState.IDLE,
    val activeProvider: CloudProvider = CloudProvider.GOOGLE_DRIVE,
    val userAccountEmail: String = "user.backup@gmail.com",
    val isConnected: Boolean = true,
    val syncedFilesCount: Int = 0,
    val totalFilesToSync: Int = 0,
    val currentFileName: String? = null,
    val progressPercent: Float = 0f,
    val lastSyncTime: Long? = System.currentTimeMillis() - (3600 * 1000 * 4), // 4 hours ago
    val uploadSpeedKbps: Int = 0,
    val linkedAccounts: List<CloudConnectionAccount> = listOf(
        CloudConnectionAccount(
            provider = CloudProvider.GOOGLE_DRIVE,
            accountEmail = "user.backup@gmail.com",
            isConnected = true,
            lastSyncTime = System.currentTimeMillis() - (3600 * 1000 * 4),
            storageUsageText = "15 GB / 100 GB"
        ),
        CloudConnectionAccount(
            provider = CloudProvider.DROPBOX,
            accountEmail = "user.photos@dropbox.com",
            isConnected = false,
            lastSyncTime = null,
            storageUsageText = "0 GB / 2 TB"
        ),
        CloudConnectionAccount(
            provider = CloudProvider.ONEDRIVE,
            accountEmail = "user@live.com",
            isConnected = false,
            lastSyncTime = null,
            storageUsageText = "0 GB / 1 TB"
        ),
        CloudConnectionAccount(
            provider = CloudProvider.PRIVATE_VAULT,
            accountEmail = "vault.secured.device@cloud.net",
            isConnected = true,
            lastSyncTime = System.currentTimeMillis() - (3600 * 1000 * 2),
            storageUsageText = "مشفر E2EE محلي وسحابي"
        )
    )
)

data class CloudRemoteFile(
    val id: String,
    val name: String,
    val sizeBytes: Long,
    val category: FileCategory,
    val uploadedTimestamp: Long,
    val provider: CloudProvider = CloudProvider.GOOGLE_DRIVE,
    val remoteUrl: String? = null
)
