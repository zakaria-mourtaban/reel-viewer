package com.zakaria.reelviewer.data

import android.content.Context
import android.util.Log
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

object VideoCache {
    private const val MAX_CACHE_BYTES = 1024L * 1024L * 1024L
    private const val TAG = "VideoCache"

    private lateinit var cache: SimpleCache

    fun init(context: Context) {
        val cacheDir = File(context.cacheDir, "media_cache")
        cache = SimpleCache(
            cacheDir,
            LeastRecentlyUsedCacheEvictor(MAX_CACHE_BYTES),
            StandaloneDatabaseProvider(context)
        )
        Log.i(TAG, "Video cache initialized at ${cacheDir.absolutePath}, current size: ${cache.cacheSpace} bytes")
    }

    fun get(): SimpleCache = cache

    fun clear() {
        for (key in cache.keys.toList()) {
            cache.removeResource(key)
        }
        Log.i(TAG, "Cache cleared")
    }

    fun sizeBytes(): Long {
        return try {
            cache.cacheSpace
        } catch (e: Exception) {
            0L
        }
    }

    fun release() {
        try {
            cache.release()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release cache", e)
        }
    }
}
