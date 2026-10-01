package com.example.ui.util

import com.example.model.AppLanguage
import com.example.model.UserGender

object AppStrings {

    fun getGreeting(lang: AppLanguage, gender: UserGender): String = when (lang) {
        AppLanguage.ARABIC -> when (gender) {
            UserGender.MALE -> "مرحباً بك عزيزي 💫"
            UserGender.FEMALE -> "مرحباً بكِ عزيزتي 💫"
            UserGender.NEUTRAL -> "مرحباً بك 💫"
        }
        AppLanguage.ENGLISH -> when (gender) {
            UserGender.MALE -> "Welcome, Sir 💫"
            UserGender.FEMALE -> "Welcome, Madam 💫"
            UserGender.NEUTRAL -> "Welcome 💫"
        }
        AppLanguage.FRENCH -> when (gender) {
            UserGender.MALE -> "Bienvenue cher monsieur 💫"
            UserGender.FEMALE -> "Bienvenue chère madame 💫"
            UserGender.NEUTRAL -> "Bienvenue 💫"
        }
        AppLanguage.SPANISH -> when (gender) {
            UserGender.MALE -> "Bienvenido, estimado 💫"
            UserGender.FEMALE -> "Bienvenida, estimada 💫"
            UserGender.NEUTRAL -> "Bienvenido 💫"
        }
        AppLanguage.GERMAN -> when (gender) {
            UserGender.MALE -> "Willkommen, mein Herr 💫"
            UserGender.FEMALE -> "Willkommen, meine Dame 💫"
            UserGender.NEUTRAL -> "Willkommen 💫"
        }
    }

    fun getSubGreeting(lang: AppLanguage, gender: UserGender): String = when (lang) {
        AppLanguage.ARABIC -> when (gender) {
            UserGender.MALE -> "أنت جاهز لاسترجاع روح جهازك وكل ذكرياتك وصور الخزائن"
            UserGender.FEMALE -> "أنتِ جاهزة لاسترجاع روح جهازكِ وكل ذكرياتكِ وصور الخزائن"
            UserGender.NEUTRAL -> "استرجع روح جهازك وكل ذكرياتك وصور الخزائن المفقودة"
        }
        AppLanguage.ENGLISH -> when (gender) {
            UserGender.MALE -> "You are ready to restore your phone's soul, memories & vault photos"
            UserGender.FEMALE -> "You are ready to restore your phone's soul, memories & vault photos"
            UserGender.NEUTRAL -> "Restore your phone's soul, memories & vault photos"
        }
        AppLanguage.FRENCH -> "Prêt à restaurer l'âme de votre téléphone, vos souvenirs et coffres"
        AppLanguage.SPANISH -> "Listo para restaurar el alma de tu teléfono, recuerdos y fotos de bóveda"
        AppLanguage.GERMAN -> "Bereit, die Seele Ihres Telefons, Erinnerungen & Tresorfotos wiederherzustellen"
    }

    fun getRecoverVaultPhotosTitle(lang: AppLanguage, gender: UserGender): String = when (lang) {
        AppLanguage.ARABIC -> when (gender) {
            UserGender.MALE -> "استرجع كل صور الهاتف والخزينة المشفرة 🔐"
            UserGender.FEMALE -> "استرجعي كل صور الهاتف والخزينة المشفرة 🔐"
            UserGender.NEUTRAL -> "استرجاع كل صور الهاتف والخزينة المشفرة 🔐"
        }
        AppLanguage.ENGLISH -> "Recover All Phone & Encrypted Vault Photos 🔐"
        AppLanguage.FRENCH -> "Récupérer toutes les photos du téléphone et du coffre-fort 🔐"
        AppLanguage.SPANISH -> "Recuperar todas las fotos del teléfono y de la bóveda 🔐"
        AppLanguage.GERMAN -> "Alle Telefon- und verschlüsselten Tresorfotos wiederherstellen 🔐"
    }

