package com.zakaria.reelviewer.util

data class PlatformInfo(
    val name: String,
    val domains: List<String>,
    val testUrl: String,
    val testVideoUrl: String,
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
            testVideoUrl = "https://www.instagram.com/reel/CxB4QPmJQ5R/",
            icon = "\uD83D\uDCF7",
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
            testVideoUrl = "https://www.tiktok.com/@khaby.lame/video/7235172738653140795",
            icon = "\uD83C\uDF99",
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
            testVideoUrl = "https://www.youtube.com/shorts/dQw4w9WgXcQ",
            icon = "\u25B6",
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
            testVideoUrl = "https://www.facebook.com/reel/696187749132400",
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
            testVideoUrl = "https://x.com/NASA/status/1720000000000000",
            icon = "X",
        ),
        PlatformInfo(
            name = "Snapchat Spotlight",
            domains = listOf(
                "snapchat.com/spotlight/*",
                "www.snapchat.com/spotlight/*",
                "www.snapchat.com/p/*",
            ),
            testUrl = "https://www.snapchat.com/spotlight/test123",
            testVideoUrl = "https://www.snapchat.com/spotlight/Fe5SU7SNWYW2adY06neXAA",
            icon = "\uD83D\uDC81",
        ),
        PlatformInfo(
            name = "Pinterest",
            domains = listOf(
                "pinterest.com/pin/*",
                "www.pinterest.com/pin/*",
                "pin.it/*",
            ),
            testUrl = "https://www.pinterest.com/pin/1234567890/",
            testVideoUrl = "https://www.pinterest.com/pin/1234567890123456789/",
            icon = "\uD83D\uDCCC",
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
            testVideoUrl = "https://clips.twitch.tv/EncouragingTalentedPenguinKippa",
            icon = "\uD83C\uDFAE",
        ),
        PlatformInfo(
            name = "Dailymotion",
            domains = listOf(
                "dailymotion.com/video/*",
                "www.dailymotion.com/video/*",
                "dai.ly/*",
            ),
            testUrl = "https://www.dailymotion.com/video/test123",
            testVideoUrl = "https://www.dailymotion.com/video/x8t5um",
            icon = "\uD83C\uDF9F",
        ),
    )
}