package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.model.PlatformDestination

object OptimizedIcons {
    val Home: ImageVector = Icons.Default.Home
    val Import: ImageVector = Icons.Default.Upload
    val Gallery: ImageVector = Icons.Default.PhotoLibrary
    val Files: ImageVector = Icons.Default.Folder
    val Settings: ImageVector = Icons.Default.Settings
    val Play: ImageVector = Icons.Default.PlayArrow
    val Pause: ImageVector = Icons.Default.Pause
    val Volume: ImageVector = Icons.Default.VolumeUp
    val Mute: ImageVector = Icons.Default.VolumeMute
    val Fullscreen: ImageVector = Icons.Default.Fullscreen
    val FullscreenExit: ImageVector = Icons.Default.FullscreenExit
    val Download: ImageVector = Icons.Default.FileDownload
    val Share: ImageVector = Icons.Default.Share
    val Delete: ImageVector = Icons.Default.Delete
    val Edit: ImageVector = Icons.Default.Edit
    val History: ImageVector = Icons.Default.History
    val Tools: ImageVector = Icons.Default.Tune
    val Video: ImageVector = Icons.Default.Videocam
    val Audio: ImageVector = Icons.Default.Audiotrack
    val Crop: ImageVector = Icons.Default.AspectRatio
    val Trim: ImageVector = Icons.Default.ContentCut
    val Compress: ImageVector = Icons.Default.GraphicEq
    val Info: ImageVector = Icons.Default.Info
    val Check: ImageVector = Icons.Default.Check
    val Close: ImageVector = Icons.Default.Close
    val ChevronRight: ImageVector = Icons.Default.KeyboardArrowRight
    val ChevronDown: ImageVector = Icons.Default.KeyboardArrowDown
    val ChevronUp: ImageVector = Icons.Default.KeyboardArrowUp
    val Back: ImageVector = Icons.AutoMirrored.Filled.ArrowBack
    val Forward: ImageVector = Icons.AutoMirrored.Filled.ArrowForward
    val More: ImageVector = Icons.Default.MoreVert
    val Refresh: ImageVector = Icons.Default.Refresh
    val Storage: ImageVector = Icons.Default.Storage
    val Privacy: ImageVector = Icons.Default.Security
    val Notifications: ImageVector = Icons.Default.Notifications
    val Smart: ImageVector = Icons.Default.AutoAwesome
    val Analytics: ImageVector = Icons.Default.Analytics
    val Lock: ImageVector = Icons.Default.Lock
    val Unlock: ImageVector = Icons.Default.LockOpen
}

@Composable
fun PlatformBrandBadge(
    platform: PlatformDestination,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val (bgColor, iconVector, tintColor) = when (platform) {
        PlatformDestination.TIKTOK -> Triple(Color(0xFF000000), Icons.Default.Movie, Color(0xFF00F2FE))
        PlatformDestination.INSTAGRAM_REELS -> Triple(Color(0xFFE1306C), Icons.Default.Videocam, Color.White)
        PlatformDestination.YOUTUBE_SHORTS -> Triple(Color(0xFFFF0000), Icons.Default.PlayArrow, Color.White)
        PlatformDestination.SNAPCHAT -> Triple(Color(0xFFFFFC00), Icons.Default.PhotoLibrary, Color(0xFF000000))
        PlatformDestination.WHATSAPP_STATUS -> Triple(Color(0xFF25D366), Icons.Default.GraphicEq, Color.White)
        PlatformDestination.CUSTOM -> Triple(Color(0xFF2563EB), Icons.Default.Tune, Color.White)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = platform.displayName,
            tint = tintColor,
            modifier = Modifier.size(size * 0.55f)
        )
    }
}
