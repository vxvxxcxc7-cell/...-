package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recovered_files")
data class RecoveredFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val originalPath: String,
    val restoredPath: String,
    val fileType: String, // PHOTO, VIDEO, AUDIO, DOCUMENT, WHATSAPP
    val sizeBytes: Long,
    val restoredTimestamp: Long,
    val recoveryStatus: String = "SUCCESS", // SUCCESS, PARTIAL
    val recoveryMethod: String = "DEEP_SCAN" // MEDIASTORE, CACHE_CARVING, SD_CARD
)
