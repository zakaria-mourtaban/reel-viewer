package com.zakaria.reelviewer

import android.app.Application
import android.util.Log
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.zakaria.reelviewer.data.VideoCache

class ReelViewerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            YoutubeDL.getInstance().init(this)
        } catch (e: YoutubeDLException) {
            Log.e(TAG, "failed to initialize youtubedl-android", e)
        }
        try {
            VideoCache.init(this)
        } catch (e: Exception) {
            Log.e(TAG, "failed to initialize video cache", e)
        }
    }

    companion object {
        private const val TAG = "ReelViewerApp"
    }
}
