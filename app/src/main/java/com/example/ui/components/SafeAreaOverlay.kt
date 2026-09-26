package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.PlatformDestination

@Composable
fun SafeAreaOverlay(
    platform: PlatformDestination,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Safe Zone Boundary Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Margins: Top 15% (profile/search bar), Bottom 22% (caption/sound), Right 18% (action buttons)
            val topMargin = when (platform) {
                PlatformDestination.TIKTOK -> h * 0.12f
                PlatformDestination.INSTAGRAM_REELS -> h * 0.10f
                PlatformDestination.YOUTUBE_SHORTS -> h * 0.08f
                else -> h * 0.10f
            }

            val bottomMargin = when (platform) {
                PlatformDestination.TIKTOK -> h * 0.22f
                PlatformDestination.INSTAGRAM_REELS -> h * 0.20f
                PlatformDestination.YOUTUBE_SHORTS -> h * 0.18f
                else -> h * 0.15f
            }

            val rightMargin = when (platform) {
                PlatformDestination.TIKTOK, PlatformDestination.INSTAGRAM_REELS, PlatformDestination.YOUTUBE_SHORTS -> w * 0.18f
                else -> w * 0.05f
            }

            val leftMargin = w * 0.05f

            // Dashed safe rectangle line
            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)

            // Draw Safe Rect Outline
            drawRect(
                color = Color(0x9900E5FF),
                topLeft = Offset(leftMargin, topMargin),
                size = androidx.compose.ui.geometry.Size(
                    w - leftMargin - rightMargin,
                    h - topMargin - bottomMargin
                ),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 2.5f,
                    pathEffect = pathEffect
                )
            )

            // Draw subtle corner crosses
            val crossSize = 14f
            val crossColor = Color(0xCC00E5FF)
            // Center cross
            drawLine(
                color = crossColor,
                start = Offset(w / 2f - crossSize, h / 2f),
                end = Offset(w / 2f + crossSize, h / 2f),
                strokeWidth = 2f
            )
            drawLine(
                color = crossColor,
                start = Offset(w / 2f, h / 2f - crossSize),
                end = Offset(w / 2f, h / 2f + crossSize),
                strokeWidth = 2f
            )
        }

        // Top UI obstruction preview indicator
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp)
                .background(Color(0x66000000), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${platform.displayName} Safe Zone",
                color = Color(0xEEFFFFFF),
                fontSize = 11.sp,
                style = MaterialTheme.typography.labelSmall
            )
        }

        // Right side mock UI obstruction indicators
        if (platform == PlatformDestination.TIKTOK || platform == PlatformDestination.INSTAGRAM_REELS || platform == PlatformDestination.YOUTUBE_SHORTS) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 6.dp)
                            .size(28.dp)
                            .background(Color(0x33FFFFFF), CircleShape)
                            .border(1.dp, Color(0x66FFFFFF), CircleShape)
                    )
                }
            }
        }

        // Bottom mock UI obstruction area
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 14.dp, bottom = 14.dp)
                .background(Color(0x44000000), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Keep text & faces inside dashed boundary",
                color = Color(0xBBFFFFFF),
                fontSize = 10.sp
            )
        }
    }
}