    fun getRecoverVaultPhotosDesc(lang: AppLanguage, gender: UserGender): String = when (lang) {
        AppLanguage.ARABIC -> when (gender) {
            UserGender.MALE -> "تمشيط فائق للصور العادية والخزائن السرية والآلة الحاسبة وكاش النظام لفك تشفيرها لك"
            UserGender.FEMALE -> "تمشيط فائق للصور العادية والخزائن السرية والآلة الحاسبة وكاش النظام لفك تشفيرها لكِ"
            UserGender.NEUTRAL -> "تمشيط فائق للصور العادية والخزائن السرية والآلة الحاسبة وكاش النظام"
        }
        AppLanguage.ENGLISH -> "Deep scan across gallery, secret calculator vaults, KeepSafe & system cache"
        AppLanguage.FRENCH -> "Analyse profonde de la galerie, des coffres calculatrice et du cache système"
        AppLanguage.SPANISH -> "Escaneo profundo de galería, bóvedas de calculadora secreta y caché"
        AppLanguage.GERMAN -> "Tiefenscan über Galerie, geheime Rechner-Tresore und System-Cache"
    }

    fun getRestoreOriginalState(lang: AppLanguage, gender: UserGender): String = when (lang) {
        AppLanguage.ARABIC -> when (gender) {
            UserGender.MALE -> "استعادة الهاتف إلى حالته الأصلية بالكامل ⚡"
            UserGender.FEMALE -> "استعادة الهاتف إلى حالته الأصلية بالكامل ⚡"
            UserGender.NEUTRAL -> "استعادة الهاتف إلى حالته الأصلية بالكامل ⚡"
        }
        AppLanguage.ENGLISH -> "Restore Phone to Original State ⚡"
        AppLanguage.FRENCH -> "Restaurer le téléphone à son état d'origine ⚡"
        AppLanguage.SPANISH -> "Restaurar teléfono al estado original ⚡"
        AppLanguage.GERMAN -> "Telefon in den Originalzustand zurücksetzen ⚡"
    }

    fun getChooseCopyTitle(lang: AppLanguage, gender: UserGender): String = when (lang) {
        AppLanguage.ARABIC -> when (gender) {
            UserGender.MALE -> "اختر نسخة الهاتف المناسبة لك لاستعادتها 📱"
            UserGender.FEMALE -> "اختاري نسخة الهاتف المناسبة لكِ لاستعادتها 📱"
            UserGender.NEUTRAL -> "اختيار نسخة الهاتف المناسبة للاستعادة 📱"
        }
        AppLanguage.ENGLISH -> "Select the phone backup copy you want to restore 📱"
        AppLanguage.FRENCH -> "Sélectionnez la copie de sauvegarde à restaurer 📱"
        AppLanguage.SPANISH -> "Selecciona la copia de respaldo que deseas restaurar 📱"
        AppLanguage.GERMAN -> "Wählen Sie das Telefon-Backup zur Wiederherstellung 📱"
    }

    fun getDownloadCopyBtn(lang: AppLanguage, gender: UserGender): String = when (lang) {
        AppLanguage.ARABIC -> when (gender) {
            UserGender.MALE -> "تحميل هذه النسخة وفحصها"
            UserGender.FEMALE -> "تحميل هذه النسخة وفحصها"
            UserGender.NEUTRAL -> "تحميل النسخة وفحصها"
        }
        AppLanguage.ENGLISH -> "Download & Inspect Copy"
        AppLanguage.FRENCH -> "Télécharger et inspecter la copie"
        AppLanguage.SPANISH -> "Descargar e inspeccionar copia"
        AppLanguage.GERMAN -> "Kopie herunterladen & prüfen"
    }

