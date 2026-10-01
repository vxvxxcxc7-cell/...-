package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FileCategory
import com.example.ui.theme.*
import com.example.viewmodel.DataRecoveryViewModel

@Composable
fun PostFormatGuideScreen(
    viewModel: DataRecoveryViewModel,
    onStartDeepScan: () -> Unit,
    onNavigateToAiChat: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Text(
                text = "دليل الاسترجاع بعد الفورمات وضبط المصنع",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 19.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "حلول حقيقية ومعتمدة علمياً وتقنياً بدون تضليل لاستعادة بياناتك",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Honest Technical Overview Card
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = BrandPrimaryContainer.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "🔐", fontSize = 24.sp)
                    Text(
                        text = "الحقيقة التقنية حول فورمات الأندرويد",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = BrandOnPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "في أنظمة أندرويد الحديثة (من إصدار 7.0 حتى 15+)، تُشفر الذاكرة الداخلية عبر تقنية FBE hardware encryption. عند إعادة ضبط المصنع، يقوم النظام بإتلاف مفاتيح التشفير الأساسية من رقاقة الأمان (Crypto-shredding).\n\nلذلك، أي تطبيق يدعي استرجاع الذاكرة الداخلية المشفرة كلياً بضغطة زر بعد الفورمات هو تضليل. ولكن هناك 5 طرق حقيقية ومضمونة لاسترجاع بياناتك نشرحها لك بالأسفل!",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = BrandOnSurfaceLight
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Solutions List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "الطرق المعتمدة لاسترجاع بياناتك بعد الفورمات:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = BrandOnSurfaceLight
            )

            // Solution 1: SD Card Carving
            SolutionCard(
                number = "1",
                emoji = "💾",
                title = "فحص كارت الذاكرة الخارجي (MicroSD)",
                description = "إعادة ضبط المصنع تفرمت ذاكرة الهاتف الداخلية فقط! كارت الميموري الخارجي يحتفظ بملفاته ويمكن استرجاع الصور والفيديوهات المحذوفة منه عبر الفحص العميق.",
                actionTitle = "فحص كارت الذاكرة الآن",
                onAction = {
                    viewModel.startScan(FileCategory.ALL)
                    onStartDeepScan()
                }
            )

            // Solution 2: Google Photos Cloud Trash
            SolutionCard(
                number = "2",
                emoji = "☁️",
                title = "سلة محذوفات Google Photos (60 يوماً)",
                description = "حتى بعد فورمات الهاتف، تظل الصور متزامنة في سلة المهملات السحابية لحساب Google الخاص بك لمدة 60 يوماً، وتُسترجع فور تسجيل الدخول.",
                actionTitle = "فتح إدارة حساب Google",
                onAction = {
                    try {
                        val intent = Intent(Settings.ACTION_SYNC_SETTINGS)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://photos.google.com/trash"))
                        context.startActivity(intent)
                    }
                }
            )

            // Solution 3: WhatsApp Cloud & Local Crypt
            SolutionCard(
                number = "3",
                emoji = "💬",
                title = "استعادة محادثات وصور واتساب",
                description = "يمتلك واتساب نسخاً احتياطية تلقائية يومية على Google Drive. بمجرد تثبيت واتساب وكتابة رقمك، اضغط 'استعادة النسخة الاحتياطية' لتنزيل كافة المحادثات والصور.",
                actionTitle = "استشارة المساعد الذكي حول تفاصيل واتساب",
                onAction = onNavigateToAiChat
            )

            // Solution 4: Google Drive Device Backup
            SolutionCard(
                number = "4",
                emoji = "🔄",
                title = "النسخ الاحتياطي للنظام (Google One)",
                description = "يشمل سجل المكالمات، جهات الاتصال، الرسائل النصية SMS، إعدادات الجهاز، وبيانات التطبيقات المسجلة على حسابك.",
                actionTitle = "فتح إعدادات النسخ الاحتياطي بالهاتف",
                onAction = {
                    try {
                        val intent = Intent(Settings.ACTION_SETTINGS)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Ignored
                    }
                }
            )

            // Solution 5: PC Forensic Tools
            SolutionCard(
                number = "5",
                emoji = "💻",
                title = "الاسترجاع عبر الكمبيوتر (أوامر ADB و TestDisk)",
                description = "إذا لم تعمل الطرق السابقة، يمكنك توصيل هاتفك بالكمبيوتر عبر كابل USB وتفعيل تصحيح أخطاء USB واستخدام أدوات مثل PhotoRec و TestDisk لفحص القطاعات العميقة.",
                actionTitle = "اسأل المستشار الذكي عن خطوات الكمبيوتر",
                onAction = onNavigateToAiChat
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // AI Assistant CTA Banner
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = BrandBluePrimary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clickable { onNavigateToAiChat() }
                .testTag("btn_ask_ai_from_guide")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🤖", fontSize = 22.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "تحدث مع خبير استرجاع البيانات الذكي AI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "اطرح أي سؤال حول نوع هاتفك ونظامك للحصول على حل مخصص لحالتك فوراً",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                Text(text = "←", fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SolutionCard(
    number: String,
    emoji: String,
    title: String,
    description: String,
    actionTitle: String,
    onAction: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = number, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Text(text = emoji, fontSize = 20.sp)
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onAction,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = actionTitle, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
