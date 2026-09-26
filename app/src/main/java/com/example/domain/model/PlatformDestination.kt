package com.example.domain.model

enum class PlatformDestination(
    val id: String,
    val displayName: String,
    val shortDescription: String,
    val defaultWidth: Int,
    val defaultHeight: Int,
    val recommendedFps: Int,
    val recommendedVideoCodec: String,
    val defaultBitrateKbps: Int,
    val audioCodec: String,
    val audioBitrateKbps: Int,
    val maxDurationSeconds: Int?,
    val maxFileSizeBytes: Long?
) {
    TIKTOK(
        id = "tiktok",
        displayName = "TikTok",
        shortDescription = "Vertical social-video optimization (9:16)",
        defaultWidth = 1080,
        defaultHeight = 1920,
        recommendedFps = 30,
        recommendedVideoCodec = "H.264",
        defaultBitrateKbps = 12000,
        audioCodec = "AAC",
        audioBitrateKbps = 192,
        maxDurationSeconds = 600,
        maxFileSizeBytes = 287 * 1024 * 1024L
    ),
    INSTAGRAM_REELS(
        id = "instagram_reels",
        displayName = "Instagram Reels",
        shortDescription = "Vertical Reel optimization (9:16)",
        defaultWidth = 1080,
        defaultHeight = 1920,
        recommendedFps = 30,
        recommendedVideoCodec = "H.264",
        defaultBitrateKbps = 10000,
        audioCodec = "AAC",
        audioBitrateKbps = 128,
        maxDurationSeconds = 90,
        maxFileSizeBytes = 100 * 1024 * 1024L
    ),
    YOUTUBE_SHORTS(
        id = "youtube_shorts",
        displayName = "YouTube Shorts",
        shortDescription = "Short-form YouTube vertical optimization",
        defaultWidth = 1080,
        defaultHeight = 1920,
        recommendedFps = 60,
        recommendedVideoCodec = "H.264",
        defaultBitrateKbps = 15000,
        audioCodec = "AAC",
        audioBitrateKbps = 256,
        maxDurationSeconds = 60,
        maxFileSizeBytes = 500 * 1024 * 1024L
    ),
    SNAPCHAT(
        id = "snapchat",
        displayName = "Snapchat",
        shortDescription = "Vertical Snapchat Spotlight & Stories optimization",
        defaultWidth = 1080,
        defaultHeight = 1920,
        recommendedFps = 30,
        recommendedVideoCodec = "H.264",
        defaultBitrateKbps = 8000,
        audioCodec = "AAC",
        audioBitrateKbps = 128,
        maxDurationSeconds = 60,
        maxFileSizeBytes = 32 * 1024 * 1024L
    ),
    WHATSAPP_STATUS(
        id = "whatsapp_status",
        displayName = "WhatsApp Status",
        shortDescription = "Status-friendly lightweight video optimization",
        defaultWidth = 720,
        defaultHeight = 1280,
        recommendedFps = 30,
        recommendedVideoCodec = "H.264",
        defaultBitrateKbps = 3500,
        audioCodec = "AAC",
        audioBitrateKbps = 128,
        maxDurationSeconds = 30,
        maxFileSizeBytes = 16 * 1024 * 1024L
    ),
    CUSTOM(
        id = "custom",
        displayName = "Custom",
        shortDescription = "Full manual control over all export parameters",
        defaultWidth = 1080,
        defaultHeight = 1920,
        recommendedFps = 30,
        recommendedVideoCodec = "H.264",
        defaultBitrateKbps = 10000,
        audioCodec = "AAC",
        audioBitrateKbps = 160,
        maxDurationSeconds = null,
        maxFileSizeBytes = null
    );

    val aspectRatioLabel: String
        get() = "${defaultWidth}:${defaultHeight}"
}
