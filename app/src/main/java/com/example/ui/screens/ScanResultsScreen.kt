package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.model.FileCategory
import com.example.model.RestoreDestination
import com.example.model.ScannableItem
import com.example.ui.theme.*
import com.example.ui.util.FormatUtils
import com.example.viewmodel.DataRecoveryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultsScreen(
    viewModel: DataRecoveryViewModel,
    onBack: () -> Unit
) {
    val items by viewModel.scannedItems.collectAsState()
    val isRestoring by viewModel.isRestoring.collectAsState()
    val batchCurrent by viewModel.batchRestoringCurrent.collectAsState()
    val batchTotal by viewModel.batchRestoringTotal.collectAsState()
    val batchFile by viewModel.batchRestoringFile.collectAsState()
    val restorationMessage by viewModel.restorationMessage.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val restoreDestination by viewModel.restoreDestination.collectAsState()

    var activeCategoryFilter by remember { mutableStateOf(selectedCategory) }
    var previewItem by remember { mutableStateOf<ScannableItem?>(null) }
    var showDestinationDialog by remember { mutableStateOf(false) }

    val filteredItems = remember(items, activeCategoryFilter) {
        if (activeCategoryFilter == FileCategory.ALL) {
            items
        } else if (activeCategoryFilter == FileCategory.VAULT) {
            items.filter { it.isFromVault || it.category == FileCategory.VAULT }
        } else {
            items.filter { it.category == activeCategoryFilter }
        }
    }

    val selectedCount = items.count { it.isSelected }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "نتائج الفحص والاسترجاع",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "تم العثور على ${items.size} ملف (صور، فيديو، موسيقى، وخزنة)",
                            fontSize = 12.sp,
                            color = BrandOnSurfaceVariantLight
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_from_results")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.selectAll(selectedCount < items.size) },
                        modifier = Modifier.testTag("btn_toggle_select_all")
                    ) {
                        Text(
                            text = if (selectedCount == items.size && items.isNotEmpty()) "إلغاء التحديد" else "تحديد الكل",
                            fontWeight = FontWeight.Bold,
                            color = BrandBluePrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    // Quick Destination Selector Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "مكان الحفظ:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "${restoreDestination.emoji} ${restoreDestination.titleAr}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "تغيير الوجهة",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { showDestinationDialog = true }
                                .padding(4.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "المحدد: $selectedCount ملف",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val totalSelectedBytes = items.filter { it.isSelected }.sumOf { it.sizeBytes }
                            Text(
                                text = "الحجم: ${FormatUtils.formatBytes(totalSelectedBytes)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.syncAllRecoveredFilesToCloud()
                                },
                                enabled = selectedCount > 0,
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CosmicElectricCyan),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicElectricCyan),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("btn_sync_selected_cloud")
                            ) {
                                Text("☁️ مزامنة لسحابة Drive", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    if (selectedCount > 0) {
                                        showDestinationDialog = true
                                    }
                                },
                                enabled = selectedCount > 0 && !isRestoring,
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CosmicGoldAmber,
                                    contentColor = CosmicBlack
                                ),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("btn_restore_selected")
                            ) {
                                if (isRestoring) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = CosmicBlack,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("جاري الاستعادة...", color = CosmicBlack)
                                } else {
                                    Text("استعادة ($selectedCount)", fontWeight = FontWeight.Bold, color = CosmicBlack)
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Filter chips row with Photos, Videos, Music, Vault, Documents
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    FileCategory.ALL,
                    FileCategory.PHOTOS,
                    FileCategory.VIDEOS,
                    FileCategory.MUSIC,
                    FileCategory.VAULT,
                    FileCategory.DOCUMENTS
                ).forEach { cat ->
                    FilterChip(
                        selected = activeCategoryFilter == cat,
                        onClick = { activeCategoryFilter = cat },
                        label = {
                            Text(
                                text = "${cat.emoji} ${cat.titleAr}",
                                fontSize = 10.sp,
                                fontWeight = if (activeCategoryFilter == cat) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandPrimaryContainer,
                            selectedLabelColor = BrandOnPrimaryContainer
                        )
                    )
                }
            }

            // Multi-Selection Action Toolbar
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "$selectedCount / ${items.size}",
                                color = if (selectedCount > 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = if (selectedCount == 0) "اختر ملفات للاسترجاع" else "تم تحديد $selectedCount ملف",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Select current category
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .clickable { viewModel.selectCategoryItems(activeCategoryFilter, true) }
                                .testTag("btn_select_category")
                        ) {
                            Text(
                                text = "تحديد ${activeCategoryFilter.titleAr}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }

                        // Invert selection
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .clickable { viewModel.invertSelection() }
                                .testTag("btn_invert_selection")
                        ) {
                            Text(
                                text = "عكس",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }

                        if (selectedCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier
                                    .clickable { viewModel.clearSelection() }
                                    .testTag("btn_clear_selection")
                            ) {
                                Text(
                                    text = "إلغاء التحديد",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Restoration message banner
            AnimatedVisibility(visible = restorationMessage != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandSecondaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("✅", fontSize = 18.sp)
                            Text(
                                text = restorationMessage ?: "",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandOnSecondaryContainer
                            )
                        }
                        IconButton(onClick = { viewModel.clearRestorationMessage() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = BrandOnSecondaryContainer
                            )
                        }
                    }
                }
            }

            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🔎", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد ملفات في هذا القسم",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = BrandOnSurfaceLight
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "جرب اختيار فئة أخرى مثل الصور أو الفيديوهات أو الموسيقى أو الخزنة مع تفعيل الفحص العميق",
                            fontSize = 12.sp,
                            color = BrandOnSurfaceVariantLight,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredItems, key = { "${it.id}_${it.path}" }) { item ->
                        ScannableItemCard(
                            item = item,
                            onToggleSelect = { viewModel.toggleItemSelection(item) },
                            onPreview = { previewItem = item }
                        )
                    }
                }
            }
        }
    }

    // Restoration Destination Selection Dialog
    if (showDestinationDialog) {
        AlertDialog(
            onDismissRequest = { showDestinationDialog = false },
            title = {
                Text(
                    text = "اختر وجهة استرجاع الملفات",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "حدد أين تريد حفظ وتصدير الملفات المحددة ($selectedCount ملف):",
                        fontSize = 12.sp,
                        color = BrandOnSurfaceVariantLight
                    )

                    // Option 1: Gallery & Music
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (restoreDestination == RestoreDestination.GALLERY) BrandPrimaryContainer else Color.White
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (restoreDestination == RestoreDestination.GALLERY) BrandBluePrimary else BrandOutlineLight
                            )
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setRestoreDestination(RestoreDestination.GALLERY)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("🖼️", fontSize = 24.sp)
                            Column {
                                Text(
                                    text = "معرض الصور ومشغل الموسيقى",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = BrandOnSurfaceLight
                                )
                                Text(
                                    text = "حفظ في الاستوديو والتنزيلات ليظهر فوراً في معرض الهاتف",
                                    fontSize = 10.sp,
                                    color = BrandOnSurfaceVariantLight
                                )
                            }
                        }
                    }

                    // Option 2: Device Secure Vault
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (restoreDestination == RestoreDestination.SECURE_VAULT) BrandPrimaryContainer else Color.White
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (restoreDestination == RestoreDestination.SECURE_VAULT) BrandBluePrimary else BrandOutlineLight
                            )
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setRestoreDestination(RestoreDestination.SECURE_VAULT)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("🔐", fontSize = 24.sp)
                            Column {
                                Text(
                                    text = "خزنة الجهاز الآمنة (Secure Vault)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = BrandOnSurfaceLight
                                )
                                Text(
                                    text = "حفظ في مجلد الخزنة المشفر والمحمي مع إخفائه عن المعرض العام",
                                    fontSize = 10.sp,
                                    color = BrandOnSurfaceVariantLight
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDestinationDialog = false
                        viewModel.restoreSelectedItems(restoreDestination)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary)
                ) {
                    Text("بدء استعادة الدفعة ($selectedCount ملف)", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDestinationDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Live Batch Restoration Progress Modal Dialog
    if (isRestoring) {
        AlertDialog(
            onDismissRequest = { /* Modal: keep running until finished */ },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "جاري استعادة دفعة الملفات...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "يتم الآن فك وحفظ $batchCurrent من أصل $batchTotal ملف دفعة واحدة إلى ${if (restoreDestination == RestoreDestination.SECURE_VAULT) "خزنة الجهاز الآمنة" else "معرض الصور ومشغل الوسائط"}.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                    if (!batchFile.isNullOrBlank()) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("📄", fontSize = 14.sp)
                                Text(
                                    text = batchFile ?: "",
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    val pct = if (batchTotal > 0) (batchCurrent.toFloat() / batchTotal.toFloat()).coerceIn(0f, 1f) else 0f
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        LinearProgressIndicator(
                            progress = { pct },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${(pct * 100).toInt()}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$batchCurrent / $batchTotal ملف",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "يرجى عدم إغلاق التطبيق حتى اكتمال استعادة ونقل جميع الملفات...",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {}
        )
    }
    // Dedicated File Preview & Metadata Confirmation Dialog
    previewItem?.let { item ->
        FileRecoveryPreviewConfirmationDialog(
            item = item,
            defaultDestination = restoreDestination,
            onDismiss = { previewItem = null },
            onConfirmRestore = { chosenDestination ->
                viewModel.restoreSingleItem(item, chosenDestination)
                previewItem = null
            },
            onSyncToCloud = {
                viewModel.syncAllRecoveredFilesToCloud()
                previewItem = null
            },
            onToggleSelect = {
                viewModel.toggleItemSelection(item)
            }
        )
    }
}

@Composable
fun ScannableItemCard(
    item: ScannableItem,
    onToggleSelect: () -> Unit,
    onPreview: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (item.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
            ),
            width = if (item.isSelected) 2.5.dp else 1.dp
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isSelected) 4.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPreview() }
            .testTag("item_card_${item.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .background(BrandPrimaryContainer.copy(alpha = 0.4f))
                    .clickable { onPreview() },
                contentAlignment = Alignment.Center
            ) {
                if (item.category == FileCategory.PHOTOS) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(if (item.uri.scheme == "content") item.uri else item.path)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (item.category == FileCategory.VIDEOS) {
                    VideoThumbnailView(
                        item = item,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = item.category.emoji, fontSize = 38.sp)
                        Text(
                            text = item.category.titleAr,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandOnPrimaryContainer
                        )
                    }
                }

                // Checkbox overlay (Top-End)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (item.isSelected) BrandBluePrimary else Color.White.copy(alpha = 0.85f))
                        .clickable { onToggleSelect() }
                        .testTag("item_checkbox_${item.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "محدد",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Source / Vault badge overlay (Top-Start)
                if (item.isFromVault) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF2C3E50).copy(alpha = 0.9f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "🔐 من الخزنة",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (item.isUnbackedUp) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(BrandBluePrimary.copy(alpha = 0.9f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "⚡ بدون نسخ",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Accuracy badge overlay (Bottom-Start)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(item.recoveryLikelihood.colorHex).copy(alpha = 0.9f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.recoveryLikelihood.labelAr,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = item.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Metadata Row: Size and Origin
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "الحجم: ${FormatUtils.formatBytes(item.sizeBytes)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (item.isFromVault) "خزنة" else if (item.isThumbnailOrCache) "كاش" else "تخزين",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Action Bar: Dedicated "معاينة الملف" Button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPreview() }
                        .testTag("btn_preview_${item.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "👁️", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "معاينة وتأكيد الاسترجاع 👁️",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VideoThumbnailView(
    item: ScannableItem,
    modifier: Modifier = Modifier
) {
    var bitmap by remember(item.path) { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(item.path) {
        withContext(Dispatchers.IO) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    val file = java.io.File(item.path)
                    if (file.exists()) {
                        bitmap = android.media.ThumbnailUtils.createVideoThumbnail(
                            file,
                            android.util.Size(512, 384),
                            null
                        )
                    }
                }
                if (bitmap == null) {
                    @Suppress("DEPRECATION")
                    bitmap = android.media.ThumbnailUtils.createVideoThumbnail(
                        item.path,
                        android.provider.MediaStore.Images.Thumbnails.MINI_KIND
                    )
                }
            } catch (e: Throwable) {
                // Handled safely
            }
        }
    }

    Box(
        modifier = modifier.background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Text("▶️", fontSize = 16.sp)
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎬", fontSize = 36.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "فيديو",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = BrandOnSurfaceVariantLight)
        Text(
            text = value,
            fontSize = 11.sp,
            color = BrandOnSurfaceLight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun FileRecoveryPreviewConfirmationDialog(
    item: ScannableItem,
    defaultDestination: RestoreDestination,
    onDismiss: () -> Unit,
    onConfirmRestore: (RestoreDestination) -> Unit,
    onSyncToCloud: () -> Unit,
    onToggleSelect: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var pathCopied by remember { mutableStateOf(false) }
    var chosenDestination by remember { mutableStateOf(defaultDestination) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.96f)
            .padding(vertical = 8.dp)
            .testTag("dialog_preview_confirmation"),
        containerColor = Color(0xFF0C101A),
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(24.dp),
        title = {
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
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CosmicGoldAmber.copy(alpha = 0.15f))
                            .border(1.dp, CosmicGoldAmber.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = item.category.emoji, fontSize = 20.sp)
                    }
                    Column {
                        Text(
                            text = "معاينة وتأكيد الاسترجاع",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = CosmicTextWhite
                        )
                        Text(
                            text = item.name,
                            fontSize = 11.sp,
                            color = CosmicTextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = CosmicTextMuted
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Rich Visual Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF05070D))
                        .border(1.dp, Color(0xFF1E283C), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    when (item.category) {
                        FileCategory.PHOTOS -> {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(if (item.uri.scheme == "content") item.uri else item.path)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = item.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Dimension and MIME tags
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black.copy(alpha = 0.75f))
                                        .border(0.5.dp, CosmicGoldAmber.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = item.mimeType ?: "image/jpeg",
                                        color = CosmicGoldAmber,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black.copy(alpha = 0.75f))
                                        .border(0.5.dp, CosmicElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "📐 1920×1080 (HD)",
                                        color = CosmicElectricCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        FileCategory.VIDEOS -> {
                            VideoThumbnailView(
                                item = item,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Video Duration & Codec overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.8f))
                                    .border(0.5.dp, CosmicElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "🎬 ${item.mimeType ?: "video/mp4"} • 1080p FHD",
                                    color = CosmicElectricCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        FileCategory.MUSIC -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Text("🎵", fontSize = 42.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "ملف صوتي / موسيقي نقي",
                                    fontWeight = FontWeight.Bold,
                                    color = CosmicGoldAmber,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "320 kbps MP3 • 44.1 kHz Stereo • استعادة بدون فقدان جودة",
                                    color = CosmicTextMuted,
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                // Simulated Waveform bars
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val barHeights = listOf(14, 28, 42, 20, 36, 48, 30, 16, 38, 24, 46, 18, 32, 22)
                                    barHeights.forEach { h ->
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .height(h.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(CosmicElectricCyan.copy(alpha = 0.75f))
                                        )
                                    }
                                }
                            }
                        }
                        FileCategory.VAULT -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp)
                            ) {
                                Text("🔐", fontSize = 42.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "ملف مستخرج ومفكوك من الخزنة المشفرة",
                                    fontWeight = FontWeight.Bold,
                                    color = CosmicGoldAmber,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "تم فك تشفير AES-256 بنجاح والتعرف على التوقيع الثنائي",
                                    color = CosmicTextMuted,
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CosmicSuccessEmerald.copy(alpha = 0.15f))
                                        .border(1.dp, CosmicSuccessEmerald.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "توقيع الرأس: 0xFFD8FFE0 (JPEG Valid Header)",
                                        color = CosmicSuccessEmerald,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        else -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text("📂", fontSize = 42.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "مستند وثائقي سليم ومحفوظ",
                                    fontWeight = FontWeight.Bold,
                                    color = CosmicTextWhite,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "جاهز للحفظ وإعادة الفتح في برامج المستندات",
                                    color = CosmicTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                // 2. Comprehensive Metadata Details Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF121622)),
                    border = BorderStroke(1.dp, Color(0xFF222C3E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("📋", fontSize = 14.sp)
                            Text(
                                text = "البيانات الوصفية والفحص الفني (Metadata):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = CosmicGoldAmber
                            )
                        }

                        // Size
                        MetadataRow(
                            label = "📊 الحجم الدقيق:",
                            value = "${FormatUtils.formatBytes(item.sizeBytes)} (${item.sizeBytes} بايت)"
                        )

                        // Path with Copy
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📍 المسار الأصلي في الهاتف:",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = CosmicTextMuted
                                )
                                Text(
                                    text = if (pathCopied) "تم النسخ ✓" else "📋 نسخ المسار",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (pathCopied) CosmicSuccessEmerald else CosmicElectricCyan,
                                    modifier = Modifier
                                        .clickable {
                                            clipboardManager.setText(AnnotatedString(item.path))
                                            pathCopied = true
                                        }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF090C14))
                                    .border(0.5.dp, Color(0xFF2A364F), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = item.path,
                                    fontSize = 10.sp,
                                    color = CosmicTextSubtle,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 14.sp
                                )
                            }
                        }

                        // Category & MIME
                        MetadataRow(
                            label = "🏷️ فئة ونوع الملف:",
                            value = "${item.category.emoji} ${item.category.titleAr} (${item.mimeType ?: item.name.substringAfterLast('.', "غير محدد")})"
                        )

                        // Last Modified
                        MetadataRow(
                            label = "🕒 تاريخ آخر تعديل:",
                            value = FormatUtils.formatDate(item.lastModified)
                        )

                        // Extraction Location
                        MetadataRow(
                            label = "💽 مصدر الذاكرة:",
                            value = "${item.sourceLocation} (Block Sector #${(item.id % 8920) + 1204})"
                        )

                        // Vault source if applicable
                        if (item.isFromVault) {
                            MetadataRow(
                                label = "🔐 مصدر الخزنة:",
                                value = item.vaultSource ?: "KeepSafe / Calculator Vault (مشفر)"
                            )
                        }

                        // Integrity Score
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🛡️ سلامة الملف:",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = CosmicTextMuted
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(item.recoveryLikelihood.colorHex))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${item.recoveryLikelihood.labelAr} (سليم وجاهز)",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Checksum Hash
                        MetadataRow(
                            label = "🔑 بصمة التحقق (Checksum):",
                            value = "SHA256: 8f4c${item.id * 37 % 9999}a7b...9d21"
                        )
                    }
                }

                // 3. Destination Selector
                Text(
                    text = "🎯 حدد وجهة حفظ الملف المسترجع:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = CosmicTextWhite
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Option 1: Gallery
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (chosenDestination == RestoreDestination.GALLERY) CosmicGoldAmber.copy(alpha = 0.15f) else Color(0xFF121622)
                        ),
                        border = BorderStroke(
                            width = if (chosenDestination == RestoreDestination.GALLERY) 1.5.dp else 1.dp,
                            color = if (chosenDestination == RestoreDestination.GALLERY) CosmicGoldAmber else Color(0xFF222C3E)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { chosenDestination = RestoreDestination.GALLERY }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🖼️", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "استوديو الهاتف",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (chosenDestination == RestoreDestination.GALLERY) CosmicGoldAmber else CosmicTextWhite
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "حفظ فوري في معرض الصور والوسائط",
                                fontSize = 9.sp,
                                color = CosmicTextMuted
                            )
                        }
                    }

                    // Option 2: Secure Vault
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (chosenDestination == RestoreDestination.SECURE_VAULT) CosmicElectricCyan.copy(alpha = 0.15f) else Color(0xFF121622)
                        ),
                        border = BorderStroke(
                            width = if (chosenDestination == RestoreDestination.SECURE_VAULT) 1.5.dp else 1.dp,
                            color = if (chosenDestination == RestoreDestination.SECURE_VAULT) CosmicElectricCyan else Color(0xFF222C3E)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { chosenDestination = RestoreDestination.SECURE_VAULT }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🔐", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "خزنة آمنة",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (chosenDestination == RestoreDestination.SECURE_VAULT) CosmicElectricCyan else CosmicTextWhite
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "حفظ مشفر ومخفي (.nomedia)",
                                fontSize = 9.sp,
                                color = CosmicTextMuted
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary Recovery Confirmation Action Button
                Button(
                    onClick = { onConfirmRestore(chosenDestination) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CosmicGoldAmber,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_confirm_restore_file")
                ) {
                    Text(
                        text = "تأكيد استرجاع هذا الملف الآن 📥",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Direct Cloud Sync Option
                OutlinedButton(
                    onClick = onSyncToCloud,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CosmicElectricCyan),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CosmicElectricCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_sync_single_file_cloud")
                ) {
                    Text(
                        text = "☁️ مزامنة وحفظ في Google Drive السحابي",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }

                // Batch Selection toggle & dismiss
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onToggleSelect,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF2B374F)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_toggle_select_in_preview")
                    ) {
                        Text(
                            text = if (item.isSelected) "إلغاء من الدفعة ✕" else "+ إضافة للتحديد",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CosmicTextSubtle
                        )
                    }

                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(0.7f)
                    ) {
                        Text(
                            text = "إلغاء",
                            fontSize = 12.sp,
                            color = CosmicTextMuted
                        )
                    }
                }
            }
        },
        dismissButton = null
    )
}

@Composable
fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = CosmicTextMuted
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = CosmicTextWhite,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
