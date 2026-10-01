package com.example.model

enum class BackupSource(val titleAr: String, val iconEmoji: String, val badgeColorHex: Long) {
    GOOGLE_DRIVE_CLOUD("سحابة Google Drive", "☁️", 0xFF00E5FF),
    PRE_FORMAT_SNAPSHOT("قطاع ما قبل الفورمات", "🛡️", 0xFFFFB300),
    PRIVATE_VAULT_SNAPSHOT("خزنة الآلة الحاسبة والوسائط المشفرة", "🔐", 0xFFD500F9),
    SYSTEM_FACTORY_IMAGE("صورة النظام والتطبيقات الشاملة", "📱", 0xFF00E676)
}

data class PhoneBackupCopy(
    val id: String,
    val title: String,
    val subtitle: String,
    val source: BackupSource,
    val backupDateText: String,
    val sizeBytes: Long,
    val photosCount: Int,
    val vaultPhotosCount: Int,
    val videosCount: Int,
    val audioCount: Int,
    val contactsCount: Int,
    val documentsCount: Int,
    val integrityScore: String = "100% سليم ومكتمل",
    val isDownloaded: Boolean = false,
    val downloadProgress: Float = 0f,
    val isRestored: Boolean = false,
    val tags: List<String> = emptyList()
) {
    val totalFiles: Int get() = photosCount + vaultPhotosCount + videosCount + audioCount + contactsCount + documentsCount
    val formattedSize: String get() {
        val gb = sizeBytes / (1024.0 * 1024.0 * 1024.0)
        return String.format(java.util.Locale.US, "%.1f GB", gb)
    }
}

data class PhoneRestoreProgress(
    val isRestoring: Boolean = false,
    val selectedCopy: PhoneBackupCopy? = null,
    val currentPhase: String = "",
    val percent: Float = 0f,
    val restoredItemsCount: Int = 0,
    val totalItemsCount: Int = 0,
    val currentRestoredItemName: String = "",
    val restoredSizeBytes: Long = 0L,
    val isCompleted: Boolean = false,
    val completionSummary: String? = null
)
