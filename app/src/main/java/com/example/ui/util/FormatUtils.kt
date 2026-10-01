package com.example.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtils {
    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 بايت"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 1.0 -> String.format(Locale.US, "%.1f جيجابايت", gb)
            mb >= 1.0 -> String.format(Locale.US, "%.1f ميجابايت", mb)
            kb >= 1.0 -> String.format(Locale.US, "%.1f كيلوبايت", kb)
            else -> "$bytes بايت"
        }
    }

    fun formatDate(timestamp: Long): String {
        if (timestamp <= 0) return "-"
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
