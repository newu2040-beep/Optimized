package com.example.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object ShareHelper {

    fun shareVideo(context: Context, file: File) {
        if (!file.exists()) return
        val authority = "${context.packageName}.fileprovider"
        val contentUri: Uri = FileProvider.getUriForFile(context, authority, file)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "Exported Video")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Optimized Video"))
    }

    fun shareMultipleVideos(context: Context, files: List<File>) {
        val validFiles = files.filter { it.exists() }
        if (validFiles.isEmpty()) return

        val authority = "${context.packageName}.fileprovider"
        val uris = ArrayList<Uri>()
        for (f in validFiles) {
            uris.add(FileProvider.getUriForFile(context, authority, f))
        }

        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "video/mp4"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            putExtra(Intent.EXTRA_SUBJECT, "Optimized Social Videos")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share All Platform Videos"))
    }

    fun openVideo(context: Context, file: File) {
        if (!file.exists()) return
        val authority = "${context.packageName}.fileprovider"
        val contentUri: Uri = FileProvider.getUriForFile(context, authority, file)

        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "video/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            context.startActivity(viewIntent)
        } catch (_: Exception) {
            shareVideo(context, file)
        }
    }
}
