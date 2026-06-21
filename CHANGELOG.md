# Changelog

## v1.2.0

- Multi-platform support: TikTok, YouTube Shorts, Facebook Reels, Twitter/X, Snapchat Spotlight, Pinterest, Twitch Clips, Dailymotion (in addition to Instagram Reels)
- Faster video extraction using --print instead of --dump-json (skips full JSON parsing)
- Faster streaming start: ExoPlayer buffer optimized for minimal initial buffer (1s) before playback
- Platform name shown during loading ("Loading from TikTok…")
- Setup screen updated to show all supported platforms
- Idle screen shows all supported platforms

## v1.1.0

- Added setup/onboarding screen shown on first launch
- Button to open system "Open by default" settings directly
- Detects whether the app is set as the default link handler
- Step-by-step instructions for enabling link handling on Android 12+

## v1.0.0

- Initial release
- View Instagram reels without the Instagram app installed
- Automatic deep-link routing for `instagram.com/reel/*` and `/reels/*` links
- Native video player controls (seekbar, play/pause, fullscreen, looping)
- Auto-updating yt-dlp extractor (NIGHTLY channel) for resilience against Instagram page changes
  - On extraction failure: force-updates yt-dlp and retries once
  - On successful play: silently checks for updates at most once per 24h
- Public reels only (no login required)
