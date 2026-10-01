package com.example

import com.example.model.FileCategory
import com.example.model.RecoveryScore
import com.example.ui.util.FormatUtils
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testFormatBytes() {
        assertEquals("0 بايت", FormatUtils.formatBytes(0))
        assertEquals("500 بايت", FormatUtils.formatBytes(500))
        assertEquals("1.0 كيلوبايت", FormatUtils.formatBytes(1024))
        assertEquals("2.0 ميجابايت", FormatUtils.formatBytes(2 * 1024 * 1024))
    }

    @Test
    fun testFileCategories() {
        assertEquals("الصور", FileCategory.PHOTOS.titleAr)
        assertEquals("الفيديو", FileCategory.VIDEOS.titleAr)
        assertEquals("الموسيقى والصوتيات", FileCategory.MUSIC.titleAr)
        assertEquals("الخزنة والمخفي", FileCategory.VAULT.titleAr)
        assertEquals("الملفات والوثائق", FileCategory.DOCUMENTS.titleAr)
        assertTrue(FileCategory.PHOTOS.extensions.contains("jpg"))
        assertTrue(FileCategory.MUSIC.extensions.contains("mp3"))
        assertTrue(FileCategory.VAULT.extensions.contains("vault"))
    }

    @Test
    fun testRecoveryScore() {
        assertEquals("دقة عالية 98%", RecoveryScore.HIGH.labelAr)
        assertEquals("متوسط 75%", RecoveryScore.MEDIUM.labelAr)
    }
}
