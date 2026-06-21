package com.zakaria.reelviewer.data

import android.content.Context
import android.util.Log
import com.yausername.youtubedl_android.UpdateChannel
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

sealed class ReelResult {
    data class Success(val url: String, val title: String?) : ReelResult()
    data class Error(val message: String) : ReelResult()
}

class ReelRepository(private val context: Context) {

    suspend fun getReelStreamUrl(reelUrl: String): ReelResult = withContext(Dispatchers.IO) {
        try {
            val request = YoutubeDLRequest(reelUrl).apply {
                addOption("-f", "best")
            }
            val info = YoutubeDL.getInstance().getInfo(request)
            val url = info.getUrl()
            if (url.isNullOrBlank()) {
                ReelResult.Error("Could not extract video URL from this reel")
            } else {
                ReelResult.Success(url, info.getTitle())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract reel", e)
            ReelResult.Error(e.message ?: "Failed to load reel")
        }
    }

    suspend fun updateYtDlp(): Boolean = withContext(Dispatchers.IO) {
        try {
            YoutubeDL.getInstance().updateYoutubeDL(context, UpdateChannel.NIGHTLY)
            Log.i(TAG, "yt-dlp update completed")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update yt-dlp", e)
            false
        }
    }

    suspend fun maybeBackgroundUpdate() = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastCheck = prefs.getLong(KEY_LAST_UPDATE_CHECK, 0)
        val now = System.currentTimeMillis()
        if (now - lastCheck < UPDATE_CHECK_INTERVAL_MS) {
            return@withContext
        }
        prefs.edit().putLong(KEY_LAST_UPDATE_CHECK, now).apply()
        try {
            YoutubeDL.getInstance().updateYoutubeDL(context, UpdateChannel.NIGHTLY)
            Log.i(TAG, "yt-dlp background update check completed")
        } catch (e: Exception) {
            Log.e(TAG, "Background update failed", e)
        }
    }

    companion object {
        private const val TAG = "ReelRepository"
        private const val PREFS_NAME = "reel_viewer_prefs"
        private const val KEY_LAST_UPDATE_CHECK = "last_update_check_ms"
        private val UPDATE_CHECK_INTERVAL_MS = TimeUnit.HOURS.toMillis(24)
    }
}
