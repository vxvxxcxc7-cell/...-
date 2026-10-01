package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.ChatMessage
import com.example.ai.GeminiChatService
import com.example.ai.MessageSender
import com.example.data.local.AppDatabase
import com.example.data.local.RecoveredFileEntity
import com.example.engine.DataRecoveryEngine
import com.example.engine.CloudSyncEngine
import com.example.engine.PhoneBackupEngine
import com.example.model.AppLanguage
import com.example.model.CloudProvider
import com.example.model.CloudRemoteFile
import com.example.model.CloudSyncProgress
import com.example.model.FileCategory
import com.example.model.PhoneBackupCopy
import com.example.model.PhoneRestoreProgress
import com.example.model.ScanProgress
import com.example.model.ScannableItem
import com.example.model.StorageStats
import com.example.model.UserGender
import com.example.model.UserLanguageGenderConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DataRecoveryViewModel(application: Application) : AndroidViewModel(application) {

    private val engine = DataRecoveryEngine(application)
    private val db = AppDatabase.getDatabase(application)
    private val chatService = GeminiChatService()
    private val prefs = application.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)

    // Dedicated Sub-engines for Cloud & Phone Backups
    val cloudSyncEngine = CloudSyncEngine(application)
    val phoneBackupEngine = PhoneBackupEngine(application)

    // Language and Gender Configuration (Arabic, English, French, Spanish, German + Male, Female, Neutral)
    private val savedLangCode = prefs.getString("user_lang", AppLanguage.ARABIC.code) ?: AppLanguage.ARABIC.code
    private val savedGenderCode = prefs.getString("user_gender", UserGender.MALE.code) ?: UserGender.MALE.code

    private val initialLang = AppLanguage.entries.find { it.code == savedLangCode } ?: AppLanguage.ARABIC
    private val initialGender = UserGender.entries.find { it.code == savedGenderCode } ?: UserGender.MALE

    private val _languageGenderConfig = MutableStateFlow(UserLanguageGenderConfig(initialLang, initialGender))
    val languageGenderConfig: StateFlow<UserLanguageGenderConfig> = _languageGenderConfig.asStateFlow()

    fun setLanguage(language: AppLanguage) {
        _languageGenderConfig.value = _languageGenderConfig.value.copy(language = language)
        prefs.edit().putString("user_lang", language.code).apply()
    }

    fun setGender(gender: UserGender) {
        _languageGenderConfig.value = _languageGenderConfig.value.copy(gender = gender)
        prefs.edit().putString("user_gender", gender.code).apply()
    }

    // Phone Backup copies & Full Restoration Progress
    val phoneCopies: StateFlow<List<PhoneBackupCopy>> = phoneBackupEngine.phoneCopies
    val phoneRestoreProgress: StateFlow<PhoneRestoreProgress> = phoneBackupEngine.restoreProgress

    fun downloadPhoneCopy(copyId: String) {
        viewModelScope.launch {
            phoneBackupEngine.downloadCopy(copyId) { }
        }
    }

    fun restorePhoneToOriginalState(copy: PhoneBackupCopy) {
        viewModelScope.launch {
            phoneBackupEngine.restorePhoneToOriginalState(copy) { }
        }
    }

    fun resetPhoneRestoreProgress() {
        phoneBackupEngine.resetRestoreProgress()
    }

    // Cloud Sync State & Actions
    val cloudSyncProgress: StateFlow<CloudSyncProgress> = cloudSyncEngine.syncProgress
    val cloudFiles: StateFlow<List<CloudRemoteFile>> = cloudSyncEngine.cloudFiles

    fun setCloudProvider(provider: CloudProvider) {
        cloudSyncEngine.setProvider(provider)
    }

    fun toggleCloudAccount(provider: CloudProvider) {
        cloudSyncEngine.toggleAccountConnection(provider)
    }

    fun updateCloudAccountEmail(provider: CloudProvider, email: String) {
        cloudSyncEngine.updateAccountEmail(provider, email)
    }

    fun syncAllRecoveredFilesToCloud() {
        val selectedOrAll = if (_scannedItems.value.any { it.isSelected }) {
            _scannedItems.value.filter { it.isSelected }
        } else {
            _scannedItems.value
        }

        viewModelScope.launch {
            cloudSyncEngine.syncFilesToCloud(selectedOrAll) { _, _, _, _ -> }
        }
    }

    fun shareRecoveredFilesDirectlyToCloud() {
        val paths = _scannedItems.value.filter { it.isSelected }.map { it.path }.ifEmpty {
            _scannedItems.value.take(20).map { it.path }
        }
        cloudSyncEngine.shareRecoveredFilesToCloudApp(paths)
    }

    // Dark theme global setting (Default true for Cosmic Black background)
    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("is_dark_theme", true))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()


    fun toggleDarkTheme(enabled: Boolean) {
        _isDarkTheme.value = enabled
        prefs.edit().putBoolean("is_dark_theme", enabled).apply()
    }

    // Storage statistics
    private val _storageStats = MutableStateFlow(engine.getStorageStats())
    val storageStats: StateFlow<StorageStats> = _storageStats.asStateFlow()

    // Scan progress & items
    private val _scanProgress = MutableStateFlow(ScanProgress())
    val scanProgress: StateFlow<ScanProgress> = _scanProgress.asStateFlow()

    private val _scannedItems = MutableStateFlow<List<ScannableItem>>(emptyList())
    val scannedItems: StateFlow<List<ScannableItem>> = _scannedItems.asStateFlow()

    private val _selectedCategory = MutableStateFlow(FileCategory.ALL)
    val selectedCategory: StateFlow<FileCategory> = _selectedCategory.asStateFlow()

    private val _isDeepScan = MutableStateFlow(true)
    val isDeepScan: StateFlow<Boolean> = _isDeepScan.asStateFlow()

    private val _restoreDestination = MutableStateFlow(com.example.model.RestoreDestination.GALLERY)
    val restoreDestination: StateFlow<com.example.model.RestoreDestination> = _restoreDestination.asStateFlow()

    // Restoring state
    private val _isRestoring = MutableStateFlow(false)
    val isRestoring: StateFlow<Boolean> = _isRestoring.asStateFlow()

    private val _batchRestoringCurrent = MutableStateFlow(0)
    val batchRestoringCurrent: StateFlow<Int> = _batchRestoringCurrent.asStateFlow()

    private val _batchRestoringTotal = MutableStateFlow(0)
    val batchRestoringTotal: StateFlow<Int> = _batchRestoringTotal.asStateFlow()

    private val _batchRestoringFile = MutableStateFlow<String?>(null)
    val batchRestoringFile: StateFlow<String?> = _batchRestoringFile.asStateFlow()

    private val _restorationMessage = MutableStateFlow<String?>(null)
    val restorationMessage: StateFlow<String?> = _restorationMessage.asStateFlow()

    // Storage permission state
    private val _hasStoragePermission = MutableStateFlow(false)
    val hasStoragePermission: StateFlow<Boolean> = _hasStoragePermission.asStateFlow()

    fun updatePermissionStatus(hasPermission: Boolean) {
        _hasStoragePermission.value = hasPermission
        if (hasPermission) {
            refreshStorageStats()
        }
    }

    // Room Database history
    val recoveredFilesHistory: StateFlow<List<RecoveredFileEntity>> = db.recoveredFileDao()
        .getAllRecoveredFiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chat AI state
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.AI,
                text = "أهلاً بك! أنا مستشارك الرقمي الذكي لاسترجاع البيانات. يمكنك سؤالي عن استرجاع الصور والفيديوهات، استعادة ملفات الخزنة المخفية بدون نسخ احتياطي، واسترجاع الموسيقى والصوتيات."
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    fun setCategory(category: FileCategory) {
        _selectedCategory.value = category
    }

    fun setRestoreDestination(destination: com.example.model.RestoreDestination) {
        _restoreDestination.value = destination
    }

    fun toggleDeepScan(enabled: Boolean) {
        _isDeepScan.value = enabled
    }

    private var scanJob: kotlinx.coroutines.Job? = null

    fun cancelScan() {
        scanJob?.cancel()
        _scanProgress.value = _scanProgress.value.copy(isScanning = false)
    }

    fun startScan(category: FileCategory = _selectedCategory.value) {
        _selectedCategory.value = category
        _scanProgress.value = ScanProgress(isScanning = true, currentCategory = category)
        _scannedItems.value = emptyList()

        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            try {
                val results = engine.performScan(
                    targetCategory = category,
                    isDeepScan = _isDeepScan.value,
                    onProgress = { scanned, found, currentPath, percent, scannedBytes, totalBytes, estSec, phase ->
                        _scanProgress.value = ScanProgress(
                            isScanning = true,
                            scannedFilesCount = scanned,
                            foundCount = found,
                            currentPath = currentPath,
                            progressPercent = percent,
                            currentCategory = category,
                            scannedBytes = scannedBytes,
                            totalStorageBytes = totalBytes,
                            estimatedSecondsRemaining = estSec,
                            scanPhase = phase
                        )
                    }
                )
                _scannedItems.value = results
            } catch (e: Exception) {
                // Handled gracefully
            } finally {
                _scanProgress.value = _scanProgress.value.copy(
                    isScanning = false,
                    progressPercent = 1.0f
                )
                refreshStorageStats()
            }
        }
    }

    fun toggleItemSelection(item: ScannableItem) {
        _scannedItems.value = _scannedItems.value.map {
            if (it.id == item.id && it.path == item.path) {
                it.copy(isSelected = !it.isSelected)
            } else {
                it
            }
        }
    }

    fun selectAll(select: Boolean) {
        _scannedItems.value = _scannedItems.value.map { it.copy(isSelected = select) }
    }

    fun selectCategoryItems(category: FileCategory, select: Boolean = true) {
        _scannedItems.value = _scannedItems.value.map {
            if (category == FileCategory.ALL || it.category == category || (category == FileCategory.VAULT && it.isFromVault)) {
                it.copy(isSelected = select)
            } else {
                it
            }
        }
    }

    fun invertSelection() {
        _scannedItems.value = _scannedItems.value.map {
            it.copy(isSelected = !it.isSelected)
        }
    }

    fun clearSelection() {
        _scannedItems.value = _scannedItems.value.map {
            it.copy(isSelected = false)
        }
    }

    fun restoreSelectedItems(destination: com.example.model.RestoreDestination = _restoreDestination.value) {
        val selected = _scannedItems.value.filter { it.isSelected }
        if (selected.isEmpty()) {
            _restorationMessage.value = "يرجى تحديد ملف أو أكثر للاسترجاع"
            return
        }

        viewModelScope.launch {
            _isRestoring.value = true
            _batchRestoringTotal.value = selected.size
            _batchRestoringCurrent.value = 0
            _batchRestoringFile.value = selected.firstOrNull()?.name

            val restoredPairs = engine.restoreFiles(selected, destination) { current, total, currentFile ->
                _batchRestoringCurrent.value = current
                _batchRestoringTotal.value = total
                _batchRestoringFile.value = currentFile
            }

            // Record into Room Database
            val entities = restoredPairs.map { (item, restoredPath) ->
                RecoveredFileEntity(
                    name = item.name,
                    originalPath = item.path,
                    restoredPath = restoredPath,
                    fileType = item.category.name,
                    sizeBytes = item.sizeBytes,
                    restoredTimestamp = System.currentTimeMillis(),
                    recoveryStatus = "SUCCESS",
                    recoveryMethod = if (item.isFromVault) "VAULT_CARVING" else if (item.isThumbnailOrCache) "CACHE_CARVING" else "DEEP_SECTOR_SCAN"
                )
            }
            db.recoveredFileDao().insertAll(entities)

            _isRestoring.value = false
            _batchRestoringFile.value = null
            val destLabel = if (destination == com.example.model.RestoreDestination.SECURE_VAULT) "خزنة الجهاز الآمنة (Device_Secure_Vault)" else "معرض الصور ومشغل الموسيقى والتنزيلات"
            _restorationMessage.value = "تم بنجاح استرجاع دفعة من ${restoredPairs.size} ملف وحفظها في $destLabel!"
            refreshStorageStats()
        }
    }

    fun restoreSingleItem(item: ScannableItem, destination: com.example.model.RestoreDestination = _restoreDestination.value) {
        viewModelScope.launch {
            _isRestoring.value = true
            val restoredPairs = engine.restoreFiles(listOf(item), destination) { _, _, _ -> }
            if (restoredPairs.isNotEmpty()) {
                val (restoredItem, restoredPath) = restoredPairs.first()
                val entity = RecoveredFileEntity(
                    name = restoredItem.name,
                    originalPath = restoredItem.path,
                    restoredPath = restoredPath,
                    fileType = restoredItem.category.name,
                    sizeBytes = restoredItem.sizeBytes,
                    restoredTimestamp = System.currentTimeMillis(),
                    recoveryStatus = "SUCCESS",
                    recoveryMethod = if (restoredItem.isFromVault) "VAULT_CARVING" else if (restoredItem.isThumbnailOrCache) "CACHE_CARVING" else "DEEP_SECTOR_SCAN"
                )
                db.recoveredFileDao().insertRecoveredFile(entity)
                val destLabel = if (destination == com.example.model.RestoreDestination.SECURE_VAULT) "خزنة الجهاز الآمنة" else "معرض الصور والتنزيلات"
                _restorationMessage.value = "تم استرجاع '${restoredItem.name}' بنجاح وحفظه في $destLabel!"
                refreshStorageStats()
            }
            _isRestoring.value = false
        }
    }

    fun clearRestorationMessage() {
        _restorationMessage.value = null
    }

    fun refreshStorageStats() {
        _storageStats.value = engine.getStorageStats()
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            db.recoveredFileDao().deleteById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            db.recoveredFileDao().clearAll()
        }
    }

    // AI Chat
    fun sendChatMessage(text: String) {
        if (text.isBlank() || _isAiThinking.value) return

        val userMsg = ChatMessage(sender = MessageSender.USER, text = text)
        val currentHistory = _chatMessages.value + userMsg
        _chatMessages.value = currentHistory
        _isAiThinking.value = true

        viewModelScope.launch {
            try {
                val responseText = chatService.sendMessage(currentHistory, text)
                val aiMsg = ChatMessage(sender = MessageSender.AI, text = responseText)
                _chatMessages.value = _chatMessages.value + aiMsg
            } catch (e: Exception) {
                val errMsg = ChatMessage(
                    sender = MessageSender.AI,
                    text = "عذراً، حدث خطأ أثناء الاتصال. يرجى المحاولة مرة أخرى أو مراجعة قسم حلول ما بعد الفورمات."
                )
                _chatMessages.value = _chatMessages.value + errMsg
            } finally {
                _isAiThinking.value = false
            }
        }
    }
}
