package com.example.ui.screens

import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.components.OptimizedIcons
import com.example.ui.util.ShareHelper
import com.example.ui.viewmodel.MainViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allProjects by viewModel.allProjects.collectAsState()
    val compact = com.example.ui.util.LocalCompactConfig.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = compact.screenPadding),
        verticalArrangement = Arrangement.spacedBy(compact.sectionSpacing)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Export History",
                        style = if (compact.isCompact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${allProjects.size} exported videos recorded locally",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (allProjects.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { viewModel.clearAllProjects() },
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Clear All", fontSize = 11.sp, color = Color(0xFFFF5252))
                    }
                }
            }
        }

        if (allProjects.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = OptimizedIcons.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Export History", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("Your completed exports will be documented here", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(allProjects, key = { it.id }) { project ->
                val sizeMb = (project.exportSizeBytes ?: 0L) / (1024.0 * 1024.0)
                val formattedSize = if (sizeMb >= 1024) String.format(Locale.US, "%.2f GB", sizeMb / 1024.0) else String.format(Locale.US, "%.1f MB", sizeMb)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Thumbnail
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                if (project.thumbnailPath != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(File(project.thumbnailPath))
                                            .build(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(imageVector = OptimizedIcons.Video, contentDescription = null, tint = Color.White.copy(alpha = 0.4f))
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = project.originalFilename,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${project.destinationPlatform} · ${project.exportResolution ?: project.sourceResolution}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "$formattedSize · ${project.exportFps?.toInt() ?: project.sourceFps.toInt()} FPS",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF00E5FF),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault()).format(Date(project.timestamp)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    fontSize = 10.sp
                                )
                            }

                            // Actions
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        project.exportPath?.let { path ->
                                            val f = File(path)
                                            if (f.exists()) {
                                                viewModel.saveVideoToDeviceGallery(f, project.destinationPlatform)
                                                android.widget.Toast.makeText(context, "Saved to device Gallery (Movies/Optimized)!", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(imageVector = OptimizedIcons.Storage, contentDescription = "Save to Gallery", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = {
                                        project.exportPath?.let { path ->
                                            val f = File(path)
                                            if (f.exists()) ShareHelper.openVideo(context, f)
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(imageVector = OptimizedIcons.Play, contentDescription = "Play", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = {
                                        project.exportPath?.let { path ->
                                            val f = File(path)
                                            if (f.exists()) ShareHelper.shareVideo(context, f)
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(imageVector = OptimizedIcons.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
