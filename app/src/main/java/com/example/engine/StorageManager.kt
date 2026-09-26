package com.example.engine

import android.content.Context
import android.os.Environment
import android.os.StatFs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
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
