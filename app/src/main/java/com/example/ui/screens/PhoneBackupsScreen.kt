package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.PhoneBackupCopy
import com.example.ui.components.CosmicBackgroundBox
import com.example.ui.theme.*
import com.example.ui.util.AppStrings
import com.example.viewmodel.DataRecoveryViewModel

@Composable
fun PhoneBackupsScreen(viewModel: DataRecoveryViewModel) {
    val copies by viewModel.phoneCopies.collectAsState()
    val restoreProgress by viewModel.phoneRestoreProgress.collectAsState()
    val langGenderConfig by viewModel.languageGenderConfig.collectAsState()
    var selectedCopyId by remember { mutableStateOf<String?>(copies.firstOrNull()?.id) }
    var showRestoreDialog by remember { mutableStateOf(false) }

    val activeSelectedCopy = copies.find { it.id == selectedCopyId } ?: copies.firstOrNull()

    CosmicBackgroundBox {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Header Banner
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CosmicSurfaceGlass),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.5.dp,
                        Brush.horizontalGradient(listOf(CosmicGoldAmber, CosmicElectricCyan)),
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(CosmicGoldAmberContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔄", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "نسخ الهاتف الاحتياطية المكتشفة",
                                color = CosmicTextWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "حمّل النسخ وقارن بينها لاستعادة الهاتف إلى حالته الأصلية",
                                color = CosmicTextSubtle,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "عُثر على ${copies.size} نسخ احتياطية شاملة للهاتف (سحابية، ما قبل الفورمات، وخزائن مشفرة). يمكنك تنزيل أي نسخة وفحص محتواها ثم اختيارها لاستعادة الهاتف بالكامل.",
                        color = CosmicTextMuted,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.getChooseCopyTitle(langGenderConfig.language, langGenderConfig.gender),
                    color = CosmicTextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${copies.size} نسخ متاحة",
                    color = CosmicGoldAmber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // List of copies
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(copies, key = { it.id }) { copy ->
                    val isChosen = copy.id == selectedCopyId
                    PhoneCopyCard(
                        copy = copy,
                        isChosen = isChosen,
                        onSelect = { selectedCopyId = copy.id },
                        onDownload = { viewModel.downloadPhoneCopy(copy.id) },
                        onRestore = {
                            selectedCopyId = copy.id
                            showRestoreDialog = true
                        }
                    )
                }
            }
        }
    }

    // Full Phone Restoration Dialog / Wizard
    if (showRestoreDialog && activeSelectedCopy != null) {
        PhoneRestoreWizardDialog(
            copy = activeSelectedCopy,
            restoreProgress = restoreProgress,
            onStartRestore = { viewModel.restorePhoneToOriginalState(activeSelectedCopy) },
            onDismiss = {
                showRestoreDialog = false
                viewModel.resetPhoneRestoreProgress()
            }
        )
    }
}

