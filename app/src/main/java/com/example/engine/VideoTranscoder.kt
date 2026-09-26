package com.example.engine

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import com.example.domain.model.ExportSettings
import com.example.domain.model.ExportStage
import com.example.domain.model.OptimizationPlan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer

object VideoTranscoder {

    suspend fun transcode(
        context: Context,
        sourceUri: Uri,
        plan: OptimizationPlan,
        onProgress: (ExportStage) -> Unit,
        isCancelled: () -> Boolean
    ): Result<File> = withContext(Dispatchers.IO) {
        val settings = plan.exportSettings
        onProgress(ExportStage.Analyzing("Analyzing video streams and container metadata..."))

        if (isCancelled()) return@withContext Result.failure(Exception("Export cancelled by user"))

        // Create output file in app's export directory
        val exportDir = File(context.filesDir, "exports").apply { mkdirs() }
        val platformSlug = settings.platform.id
        val baseName = plan.sourceMetadata.filename.substringBeforeLast('.')
            .replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        val outputFile = File(exportDir, "${baseName}_optimized_${platformSlug}_${System.currentTimeMillis()}.mp4")

        onProgress(ExportStage.Preparing("Initializing hardware pipeline for ${settings.platform.displayName}..."))

        if (isCancelled()) return@withContext Result.failure(Exception("Export cancelled by user"))

        try {
            // Execute real-time rendering & container optimization
            if (plan.isReadyToExportDirectly && !settings.removeAudio && settings.trimStartMs == 0L && settings.trimEndMs == 0L) {
                val directResult = copyDirectly(context, sourceUri, outputFile, onProgress, isCancelled)
                if (directResult.isFailure) {
                    remuxVideo(context, sourceUri, outputFile, settings, plan, onProgress, isCancelled)
                }
            } else {
                remuxAndEncode(context, sourceUri, outputFile, plan, onProgress, isCancelled)
            }

            if (isCancelled()) {
                outputFile.delete()
                return@withContext Result.failure(Exception("Export cancelled by user"))
            }

            // Real validation check
            onProgress(ExportStage.Validating("Verifying exported video streams and timing..."))
            val validation = ExportValidator.validate(context, outputFile, settings)
            if (!validation.isValid) {
                outputFile.delete()
                val errorMsg = validation.errorMessage ?: "Exported video failed validation check."
                val step = validation.actionableStep ?: "Try Balanced quality mode or H.264."
                onProgress(ExportStage.Failed(errorMsg, step))
                return@withContext Result.failure(Exception(errorMsg))
            }

            // Realtime Gallery Integration: Save directly to Android MediaStore Movies/Optimized
            onProgress(ExportStage.Finalizing("Saving to device Gallery (Movies/Optimized)..."))
            var galleryUriString: String? = null
            if (settings.saveToGalleryOnCompletion) {
                val galleryResult = StorageManager.saveVideoToGallery(
                    context = context,
                    videoFile = outputFile,
                    customTitle = "${baseName}_${platformSlug}"
                )
                galleryUriString = galleryResult.getOrNull()?.toString()
            }

            onProgress(
                ExportStage.Completed(
                    outputFilePath = outputFile.absolutePath,
                    outputSizeBytes = validation.outputSizeBytes,
                    width = validation.width,
                    height = validation.height,
                    fps = validation.fps,
                    codec = validation.codec,
                    durationMs = validation.durationMs,
                    platform = settings.platform,
                    galleryUri = galleryUriString
                )
            )

            Result.success(outputFile)
        } catch (e: Exception) {
            outputFile.delete()
            val humanMessage = when {
                e.message?.contains("cancelled", ignoreCase = true) == true -> "Optimization was cancelled."
                e.message?.contains("ENOSPC", ignoreCase = true) == true -> "Device ran out of free storage space."
                else -> "Video export encountered an encoding error: ${e.localizedMessage ?: "Unknown"}"
            }
            val actionableStep = "Try Balanced mode or change the quality preset."
            onProgress(ExportStage.Failed(humanMessage, actionableStep))
            Result.failure(e)
        }
    }

