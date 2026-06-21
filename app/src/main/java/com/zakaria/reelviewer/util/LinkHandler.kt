package com.zakaria.reelviewer.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings

object LinkHandler {

    fun isDefaultForInstagramReels(context: Context): Boolean {
        val testUri = "https://www.instagram.com/reel/test123"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(testUri))
        val pm = context.packageManager
        val resolved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.resolveActivity(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        }
        return resolved?.packageName == context.packageName
    }

    fun openDefaultLinkSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS)
            .setData(Uri.parse("package:${activity.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            activity.startActivity(intent)
        } catch (e: Exception) {
            openAppDetailsSettings(activity)
        }
    }

    private fun openAppDetailsSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.parse("package:${context.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
