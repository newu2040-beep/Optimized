package com.example.domain.model

enum class QualityMode(
    val displayName: String,
    val description: String,
    val bitrateMultiplier: Float
) {
    MAXIMUM_QUALITY(
        displayName = "Maximum Quality",
        description = "Prioritize maximum practical visual fidelity and clarity",
        bitrateMultiplier = 1.35f
    ),
    BALANCED(
        displayName = "Balanced",
        description = "Optimal balance between visual quality and upload file size",
        bitrateMultiplier = 1.0f
    ),
    SMALLER_FILE(
        displayName = "Smaller File",
        description = "Reduce file size while maintaining clean, artifact-free playback",
        bitrateMultiplier = 0.65f
    ),
    CUSTOM(
        displayName = "Custom",
        description = "Fine-tune resolution, fps, bitrate, and audio parameters",
        bitrateMultiplier = 1.0f
    )
}

enum class CanvasMode(
    val displayName: String,
    val description: String
) {
    FIT(
        displayName = "Fit",
        description = "Preserve the entire video canvas (letterboxed if needed)"
    ),
    CROP(
        displayName = "Crop",
        description = "Fill the vertical 9:16 canvas cleanly without stretching"
    ),
    SMART_CROP(
        displayName = "Smart Crop",
        description = "Intelligently center crop on important focal elements"
    ),
    ORIGINAL(
        displayName = "Original",
        description = "Keep original framing without canvas padding or cropping"
    )
}

data class ExportSettings(
    val platform: PlatformDestination = PlatformDestination.TIKTOK,
    val qualityMode: QualityMode = QualityMode.BALANCED,
    val canvasMode: CanvasMode = CanvasMode.FIT,
    val targetWidth: Int = 1080,
    val targetHeight: Int = 1920,
    val lockAspectRatio: Boolean = true,
    val targetFps: Int = 30,
    val targetVideoCodec: String = "H.264",
    val targetBitrateKbps: Int = 12000,
    val targetAudioCodec: String = "AAC",
    val targetAudioBitrateKbps: Int = 192,
    val removeAudio: Boolean = false,
    val normalizeAudio: Boolean = true,
    val hdrToSdrConversion: Boolean = true,
    val preserveMetadata: Boolean = false,
    val showSafeAreas: Boolean = false,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = 0L
)

data class OptimizationPlan(
    val sourceMetadata: VideoMetadata,
    val exportSettings: ExportSettings,
    val requiresReEncode: Boolean,
    val isReadyToExportDirectly: Boolean,
    val requiresResolutionChange: Boolean,
    val requiresFpsChange: Boolean,
    val requiresCodecChange: Boolean,
    val requiresBitrateChange: Boolean,
    val requiresAudioTranscode: Boolean,
    val requiresHdrConversion: Boolean,
    val explanations: List<String>,
    val estimatedOutputSizeBytes: Long,
    val estimatedReductionPercent: Int
)

sealed interface ExportStage {
    object Idle : ExportStage
    data class Analyzing(val message: String = "Analyzing source media...") : ExportStage
    data class Preparing(val message: String = "Preparing encoding configuration...") : ExportStage
    data class Encoding(val progressPercent: Int, val message: String = "Optimizing video...") : ExportStage
    data class Validating(val message: String = "Validating exported file integrity...") : ExportStage
    data class Finalizing(val message: String = "Finalizing output container...") : ExportStage
    data class Completed(
        val outputFilePath: String,
        val outputSizeBytes: Long,
        val width: Int,
        val height: Int,
        val fps: Float,
        val codec: String,
        val durationMs: Long,
        val platform: PlatformDestination
    ) : ExportStage
    data class Failed(val humanReadableError: String, val actionableStep: String) : ExportStage
}