@Composable
fun PhoneCopyCard(
    copy: PhoneBackupCopy,
    isChosen: Boolean,
    onSelect: () -> Unit,
    onDownload: () -> Unit,
    onRestore: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isChosen) Color(0xFF131A28) else Color(0xD90A0D15)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(
                width = if (isChosen) 2.dp else 1.dp,
                brush = if (isChosen) {
                    Brush.horizontalGradient(listOf(CosmicGoldAmber, CosmicElectricCyan))
                } else {
                    Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.04f)))
                },
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(copy.source.badgeColorHex).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(copy.source.iconEmoji, fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = copy.title,
                        color = CosmicTextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = copy.backupDateText,
                        color = CosmicTextMuted,
                        fontSize = 11.sp
                    )
                }

                // Radio-like Selection pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isChosen) CosmicGoldAmber else Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isChosen) "✓ تم الاختيار" else "اختر هذه",
                        color = if (isChosen) CosmicBlack else CosmicTextWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = copy.subtitle,
                color = CosmicTextSubtle,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics Grid (Photos, Vault, Videos, Audio, Size)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MetricColumn(icon = "📸", label = "الصور", value = "${copy.photosCount}")
                MetricColumn(icon = "🔐", label = "الخزينة", value = "${copy.vaultPhotosCount}", highlight = true)
                MetricColumn(icon = "🎬", label = "الفيديو", value = "${copy.videosCount}")
                MetricColumn(icon = "🎵", label = "صوتيات", value = "${copy.audioCount}")
                MetricColumn(icon = "💾", label = "الحجم", value = copy.formattedSize)
            }

            // Download Progress if in progress
            if (copy.downloadProgress > 0f && copy.downloadProgress < 1.0f) {
                Spacer(modifier = Modifier.height(10.dp))
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("جاري تنزيل النسخة وفك التشفير...", color = CosmicGoldAmber, fontSize = 11.sp)
                        Text("${(copy.downloadProgress * 100).toInt()}%", color = CosmicGoldAmber, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { copy.downloadProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = CosmicGoldAmber,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!copy.isDownloaded) {
                    OutlinedButton(
                        onClick = onDownload,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("download_copy_btn_${copy.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CosmicGoldAmber),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicGoldAmber)
                    ) {
                        Text("📥 تحميل النسخة", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CosmicSuccessEmerald.copy(alpha = 0.15f))
                            .border(1.dp, CosmicSuccessEmerald.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✓ تم التحميل وجاهزة", color = CosmicSuccessEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onRestore,
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("restore_phone_state_btn_${copy.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicGoldAmber)
                ) {
                    Text(
                        text = "⚡ استعادة الهاتف لحالته",
                        color = CosmicBlack,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MetricColumn(icon: String, label: String, value: String, highlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = if (highlight) CosmicElectricCyan else CosmicTextWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = CosmicTextMuted,
            fontSize = 9.5.sp
        )
    }
}

@Composable
fun PhoneRestoreWizardDialog(
    copy: PhoneBackupCopy,
    restoreProgress: com.example.model.PhoneRestoreProgress,
    onStartRestore: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF090C14)),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    2.dp,
                    Brush.verticalGradient(listOf(CosmicGoldAmber, CosmicElectricCyan, CosmicNebulaMagenta)),
                    RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Badge
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(CosmicGoldAmber, CosmicElectricCyan))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚡", fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "استعادة الهاتف إلى حالته الأصلية",
                    color = CosmicTextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "النسخة المختارة: ${copy.title}",
                    color = CosmicGoldAmber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(14.dp))

                if (!restoreProgress.isRestoring && !restoreProgress.isCompleted) {
                    // Pre-restoration confirmation
                    Text(
                        text = "سيتم الآن استرجاع وإعادة بناء كافة ملفات هذه النسخة في مسارات التخزين الأصلية لجهازك:",
                        color = CosmicTextSubtle,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RestoreSummaryRow("📸 كل الصور الملتقطة ومكتبة الكاميرا", "${copy.photosCount} صورة")
                        RestoreSummaryRow("🔐 صور الخزينة المشفرة والآلة الحاسبة", "${copy.vaultPhotosCount} صورة")
                        RestoreSummaryRow("🎬 مقاطع الفيديو الأصلية", "${copy.videosCount} فيديو")
                        RestoreSummaryRow("🎵 التسجيلات والمكالمات والموسيقى", "${copy.audioCount} تسجيل")
                        RestoreSummaryRow("📁 جهات الاتصال والمستندات", "${copy.contactsCount + copy.documentsCount} ملف")
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CosmicTextMuted)
                        ) {
                            Text("إلغاء")
                        }

                        Button(
                            onClick = onStartRestore,
                            modifier = Modifier
                                .weight(1.6f)
                                .testTag("confirm_full_phone_restore_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicGoldAmber)
                        ) {
                            Text(
                                text = "بدء الاستعادة الشاملة",
                                color = CosmicBlack,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else if (restoreProgress.isRestoring) {
                    // Active Restoration in progress
                    Text(
                        text = restoreProgress.currentPhase,
                        color = CosmicElectricCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { restoreProgress.percent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = CosmicGoldAmber,
                        trackColor = Color.White.copy(alpha = 0.12f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${restoreProgress.restoredItemsCount} / ${restoreProgress.totalItemsCount} ملف",
                            color = CosmicTextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${(restoreProgress.percent * 100).toInt()}%",
                            color = CosmicGoldAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "جاري استرجاع: ${restoreProgress.currentRestoredItemName}",
                        color = CosmicTextMuted,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                } else if (restoreProgress.isCompleted) {
                    // Success completion
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(CosmicSuccessEmerald.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✅", fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "تمت استعادة الهاتف لحالته الأصلية بنجاح! 💫",
                        color = CosmicSuccessEmerald,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = restoreProgress.completionSummary ?: "تم استرجاع كامل الصور والخزائن ومقاطع الفيديو بنجاح وحفظها في التخزين الأصلي.",
                        color = CosmicTextWhite,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicGoldAmber)
                    ) {
                        Text(
                            text = "تم وفحص الملفات المستعادة",
                            color = CosmicBlack,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RestoreSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = CosmicTextWhite, fontSize = 11.5.sp)
        Text(text = value, color = CosmicGoldAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
