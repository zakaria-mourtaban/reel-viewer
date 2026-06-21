package com.zakaria.reelviewer.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.media3.common.C
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.cache.CacheDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

sealed class DownloadResult {
    data class Success(val path: String) : DownloadResult()
    data class Error(val message: String) : DownloadResult()
}

class DownloadManager(private val context: Context) {

    suspend fun download(
        videoUrl: String,
        title: String?,
        onProgress: (Float) -> Unit,
    ): DownloadResult = withContext(Dispatchers.IO) {
        try {
            val cache = VideoCache.get()
            val cacheDataSourceFactory = CacheDataSource.Factory()
                .setCache(cache)
                .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE)

            val dataSource = cacheDataSourceFactory.createDataSourceForDownloading()
            val dataSpec = DataSpec.Builder()
                .setUri(Uri.parse(videoUrl))
                .setPosition(0)
                .setLength(C.LENGTH_UNSET)
                .setKey(videoUrl)
                .build()

            dataSource.open(dataSpec)
            val totalBytes = if (dataSpec.length != C.LENGTH_UNSET) dataSpec.length else -1L
            val tempFile = File(context.cacheDir, "download_temp_${System.currentTimeMillis()}.mp4")
            val outputStream = FileOutputStream(tempFile)
            val buffer = ByteArray(64 * 1024)
            var bytesRead: Int
            var totalRead = 0L

            while (true) {
                bytesRead = dataSource.read(buffer, 0, buffer.size)
                if (bytesRead == C.RESULT_END_OF_INPUT) break
                outputStream.write(buffer, 0, bytesRead)
                totalRead += bytesRead
                if (totalBytes > 0) {
                    onProgress(totalRead.toFloat() / totalBytes.toFloat())
                }
            }
            outputStream.close()
            dataSource.close()

            val savedPath = saveToDownloads(tempFile, title ?: "video_${System.currentTimeMillis()}")
            tempFile.delete()
            onProgress(1f)

            if (savedPath != null) {
                DownloadResult.Success(savedPath)
            } else {
                DownloadResult.Error("Failed to save to Downloads")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download failed", e)
            DownloadResult.Error(e.message ?: "Download failed")
        }
    }

    private fun saveToDownloads(tempFile: File, title: String): String? {
        val sanitizedTitle = title.replace(Regex("[\\\\/:*?\"<>|]"), "_").take(80)
        val fileName = "${sanitizedTitle}.mp4"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveViaMediaStore(tempFile, fileName)
        } else {
            saveViaLegacyFile(tempFile, fileName)
        }
    }

    private fun saveViaMediaStore(tempFile: File, fileName: String): String? {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/Reel Viewer")
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null
        resolver.openOutputStream(uri)?.use { outputStream ->
            tempFile.inputStream().use { input ->
                input.copyTo(outputStream)
            }
        } ?: return null
        return uri.toString()
    }

    @Suppress("DEPRECATION")
    private fun saveViaLegacyFile(tempFile: File, fileName: String): String? {
        val downloadsDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "Reel Viewer"
        )
        if (!downloadsDir.exists()) downloadsDir.mkdirs()
        val destFile = File(downloadsDir, fileName)
        tempFile.inputStream().use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        return destFile.absolutePath
    }

    companion object {
        private const val TAG = "DownloadManager"
    }
}
