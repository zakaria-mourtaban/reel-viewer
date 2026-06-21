package com.zakaria.reelviewer.data

import android.content.Context
import android.util.Log
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDL.UpdateChannel
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

sealed class ReelResult {
    data class Success(val url: String, val title: String?, val platform: String) : ReelResult()
    data class Error(val message: String) : ReelResult()
}

class ReelRepository(private val context: Context) {

    suspend fun getStreamUrl(videoUrl: String): ReelResult = withContext(Dispatchers.IO) {
        try {
            val request = YoutubeDLRequest(videoUrl).apply {
                addOption("-f", "best")
                addOption("--print", "%(url)s")
                addOption("--print", "%(title)s")
                addOption("--no-warnings")
                addOption("--no-progress")
            }
            val response = YoutubeDL.getInstance().execute(request)
            val lines = response.out.trim().lines().filter { it.isNotBlank() }
            val streamUrl = lines.firstOrNull { it.startsWith("http") }
            val title = lines.getOrNull(1)

            if (streamUrl.isNullOrBlank()) {
                ReelResult.Error("Could not extract video URL from this link")
            } else {
                val platform = detectPlatform(videoUrl)
                ReelResult.Success(streamUrl, title, platform)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract video", e)
            ReelResult.Error(e.message ?: "Failed to load video")
        }
    }

    private fun detectPlatform(url: String): String {
        val lower = url.lowercase()
        return when {
            "instagram.com" in lower -> "Instagram"
            "tiktok.com" in lower -> "TikTok"
            "youtube.com" in lower || "youtu.be" in lower -> "YouTube"
            "facebook.com" in lower || "fb.watch" in lower -> "Facebook"
            "twitter.com" in lower || "x.com" in lower -> "X"
            "snapchat.com" in lower -> "Snapchat"
            "pinterest.com" in lower || "pin.it" in lower -> "Pinterest"
            "twitch.tv" in lower -> "Twitch"
            "dailymotion.com" in lower || "dai.ly" in lower -> "Dailymotion"
            else -> "Video"
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
