package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.DataRecoveryViewModel

enum class AppTab(val id: String, val emoji: String) {
    HOME("home", "🏠"),
    COPIES("copies", "🔄"),
    CLOUD("cloud", "☁️"),
    POST_FORMAT("post_format", "🛡️"),
    AI_CHAT("ai_chat", "🤖")
}

@Composable
fun MainAppScreen(viewModel: DataRecoveryViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(AppTab.HOME) }
    var isShowingResults by remember { mutableStateOf(false) }
    var hasRequestedPermission by remember { mutableStateOf(false) }
    val langGenderConfig by viewModel.languageGenderConfig.collectAsState()

    // Runtime Permission handling for Media & Storage using StoragePermissionHelper
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        val granted = com.example.ui.util.StoragePermissionHelper.hasStoragePermissions(context)
        viewModel.updatePermissionStatus(granted)
        viewModel.refreshStorageStats()
    }

    LaunchedEffect(Unit) {
        if (!hasRequestedPermission) {
            hasRequestedPermission = true
            val hasPerm = com.example.ui.util.StoragePermissionHelper.hasStoragePermissions(context)
            viewModel.updatePermissionStatus(hasPerm)
            if (!hasPerm) {
                permissionLauncher.launch(com.example.ui.util.StoragePermissionHelper.getRequiredStoragePermissions())
            }
        }
    }

    val isRtl = langGenderConfig.language == com.example.model.AppLanguage.ARABIC
    val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            bottomBar = {
                if (!isShowingResults) {
                    CosmicBottomNav(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        language = langGenderConfig.language
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isShowingResults) {
                    ScanResultsScreen(
                        viewModel = viewModel,
                        onBack = { isShowingResults = false }
                    )
                } else {
                    Crossfade(targetState = selectedTab, label = "tab_fade") { tab ->
                        when (tab) {
                            AppTab.HOME -> HomeScreen(
                                viewModel = viewModel,
                                onNavigateToResults = { isShowingResults = true },
                                onNavigateToCopies = { selectedTab = AppTab.COPIES },
                                onNavigateToCloud = { selectedTab = AppTab.CLOUD },
                                onNavigateToPostFormatGuide = { selectedTab = AppTab.POST_FORMAT }
                            )
                            AppTab.COPIES -> PhoneBackupsScreen(viewModel = viewModel)
                            AppTab.CLOUD -> CloudSyncScreen(viewModel = viewModel)
                            AppTab.POST_FORMAT -> PostFormatGuideScreen(
                                viewModel = viewModel,
                                onStartDeepScan = { isShowingResults = true },
                                onNavigateToAiChat = { selectedTab = AppTab.AI_CHAT }
                            )
                            AppTab.AI_CHAT -> AiExpertChatScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CosmicBottomNav(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    language: com.example.model.AppLanguage
) {
    Surface(
        color = Color(0xFF06090F),
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Top ambient neon hairline
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(CosmicGoldAmber.copy(alpha = 0.6f), CosmicElectricCyan.copy(alpha = 0.6f))
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    val tabTitle = when (tab) {
                        AppTab.HOME -> if (language == com.example.model.AppLanguage.ARABIC) "الرئيسية" else "Home"
                        AppTab.COPIES -> if (language == com.example.model.AppLanguage.ARABIC) "نسخ الهاتف" else "Copies"
                        AppTab.CLOUD -> if (language == com.example.model.AppLanguage.ARABIC) "السحابة" else "Cloud"
                        AppTab.POST_FORMAT -> if (language == com.example.model.AppLanguage.ARABIC) "بعد الفورمات" else "Recovery"
                        AppTab.AI_CHAT -> if (language == com.example.model.AppLanguage.ARABIC) "المستشار" else "AI Chat"
                    }

                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) CosmicGoldAmberContainer.copy(alpha = 0.6f) else Color.Transparent)
                            .clickable { onTabSelected(tab) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("nav_tab_${tab.name.lowercase()}"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = tab.emoji,
                            fontSize = if (isSelected) 22.sp else 19.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tabTitle,
                            fontSize = 10.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) CosmicGoldAmber else CosmicTextMuted
                        )
                    }
                }
            }
        }
    }
}

