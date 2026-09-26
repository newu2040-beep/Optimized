package com.example.engine

import com.example.domain.model.CanvasMode
import com.example.domain.model.ExportSettings
import com.example.domain.model.PlatformDestination
import com.example.domain.model.QualityMode
import com.example.domain.model.VideoMetadata

interface PlatformProfile {
    val platform: PlatformDestination
    val targetWidth: Int
    val targetHeight: Int
    val maxFps: Int
    val targetVideoCodec: String
    val baseBitrateKbps: Int
    val targetAudioCodec: String
    val targetAudioBitrateKbps: Int
    val maxDurationSeconds: Int?
    val requiresSdrConversion: Boolean

    fun calculateBitrate(qualityMode: QualityMode): Int {
        return (baseBitrateKbps * qualityMode.bitrateMultiplier).toInt()
    }
}

class TikTokProfile : PlatformProfile {
    override val platform = PlatformDestination.TIKTOK
    override val targetWidth = 1080
    override val targetHeight = 1920
    override val maxFps = 60
    override val targetVideoCodec = "H.264"
    override val baseBitrateKbps = 12000
    override val targetAudioCodec = "AAC"
    override val targetAudioBitrateKbps = 192
    override val maxDurationSeconds = 600
    override val requiresSdrConversion = true
}

class InstagramReelsProfile : PlatformProfile {
    override val platform = PlatformDestination.INSTAGRAM_REELS
    override val targetWidth = 1080
    override val targetHeight = 1920
    override val maxFps = 30
    override val targetVideoCodec = "H.264"
    override val baseBitrateKbps = 10000
    override val targetAudioCodec = "AAC"
    override val targetAudioBitrateKbps = 128
    override val maxDurationSeconds = 90
    override val requiresSdrConversion = true
}

class YouTubeShortsProfile : PlatformProfile {
    override val platform = PlatformDestination.YOUTUBE_SHORTS
    override val targetWidth = 1080
    override val targetHeight = 1920
    override val maxFps = 60
    override val targetVideoCodec = "H.264"
    override val baseBitrateKbps = 15000
    override val targetAudioCodec = "AAC"
    override val targetAudioBitrateKbps = 256
    override val maxDurationSeconds = 60
    override val requiresSdrConversion = false // YouTube handles HDR well, but converts if user requests
}

class SnapchatProfile : PlatformProfile {
    override val platform = PlatformDestination.SNAPCHAT
    override val targetWidth = 1080
    override val targetHeight = 1920
    override val maxFps = 30
    override val targetVideoCodec = "H.264"
    override val baseBitrateKbps = 8000
    override val targetAudioCodec = "AAC"
    override val targetAudioBitrateKbps = 128
    override val maxDurationSeconds = 60
    override val requiresSdrConversion = true
}

class WhatsAppStatusProfile : PlatformProfile {
    override val platform = PlatformDestination.WHATSAPP_STATUS
    override val targetWidth = 720
    override val targetHeight = 1280
    override val maxFps = 30
    override val targetVideoCodec = "H.264"
    override val baseBitrateKbps = 3500
    override val targetAudioCodec = "AAC"
    override val targetAudioBitrateKbps = 128
    override val maxDurationSeconds = 30
    override val requiresSdrConversion = true
}

class CustomProfile(
    override val targetWidth: Int = 1080,
    override val targetHeight: Int = 1920,
    override val maxFps: Int = 30,
    override val targetVideoCodec: String = "H.264",
    override val baseBitrateKbps: Int = 10000,
    override val targetAudioCodec: String = "AAC",
    override val targetAudioBitrateKbps: Int = 160
) : PlatformProfile {
    override val platform = PlatformDestination.CUSTOM
    override val maxDurationSeconds: Int? = null
    override val requiresSdrConversion = true
}

object PlatformProfileEngine {
    private val profiles: Map<PlatformDestination, PlatformProfile> = mapOf(
        PlatformDestination.TIKTOK to TikTokProfile(),
        PlatformDestination.INSTAGRAM_REELS to InstagramReelsProfile(),
        PlatformDestination.YOUTUBE_SHORTS to YouTubeShortsProfile(),
        PlatformDestination.SNAPCHAT to SnapchatProfile(),
        PlatformDestination.WHATSAPP_STATUS to WhatsAppStatusProfile(),
        PlatformDestination.CUSTOM to CustomProfile()
    )

    fun getProfile(platform: PlatformDestination): PlatformProfile {
        return profiles[platform] ?: TikTokProfile()
    }

    fun createInitialSettings(metadata: VideoMetadata, platform: PlatformDestination): ExportSettings {
        val profile = getProfile(platform)
        val targetFps = if (metadata.fps > 0 && metadata.fps <= profile.maxFps) {
            Math.round(metadata.fps)
        } else {
            profile.maxFps
        }

        return ExportSettings(
            platform = platform,
            qualityMode = QualityMode.BALANCED,
            canvasMode = CanvasMode.FIT,
            targetWidth = profile.targetWidth,
            targetHeight = profile.targetHeight,
            lockAspectRatio = true,
            targetFps = targetFps,
            targetVideoCodec = profile.targetVideoCodec,
            targetBitrateKbps = profile.calculateBitrate(QualityMode.BALANCED),
            targetAudioCodec = profile.targetAudioCodec,
            targetAudioBitrateKbps = profile.targetAudioBitrateKbps,
            hdrToSdrConversion = profile.requiresSdrConversion && metadata.isHdr
        )
    }
}
