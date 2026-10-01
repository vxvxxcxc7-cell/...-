package com.example.model

enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    ARABIC("ar", "العربية", "🇸🇦"),
    ENGLISH("en", "English", "🇺🇸"),
    FRENCH("fr", "Français", "🇫🇷"),
    SPANISH("es", "Español", "🇪🇸"),
    GERMAN("de", "Deutsch", "🇩🇪")
}

enum class UserGender(val code: String, val titleAr: String, val titleEn: String, val emoji: String) {
    MALE("male", "ذكر", "Male", "👨"),
    FEMALE("female", "أنثى", "Female", "👩"),
    NEUTRAL("neutral", "عام / محايد", "Neutral", "✨")
}

data class UserLanguageGenderConfig(
    val language: AppLanguage = AppLanguage.ARABIC,
    val gender: UserGender = UserGender.MALE
)
