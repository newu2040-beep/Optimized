package com.example.engine

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Locale

data class StorageStats(
    val freeDeviceBytes: Long,
    val totalDeviceBytes: Long,
    val tempStorageBytes: Long,
    val exportCacheBytes: Long,
    val savedProjectCount: Int
) {
    val formattedFreeStorage: String
        get() = formatSize(freeDeviceBytes)

    val formattedTempStorage: String
        get() = formatSize(tempStorageBytes)

    val formattedExportCache: String
        get() = formatSize(exportCacheBytes)

    companion object {
        fun formatSize(bytes: Long): String {
            val mb = bytes / (1024.0 * 1024.0)
            return if (mb >= 1024.0) {
                String.format(Locale.US, "%.2f GB", mb / 1024.0)
            } else {
                String.format(Locale.US, "%.1f MB", mb)
            }
        }
    }
}

object StorageManager {

    suspend fun saveVideoToGallery(
        context: Context,
        videoFile: File,
        customTitle: String? = null
    ): Result<Uri> = withContext(Dispatchers.IO) {
        if (!videoFile.exists() || videoFile.length() == 0L) {
            return@withContext Result.failure(Exception("Optimized video file is missing or empty."))
        }

        try {
            val resolver = context.contentResolver
            val displayName = if (customTitle.isNullOrBlank()) {
                videoFile.name
            } else {
                val clean = customTitle.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
                if (clean.endsWith(".mp4", ignoreCase = true)) clean else "$clean.mp4"
            }

            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                put(MediaStore.Video.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/Optimized")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
            }

            val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            val itemUri = resolver.insert(collectionUri, values)
                ?: return@withContext Result.failure(Exception("Failed to register media in device gallery."))

            resolver.openOutputStream(itemUri)?.use { outStream ->
                FileInputStream(videoFile).use { inStream ->
                    inStream.copyTo(outStream)
                }
            } ?: return@withContext Result.failure(Exception("Cannot open write stream for device gallery."))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Video.Media.IS_PENDING, 0)
                resolver.update(itemUri, values, null, null)
            }

            // Immediately trigger MediaScanner so all Gallery, Google Photos, and Files apps index the video
            MediaScannerConnection.scanFile(
                context,
                arrayOf(videoFile.absolutePath),
                arrayOf("video/mp4")
            ) { _, _ -> }

            Result.success(itemUri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveVideoToCustomUri(
        context: Context,
        videoFile: File,
        targetUri: Uri
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (!videoFile.exists() || videoFile.length() == 0L) {
            return@withContext Result.failure(Exception("Source video file is missing or empty."))
        }
        try {
            val resolver = context.contentResolver
            resolver.openOutputStream(targetUri)?.use { outStream ->
                FileInputStream(videoFile).use { inStream ->
                    inStream.copyTo(outStream)
                }
            } ?: return@withContext Result.failure(Exception("Could not open destination stream."))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStorageStats(context: Context, projectCount: Int): StorageStats = withContext(Dispatchers.IO) {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong
        val totalBlocks = stat.blockCountLong

        val freeDeviceBytes = availableBlocks * blockSize
        val totalDeviceBytes = totalBlocks * blockSize

        val tempDir = File(context.cacheDir, "temp_exports")
        val tempBytes = getDirectorySize(tempDir)

        val cacheBytes = getDirectorySize(context.cacheDir) - tempBytes

        StorageStats(
            freeDeviceBytes = freeDeviceBytes,
            totalDeviceBytes = totalDeviceBytes,
            tempStorageBytes = Math.max(0L, tempBytes),
            exportCacheBytes = Math.max(0L, cacheBytes),
            savedProjectCount = projectCount
        )
    }

    suspend fun clearTemporaryFiles(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val tempDir = File(context.cacheDir, "temp_exports")
            if (tempDir.exists()) {
                tempDir.listFiles()?.forEach { it.delete() }
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun clearCache(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            context.cacheDir.listFiles()?.forEach { file ->
                if (file.isDirectory) file.deleteRecursively() else file.delete()
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun getDirectorySize(dir: File): Long {
        if (!dir.exists()) return 0L
        var size = 0L
        dir.listFiles()?.forEach { file ->
            size += if (file.isDirectory) getDirectorySize(file) else file.length()
        }
        return size
    }
}
