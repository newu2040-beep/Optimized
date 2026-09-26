package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.domain.model.ExportStage
import com.example.engine.StorageManager
import com.example.ui.components.OptimizedIcons
import com.example.ui.util.LocalCompactConfig
import com.example.ui.util.ShareHelper
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

@Composable
fun ExportProgressScreen(
    viewModel: MainViewModel,
    onOptimizeAnother: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val compact = LocalCompactConfig.current
    val exportStage by viewModel.exportStage.collectAsState()
    val metadata by viewModel.activeMetadata.collectAsState()
    val settings by viewModel.activeSettings.collectAsState()

    BackHandler {
        if (exportStage is ExportStage.Completed || exportStage is ExportStage.Failed) {
            onOptimizeAnother()
        } else {
            viewModel.cancelExport()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(compact.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (val stage = exportStage) {
            is ExportStage.Completed -> {
                CompletedCard(
                    stage = stage,
                    thumbnailPath = metadata?.thumbnailPath,
                    viewModel = viewModel,
                    context = context,
                    onOptimizeAnother = onOptimizeAnother
                )
            }

            is ExportStage.Failed -> {
                FailedCard(
                    stage = stage,
                    onRetry = { viewModel.startExport() },
                    onDismiss = onOptimizeAnother
                )
            }

            else -> {
                ProcessingCard(
                    stage = stage,
                    thumbnailPath = metadata?.thumbnailPath,
                    platformName = settings.platform.displayName,
                    onCancel = { viewModel.cancelExport() }
                )
            }
        }
    }
}

@Composable
private fun ProcessingCard(
    stage: ExportStage,
    thumbnailPath: String?,
    platformName: String,
    onCancel: () -> Unit
) {
    val compact = LocalCompactConfig.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("processing_card"),
        shape = RoundedCornerShape(if (compact.isCompact) 18.dp else 26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    ) {
        Column(
            modifier = Modifier.padding(compact.cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Thumbnail Preview with spinner
            val thumbSize = if (compact.isCompact) 64.dp else 84.dp
            Box(
                modifier = Modifier
                    .size(thumbSize)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (thumbnailPath != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(File(thumbnailPath))
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = OptimizedIcons.Video,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(28.dp)
                    )
                }

                CircularProgressIndicator(
                    modifier = Modifier.size(thumbSize - 16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp
                )
            }

            Spacer(modifier = Modifier.height(compact.itemSpacing))

            Text(
                text = "Optimizing for $platformName",
                style = if (compact.isCompact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            val stageName = when (stage) {
                is ExportStage.Analyzing -> "Analyzing Streams"
                is ExportStage.Preparing -> "Configuring Codecs"
                is ExportStage.Encoding -> "Realtime Rendering"
                is ExportStage.Validating -> "Verifying Streams"
                is ExportStage.Finalizing -> "Saving to Gallery"
                else -> "Optimizing"
            }

            val progressPercent = if (stage is ExportStage.Encoding) stage.progressPercent else 20
            val stageMessage = when (stage) {
                is ExportStage.Analyzing -> stage.message
                is ExportStage.Preparing -> stage.message
                is ExportStage.Encoding -> stage.message
                is ExportStage.Validating -> stage.message
                is ExportStage.Finalizing -> stage.message
                else -> "Running real-time video optimization pipeline..."
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$stageName · $progressPercent%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = stageMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = if (compact.isCompact) 11.sp else 12.sp
            )

            Spacer(modifier = Modifier.height(compact.itemSpacing))

            // Progress bar
            LinearProgressIndicator(
                progress = { progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compact.isCompact) 6.dp else 8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface
            )

            Spacer(modifier = Modifier.height(compact.sectionSpacing))

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(compact.secondaryButtonHeight),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline
                )
            ) {
                Icon(imageVector = OptimizedIcons.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cancel Optimization")
            }
        }
    }
}

@Composable
private fun CompletedCard(
    stage: ExportStage.Completed,
    thumbnailPath: String?,
    viewModel: MainViewModel,
    context: Context,
    onOptimizeAnother: () -> Unit
) {
    val compact = LocalCompactConfig.current
    val coroutineScope = rememberCoroutineScope()
    val file = File(stage.outputFilePath)
    val sizeMb = stage.outputSizeBytes / (1024.0 * 1024.0)
    val formattedSize = if (sizeMb >= 1024) String.format(Locale.US, "%.2f GB", sizeMb / 1024.0) else String.format(Locale.US, "%.1f MB", sizeMb)
    val galleryStatus by viewModel.gallerySaveStatus.collectAsState()

    val saveAsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("video/mp4")
    ) { targetUri ->
        targetUri?.let { uri ->
            coroutineScope.launch {
                val res = StorageManager.saveVideoToCustomUri(context, file, uri)
                if (res.isSuccess) {
                    Toast.makeText(context, "Saved to device successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to save: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("export_complete_card"),
        shape = RoundedCornerShape(if (compact.isCompact) 18.dp else 26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            Color(0xFF00E676).copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier.padding(compact.cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Success check icon
            val checkSize = if (compact.isCompact) 48.dp else 60.dp
            Box(
                modifier = Modifier
                    .size(checkSize)
                    .clip(CircleShape)
                    .background(Color(0xFF00E676).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = OptimizedIcons.Check,
                    contentDescription = null,
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(if (compact.isCompact) 28.dp else 34.dp)
                )
            }

            Spacer(modifier = Modifier.height(compact.itemSpacing))

            Text(
                text = "Optimization Complete",
                style = if (compact.isCompact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Fully rendered and validated for ${stage.platform.displayName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(compact.itemSpacing))

            // GALLERY STATUS BANNER
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF00E676).copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = OptimizedIcons.Check,
                        contentDescription = null,
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = galleryStatus ?: "Available in Phone Gallery",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E676)
                        )
                        Text(
                            text = "Saved to Movies/Optimized (Photos & Gallery app)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(compact.itemSpacing))

            // Specs Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Destination", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(stage.platform.displayName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Resolution", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${stage.width} × ${stage.height}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Frame Rate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${stage.fps.toInt()} FPS", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Video Codec", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(stage.codec, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Optimized Size", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formattedSize, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                    }
                }
            }

            Spacer(modifier = Modifier.height(compact.sectionSpacing))

            // Primary: SAVE TO DEVICE / RE-SAVE
            Button(
                onClick = {
                    viewModel.saveVideoToDeviceGallery(file, stage.platform.id)
                    Toast.makeText(context, "Saved to device Gallery!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(compact.primaryButtonHeight)
                    .testTag("save_to_device_btn"),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = OptimizedIcons.Storage, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save to Device / Gallery", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Secondary row: Share Video & Save As (Custom Folder)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = { ShareHelper.shareVideo(context, file) },
                    modifier = Modifier
                        .weight(1f)
                        .height(compact.secondaryButtonHeight)
                        .testTag("share_video_btn"),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(imageVector = OptimizedIcons.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share", fontSize = 13.sp)
                }

                FilledTonalButton(
                    onClick = {
                        val base = file.nameWithoutExtension
                        saveAsLauncher.launch("$base.mp4")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(compact.secondaryButtonHeight)
                        .testTag("save_as_folder_btn"),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(imageVector = OptimizedIcons.Files, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save As...", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom row: Open & Optimize Another
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { ShareHelper.openVideo(context, file) },
                    modifier = Modifier
                        .weight(1f)
                        .height(compact.secondaryButtonHeight)
                        .testTag("open_video_btn"),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(imageVector = OptimizedIcons.Play, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Play", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onOptimizeAnother,
                    modifier = Modifier
                        .weight(1f)
                        .height(compact.secondaryButtonHeight)
                        .testTag("optimize_another_btn"),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(imageVector = OptimizedIcons.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Video", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun FailedCard(
    stage: ExportStage.Failed,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    val compact = LocalCompactConfig.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact.isCompact) 18.dp else 26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            Color(0xFFFF5252).copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(compact.cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val iconSize = if (compact.isCompact) 48.dp else 60.dp
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .clip(CircleShape)
                    .background(Color(0xFFFF5252).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = OptimizedIcons.Close,
                    contentDescription = null,
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(if (compact.isCompact) 28.dp else 34.dp)
                )
            }

            Spacer(modifier = Modifier.height(compact.itemSpacing))

            Text(
                text = "Processing stopped",
                style = if (compact.isCompact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stage.humanReadableError,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stage.actionableStep,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(compact.sectionSpacing))

            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(compact.primaryButtonHeight),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Try Compatible Codec")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(compact.secondaryButtonHeight),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Change Settings")
            }
        }
    }
}
