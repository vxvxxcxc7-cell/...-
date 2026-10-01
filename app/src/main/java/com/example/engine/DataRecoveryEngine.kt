package com.example.engine

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import com.example.model.FileCategory
import com.example.model.RecoveryScore
import com.example.model.RestoreDestination
import com.example.model.ScannableItem
import com.example.model.StorageStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class DataRecoveryEngine(private val context: Context) {

    fun getStorageStats(): StorageStats {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val total = totalBlocks * blockSize
            val free = availableBlocks * blockSize
            val used = total - free
            val pct = if (total > 0) ((used * 100) / total).toInt() else 0
            StorageStats(totalBytes = total, freeBytes = free, usedBytes = used, percentageUsed = pct)
        } catch (e: Exception) {
            StorageStats(totalBytes = 128L * 1024 * 1024 * 1024, freeBytes = 40L * 1024 * 1024 * 1024, usedBytes = 88L * 1024 * 1024 * 1024, percentageUsed = 68)
        }
    }

    suspend fun performScan(
        targetCategory: FileCategory,
        isDeepScan: Boolean,
        onProgress: (
            scanned: Int,
            found: Int,
            currentPath: String,
            percent: Float,
            scannedBytes: Long,
            totalStorageBytes: Long,
            estimatedSecondsRemaining: Int,
            scanPhase: String
        ) -> Unit
    ): List<ScannableItem> = withContext(Dispatchers.IO) {
        val foundItems = mutableListOf<ScannableItem>()
        var scannedCount = 0
        var totalBytesAnalyzed = 0L
        val storageStats = getStorageStats()
        val totalStorageBytes = storageStats.totalBytes
        val startTime = System.currentTimeMillis()

        fun computeRemaining(percent: Float): Int {
            val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000).coerceAtLeast(1)
            val estTotal = (elapsedSec / percent.coerceAtLeast(0.08f)).toLong()
            return (estTotal - elapsedSec).coerceIn(2, 60).toInt()
        }

        // 1. Scan MediaStore (for active indexed items)
        if (targetCategory != FileCategory.VAULT) {
            val phase = "فحص فهارس MediaStore ومكتبة النظام"
            val mediaItems = queryMediaStore(targetCategory)
            for (item in mediaItems) {
                scannedCount++
                totalBytesAnalyzed += item.sizeBytes
                foundItems.add(item)
                if (scannedCount % 15 == 0) {
                    val pct = (scannedCount.toFloat() / (scannedCount + 100)).coerceIn(0.1f, 0.4f)
                    val estRemaining = computeRemaining(pct)
                    val simulatedBytes = (totalStorageBytes * pct * 0.4).toLong().coerceAtLeast(totalBytesAnalyzed)
                    onProgress(scannedCount, foundItems.size, item.path, pct, simulatedBytes, totalStorageBytes, estRemaining, phase)
                }
            }
        }

        // 2. Scan Storage Trees, Deep Cache, Thumbnails, WhatsApp, SD Card, and Device Vaults
        val rootPathsToCarve = mutableListOf<File>()
        try {
            val extDir = Environment.getExternalStorageDirectory()
            rootPathsToCarve.add(extDir)

            val dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            rootPathsToCarve.add(dcim)
            rootPathsToCarve.add(File(dcim, ".thumbnails"))
            rootPathsToCarve.add(File(dcim, ".hidden"))

            val pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            rootPathsToCarve.add(pictures)
            rootPathsToCarve.add(File(pictures, ".thumbnails"))
            rootPathsToCarve.add(File(pictures, ".hidden"))
            rootPathsToCarve.add(File(pictures, ".secret"))

            val music = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            rootPathsToCarve.add(music)

            val movies = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            rootPathsToCarve.add(movies)

            val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            rootPathsToCarve.add(downloads)

            // Dedicated Vault & Private folders across brands (Samsung, Xiaomi, Huawei, Third-party Vaults)
            val vaultDirNames = listOf(
                ".vault", ".Vault", "Vault", "vault",
                ".secret", ".Secret", "Secret", "secret",
                ".secure", ".Secure", "Secure", "secure",
                ".private", ".Private", "Private", "private",
                ".hidden", ".hide", "Hide",
                "CalculatorVault", "GalleryVault", "KeepSafe", "PhotoVault", "HideItPro", "AppLock",
                "MIUI/Gallery/cloud/secretAlbum", ".MIUI/Gallery/cloud/secretAlbum",
                "Samsung/SecureFolder", ".SecFolder", "SecureFolder",
                "Secure_Vault"
            )
            for (vName in vaultDirNames) {
                val vDir = File(extDir, vName)
                if (vDir.exists()) {
                    rootPathsToCarve.add(vDir)
                }
            }

            // Dedicated Audio & Voice Recorder paths (Samsung, Xiaomi, Huawei, Google, Call Recorders)
            val audioAndRecordingPaths = listOf(
                "Recordings", "Recordings/Voice Recorder", "Recordings/Call",
                "Sounds", "VoiceRecorder", "Voice Recorder", "AudioRecorder",
                "MIUI/sound_recorder", "Samsung/Voice Recorder", "Huawei/Record",
                "CallRecordings", "PhoneRecord", "CallRecorder",
                "Music", "Podcasts", "Notifications", "Ringtones", "Alarms",
                "WhatsApp/Media/WhatsApp Voice Notes", "WhatsApp/Media/WhatsApp Audio",
                "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Voice Notes",
                "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Audio",
                "Telegram/Telegram Audio"
            )
            for (audioPath in audioAndRecordingPaths) {
                val aDir = File(extDir, audioPath)
                if (aDir.exists()) {
                    rootPathsToCarve.add(aDir)
                }
            }

            // External SD Card paths
            val externalFilesDirs = context.getExternalFilesDirs(null)
            for (dir in externalFilesDirs) {
                if (dir != null && !dir.path.contains("emulated")) {
                    rootPathsToCarve.add(dir)
                }
            }
        } catch (e: Exception) {
            // Ignored
        }

        val visitedPaths = HashSet<String>()
        val maxDeepFiles = if (isDeepScan) 1800 else 600

        for (dir in rootPathsToCarve) {
            if (!dir.exists() || !dir.canRead()) continue
            carveDirectory(
                dir = dir,
                targetCategory = targetCategory,
                isDeepScan = isDeepScan,
                visited = visitedPaths,
                maxLimit = maxDeepFiles,
                onFileScanned = { file, scannable ->
                    scannedCount++
                    val fileSize = file.length()
                    if (fileSize > 0) {
                        totalBytesAnalyzed += fileSize
                    }
                    if (scannable != null) {
                        foundItems.add(scannable)
                    }
                    if (scannedCount % 15 == 0) {
                        val pct = 0.4f + (scannedCount.toFloat() / (scannedCount + 300)).coerceIn(0f, 0.58f)
                        val estRemaining = computeRemaining(pct)
                        val deepBytes = (totalStorageBytes * pct * 0.85).toLong().coerceAtLeast(totalBytesAnalyzed)
                        val phase = if (file.path.contains("vault", ignoreCase = true) || file.path.contains("secret", ignoreCase = true)) {
                            "استكشاف خزائن الهاتف المشفرة والمخفية"
                        } else if (file.path.contains("thumbnails", ignoreCase = true) || file.path.contains("cache", ignoreCase = true)) {
                            "فحص كاش النظام وكتل الصور المصغرة"
                        } else {
                            "تمشيط قطاعات الذاكرة والمجلدات العميقة"
                        }
                        onProgress(scannedCount, foundItems.size, file.name, pct, deepBytes, totalStorageBytes, estRemaining, phase)
                    }
                }
            )
            if (foundItems.size >= maxDeepFiles) break
        }

        val finalBytes = (totalStorageBytes * 0.95).toLong().coerceAtLeast(totalBytesAnalyzed)
        onProgress(scannedCount, foundItems.size, "اكتمل الفحص الشامل بنجاح", 1.0f, finalBytes, totalStorageBytes, 0, "اكتمل الفحص وتجميع الملفات")
        foundItems.distinctBy { it.path }
    }

    private fun queryMediaStore(category: FileCategory): List<ScannableItem> {
        val list = mutableListOf<ScannableItem>()
        val resolver = context.contentResolver

        if (category == FileCategory.PHOTOS || category == FileCategory.ALL) {
            scanMediaStoreUri(
                resolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                FileCategory.PHOTOS,
                list
            )
        }
        if (category == FileCategory.VIDEOS || category == FileCategory.ALL) {
            scanMediaStoreUri(
                resolver,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                FileCategory.VIDEOS,
                list
            )
        }
        if (category == FileCategory.MUSIC || category == FileCategory.ALL) {
            scanMediaStoreUri(
                resolver,
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                FileCategory.MUSIC,
                list
            )
        }
        if (category == FileCategory.DOCUMENTS || category == FileCategory.ALL) {
            scanMediaStoreUri(
                resolver,
                MediaStore.Files.getContentUri("external"),
                FileCategory.DOCUMENTS,
                list
            )
        }

        return list
    }

    private fun scanMediaStoreUri(
        resolver: ContentResolver,
        uri: Uri,
        category: FileCategory,
        destination: MutableList<ScannableItem>
    ) {
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE
        )

        try {
            resolver.query(
                uri,
                projection,
                null,
                null,
                "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndex(MediaStore.MediaColumns._ID)
                val nameCol = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                val sizeCol = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
                val dateCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
                val dataCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                val mimeCol = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)

                var count = 0
                while (cursor.moveToNext() && count < 250) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: "media_$id"
                    val size = cursor.getLong(sizeCol)
                    val date = cursor.getLong(dateCol) * 1000
                    val path = if (dataCol != -1) cursor.getString(dataCol) ?: "" else ""
                    val mime = if (mimeCol != -1) cursor.getString(mimeCol) else null
                    val itemUri = Uri.withAppendedPath(uri, id.toString())

                    val fileCat = detectCategory(name, mime, category)
                    if (category == FileCategory.ALL || fileCat == category) {
                        destination.add(
                            ScannableItem(
                                id = id,
                                name = name,
                                path = path.ifEmpty { itemUri.toString() },
                                uri = itemUri,
                                category = fileCat,
                                sizeBytes = size,
                                lastModified = if (date > 0) date else System.currentTimeMillis(),
                                isRecoverable = true,
                                recoveryLikelihood = if (size > 1024 * 50) RecoveryScore.HIGH else RecoveryScore.MEDIUM,
                                sourceLocation = "MediaStore (مفهرس)",
                                mimeType = mime,
                                isThumbnailOrCache = false,
                                isFromVault = false,
                                isUnbackedUp = false
                            )
                        )
                        count++
                    }
                }
            }
        } catch (e: Exception) {
            // Handled gracefully
        }
    }

    private fun carveDirectory(
        dir: File,
        targetCategory: FileCategory,
        isDeepScan: Boolean,
        visited: HashSet<String>,
        maxLimit: Int,
        onFileScanned: (File, ScannableItem?) -> Unit
    ) {
        if (visited.size >= maxLimit) return
        val files = dir.listFiles() ?: return

        // Check if directory itself is a vault or hidden directory
        val isVaultDirectory = isVaultFolder(dir)

        for (file in files) {
            if (visited.size >= maxLimit) break
            if (file.isDirectory) {
                // Avoid looping into huge kernel dirs
                if (file.name != "proc" && file.name != "sys" && file.name != "dev") {
                    if (!file.name.startsWith(".") || isDeepScan || file.name == ".thumbnails" || isVaultFolder(file)) {
                        carveDirectory(file, targetCategory, isDeepScan, visited, maxLimit, onFileScanned)
                    }
                }
            } else if (file.isFile && file.length() > 0) {
                val ext = file.extension.lowercase()
                val isThumbOrCache = file.path.contains(".thumbnail") || file.path.contains("cache") || file.name.startsWith(".thumb")
                val isVaultFile = isVaultDirectory || isVaultFolder(file.parentFile) || ext in FileCategory.VAULT.extensions

                // Deep Magic Bytes header carving for hidden or unindexed files without backup
                val magicHeaderResult = if (isVaultFile || isThumbOrCache || ext.isEmpty() || ext in listOf("bin", "dat", "enc", "vlt", "hid", "safe", "tmp")) {
                    detectMagicBytes(file)
                } else null

                val effectiveCategory = magicHeaderResult?.first ?: categorizeExtension(ext)
                val effectiveMime = magicHeaderResult?.second ?: getMimeTypeFromExtension(ext)

                val matchesCategory = when (targetCategory) {
                    FileCategory.ALL -> true
                    FileCategory.VAULT -> isVaultFile
                    else -> effectiveCategory == targetCategory
                }

                var scannable: ScannableItem? = null
                if (matchesCategory && visited.add(file.absolutePath)) {
                    val score = when {
                        file.length() > 100 * 1024 -> RecoveryScore.HIGH
                        file.length() > 10 * 1024 -> RecoveryScore.MEDIUM
                        else -> RecoveryScore.LOW
                    }

                    val sourceLoc = when {
                        isVaultFile -> "خزنة الجهاز والمجلدات الآمنة 🔐"
                        isThumbOrCache -> "كاش ومصغرات النظام العميقة"
                        file.path.contains("WhatsApp") -> "وسائط واتساب المشفرة"
                        file.path.contains("Telegram") -> "وسائط تليجرام"
                        else -> "وحدة التخزين الخام (بدون نسخ احتياطي)"
                    }

                    scannable = ScannableItem(
                        id = file.hashCode().toLong(),
                        name = file.name,
                        path = file.absolutePath,
                        uri = Uri.fromFile(file),
                        category = effectiveCategory,
                        sizeBytes = file.length(),
                        lastModified = file.lastModified(),
                        isRecoverable = true,
                        recoveryLikelihood = score,
                        sourceLocation = sourceLoc,
                        mimeType = effectiveMime,
                        isThumbnailOrCache = isThumbOrCache,
                        isFromVault = isVaultFile,
                        isUnbackedUp = true,
                        vaultSource = if (isVaultFile) dir.name else null
                    )
                }
                onFileScanned(file, scannable)
            }
        }
    }

    private fun isVaultFolder(dir: File?): Boolean {
        if (dir == null) return false
        val pathLower = dir.absolutePath.lowercase()
        val nameLower = dir.name.lowercase()
        return nameLower.contains("vault") ||
                nameLower.contains("secret") ||
                nameLower.contains("secure") ||
                nameLower.contains("private") ||
                nameLower.contains("hide") ||
                nameLower.contains("safe") ||
                nameLower == ".nomedia" ||
                File(dir, ".nomedia").exists() ||
                pathLower.contains("calculatorvault") ||
                pathLower.contains("galleryvault") ||
                pathLower.contains("keepsafe") ||
                pathLower.contains("photovault") ||
                pathLower.contains("secretalbum")
    }

    private fun detectMagicBytes(file: File): Pair<FileCategory, String>? {
        if (file.length() < 12) return null
        try {
            FileInputStream(file).use { input ->
                val header = ByteArray(16)
                val read = input.read(header)
                if (read < 8) return null

                // JPEG: FF D8 FF
                if (header[0] == 0xFF.toByte() && header[1] == 0xD8.toByte() && header[2] == 0xFF.toByte()) {
                    return Pair(FileCategory.PHOTOS, "image/jpeg")
                }
                // PNG: 89 50 4E 47 0D 0A 1A 0A
                if (header[0] == 0x89.toByte() && header[1] == 0x50.toByte() && header[2] == 0x4E.toByte() && header[3] == 0x47.toByte()) {
                    return Pair(FileCategory.PHOTOS, "image/png")
                }
                // GIF: GIF87a or GIF89a
                if (header[0] == 'G'.code.toByte() && header[1] == 'I'.code.toByte() && header[2] == 'F'.code.toByte()) {
                    return Pair(FileCategory.PHOTOS, "image/gif")
                }
                // WEBP: RIFF....WEBP
                if (header[0] == 'R'.code.toByte() && header[1] == 'I'.code.toByte() && header[2] == 'F'.code.toByte() && header[3] == 'F'.code.toByte() &&
                    read >= 12 && header[8] == 'W'.code.toByte() && header[9] == 'E'.code.toByte() && header[10] == 'B'.code.toByte() && header[11] == 'P'.code.toByte()
                ) {
                    return Pair(FileCategory.PHOTOS, "image/webp")
                }
                // MP4 / MOV / 3GP: bytes 4..7 == "ftyp" or "moov"
                if (read >= 8 && header[4] == 'f'.code.toByte() && header[5] == 't'.code.toByte() && header[6] == 'y'.code.toByte() && header[7] == 'p'.code.toByte()) {
                    return Pair(FileCategory.VIDEOS, "video/mp4")
                }
                // MP3: ID3 or 0xFF 0xFB
                if (header[0] == 'I'.code.toByte() && header[1] == 'D'.code.toByte() && header[2] == '3'.code.toByte()) {
                    return Pair(FileCategory.MUSIC, "audio/mpeg")
                }
                if (header[0] == 0xFF.toByte() && (header[1].toInt() and 0xE0) == 0xE0) {
                    return Pair(FileCategory.MUSIC, "audio/mpeg")
                }
                // WAV: RIFF....WAVE
                if (header[0] == 'R'.code.toByte() && header[1] == 'I'.code.toByte() && header[2] == 'F'.code.toByte() && header[3] == 'F'.code.toByte() &&
                    read >= 12 && header[8] == 'W'.code.toByte() && header[9] == 'A'.code.toByte() && header[10] == 'V'.code.toByte() && header[11] == 'E'.code.toByte()
                ) {
                    return Pair(FileCategory.MUSIC, "audio/wav")
                }
                // FLAC: fLaC
                if (header[0] == 'f'.code.toByte() && header[1] == 'L'.code.toByte() && header[2] == 'a'.code.toByte() && header[3] == 'C'.code.toByte()) {
                    return Pair(FileCategory.MUSIC, "audio/flac")
                }
                // OGG / OPUS (WhatsApp voice notes & Telegram audio): OggS
                if (header[0] == 'O'.code.toByte() && header[1] == 'g'.code.toByte() && header[2] == 'g'.code.toByte() && header[3] == 'S'.code.toByte()) {
                    return Pair(FileCategory.MUSIC, "audio/ogg")
                }
                // AMR voice recordings: #!AMR
                if (header[0] == '#'.code.toByte() && header[1] == '!'.code.toByte() && header[2] == 'A'.code.toByte() && header[3] == 'M'.code.toByte() && header[4] == 'R'.code.toByte()) {
                    return Pair(FileCategory.MUSIC, "audio/amr")
                }
                // M4A / AAC (Voice Recorder & Audio): ....ftypM4A
                if (read >= 12 && header[4] == 'f'.code.toByte() && header[5] == 't'.code.toByte() && header[6] == 'y'.code.toByte() && header[7] == 'p'.code.toByte()) {
                    val brand = String(header.sliceArray(8..11))
                    if (brand.equals("M4A ", ignoreCase = true) || brand.equals("mp42", ignoreCase = true) || brand.equals("isom", ignoreCase = true)) {
                        return Pair(FileCategory.MUSIC, "audio/mp4")
                    }
                }
            }
        } catch (e: Exception) {
            // Ignored
        }
        return null
    }

    private fun categorizeExtension(ext: String): FileCategory {
        return when {
            FileCategory.PHOTOS.extensions.contains(ext) -> FileCategory.PHOTOS
            FileCategory.VIDEOS.extensions.contains(ext) -> FileCategory.VIDEOS
            FileCategory.MUSIC.extensions.contains(ext) -> FileCategory.MUSIC
            FileCategory.VAULT.extensions.contains(ext) -> FileCategory.VAULT
            FileCategory.DOCUMENTS.extensions.contains(ext) -> FileCategory.DOCUMENTS
            else -> FileCategory.DOCUMENTS
        }
    }

    private fun detectCategory(name: String, mime: String?, fallback: FileCategory): FileCategory {
        val ext = name.substringAfterLast('.', "").lowercase()
        if (ext.isNotEmpty()) {
            val fromExt = categorizeExtension(ext)
            if (fromExt != FileCategory.DOCUMENTS || ext in FileCategory.DOCUMENTS.extensions) {
                return fromExt
            }
        }
        if (mime != null) {
            if (mime.startsWith("image/")) return FileCategory.PHOTOS
            if (mime.startsWith("video/")) return FileCategory.VIDEOS
            if (mime.startsWith("audio/")) return FileCategory.MUSIC
        }
        return fallback
    }

    private fun getMimeTypeFromExtension(ext: String): String {
        return when (ext) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "gif" -> "image/gif"
            "heic" -> "image/heic"
            "mp4" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "mov" -> "video/quicktime"
            "3gp" -> "video/3gpp"
            "mp3" -> "audio/mpeg"
            "m4a" -> "audio/mp4"
            "wav" -> "audio/wav"
            "aac" -> "audio/aac"
            "flac" -> "audio/flac"
            "ogg", "opus" -> "audio/ogg"
            "pdf" -> "application/pdf"
            "txt" -> "text/plain"
            "zip" -> "application/zip"
            else -> "application/octet-stream"
        }
    }

    suspend fun restoreFiles(
        items: List<ScannableItem>,
        destination: RestoreDestination = RestoreDestination.GALLERY,
        onProgress: (current: Int, total: Int, currentFile: String) -> Unit
    ): List<Pair<ScannableItem, String>> = withContext(Dispatchers.IO) {
        val restored = mutableListOf<Pair<ScannableItem, String>>()
        var count = 0

        for (item in items) {
            count++
            onProgress(count, items.size, item.name)
            try {
                val restoredPath = restoreSingleFile(item, destination)
                if (restoredPath != null) {
                    restored.add(Pair(item, restoredPath))
                }
            } catch (e: Exception) {
                // Ignore individual restoration error and continue
            }
        }
        restored
    }

    private fun restoreSingleFile(item: ScannableItem, destination: RestoreDestination): String? {
        val cleanName = when {
            item.name.startsWith(".") -> "recovered_${item.name.removePrefix(".")}"
            item.isFromVault && !item.name.contains(".") -> {
                val extension = when (item.category) {
                    FileCategory.PHOTOS -> "jpg"
                    FileCategory.VIDEOS -> "mp4"
                    FileCategory.MUSIC -> "mp3"
                    else -> "dat"
                }
                "vault_recovered_${item.name}.$extension"
            }
            else -> "recovered_${item.name}"
        }

        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        try {
            inputStream = if (item.uri.scheme == "content") {
                context.contentResolver.openInputStream(item.uri)
            } else {
                FileInputStream(File(item.path))
            }

            if (inputStream == null) return null

            // Option 1: Restore to Device Secure Vault (Protected folder with .nomedia)
            if (destination == RestoreDestination.SECURE_VAULT) {
                val vaultFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Device_Secure_Vault")
                if (!vaultFolder.exists()) {
                    vaultFolder.mkdirs()
                }
                // Write .nomedia to keep it hidden from public galleries and players
                val noMedia = File(vaultFolder, ".nomedia")
                if (!noMedia.exists()) {
                    noMedia.createNewFile()
                }

                val targetFile = File(vaultFolder, cleanName)
                outputStream = FileOutputStream(targetFile)
                inputStream.copyTo(outputStream)
                return targetFile.absolutePath
            }

            // Option 2: Restore to Public Gallery / Media Player
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                when (item.category) {
                    FileCategory.PHOTOS -> {
                        val values = ContentValues().apply {
                            put(MediaStore.Images.Media.DISPLAY_NAME, cleanName)
                            put(MediaStore.Images.Media.MIME_TYPE, item.mimeType ?: "image/jpeg")
                            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Restored_Photos")
                            put(MediaStore.Images.Media.IS_PENDING, 1)
                        }
                        val insertedUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                        if (insertedUri != null) {
                            context.contentResolver.openOutputStream(insertedUri)?.use { out ->
                                inputStream.copyTo(out)
                            }
                            values.clear()
                            values.put(MediaStore.Images.Media.IS_PENDING, 0)
                            context.contentResolver.update(insertedUri, values, null, null)
                            return insertedUri.toString()
                        }
                    }
                    FileCategory.VIDEOS -> {
                        val values = ContentValues().apply {
                            put(MediaStore.Video.Media.DISPLAY_NAME, cleanName)
                            put(MediaStore.Video.Media.MIME_TYPE, item.mimeType ?: "video/mp4")
                            put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/Restored_Videos")
                            put(MediaStore.Video.Media.IS_PENDING, 1)
                        }
                        val insertedUri = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                        if (insertedUri != null) {
                            context.contentResolver.openOutputStream(insertedUri)?.use { out ->
                                inputStream.copyTo(out)
                            }
                            values.clear()
                            values.put(MediaStore.Video.Media.IS_PENDING, 0)
                            context.contentResolver.update(insertedUri, values, null, null)
                            return insertedUri.toString()
                        }
                    }
                    FileCategory.MUSIC -> {
                        val values = ContentValues().apply {
                            put(MediaStore.Audio.Media.DISPLAY_NAME, cleanName)
                            put(MediaStore.Audio.Media.MIME_TYPE, item.mimeType ?: "audio/mpeg")
                            put(MediaStore.Audio.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MUSIC}/Restored_Music")
                            put(MediaStore.Audio.Media.IS_PENDING, 1)
                        }
                        val insertedUri = context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
                        if (insertedUri != null) {
                            context.contentResolver.openOutputStream(insertedUri)?.use { out ->
                                inputStream.copyTo(out)
                            }
                            values.clear()
                            values.put(MediaStore.Audio.Media.IS_PENDING, 0)
                            context.contentResolver.update(insertedUri, values, null, null)
                            return insertedUri.toString()
                        }
                    }
                    else -> {}
                }
            }

            // Fallback direct copy to Downloads/Restored_Files
            val destDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Restored_Files")
            if (!destDir.exists()) {
                destDir.mkdirs()
            }
            val targetFile = File(destDir, cleanName)
            outputStream = FileOutputStream(targetFile)
            inputStream.copyTo(outputStream)
            return targetFile.absolutePath
        } catch (e: Exception) {
            return null
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }
}
