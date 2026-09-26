package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.entity.OptimizedProject
import com.example.domain.model.PlatformDestination
import com.example.ui.components.OptimizedIcons
import com.example.ui.components.PlatformBrandBadge
import com.example.ui.navigation.Screen
import com.example.ui.util.PermissionStatusState
import com.example.ui.util.ShareHelper
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.SubScreen
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    permissionStatus: PermissionStatusState,
    onOpenPermissionsDialog: () -> Unit,
    onRequestRuntimePermissions: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val recentProjects by viewModel.recentProjects.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val themeStyle by viewModel.themeStyle.collectAsState()

    // Pickers
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.importVideo(it) }
    }

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importVideo(it) }
    }

    val compact = com.example.ui.util.LocalCompactConfig.current
    val compactMode by viewModel.preferences.compactMode.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = compact.screenPadding),
        verticalArrangement = Arrangement.spacedBy(compact.sectionSpacing)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // TOP HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "OPTIMIZED",
                        style = if (compact.isCompact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Professional video optimization",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Compact Mode Toggle
                    IconButton(
                        onClick = { viewModel.preferences.setCompactMode(!compactMode) },
                        modifier = Modifier
                            .testTag("compact_mode_toggle_btn")
                            .clip(CircleShape)
                            .background(
                                if (compact.isCompact) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            )
                    ) {
                        Icon(
                            imageVector = OptimizedIcons.Crop,
                            contentDescription = "Toggle Compact Mode",
                            tint = if (compact.isCompact) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Theme cycle button
                    IconButton(
                        onClick = {
                            val next = when (themeStyle) {
                                com.example.data.preferences.ThemeStyle.GRAPHITE -> com.example.data.preferences.ThemeStyle.DARK
                                com.example.data.preferences.ThemeStyle.DARK -> com.example.data.preferences.ThemeStyle.MIDNIGHT
                                com.example.data.preferences.ThemeStyle.MIDNIGHT -> com.example.data.preferences.ThemeStyle.AURORA
                                com.example.data.preferences.ThemeStyle.AURORA -> com.example.data.preferences.ThemeStyle.CYBERPUNK
                                com.example.data.preferences.ThemeStyle.CYBERPUNK -> com.example.data.preferences.ThemeStyle.SUNSET
                                com.example.data.preferences.ThemeStyle.SUNSET -> com.example.data.preferences.ThemeStyle.EMERALD
                                com.example.data.preferences.ThemeStyle.EMERALD -> com.example.data.preferences.ThemeStyle.OCEANIC
                                com.example.data.preferences.ThemeStyle.OCEANIC -> com.example.data.preferences.ThemeStyle.AMOLED
                                com.example.data.preferences.ThemeStyle.AMOLED -> com.example.data.preferences.ThemeStyle.CREAM
                                com.example.data.preferences.ThemeStyle.CREAM -> com.example.data.preferences.ThemeStyle.LIGHT
                                com.example.data.preferences.ThemeStyle.LIGHT -> com.example.data.preferences.ThemeStyle.GRAPHITE
                                else -> com.example.data.preferences.ThemeStyle.GRAPHITE
                            }
                            viewModel.preferences.setThemeStyle(next)
                        },
                        modifier = Modifier
                            .testTag("theme_button")
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = OptimizedIcons.Smart,
                            contentDescription = "Theme Selector",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .testTag("settings_button")
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = OptimizedIcons.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // MAIN HERO IMPORT CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("import_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Big glowing icon pill
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(
                                imageVector = OptimizedIcons.Import,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isAnalyzing) "Analyzing Video..." else "Import Video",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isAnalyzing) "Extracting stream metadata, codecs and framerates" else "Optimize your video for any platform",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("import_gallery_btn"),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            enabled = !isAnalyzing
                        ) {
                            Icon(
                                imageVector = OptimizedIcons.Gallery,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gallery",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }

                        OutlinedButton(
                            onClick = { fileLauncher.launch("video/*") },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("import_files_btn"),
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                MaterialTheme.colorScheme.outline
                            ),
                            enabled = !isAnalyzing
                        ) {
                            Icon(
                                imageVector = OptimizedIcons.Files,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Files",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // PERMISSION STATUS BANNER (if any permission is missing)
        if (!permissionStatus.hasAllGranted) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onOpenPermissionsDialog() }
                        .testTag("permissions_banner"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Permissions & Full Access (${permissionStatus.grantedCount}/${permissionStatus.totalCount})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Grant notification, gallery & files full access",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = onOpenPermissionsDialog,
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Grant", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // RECENT PROJECTS SECTION
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Projects",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (recentProjects.isNotEmpty()) {
                    Text(
                        text = "View all (${recentProjects.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { viewModel.navigateTo(Screen.PROJECTS) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (recentProjects.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = OptimizedIcons.Video,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No recent projects yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Import a vertical video above to get started",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(recentProjects, key = { it.id }) { project ->
                        ProjectItemCard(
                            project = project,
                            onOpen = {
                                project.exportPath?.let { path ->
                                    val f = File(path)
                                    if (f.exists()) ShareHelper.openVideo(context, f)
                                }
                            },
                            onReOptimize = {
                                viewModel.importVideo(Uri.parse(project.originalUri))
                            },
                            onShare = {
                                project.exportPath?.let { path ->
                                    val f = File(path)
                                    if (f.exists()) ShareHelper.shareVideo(context, f)
                                }
                            },
                            onDelete = { viewModel.deleteProject(project.id) }
                        )
                    }
                }
            }
        }

        // QUICK TOOLS SECTION
        item {
            Text(
                text = "Quick Tools",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickToolCard(
                        title = "Smart Optimize",
                        subtitle = "Adaptive profiles",
                        icon = OptimizedIcons.Smart,
                        modifier = Modifier.weight(1f),
                        onClick = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) }
                    )
                    QuickToolCard(
                        title = "Compress",
                        subtitle = "Size reducer",
                        icon = OptimizedIcons.Compress,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateToSubScreen(SubScreen.COMPRESS_TOOL) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickToolCard(
                        title = "Video Info",
                        subtitle = "Deep inspection",
                        icon = OptimizedIcons.Info,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateToSubScreen(SubScreen.VIDEO_INFO_TOOL) }
                    )
                    QuickToolCard(
                        title = "Crop & Canvas",
                        subtitle = "9:16 framing",
                        icon = OptimizedIcons.Crop,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateToSubScreen(SubScreen.CROP_TOOL) }
                    )
                }

                QuickToolCard(
                    title = "Multi-Platform Export",
                    subtitle = "One video → TikTok, Reels, Shorts & WhatsApp",
                    icon = OptimizedIcons.Video,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (viewModel.activeMetadata.value != null) {
                            viewModel.startMultiPlatformExport()
                        } else {
                            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                        }
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun QuickToolCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun ProjectItemCard(
    project: OptimizedProject,
    onOpen: () -> Unit,
    onReOptimize: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column {
            // Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (project.thumbnailPath != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(File(project.thumbnailPath))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = OptimizedIcons.Video,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Platform tag
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xCC000000))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = project.destinationPlatform,
                        color = Color(0xFF00E5FF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Info
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = project.originalFilename,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${project.exportResolution ?: project.sourceResolution} · ${project.exportFps?.toInt() ?: project.sourceFps.toInt()} FPS",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action icons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onOpen, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = OptimizedIcons.Play, contentDescription = "Open", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onReOptimize, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = OptimizedIcons.Refresh, contentDescription = "Re-optimize", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = OptimizedIcons.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = OptimizedIcons.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
