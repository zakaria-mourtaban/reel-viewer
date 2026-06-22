package com.zakaria.reelviewer.util

data class PlatformInfo(
    val name: String,
    val domains: List<String>,
    val testUrl: String,
    val icon: String,
)

enum class LinkStatus { ENABLED, DISABLED, NONE }

object PlatformRegistry {
    val platforms: List<PlatformInfo> = listOf(
        PlatformInfo(
            name = "Instagram",
            domains = listOf(
                "instagram.com/reel/*",
                "instagram.com/reels/*",
                "instagram.com/tv/*",
                "instagram.com/p/*",
                "www.instagram.com/reel/*",
                "www.instagram.com/reels/*",
                "www.instagram.com/tv/*",
                "www.instagram.com/p/*",
                "m.instagram.com/reel/*",
                "m.instagram.com/reels/*",
                "m.instagram.com/p/*",
                "instagr.am/reel/*",
                "cdninstagram.com/*",
            ),
            testUrl = "https://www.instagram.com/reel/test123/",
            icon = "📷",
        ),
        PlatformInfo(
            name = "TikTok",
            domains = listOf(
                "tiktok.com/@*/video/*",
                "www.tiktok.com/t/*",
                "vm.tiktok.com/*",
                "vt.tiktok.com/*",
            ),
            testUrl = "https://www.tiktok.com/@test/video/1234567890",
            icon = "🎵",
        ),
        PlatformInfo(
            name = "YouTube Shorts",
            domains = listOf(
                "youtube.com/shorts/*",
                "m.youtube.com/shorts/*",
                "youtu.be/*",
                "www.youtube.com/shorts/*",
            ),
            testUrl = "https://www.youtube.com/shorts/dQw4w9WgXcQ",
            icon = "▶",
        ),
        PlatformInfo(
            name = "Facebook",
            domains = listOf(
                "facebook.com/reel/*",
                "www.facebook.com/reel/*",
                "www.facebook.com/share/v/*",
                "www.facebook.com/share/r/*",
                "www.facebook.com/watch",
                "www.facebook.com/*/videos/*",
                "m.facebook.com/reel/*",
                "fb.watch/*",
            ),
            testUrl = "https://www.facebook.com/reel/1234567890",
            icon = "f",
        ),
        PlatformInfo(
            name = "Twitter / X",
            domains = listOf(
                "twitter.com/*/status/*",
                "www.twitter.com/*/status/*",
                "mobile.twitter.com/*/status/*",
                "x.com/*/status/*",
                "www.x.com/*/status/*",
            ),
            testUrl = "https://x.com/test/status/1234567890",
            icon = "𝕏",
        ),
        PlatformInfo(
            name = "Snapchat Spotlight",
            domains = listOf(
                "snapchat.com/spotlight/*",
                "www.snapchat.com/spotlight/*",
                "www.snapchat.com/p/*",
            ),
            testUrl = "https://www.snapchat.com/spotlight/test123",
            icon = "👻",
        ),
        PlatformInfo(
            name = "Pinterest",
            domains = listOf(
                "pinterest.com/pin/*",
                "www.pinterest.com/pin/*",
                "pin.it/*",
            ),
            testUrl = "https://www.pinterest.com/pin/1234567890/",
            icon = "📌",
        ),
        PlatformInfo(
            name = "Twitch Clips",
            domains = listOf(
                "twitch.tv/*/clip/*",
                "www.twitch.tv/*/clip/*",
                "m.twitch.tv/*/clip/*",
                "clips.twitch.tv/*",
            ),
            testUrl = "https://clips.twitch.tv/testclip",
            icon = "🎮",
        ),
        PlatformInfo(
            name = "Dailymotion",
            domains = listOf(
                "dailymotion.com/video/*",
                "www.dailymotion.com/video/*",
                "dai.ly/*",
            ),
            testUrl = "https://www.dailymotion.com/video/test123",
            icon = "🎥",
        ),
    )
}