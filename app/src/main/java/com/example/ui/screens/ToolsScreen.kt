package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.OptimizedIcons
import com.example.ui.navigation.Screen
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.SubScreen

@Composable
fun ToolsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val activeMetadata by viewModel.activeMetadata.collectAsState()
    var showTrimDialog by remember { mutableStateOf(false) }
    var showCropDialog by remember { mutableStateOf(false) }
    var showQualityInspectorDialog by remember { mutableStateOf(false) }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.importVideo(it) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Creator Tools",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Specialized media utilities for vertical video workflows",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // CATEGORY: OPTIMIZE
        item {
            ToolCategorySection(
                title = "OPTIMIZE",
                icon = OptimizedIcons.Smart,
                tools = listOf(
                    ToolItem("Smart Optimize", "Automatic parameter matching & zero re-encode detection", OptimizedIcons.Smart) {
                        videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                    },
                    ToolItem("Multi-Platform Export", "Simultaneous export for TikTok, Reels, Shorts, Snapchat", OptimizedIcons.Video) {
                        if (activeMetadata != null) viewModel.startMultiPlatformExport()
                        else videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                    },
                    ToolItem("Batch Optimize", "Process multiple videos simultaneously in a queue", OptimizedIcons.Files) {
                        viewModel.navigateToSubScreen(SubScreen.BATCH_OPTIMIZE)
                    }
                )
            )
        }

        // CATEGORY: EDIT
        item {
            ToolCategorySection(
                title = "EDIT & FRAME",
                icon = OptimizedIcons.Crop,
                tools = listOf(
                    ToolItem("Trim Tool", "Set precise in/out cut points on timeline", OptimizedIcons.Trim) {
                        if (activeMetadata != null) showTrimDialog = true
                        else videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                    },
                    ToolItem("Crop & Canvas", "9:16 vertical canvas adaptation without stretching", OptimizedIcons.Crop) {
                        if (activeMetadata != null) showCropDialog = true
                        else videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                    }
                )
            )
        }

        // CATEGORY: ANALYZE
        item {
            ToolCategorySection(
                title = "ANALYZE & INSPECT",
                icon = OptimizedIcons.Analytics,
                tools = listOf(
                    ToolItem("Quality Inspector", "Compare source bitrate & color standard vs platform limits", OptimizedIcons.Info) {
                        if (activeMetadata != null) showQualityInspectorDialog = true
                        else videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                    },
                    ToolItem("Video Info", "Deep metadata analyzer: Codecs, Audio tracks, HDR, FPS", OptimizedIcons.Info) {
                        if (activeMetadata != null) viewModel.navigateToSubScreen(SubScreen.OPTIMIZE)
                        else videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                    }
                )
            )
        }

        // CATEGORY: MANAGE
        item {
            ToolCategorySection(
                title = "MANAGE & STORAGE",
                icon = OptimizedIcons.Storage,
                tools = listOf(
                    ToolItem("Export History", "View past optimized videos and re-export", OptimizedIcons.History) {
                        viewModel.navigateTo(Screen.HISTORY)
                    },
                    ToolItem("Storage Management", "Clear temp files, cached thumbnails and inspect disk", OptimizedIcons.Storage) {
                        viewModel.navigateTo(Screen.SETTINGS)
                    }
                )
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // TRIM DIALOG
    if (showTrimDialog && activeMetadata != null) {
        val meta = activeMetadata!!
        var startFraction by remember { mutableFloatStateOf(0f) }
        var endFraction by remember { mutableFloatStateOf(1f) }

        AlertDialog(
            onDismissRequest = { showTrimDialog = false },
            title = { Text("Trim Video", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Select in/out timeline points for ${meta.filename}:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Start: ${(startFraction * meta.durationMs / 1000).toInt()}s", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = startFraction,
                        onValueChange = { startFraction = it.coerceAtMost(endFraction - 0.05f) }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text("End: ${(endFraction * meta.durationMs / 1000).toInt()}s", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = endFraction,
                        onValueChange = { endFraction = it.coerceAtLeast(startFraction + 0.05f) }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val startMs = (startFraction * meta.durationMs).toLong()
                        val endMs = (endFraction * meta.durationMs).toLong()
                        viewModel.updateSettings(viewModel.activeSettings.value.copy(trimStartMs = startMs, trimEndMs = endMs))
                        showTrimDialog = false
                        viewModel.navigateToSubScreen(SubScreen.OPTIMIZE)
                    }
                ) {
                    Text("Apply Trim")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showTrimDialog = false }) { Text("Cancel") }
            }
        )
    }

    // CROP DIALOG
    if (showCropDialog && activeMetadata != null) {
        AlertDialog(
            onDismissRequest = { showCropDialog = false },
            title = { Text("Crop & Canvas Framing", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Choose how the source video is positioned in the 9:16 vertical canvas:", fontSize = 13.sp)
                    com.example.domain.model.CanvasMode.values().forEach { mode ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateSettings(viewModel.activeSettings.value.copy(canvasMode = mode))
                                    showCropDialog = false
                                    viewModel.navigateToSubScreen(SubScreen.OPTIMIZE)
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(mode.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                Text(mode.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(onClick = { showCropDialog = false }) { Text("Close") }
            }
        )
    }

    // QUALITY INSPECTOR DIALOG
    if (showQualityInspectorDialog && activeMetadata != null) {
        val meta = activeMetadata!!
        AlertDialog(
            onDismissRequest = { showQualityInspectorDialog = false },
            title = { Text("Quality Inspector", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("SOURCE MEDIA", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Text("• Resolution: ${meta.displayWidth} × ${meta.displayHeight} (${meta.aspectRatioLabel})", fontSize = 12.sp)
                    Text("• Bitrate: ${meta.formattedBitrate}", fontSize = 12.sp)
                    Text("• Dynamic Range: ${if (meta.isHdr) "HDR (BT.2020)" else "SDR (BT.709)"}", fontSize = 12.sp)
                    Text("• Codec: ${meta.videoCodec}", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("DIAGNOSTIC STATUS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    Text("• Quality rating: ${meta.qualityStatus}", fontSize = 12.sp)
                    Text("• Compression potential: Excellent (optimized profile saves up to 40% file size)", fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showQualityInspectorDialog = false }) { Text("Done") }
            }
        )
    }
}

data class ToolItem(val title: String, val subtitle: String, val icon: ImageVector, val onClick: () -> Unit)

@Composable
private fun ToolCategorySection(
    title: String,
    icon: ImageVector,
    tools: List<ToolItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 0.5.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tools.forEach { tool ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { tool.onClick() },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = tool.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tool.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                Text(tool.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            }
                            Icon(imageVector = OptimizedIcons.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
