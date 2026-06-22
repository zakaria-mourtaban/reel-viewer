# Changelog

## v1.3.1

- Fix TikTok: prefer H264 codec (-S vcodec:h264,res) to avoid HEVC formats that download without audio
- Debug info button (ℹ) on player overlay: shows verbose yt-dlp output in a scrollable monospace dialog
- Copy button in debug dialog: copies full verbose output to clipboard
- Debug info auto-opens on extraction errors with verbose output pre-loaded
- Back navigation: pressing back from player finishes activity and returns to calling app
- Comprehensive intent-filters: added Instagram /p/ (posts), Instagram cdninstagram.com (CDN direct video),
  YouTube youtu.be, Facebook /watch and /*/videos/, Snapchat /p/ (share links)
- Direct video URL bypass: CDN links (cdninstagram.com, scontent) and direct video files skip yt-dlp extraction
- Platform dropdown: green dot = enabled, red dot = disabled, grey = no links selected
- Greyed platforms sorted to bottom; enabled platforms sorted to top
- Platform domains colored to match platform status

## v1.3.0

- Settings screen with 4 sections: Link Handling, yt-dlp Extractor, Cache, About
- Link Handling section: expandable dropdown listing each platform and its domains, with button to open system link settings
- Note in Settings explaining the link handling limitation is imposed by the platforms, not the app
- yt-dlp version display in Settings with manual "Check for Updates" button
- Video cache (1 GB LRU): ExoPlayer streams through SimpleCache so watched videos are cached on disk
- Download button: saves video to Downloads folder, pulls from cache first (near-instant for already-watched videos)
- Cache management: auto-clears at 1 GB, manual "Clear Cache" button in Settings
- Player UI hidden by default; tap screen to show controls, auto-hides after 4s
- Hold to pause, release to resume (300ms threshold to distinguish from tap)
- Playback speed control: 0.5x / 1x / 1.5x / 2x cycle button on player overlay
- Share URL button on player overlay
- Settings gear icon on player overlay
- Simplified setup screen with "Open Settings" button and hint text
- Back button properly navigates from Settings to previous screen

## v1.2.1

- Fix: Removed autoVerify from intent-filters so the app appears in Android's "Open with" dialog automatically when a supported link is tapped (no manual setup needed)
- On Android 12+, autoVerify=true without a hosted assetlinks.json caused the app to be excluded from the disambiguation dialog entirely
- Now all supported links trigger the standard "Open with" dialog with Reel Viewer as an option
- Setup screen updated: shows "Ready to go!" with optional settings button for users who want to skip the dialog entirely

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
