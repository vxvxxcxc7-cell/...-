package com.example.model

import android.net.Uri

enum class FileCategory(val titleAr: String, val extensions: List<String>, val emoji: String) {
    PHOTOS("الصور", listOf("jpg", "jpeg", "png", "webp", "heic", "gif", "bmp", "raw", "dng"), "🖼️"),
    VIDEOS("الفيديو", listOf("mp4", "mkv", "mov", "3gp", "avi", "webm", "flv", "ts"), "🎬"),
    MUSIC("الموسيقى والصوتيات", listOf("mp3", "m4a", "wav", "aac", "flac", "ogg", "opus", "amr", "wma"), "🎵"),
    VAULT("الخزنة والمخفي", listOf("vlt", "dat", "bin", "enc", "safe", "hid", "vault", "rcv", "secret"), "🔐"),
    DOCUMENTS("الملفات والوثائق", listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "zip", "apk"), "📂"),
    ALL("كل الملفات", emptyList(), "🔍")
}

enum class RestoreDestination(val titleAr: String, val emoji: String, val descriptionAr: String) {
    GALLERY("معرض الهاتف والموسيقى", "🖼️", "حفظ في الاستوديو والتنزيلات ليظهر فوراً في مشغل الوسائط"),
    SECURE_VAULT("خزنة الهاتف الآمنة", "🔐", "حفظ في مجلد الخزنة المشفر والمحمي (.nomedia) لحماية الخصوصية")
}

data class ScannableItem(
    val id: Long,
    val name: String,
    val path: String,
    val uri: Uri,
    val category: FileCategory,
    val sizeBytes: Long,
    val lastModified: Long,
    val isRecoverable: Boolean = true,
    val recoveryLikelihood: RecoveryScore = RecoveryScore.HIGH,
    val sourceLocation: String = "Internal Storage",
    val mimeType: String? = null,
    val isThumbnailOrCache: Boolean = false,
    val isSelected: Boolean = false,
    val isFromVault: Boolean = false,
    val isUnbackedUp: Boolean = true,
    val vaultSource: String? = null
)

enum class RecoveryScore(val labelAr: String, val colorHex: Long) {
    HIGH("دقة عالية 98%", 0xFF1B873F),
    MEDIUM("متوسط 75%", 0xFFC97A00),
    LOW("منخفض 45%", 0xFFBA1A1A)
}

data class StorageStats(
    val totalBytes: Long,
    val freeBytes: Long,
    val usedBytes: Long,
    val percentageUsed: Int
)

data class ScanProgress(
    val isScanning: Boolean = false,
    val scannedFilesCount: Int = 0,
    val foundCount: Int = 0,
    val currentPath: String = "",
    val progressPercent: Float = 0f,
    val currentCategory: FileCategory = FileCategory.ALL,
    val scannedBytes: Long = 0L,
    val totalStorageBytes: Long = 0L,
    val estimatedSecondsRemaining: Int = 0,
    val scanPhase: String = "بدء الفحص"
) {
    val estimatedTimeFormatted: String
        get() = when {
            estimatedSecondsRemaining <= 0 -> "لحظات فقط..."
            estimatedSecondsRemaining < 60 -> "~$estimatedSecondsRemaining ثانية"
            else -> "~${estimatedSecondsRemaining / 60} دقيقة و${estimatedSecondsRemaining % 60} ثانية"
        }
}