    fun getCloudSyncTitle(lang: AppLanguage, gender: UserGender): String = when (lang) {
        AppLanguage.ARABIC -> when (gender) {
            UserGender.MALE -> "مزامنة سحابية مع Google Drive ☁️"
            UserGender.FEMALE -> "مزامنة سحابية مع Google Drive ☁️"
            UserGender.NEUTRAL -> "مزامنة سحابية مع Google Drive ☁️"
        }
        AppLanguage.ENGLISH -> "Cloud Sync with Google Drive ☁️"
        AppLanguage.FRENCH -> "Synchronisation Cloud Google Drive ☁️"
        AppLanguage.SPANISH -> "Sincronización en la Nube Google Drive ☁️"
        AppLanguage.GERMAN -> "Cloud-Synchronisierung mit Google Drive ☁️"
    }

    fun getSyncActionBtn(lang: AppLanguage, gender: UserGender): String = when (lang) {
        AppLanguage.ARABIC -> when (gender) {
            UserGender.MALE -> "زامن جميع ملفاتك المسترجعة إلى السحابة الآن"
            UserGender.FEMALE -> "زامني جميع ملفاتكِ المسترجعة إلى السحابة الآن"
            UserGender.NEUTRAL -> "مزامنة الملفات المسترجعة إلى السحابة الآن"
        }
        AppLanguage.ENGLISH -> "Sync All Recovered Files to Cloud Now"
        AppLanguage.FRENCH -> "Synchroniser tous les fichiers récupérés sur le Cloud"
        AppLanguage.SPANISH -> "Sincronizar todos los archivos recuperados a la nube"
        AppLanguage.GERMAN -> "Alle wiederhergestellten Dateien in die Cloud synchronisieren"
    }

    fun getStartDeepScanBtn(lang: AppLanguage, gender: UserGender): String = when (lang) {
        AppLanguage.ARABIC -> when (gender) {
            UserGender.MALE -> "ابدأ الفحص الشامل للذاكرة"
            UserGender.FEMALE -> "ابدئي الفحص الشامل للذاكرة"
            UserGender.NEUTRAL -> "بدء الفحص الشامل للذاكرة"
        }
        AppLanguage.ENGLISH -> "Start Full Memory Deep Scan"
        AppLanguage.FRENCH -> "Lancer l'analyse approfondie"
        AppLanguage.SPANISH -> "Iniciar escaneo profundo"
        AppLanguage.GERMAN -> "Tiefenscan starten"
    }

    fun getTabHome(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الرئيسية"
        AppLanguage.ENGLISH -> "Home"
        AppLanguage.FRENCH -> "Accueil"
        AppLanguage.SPANISH -> "Inicio"
        AppLanguage.GERMAN -> "Start"
    }

    fun getTabBackups(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "نسخ الهاتف"
        AppLanguage.ENGLISH -> "Phone Copies"
        AppLanguage.FRENCH -> "Sauvegardes"
        AppLanguage.SPANISH -> "Copias"
        AppLanguage.GERMAN -> "Kopien"
    }

    fun getTabCloud(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "السحابة"
        AppLanguage.ENGLISH -> "Cloud"
        AppLanguage.FRENCH -> "Nuage"
        AppLanguage.SPANISH -> "Nube"
        AppLanguage.GERMAN -> "Cloud"
    }

    fun getTabHistory(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "السجل"
        AppLanguage.ENGLISH -> "History"
        AppLanguage.FRENCH -> "Historique"
        AppLanguage.SPANISH -> "Historial"
        AppLanguage.GERMAN -> "Verlauf"
    }

    fun getTabAi(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "المستشار AI"
        AppLanguage.ENGLISH -> "AI Advisor"
        AppLanguage.FRENCH -> "Conseiller IA"
        AppLanguage.SPANISH -> "Asesor IA"
        AppLanguage.GERMAN -> "KI Berater"
    }

    fun getLanguageGenderLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "اللغة والتخاطب"
        AppLanguage.ENGLISH -> "Language & Gender"
        AppLanguage.FRENCH -> "Langue & Genre"
        AppLanguage.SPANISH -> "Idioma y Género"
        AppLanguage.GERMAN -> "Sprache & Geschlecht"
    }
}
