package com.example.engine

import com.example.domain.model.CanvasMode
import com.example.domain.model.ExportSettings
import com.example.domain.model.OptimizationPlan
import com.example.domain.model.VideoMetadata

object SmartOptimizationEngine {

    fun generatePlan(
        source: VideoMetadata,
        settings: ExportSettings
    ): OptimizationPlan {
        val explanations = mutableListOf<String>()

        val srcW = source.displayWidth
        val srcH = source.displayHeight
        val targetW = settings.targetWidth
        val targetH = settings.targetHeight

        val resolutionMatches = (srcW == targetW && srcH == targetH)
        val rotationNeedsFix = source.rotationDegrees != 0
        val requiresResolutionChange = !resolutionMatches || rotationNeedsFix || settings.canvasMode == CanvasMode.CROP

        if (!requiresResolutionChange) {
            explanations.add("Resolution already matches $targetW × $targetH (no scaling needed)")
        } else {
            explanations.add("Resolution adjustment: $srcW × $srcH → $targetW × $targetH (${settings.canvasMode.displayName})")
        }

        val srcFpsRounded = Math.round(source.fps)
        val requiresFpsChange = srcFpsRounded > settings.targetFps
        if (requiresFpsChange) {
            explanations.add("FPS optimization: ${source.fps.toInt()} FPS → ${settings.targetFps} FPS for ${settings.platform.displayName}")
        } else {
            explanations.add("FPS preserved at ${source.fps.toInt()} FPS")
        }

        val srcCodecIsH264 = source.videoCodec.contains("H.264", ignoreCase = true) || source.videoCodec.contains("AVC", ignoreCase = true)
        val targetCodecIsH264 = settings.targetVideoCodec.contains("H.264", ignoreCase = true)
        val requiresCodecChange = (srcCodecIsH264 != targetCodecIsH264) || (!srcCodecIsH264 && targetCodecIsH264)

        if (requiresCodecChange) {
            explanations.add("Codec conversion: ${source.videoCodec} → ${settings.targetVideoCodec} for maximum social platform compatibility")
        } else {
            explanations.add("Video codec compatible (${source.videoCodec})")
        }

        val srcBitrateKbps = source.videoBitrateBps / 1000
        val bitrateDiffPercent = if (srcBitrateKbps > 0) {
            Math.abs(srcBitrateKbps - settings.targetBitrateKbps).toFloat() / srcBitrateKbps.toFloat()
        } else 1.0f
        val requiresBitrateChange = bitrateDiffPercent > 0.20f // If more than 20% different

        if (requiresBitrateChange) {
            explanations.add("Bitrate optimization: ${source.formattedBitrate} → ${settings.targetBitrateKbps / 1000.0} Mbps")
        } else {
            explanations.add("Bitrate optimal (${source.formattedBitrate})")
        }

        val requiresHdrConversion = source.isHdr && settings.hdrToSdrConversion
        if (requiresHdrConversion) {
            explanations.add("HDR → SDR tone mapping to avoid washed-out highlights on social feeds")
        }

        val audioIsAac = source.audioCodec?.contains("AAC", ignoreCase = true) == true
        val requiresAudioTranscode = settings.removeAudio || (!audioIsAac && !settings.removeAudio) || settings.normalizeAudio
        if (settings.removeAudio) {
            explanations.add("Audio track will be stripped")
        } else if (requiresAudioTranscode) {
            explanations.add("Audio re-encoded to ${settings.targetAudioCodec} ${settings.targetAudioBitrateKbps} kbps with normalization")
        } else {
            explanations.add("Audio stream preserved (${source.audioCodec ?: "AAC"})")
        }

        val hasTrimming = settings.trimEndMs > 0 && settings.trimEndMs > settings.trimStartMs

        // Direct export eligibility:
        val isReadyToExportDirectly = !requiresResolutionChange &&
                !requiresFpsChange &&
                !requiresCodecChange &&
                !requiresBitrateChange &&
                !requiresHdrConversion &&
                !requiresAudioTranscode &&
                !hasTrimming &&
                source.container.equals("MP4", ignoreCase = true)

        val requiresReEncode = !isReadyToExportDirectly

        // Estimate output file size
        val effectiveDurationSec = if (hasTrimming) {
            (settings.trimEndMs - settings.trimStartMs) / 1000.0
        } else {
            Math.max(1.0, source.durationMs / 1000.0)
        }

        val totalBitrateKbps = if (settings.removeAudio) {
            settings.targetBitrateKbps
        } else {
            settings.targetBitrateKbps + settings.targetAudioBitrateKbps
        }

        val estimatedOutputSizeBytes = if (isReadyToExportDirectly) {
            source.sizeBytes
        } else {
            ((totalBitrateKbps * 1000L / 8.0) * effectiveDurationSec).toLong()
        }

        val reduction = if (source.sizeBytes > 0 && estimatedOutputSizeBytes < source.sizeBytes) {
            (((source.sizeBytes - estimatedOutputSizeBytes).toDouble() / source.sizeBytes.toDouble()) * 100).toInt()
        } else {
            0
        }

        return OptimizationPlan(
            sourceMetadata = source,
            exportSettings = settings,
            requiresReEncode = requiresReEncode,
            isReadyToExportDirectly = isReadyToExportDirectly,
            requiresResolutionChange = requiresResolutionChange,
            requiresFpsChange = requiresFpsChange,
            requiresCodecChange = requiresCodecChange,
            requiresBitrateChange = requiresBitrateChange,
            requiresAudioTranscode = requiresAudioTranscode,
            requiresHdrConversion = requiresHdrConversion,
            explanations = explanations,
            estimatedOutputSizeBytes = estimatedOutputSizeBytes,
            estimatedReductionPercent = reduction
        )
    }
}
