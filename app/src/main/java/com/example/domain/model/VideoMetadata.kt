package com.example.domain.model

import java.util.Locale

data class VideoMetadata(
    val filename: String,
    val uriString: String,
    val sizeBytes: Long,
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val fps: Float,
    val videoCodec: String,
    val videoBitrateBps: Long,
    val isHdr: Boolean,
    val colorSpace: String,
    val rotationDegrees: Int,
    val container: String,
    val audioCodec: String?,
    val audioBitrateBps: Long?,
    val audioSampleRate: Int?,
    val audioChannels: Int?,
    val thumbnailPath: String? = null
) {
    val displayWidth: Int
        get() = if (rotationDegrees == 90 || rotationDegrees == 270) height else width

    val displayHeight: Int
        get() = if (rotationDegrees == 90 || rotationDegrees == 270) width else height

    val resolutionLabel: String
        get() = "$displayWidth × $displayHeight"

    val aspectRatioLabel: String
        get() {
            if (displayHeight == 0) return "Unknown"
            val ratio = displayWidth.toFloat() / displayHeight.toFloat()
            return when {
                Math.abs(ratio - 9f / 16f) < 0.05f -> "9:16 (Vertical)"
                Math.abs(ratio - 16f / 9f) < 0.05f -> "16:9 (Landscape)"
                Math.abs(ratio - 1f) < 0.05f -> "1:1 (Square)"
                Math.abs(ratio - 4f / 5f) < 0.05f -> "4:5 (Portrait)"
                else -> String.format(Locale.US, "%.2f:1", ratio)
            }
        }

    val formattedDuration: String
        get() {
            val totalSeconds = (durationMs / 1000).toInt()
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format(Locale.US, "%d:%02d", minutes, seconds)
        }

    val formattedSize: String
        get() {
            val mb = sizeBytes / (1024.0 * 1024.0)
            return if (mb >= 1024) {
                String.format(Locale.US, "%.2f GB", mb / 1024.0)
            } else {
                String.format(Locale.US, "%.1f MB", mb)
            }
        }

    val formattedBitrate: String
        get() {
            val kbps = videoBitrateBps / 1000
            return if (kbps >= 1000) {
                String.format(Locale.US, "%.1f Mbps", kbps / 1000.0)
            } else {
                "$kbps kbps"
            }
        }

    val qualityStatus: String
        get() {
            val pixels = displayWidth * displayHeight
            return when {
                pixels >= 1080 * 1920 && fps >= 29.9f && videoBitrateBps >= 8_000_000 -> "Excellent"
                pixels >= 720 * 1280 && videoBitrateBps >= 4_000_000 -> "Good"
                pixels < 720 * 1280 || videoBitrateBps < 2_500_000 -> "Needs optimization"
                videoCodec.contains("HEVC", ignoreCase = true) || isHdr -> "High fidelity (check compatibility)"
                else -> "Standard"
            }
        }
}
