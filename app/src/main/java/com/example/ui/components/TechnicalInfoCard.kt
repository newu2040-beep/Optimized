package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.VideoMetadata

@Composable
fun TechnicalInfoCard(
    metadata: VideoMetadata,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with filename and Source Quality Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = metadata.filename,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = "${metadata.formattedSize} · ${metadata.formattedDuration}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Source Quality Status Pill
                val (badgeBg, badgeText) = when (metadata.qualityStatus) {
                    "Excellent" -> Pair(Color(0xFF00E676).copy(alpha = 0.15f), Color(0xFF00E676))
                    "Good" -> Pair(Color(0xFF00E5FF).copy(alpha = 0.15f), Color(0xFF00E5FF))
                    "Needs optimization" -> Pair(Color(0xFFFFB300).copy(alpha = 0.15f), Color(0xFFFFB300))
                    else -> Pair(Color(0xFF2979FF).copy(alpha = 0.15f), Color(0xFF2979FF))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeBg)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = metadata.qualityStatus,
                        color = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(14.dp))

            // VIDEO SPECS
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = OptimizedIcons.Video,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "VIDEO SPECIFICATIONS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SpecRow("Resolution", "${metadata.displayWidth} × ${metadata.displayHeight} (${metadata.aspectRatioLabel})")
                SpecRow("Frame Rate", "${metadata.fps.toInt()} FPS")
                SpecRow("Video Codec", metadata.videoCodec)
                SpecRow("Video Bitrate", metadata.formattedBitrate)
                SpecRow("Color / Dynamic Range", "${metadata.colorSpace} ${if (metadata.isHdr) "• HDR" else "• SDR"}")
                SpecRow("Container & Rotation", "${metadata.container} · ${metadata.rotationDegrees}°")
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(14.dp))

            // AUDIO SPECS
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = OptimizedIcons.Audio,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AUDIO SPECIFICATIONS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SpecRow("Audio Codec", metadata.audioCodec ?: "None / Muted")
                if (metadata.audioBitrateBps != null && metadata.audioBitrateBps > 0) {
                    SpecRow("Audio Bitrate", "${metadata.audioBitrateBps / 1000} kbps")
                }
                if (metadata.audioSampleRate != null) {
                    SpecRow("Sample Rate", "${metadata.audioSampleRate} Hz")
                }
                if (metadata.audioChannels != null) {
                    SpecRow("Channels", if (metadata.audioChannels == 2) "Stereo (2.0)" else "Mono (1.0)")
                }
            }
        }
    }
}

@Composable
fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
