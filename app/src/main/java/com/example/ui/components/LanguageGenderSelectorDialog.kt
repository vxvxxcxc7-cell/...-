package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AppLanguage
import com.example.model.UserGender
import com.example.model.UserLanguageGenderConfig
import com.example.ui.theme.*

@Composable
fun LanguageGenderSelectorDialog(
    config: UserLanguageGenderConfig,
    onLanguageSelected: (AppLanguage) -> Unit,
    onGenderSelected: (UserGender) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F141F)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, CosmicGoldAmber.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CosmicGoldAmberContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🌐", fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "لغة التطبيق ونظام التخاطب",
                            color = CosmicTextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تخصيص الصياغة والضمائر بحسب الجنس",
                            color = CosmicTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(14.dp))

                // Language Section
                Text(
                    text = "اختر لغة الواجهة (Language):",
                    color = CosmicGoldAmber,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppLanguage.entries.take(3).forEach { lang ->
                        val isSelected = config.language == lang
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) CosmicGoldAmberContainer else Color.White.copy(alpha = 0.06f))
                                .border(
                                    1.dp,
                                    if (isSelected) CosmicGoldAmber else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onLanguageSelected(lang) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(lang.flag, fontSize = 20.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = lang.displayName,
                                    color = if (isSelected) CosmicGoldAmber else CosmicTextWhite,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppLanguage.entries.drop(3).forEach { lang ->
                        val isSelected = config.language == lang
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) CosmicGoldAmberContainer else Color.White.copy(alpha = 0.06f))
                                .border(
                                    1.dp,
                                    if (isSelected) CosmicGoldAmber else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onLanguageSelected(lang) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(lang.flag, fontSize = 20.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = lang.displayName,
                                    color = if (isSelected) CosmicGoldAmber else CosmicTextWhite,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(14.dp))

                // Gender Section
                Text(
                    text = "صيغة المخاطبة في النصوص (Gender):",
                    color = CosmicElectricCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UserGender.entries.forEach { gender ->
                        val isSelected = config.gender == gender
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) CosmicElectricCyanContainer else Color.White.copy(alpha = 0.06f))
                                .border(
                                    1.dp,
                                    if (isSelected) CosmicElectricCyan else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onGenderSelected(gender) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(gender.emoji, fontSize = 22.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = gender.titleAr,
                                    color = if (isSelected) CosmicElectricCyan else CosmicTextWhite,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicGoldAmber),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "تطبيق التفضيلات",
                        color = CosmicBlack,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