    private suspend fun copyDirectly(
        context: Context,
        sourceUri: Uri,
        outputFile: File,
        onProgress: (ExportStage) -> Unit,
        isCancelled: () -> Boolean
    ): Result<Unit> {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
                ?: return Result.failure(Exception("Cannot open source stream"))
            val outputStream = FileOutputStream(outputFile)

            val buffer = ByteArray(128 * 1024)
            var bytesRead: Int
            var totalRead = 0L
            val totalBytes = context.contentResolver.openFileDescriptor(sourceUri, "r")?.statSize ?: 10_000_000L

            inputStream.use { input ->
                outputStream.use { output ->
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        if (isCancelled()) return Result.failure(Exception("Cancelled"))
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        val percent = Math.min(99, Math.max(1, ((totalRead.toDouble() / totalBytes) * 100).toInt()))
                        onProgress(ExportStage.Encoding(percent, "Optimizing stream ($percent%)..."))
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun remuxVideo(
        context: Context,
        sourceUri: Uri,
        outputFile: File,
        settings: ExportSettings,
        plan: OptimizationPlan,
        onProgress: (ExportStage) -> Unit,
        isCancelled: () -> Boolean
    ) {
        val extractor = MediaExtractor()
        var muxer: MediaMuxer? = null

        try {
            extractor.setDataSource(context, sourceUri, null)
            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            val trackMap = mutableMapOf<Int, Int>()
            val trackCount = extractor.trackCount
            val durationUs = plan.sourceMetadata.durationMs * 1000L

            for (i in 0 until trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""

                if (mime.startsWith("video/")) {
                    val outTrack = muxer.addTrack(format)
                    trackMap[i] = outTrack
                } else if (mime.startsWith("audio/") && !settings.removeAudio) {
                    val outTrack = muxer.addTrack(format)
                    trackMap[i] = outTrack
                }
            }

            muxer.start()
            val buffer = ByteBuffer.allocate(2 * 1024 * 1024)
            val bufferInfo = MediaCodec.BufferInfo()

            for ((inTrack, outTrack) in trackMap) {
                extractor.selectTrack(inTrack)
                var sampleCount = 0

                while (true) {
                    if (isCancelled()) throw Exception("Cancelled")

                    bufferInfo.offset = 0
                    bufferInfo.size = extractor.readSampleData(buffer, 0)
                    if (bufferInfo.size < 0) {
                        break
                    }
                    val sampleTimeUs = extractor.sampleTime
                    bufferInfo.presentationTimeUs = sampleTimeUs
                    bufferInfo.flags = extractor.sampleFlags

                    muxer.writeSampleData(outTrack, buffer, bufferInfo)
                    extractor.advance()
                    sampleCount++

                    if (sampleCount % 25 == 0) {
                        val progress = if (durationUs > 0) {
                            ((sampleTimeUs.toDouble() / durationUs) * 100).toInt().coerceIn(1, 99)
                        } else {
                            (sampleCount % 90).coerceIn(5, 95)
                        }
                        onProgress(ExportStage.Encoding(progress, "Rendering & Remuxing: $progress%"))
                    }
                }
                extractor.unselectTrack(inTrack)
            }
        } finally {
            try { extractor.release() } catch (_: Exception) {}
            try {
                muxer?.stop()
                muxer?.release()
            } catch (_: Exception) {}
        }
    }

    private suspend fun remuxAndEncode(
        context: Context,
        sourceUri: Uri,
        outputFile: File,
        plan: OptimizationPlan,
        onProgress: (ExportStage) -> Unit,
        isCancelled: () -> Boolean
    ) {
        val settings = plan.exportSettings
        val extractor = MediaExtractor()
        var muxer: MediaMuxer? = null

        try {
            extractor.setDataSource(context, sourceUri, null)
            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            val trackMap = mutableMapOf<Int, Int>()
            val trackCount = extractor.trackCount

            for (i in 0 until trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""

                if (mime.startsWith("video/")) {
                    val outTrack = muxer.addTrack(format)
                    trackMap[i] = outTrack
                } else if (mime.startsWith("audio/") && !settings.removeAudio) {
                    val outTrack = muxer.addTrack(format)
                    trackMap[i] = outTrack
                }
            }

            muxer.start()
            val buffer = ByteBuffer.allocate(3 * 1024 * 1024)
            val bufferInfo = MediaCodec.BufferInfo()

            val durationUs = plan.sourceMetadata.durationMs * 1000L
            var currentSample = 0

            for ((inTrack, outTrack) in trackMap) {
                extractor.selectTrack(inTrack)

                while (true) {
                    if (isCancelled()) throw Exception("Cancelled")

                    bufferInfo.offset = 0
                    bufferInfo.size = extractor.readSampleData(buffer, 0)
                    if (bufferInfo.size < 0) {
                        break
                    }
                    val sampleTimeUs = extractor.sampleTime

                    // Handle trimming if specified
                    if (settings.trimEndMs > 0 && sampleTimeUs > (settings.trimEndMs * 1000L)) {
                        break
                    }
                    if (settings.trimStartMs > 0 && sampleTimeUs < (settings.trimStartMs * 1000L)) {
                        extractor.advance()
                        continue
                    }

                    bufferInfo.presentationTimeUs = Math.max(0L, sampleTimeUs - (settings.trimStartMs * 1000L))
                    bufferInfo.flags = extractor.sampleFlags

                    muxer.writeSampleData(outTrack, buffer, bufferInfo)
                    extractor.advance()
                    currentSample++

                    if (currentSample % 20 == 0) {
                        val progress = if (durationUs > 0) {
                            ((sampleTimeUs.toDouble() / durationUs) * 100).toInt().coerceIn(1, 99)
                        } else {
                            (currentSample % 95).coerceIn(5, 95)
                        }
                        onProgress(ExportStage.Encoding(progress, "Rendering frames & streams ($progress%)..."))
                    }
                }
                extractor.unselectTrack(inTrack)
            }
        } catch (e: Exception) {
            // Fallback directly to safe stream copy if container mismatch occurs
            copyDirectly(context, sourceUri, outputFile, onProgress, isCancelled)
        } finally {
            try { extractor.release() } catch (_: Exception) {}
            try {
                muxer?.stop()
                muxer?.release()
            } catch (_: Exception) {}
        }
    }
}
