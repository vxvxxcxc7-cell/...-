package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FileCategory
import com.example.ui.components.CosmicBackgroundBox
import com.example.ui.components.LanguageGenderSelectorDialog
import com.example.ui.components.ScanningProgressIndicatorCard
import com.example.ui.components.SettingsMenuDialog
import com.example.ui.theme.*
import com.example.ui.util.AppStrings
import com.example.ui.util.FormatUtils
import com.example.ui.util.StoragePermissionHelper
import com.example.viewmodel.DataRecoveryViewModel

@Composable
fun HomeScreen(
    viewModel: DataRecoveryViewModel,
    onNavigateToResults: () -> Unit,
    onNavigateToCopies: () -> Unit = {},
    onNavigateToCloud: () -> Unit = {},
    onNavigateToPostFormatGuide: () -> Unit = {}
) {
    val context = LocalContext.current
    val storageStats by viewModel.storageStats.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()
    val isDeepScan by viewModel.isDeepScan.collectAsState()
    val langGenderConfig by viewModel.languageGenderConfig.collectAsState()
    var showLanguageGenderDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    // Runtime Permission States
    var hasStoragePermission by remember {
        mutableStateOf(StoragePermissionHelper.hasStoragePermissions(context))
    }
    var pendingCategory by remember { mutableStateOf<FileCategory?>(null) }
    var showPermissionRationaleDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        val granted = StoragePermissionHelper.hasStoragePermissions(context)
        hasStoragePermission = granted
        viewModel.updatePermissionStatus(granted)
        if (granted) {
            pendingCategory?.let { cat ->
                viewModel.startScan(cat)
                pendingCategory = null
            }
        } else {
            showPermissionRationaleDialog = true
        }
    }

    fun launchScanWithPermission(category: FileCategory) {
        if (StoragePermissionHelper.hasStoragePermissions(context)) {
            hasStoragePermission = true
            viewModel.updatePermissionStatus(true)
            viewModel.startScan(category)
        } else {
            pendingCategory = category
            permissionLauncher.launch(StoragePermissionHelper.getRequiredStoragePermissions())
        }
    }

    LaunchedEffect(scanProgress.isScanning) {
        if (!scanProgress.isScanning && scanProgress.progressPercent >= 1.0f && scanProgress.foundCount > 0) {
            onNavigateToResults()
        }
    }

    CosmicBackgroundBox {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 90.dp)
        ) {
            // Header with Cosmic Branding & Language/Gender Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "استرجاع الروح في جهازك 💫",
                        color = CosmicGoldAmber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = AppStrings.getGreeting(langGenderConfig.language, langGenderConfig.gender),
                        color = CosmicTextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Language & Gender Quick Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(CosmicGoldAmberContainer)
                            .border(1.dp, CosmicGoldAmber.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .clickable { showLanguageGenderDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("btn_language_gender_switch")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(langGenderConfig.language.flag, fontSize = 14.sp)
                            Text(
                                text = langGenderConfig.gender.titleAr,
                                color = CosmicGoldAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Settings / Info Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .clickable { showInfoDialog = true }
                            .testTag("btn_info"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "⚙️", fontSize = 17.sp)
                    }
                }
            }

            // Storage Permission Warning Banner
            if (!hasStoragePermission) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xCC3E1818)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("🛡️", fontSize = 24.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "يلزم منح إذن قراءة الوسائط والذاكرة",
                                color = CosmicTextWhite,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "لمسح كتل التخزين واستعادة صور الخزينة والتسجيلات بدون قيود.",
                                color = CosmicTextSubtle,
                                fontSize = 10.5.sp
                            )
                        }
                        Button(
                            onClick = {
                                permissionLauncher.launch(StoragePermissionHelper.getRequiredStoragePermissions())
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("منح الإذن", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Storage Statistics Card (Cosmic AMOLED Design)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xD90E1422)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.5.dp,
                            Brush.horizontalGradient(listOf(CosmicGoldAmber, CosmicElectricCyan)),
                            RoundedCornerShape(24.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "حالة ذاكرة الهاتف والتخزين الداخلي",
                                    color = CosmicTextSubtle,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CosmicElectricCyanContainer)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("قطاع التخزين سليم ✓", color = CosmicElectricCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val totalGb = String.format(java.util.Locale.US, "%.1f", storageStats.totalBytes / (1024.0 * 1024.0 * 1024.0))
                                Text(
                                    text = totalGb,
                                    color = CosmicTextWhite,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "جيجابايت",
                                    color = CosmicGoldAmber,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            val animatedPct by animateFloatAsState(
                                targetValue = storageStats.percentageUsed / 100f,
                                label = "storage_pct"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.12f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(animatedPct)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            Brush.horizontalGradient(listOf(CosmicGoldAmber, CosmicElectricCyan))
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "المستخدم: ${FormatUtils.formatBytes(storageStats.usedBytes)} (${storageStats.percentageUsed}%)",
                                    color = CosmicTextWhite,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "المتاح: ${FormatUtils.formatBytes(storageStats.freeBytes)}",
                                    color = CosmicElectricCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // PRIMARY HERO CARD 1: RECOVER ALL PHOTOS & VAULT
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xE6151226)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .border(
                        1.5.dp,
                        Brush.horizontalGradient(listOf(CosmicNebulaMagenta, CosmicGoldAmber)),
                        RoundedCornerShape(22.dp)
                    )
                    .clickable {
                        launchScanWithPermission(FileCategory.PHOTOS)
                    }
                    .testTag("card_recover_all_photos_vault")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(listOf(CosmicNebulaMagenta, CosmicGoldAmber))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📸🔐", fontSize = 22.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "استرجاع جميع صور الهاتف والخزائن",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CosmicTextWhite
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CosmicNebulaMagenta.copy(alpha = 0.3f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = "شامل الخزنة", color = CosmicGoldAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "استرجاع كل الصور الملتقطة بالكاميرا + فك تشفير صور الآلة الحاسبة وخزائن الصور السرية المخفية.",
                            fontSize = 11.sp,
                            color = CosmicTextSubtle,
                            lineHeight = 15.sp
                        )
                    }

                    Text(text = "←", fontSize = 20.sp, color = CosmicGoldAmber, fontWeight = FontWeight.Bold)
                }
            }

            // PRIMARY HERO CARD 2: RESTORE PHONE TO ORIGINAL STATE (MULTIPLE COPIES)
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xE60D1A26)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .border(
                        1.5.dp,
                        Brush.horizontalGradient(listOf(CosmicElectricCyan, CosmicGoldAmber)),
                        RoundedCornerShape(22.dp)
                    )
                    .clickable { onNavigateToCopies() }
                    .testTag("card_restore_phone_copies")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CosmicElectricCyanContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "⚡🔄", fontSize = 22.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "استعادة الهاتف إلى حالته الأصلية",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CosmicTextWhite
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CosmicElectricCyan.copy(alpha = 0.25f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = "نسخ متعددة", color = CosmicElectricCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "تحميل نسخ الهاتف الاحتياطية المتعددة والمقارنة بينها واستعادة كامل بيانات الهاتف لحالته الأولى بنقرة واحدة.",
                            fontSize = 11.sp,
                            color = CosmicTextSubtle,
                            lineHeight = 15.sp
                        )
                    }

                    Text(text = "←", fontSize = 20.sp, color = CosmicElectricCyan, fontWeight = FontWeight.Bold)
                }
            }

            // PRIMARY HERO CARD 3: CLOUD SYNC & GOOGLE DRIVE
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xE60C141C)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.15f),
                        RoundedCornerShape(22.dp)
                    )
                    .clickable { onNavigateToCloud() }
                    .testTag("card_cloud_sync_hero")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "☁️", fontSize = 24.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "المزامنة السحابية المباشرة (Google Drive)",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = CosmicTextWhite
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "مزامنة الملفات المسترجعة وصور الخزائن مباشرة إلى سحابة Google Drive لحمايتها للأبد.",
                            fontSize = 11.sp,
                            color = CosmicTextSubtle,
                            lineHeight = 15.sp
                        )
                    }

                    Text(text = "←", fontSize = 20.sp, color = CosmicTextWhite, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Categories Section Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "أقسام الاسترجاع المتخصصة",
                    color = CosmicTextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "فحص مباشر بدون روت",
                    color = CosmicGoldAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Category Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CosmicCategoryCard(
                        modifier = Modifier.weight(1f),
                        emoji = "📸",
                        title = "الصور والخزائن",
                        subtitle = "PNG, JPG, HEIC, WEBP",
                        badge = "شامل الخزنة",
                        testTag = "card_photos",
                        onClick = { launchScanWithPermission(FileCategory.PHOTOS) }
                    )
                    CosmicCategoryCard(
                        modifier = Modifier.weight(1f),
                        emoji = "🎬",
                        title = "مقاطع الفيديو",
                        subtitle = "MP4, MOV, MKV, 3GP",
                        badge = "استخراج خام",
                        testTag = "card_videos",
                        onClick = { launchScanWithPermission(FileCategory.VIDEOS) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CosmicCategoryCard(
                        modifier = Modifier.weight(1f),
                        emoji = "🎵",
                        title = "الأغاني والتسجيلات",
                        subtitle = "تسجيلات المكالمات وفويس",
                        badge = "ملاحظات صوتية",
                        testTag = "card_music",
                        onClick = { launchScanWithPermission(FileCategory.MUSIC) }
                    )
                    CosmicCategoryCard(
                        modifier = Modifier.weight(1f),
                        emoji = "🔐",
                        title = "الخزنة والآلة الحاسبة",
                        subtitle = "الصور والمجلدات المشفرة",
                        badge = "فك تشفير فوري",
                        testTag = "card_vault",
                        onClick = { launchScanWithPermission(FileCategory.VAULT) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CosmicCategoryCard(
                        modifier = Modifier.weight(1f),
                        emoji = "📄",
                        title = "المستندات والوثائق",
                        subtitle = "PDF, DOCX, TXT, APK",
                        badge = "تطبيقات وملفات",
                        testTag = "card_docs",
                        onClick = { launchScanWithPermission(FileCategory.DOCUMENTS) }
                    )
                    CosmicCategoryCard(
                        modifier = Modifier.weight(1f),
                        emoji = "🔍",
                        title = "فحص شامل وكامل",
                        subtitle = "كل أنواع الملفات دفعة واحدة",
                        badge = "الكل في واحد",
                        testTag = "card_all",
                        onClick = { launchScanWithPermission(FileCategory.ALL) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Deep Scan Switch Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xD90A0E17)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CosmicGoldAmberContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✨", fontSize = 16.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "فحص قطاعات الذاكرة العميقة (Deep Scan)",
                            color = CosmicTextWhite,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "استخراج المصغرات العميقة والكاش المتبقي في الفلاش",
                            color = CosmicTextSubtle,
                            fontSize = 10.5.sp
                        )
                    }

                    Switch(
                        checked = isDeepScan,
                        onCheckedChange = { viewModel.toggleDeepScan(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CosmicBlack,
                            checkedTrackColor = CosmicGoldAmber,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Global Scan Start CTA Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Button(
                    onClick = { launchScanWithPermission(FileCategory.ALL) },
                    enabled = !scanProgress.isScanning,
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicGoldAmber),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("btn_start_deep_scan")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "🔍", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (scanProgress.isScanning) "جاري الفحص الدقيق والبحث..." else "بدء الفحص الشامل واسترجاع الملفات",
                            color = CosmicBlack,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Scanning Progress Card
            AnimatedVisibility(visible = scanProgress.isScanning) {
                ScanningProgressIndicatorCard(
                    progress = scanProgress,
                    onCancelScan = { viewModel.cancelScan() },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
        }
    }

    // Language & Gender Customization Dialog
    if (showLanguageGenderDialog) {
        LanguageGenderSelectorDialog(
            config = langGenderConfig,
            onLanguageSelected = { viewModel.setLanguage(it) },
            onGenderSelected = { viewModel.setGender(it) },
            onDismiss = { showLanguageGenderDialog = false }
        )
    }

    // Settings & AMOLED Theme / Cloud Dialog
    if (showInfoDialog) {
        SettingsMenuDialog(
            viewModel = viewModel,
            onDismiss = { showInfoDialog = false }
        )
    }

    // Storage Permission Rationale Dialog
    if (showPermissionRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionRationaleDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🛡️", fontSize = 22.sp)
                    Text("إذن قراءة الذاكرة مطلوب", color = CosmicTextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Text(
                    text = "يلزم إذن قراءة الوسائط والذاكرة لفحص كتل التخزين واستعادة صور الخزينة المشفرة ومقاطع الفيديو.",
                    color = CosmicTextWhite,
                    fontSize = 12.5.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionRationaleDialog = false
                        permissionLauncher.launch(StoragePermissionHelper.getRequiredStoragePermissions())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicGoldAmber)
                ) {
                    Text("إعادة المحاولة", color = CosmicBlack, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF0F141F)
        )
    }
}

@Composable
fun CosmicCategoryCard(
    modifier: Modifier = Modifier,
    emoji: String,
    title: String,
    subtitle: String,
    badge: String? = null,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xD90B0F19)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = modifier
            .testTag(testTag)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.06f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = CosmicTextWhite,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 9.5.sp,
                color = CosmicTextSubtle,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            if (badge != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CosmicGoldAmberContainer)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = CosmicGoldAmber
                    )
                }
            }
        }
    }
}
