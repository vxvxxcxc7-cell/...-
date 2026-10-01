package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.CloudConnectionAccount
import com.example.model.CloudProvider
import com.example.ui.theme.*
import com.example.viewmodel.DataRecoveryViewModel

@Composable
fun SettingsMenuDialog(
    viewModel: DataRecoveryViewModel,
    onDismiss: () -> Unit
) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val cloudProgress by viewModel.cloudSyncProgress.collectAsState()
    var editingAccount by remember { mutableStateOf<CloudConnectionAccount?>(null) }
    var newEmailText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 16.dp,
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (isDarkTheme) CosmicGoldAmber.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("dialog_settings_menu")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isDarkTheme) CosmicGoldAmberContainer else MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⚙️", fontSize = 20.sp)
                        }
                        Column {
                            Text(
                                text = "الإعدادات والتخصيص",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "السمات والمظهر وحسابات السحابة",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("btn_close_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // SECTION 1: THEME TOGGLE (Cosmic Dark AMOLED)
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "مظهر التطبيق (Display Theme)",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDarkTheme) Color(0xFF070B12) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isDarkTheme) CosmicGoldAmber.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = if (isDarkTheme) "🌌" else "☀️", fontSize = 26.sp)
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "Cosmic Dark AMOLED",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (isDarkTheme) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(CosmicGoldAmberContainer)
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("نشط 💫", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CosmicGoldAmber)
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (isDarkTheme) "خلفية سوداء حالكة AMOLED موفرة للطاقة مع ألوان نيون براقة" else "الوضع النهاري الفاتح عالي التباين",
                                                fontSize = 10.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = isDarkTheme,
                                        onCheckedChange = { enabled ->
                                            viewModel.toggleDarkTheme(enabled)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = CosmicBlack,
                                            checkedTrackColor = CosmicGoldAmber,
                                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        modifier = Modifier.testTag("switch_cosmic_dark_theme")
                                    )
                                }
                            }
                        }
                    }

                    // SECTION 2: LINKED CLOUD ACCOUNTS MANAGEMENT
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "إدارة الحسابات السحابية المتصلة ☁️",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${cloudProgress.linkedAccounts.count { it.isConnected }} متصل",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Text(
                                text = "يمكنك ربط وفصل حسابات التخزين السحابي لمزامنة الملفات المسترجعة فورياً:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Cloud accounts list
                    items(cloudProgress.linkedAccounts) { account ->
                        CloudAccountItemRow(
                            account = account,
                            isActiveProvider = cloudProgress.activeProvider == account.provider,
                            onToggleConnection = {
                                viewModel.toggleCloudAccount(account.provider)
                            },
                            onSelectActive = {
                                viewModel.setCloudProvider(account.provider)
                            },
                            onEditEmail = {
                                editingAccount = account
                                newEmailText = account.accountEmail
                            }
                        )
                    }

                    // SECTION 3: APP INFO BRIEF
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "💡 معلومات الاسترجاع والحفظ",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "تتيح الحسابات السحابية المتصلة رفع الملفات المستعادة لضمان عدم فقدانها في حال فورمات الجهاز أو تلف الذاكرة.",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkTheme) CosmicGoldAmber else MaterialTheme.colorScheme.primary,
                        contentColor = if (isDarkTheme) CosmicBlack else Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_done_settings")
                ) {
                    Text("تم وحفظ الإعدادات", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                }
            }
        }
    }

    // Edit Account Email Sub-Dialog
    editingAccount?.let { acc ->
        AlertDialog(
            onDismissRequest = { editingAccount = null },
            title = {
                Text(
                    text = "تعديل حساب ${acc.provider.displayName}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "أدخل البريد الإلكتروني أو المعرّف السحابي للحساب:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = newEmailText,
                        onValueChange = { newEmailText = it },
                        singleLine = true,
                        placeholder = { Text("example@gmail.com") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_edit_cloud_email")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateCloudAccountEmail(acc.provider, newEmailText)
                        editingAccount = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicGoldAmber)
                ) {
                    Text("حفظ الحساب", color = CosmicBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingAccount = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun CloudAccountItemRow(
    account: CloudConnectionAccount,
    isActiveProvider: Boolean,
    onToggleConnection: () -> Unit,
    onSelectActive: () -> Unit,
    onEditEmail: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (account.isConnected) {
                if (isActiveProvider) CosmicElectricCyanContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (account.isConnected && isActiveProvider) CosmicElectricCyan.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_cloud_account_${account.provider.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = account.provider.iconEmoji, fontSize = 24.sp)
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = account.provider.displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isActiveProvider && account.isConnected) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CosmicElectricCyanContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("المزود الافتراضي", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = CosmicElectricCyan)
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (account.isConnected) account.accountEmail else "غير متصل (Disconnected)",
                                fontSize = 11.sp,
                                color = if (account.isConnected) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFFFF5252)
                            )
                            if (account.isConnected) {
                                IconButton(
                                    onClick = onEditEmail,
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "تعديل الإيميل",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Connect / Disconnect Toggle Button
                Button(
                    onClick = onToggleConnection,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (account.isConnected) MaterialTheme.colorScheme.surfaceVariant else CosmicGoldAmber,
                        contentColor = if (account.isConnected) MaterialTheme.colorScheme.onSurface else CosmicBlack
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("btn_toggle_cloud_${account.provider.id}")
                ) {
                    Text(
                        text = if (account.isConnected) "فصل 🔌" else "ربط 🔗",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (account.isConnected) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "المساحة: ${account.storageUsageText.ifEmpty { account.provider.defaultQuota }}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!isActiveProvider) {
                        Text(
                            text = "تعيين كمزود رئيسي",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { onSelectActive() }
                                .padding(2.dp)
                        )
                    }
                }
            }
        }
    }
}
