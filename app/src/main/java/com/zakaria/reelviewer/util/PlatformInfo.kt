package com.zakaria.reelviewer.util

data class PlatformInfo(
    val name: String,
    val domains: List<String>,
    val icon: String,
)

object PlatformRegistry {
    val platforms: List<PlatformInfo> = listOf(
        PlatformInfo(
            name = "Instagram",
            domains = listOf(
                "instagram.com/reel/*",
                "instagram.com/reels/*",
                "instagram.com/tv/*",
                "instagr.am/reel/*",
            ),
            icon = "📷",
        ),
        PlatformInfo(
            name = "TikTok",
            domains = listOf(
                "tiktok.com/@*/video/*",
                "tiktok.com/t/*",
                "vm.tiktok.com/*",
                "vt.tiktok.com/*",
            ),
            icon = "🎵",
        ),
        PlatformInfo(
            name = "YouTube Shorts",
            domains = listOf(
                "youtube.com/shorts/*",
                "m.youtube.com/shorts/*",
            ),
            icon = "▶",
        ),
        PlatformInfo(
            name = "Facebook",
            domains = listOf(
                "facebook.com/reel/*",
                "facebook.com/share/v/*",
                "facebook.com/share/r/*",
                "fb.watch/*",
            ),
            icon = "f",
        ),
        PlatformInfo(
            name = "Twitter / X",
            domains = listOf(
                "twitter.com/*/status/*",
                "x.com/*/status/*",
            ),
            icon = "𝕏",
        ),
        PlatformInfo(
            name = "Snapchat Spotlight",
            domains = listOf(
                "snapchat.com/spotlight/*",
            ),
            icon = "👻",
        ),
        PlatformInfo(
            name = "Pinterest",
            domains = listOf(
                "pinterest.com/pin/*",
                "pin.it/*",
            ),
            icon = "📌",
        ),
        PlatformInfo(
            name = "Twitch Clips",
            domains = listOf(
                "twitch.tv/*/clip/*",
                "clips.twitch.tv/*",
            ),
            icon = "🎮",
        ),
        PlatformInfo(
            name = "Dailymotion",
            domains = listOf(
                "dailymotion.com/video/*",
                "dai.ly/*",
            ),
            icon = "🎥",
        ),
    )
}
