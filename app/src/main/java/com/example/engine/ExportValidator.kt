package com.example.engine

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.example.domain.model.ExportSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val actionableStep: String? = null,
    val outputSizeBytes: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val fps: Float = 0f,
    val durationMs: Long = 0L,
    val codec: String = ""
)

object ExportValidator {

    suspend fun validate(
        context: Context,
        outputFile: File,
        settings: ExportSettings
    ): ValidationResult = withContext(Dispatchers.IO) {
        if (!outputFile.exists() || outputFile.length() == 0L) {
            return@withContext ValidationResult(
                isValid = false,
                errorMessage = "Export file could not be written to storage.",
                actionableStep = "Check device free storage and try again."
            )
        }

        val retriever = MediaMetadataRetriever()
        val extractor = MediaExtractor()
        try {
            retriever.setDataSource(context, Uri.fromFile(outputFile))
            extractor.setDataSource(outputFile.absolutePath)

            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationStr?.toLongOrNull() ?: 0L
            if (durationMs <= 0L) {
                return@withContext ValidationResult(
                    isValid = false,
                    errorMessage = "Exported file has an invalid or zero duration header.",
                    actionableStep = "Try selecting Balanced mode or H.264 codec."
                )
            }

            var hasVideoTrack = false
            var hasAudioTrack = false
            var outWidth = 0
            var outHeight = 0
            var outFps = settings.targetFps.toFloat()
            var outCodec = settings.targetVideoCodec

            val trackCount = extractor.trackCount
            for (i in 0 until trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("video/")) {
                    hasVideoTrack = true
                    if (format.containsKey(MediaFormat.KEY_WIDTH)) {
                        outWidth = format.getInteger(MediaFormat.KEY_WIDTH)
                    }
                    if (format.containsKey(MediaFormat.KEY_HEIGHT)) {
                        outHeight = format.getInteger(MediaFormat.KEY_HEIGHT)
                    }
                    if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                        outFps = format.getInteger(MediaFormat.KEY_FRAME_RATE).toFloat()
                    }
                    outCodec = if (mime.contains("hevc", true)) "H.265 (HEVC)" else "H.264 (AVC)"
                } else if (mime.startsWith("audio/")) {
                    hasAudioTrack = true
                }
            }

            if (!hasVideoTrack) {
                return@withContext ValidationResult(
                    isValid = false,
                    errorMessage = "Video stream track is missing in the exported file.",
                    actionableStep = "Retry with hardware acceleration toggled in Settings."
                )
            }

            if (!settings.removeAudio && !hasAudioTrack) {
                // Warning only if source had audio, but file is still usable
            }

            if (outWidth == 0 || outHeight == 0) {
                outWidth = settings.targetWidth
                outHeight = settings.targetHeight
            }

            ValidationResult(
                isValid = true,
                outputSizeBytes = outputFile.length(),
                width = outWidth,
                height = outHeight,
                fps = outFps,
                durationMs = durationMs,
                codec = outCodec
            )
        } catch (e: Exception) {
            ValidationResult(
                isValid = false,
                errorMessage = "The exported video could not be read or is corrupted.",
                actionableStep = "Try exporting with standard H.264 settings."
            )
        } finally {
            try { retriever.release() } catch (_: Exception) {}
            try { extractor.release() } catch (_: Exception) {}
        }
    }
}
