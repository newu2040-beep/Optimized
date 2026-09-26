package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.example.domain.model.VideoMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object VideoAnalyzer {

    suspend fun analyze(context: Context, videoUri: Uri): Result<VideoMetadata> =
        withContext(Dispatchers.IO) {
            val retriever = MediaMetadataRetriever()
            val extractor = MediaExtractor()
            try {
                retriever.setDataSource(context, videoUri)
                try {
                    extractor.setDataSource(context, videoUri, null)
                } catch (_: Exception) {
                    // Extractor may fail on some content URIs without direct FD; handled gracefully
                }

                // Extract query filename and size
                var filename = "video_${System.currentTimeMillis()}.mp4"
                var sizeBytes = 0L
                context.contentResolver.query(videoUri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex != -1) filename = cursor.getString(nameIndex) ?: filename
                        if (sizeIndex != -1) sizeBytes = cursor.getLong(sizeIndex)
                    }
                }

                if (sizeBytes <= 0L) {
                    try {
                        context.contentResolver.openFileDescriptor(videoUri, "r")?.use { pfd ->
                            sizeBytes = pfd.statSize
                        }
                    } catch (_: Exception) {
                        sizeBytes = 10 * 1024 * 1024L // Reasonable fallback
                    }
                }

                val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val durationMs = durationStr?.toLongOrNull() ?: 0L

                val widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                val heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                var width = widthStr?.toIntOrNull() ?: 1080
                var height = heightStr?.toIntOrNull() ?: 1920

                val rotationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                val rotation = rotationStr?.toIntOrNull() ?: 0

                val bitrateStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                var bitrateBps = bitrateStr?.toLongOrNull() ?: 0L
                if (bitrateBps <= 0L && durationMs > 0) {
                    bitrateBps = (sizeBytes * 8 * 1000) / durationMs
                }

                var videoCodec = "H.264 (AVC)"
                var fps = 30.0f
                var isHdr = false
                var colorSpace = "BT.709 (SDR)"

                var audioCodec: String? = null
                var audioBitrate: Long? = null
                var audioSampleRate: Int? = null
                var audioChannels: Int? = null

                // Inspect tracks via MediaExtractor
                val numTracks = extractor.trackCount
                for (i in 0 until numTracks) {
                    val format = extractor.getTrackFormat(i)
                    val mime = format.getString(MediaFormat.KEY_MIME) ?: ""

                    if (mime.startsWith("video/")) {
                        videoCodec = when {
                            mime.contains("hevc", ignoreCase = true) -> "H.265 (HEVC)"
                            mime.contains("av01", ignoreCase = true) || mime.contains("av1", ignoreCase = true) -> "AV1"
                            mime.contains("vp9", ignoreCase = true) -> "VP9"
                            mime.contains("avc", ignoreCase = true) -> "H.264 (AVC)"
                            else -> mime.substringAfter("video/").uppercase()
                        }
                        if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                            fps = format.getInteger(MediaFormat.KEY_FRAME_RATE).toFloat()
                        }
                        if (format.containsKey(MediaFormat.KEY_COLOR_STANDARD)) {
                            val colorStd = format.getInteger(MediaFormat.KEY_COLOR_STANDARD)
                            if (colorStd == MediaFormat.COLOR_STANDARD_BT2020) {
                                isHdr = true
                                colorSpace = "BT.2020 (HDR)"
                            }
                        }
                        if (format.containsKey(MediaFormat.KEY_COLOR_TRANSFER)) {
                            val transfer = format.getInteger(MediaFormat.KEY_COLOR_TRANSFER)
                            if (transfer == MediaFormat.COLOR_TRANSFER_ST2084 || transfer == MediaFormat.COLOR_TRANSFER_HLG) {
                                isHdr = true
                            }
                        }
                    } else if (mime.startsWith("audio/")) {
                        audioCodec = when {
                            mime.contains("aac", ignoreCase = true) -> "AAC"
                            mime.contains("opus", ignoreCase = true) -> "Opus"
                            mime.contains("mp4a", ignoreCase = true) -> "AAC"
                            mime.contains("raw", ignoreCase = true) -> "PCM"
                            else -> mime.substringAfter("audio/").uppercase()
                        }
                        if (format.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                            audioSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                        }
                        if (format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                            audioChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                        }
                        if (format.containsKey(MediaFormat.KEY_BIT_RATE)) {
                            audioBitrate = format.getInteger(MediaFormat.KEY_BIT_RATE).toLong()
                        }
                    }
                }

                // Generate and cache thumbnail frame
                var thumbnailPath: String? = null
                try {
                    val frameBitmap = retriever.getFrameAtTime(
                        Math.min(1_000_000L, durationMs * 500L),
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                    ) ?: retriever.frameAtTime

                    if (frameBitmap != null) {
                        val thumbDir = File(context.cacheDir, "thumbnails").apply { mkdirs() }
                        val thumbFile = File(thumbDir, "thumb_${System.currentTimeMillis()}.jpg")
                        FileOutputStream(thumbFile).use { out ->
                            frameBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                        }
                        thumbnailPath = thumbFile.absolutePath
                    }
                } catch (_: Exception) {
                    // Thumbnail creation fallback
                }

                val ext = filename.substringAfterLast('.', "mp4").uppercase()

                val metadata = VideoMetadata(
                    filename = filename,
                    uriString = videoUri.toString(),
                    sizeBytes = sizeBytes,
                    durationMs = durationMs,
                    width = width,
                    height = height,
                    fps = if (fps > 0f) fps else 30.0f,
                    videoCodec = videoCodec,
                    videoBitrateBps = bitrateBps,
                    isHdr = isHdr,
                    colorSpace = colorSpace,
                    rotationDegrees = rotation,
                    container = ext,
                    audioCodec = audioCodec ?: "AAC",
                    audioBitrateBps = audioBitrate ?: 128_000L,
                    audioSampleRate = audioSampleRate ?: 44100,
                    audioChannels = audioChannels ?: 2,
                    thumbnailPath = thumbnailPath
                )
                Result.success(metadata)
            } catch (e: Exception) {
                Result.failure(e)
            } finally {
                try { retriever.release() } catch (_: Exception) {}
                try { extractor.release() } catch (_: Exception) {}
            }
        }
}
