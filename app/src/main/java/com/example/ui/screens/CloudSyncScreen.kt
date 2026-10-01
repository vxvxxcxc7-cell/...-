package com.example.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CloudProvider
import com.example.model.CloudSyncState
import com.example.ui.components.CosmicBackgroundBox
import com.example.ui.theme.*
import com.example.ui.util.AppStrings
import com.example.viewmodel.DataRecoveryViewModel

@Composable
fun CloudSyncScreen(viewModel: DataRecoveryViewModel) {
    val syncProgress by viewModel.cloudSyncProgress.collectAsState()
    val cloudFiles by viewModel.cloudFiles.collectAsState()
    val langGenderConfig by viewModel.languageGenderConfig.collectAsState()

    CosmicBackgroundBox {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Cloud Header Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CosmicSurfaceGlass),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.5.dp,
                        Brush.horizontalGradient(listOf(CosmicElectricCyan, CosmicGoldAmber)),
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
                                .background(CosmicElectricCyanContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("☁️", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AppStrings.getCloudSyncTitle(langGenderConfig.language, langGenderConfig.gender),
                                color = CosmicTextWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "حفظ ونسخ احتياطي فوري للملفات المسترجعة وصور الخزينة",
                                color = CosmicTextSubtle,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Connected Account Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "الحساب المتصل:", color = CosmicTextMuted, fontSize = 11.sp)
                            Text(text = syncProgress.userAccountEmail, color = CosmicTextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CosmicSuccessEmerald.copy(alpha = 0.15f))
                                .border(1.dp, CosmicSuccessEmerald.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("متصل بالسحابة ✓", color = CosmicSuccessEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Cloud Quota Progress
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("مساحة Google Drive:", color = CosmicTextMuted, fontSize = 11.sp)
                        Text("48.6 GB مستخدم من 100 GB", color = CosmicGoldAmber, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { 0.486f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = CosmicElectricCyan,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cloud Provider Selector Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CloudProvider.entries.forEach { provider ->
                    val isSelected = syncProgress.activeProvider == provider
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) CosmicElectricCyanContainer else Color.White.copy(alpha = 0.05f))
                            .border(
                                1.dp,
                                if (isSelected) CosmicElectricCyan else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.setCloudProvider(provider) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(provider.iconEmoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = provider.displayName.take(12),
                                color = if (isSelected) CosmicElectricCyan else CosmicTextWhite,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sync Trigger Actions
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xD90C111C)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CosmicGoldAmber.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "إجراءات المزامنة والرفع السحابي",
                        color = CosmicTextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "رفع الصور المسترجعة وصور الخزائن المشفرة مباشرة إلى مجلد مخصص في Google Drive لتأمينها من أي مسح أو تلف في الجهاز.",
                        color = CosmicTextSubtle,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (syncProgress.state == CloudSyncState.SYNCING) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "جاري رفع: ${syncProgress.currentFileName ?: "الملفات"}",
                                    color = CosmicElectricCyan,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${(syncProgress.progressPercent * 100).toInt()}% (${syncProgress.uploadSpeedKbps} KB/s)",
                                    color = CosmicGoldAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { syncProgress.progressPercent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = CosmicGoldAmber,
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                        }
                    } else if (syncProgress.state == CloudSyncState.COMPLETED) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(CosmicSuccessEmerald.copy(alpha = 0.15f))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✓ تمت المزامنة السحابية بنجاح وحفظ الملفات في Google Drive",
                                color = CosmicSuccessEmerald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.syncAllRecoveredFilesToCloud() },
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("sync_to_gdrive_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicGoldAmber)
                        ) {
                            Text(
                                text = "☁️ مزامنة إلى Google Drive",
                                color = CosmicBlack,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.shareRecoveredFilesDirectlyToCloud() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_cloud_share_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CosmicElectricCyan),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CosmicElectricCyan)
                        ) {
                            Text("فتح بالتطبيق ↗", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Synced Files List
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الملفات المحفوظة على السحابة (${cloudFiles.size})",
                    color = CosmicTextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = syncProgress.activeProvider.displayName,
                    color = CosmicElectricCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(cloudFiles, key = { it.id }) { file ->
                    CloudFileItemCard(file = file)
                }
            }
        }
    }
}

@Composable
fun CloudFileItemCard(file: com.example.model.CloudRemoteFile) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x990A0D15)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.07f)),
                contentAlignment = Alignment.Center
            ) {
                Text(file.category.emoji, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    color = CosmicTextWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text(
                    text = "${file.category.titleAr} • ${file.sizeBytes / 1024} KB • محفوظ في ${file.provider.displayName}",
                    color = CosmicTextMuted,
                    fontSize = 10.5.sp
                )
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(CosmicSuccessEmerald.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", color = CosmicSuccessEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
