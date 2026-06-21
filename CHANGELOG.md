# Changelog

## v1.0.0

- Initial release
- View Instagram reels without the Instagram app installed
- Automatic deep-link routing for `instagram.com/reel/*` and `/reels/*` links
- Native video player controls (seekbar, play/pause, fullscreen, looping)
- Auto-updating yt-dlp extractor (NIGHTLY channel) for resilience against Instagram page changes
  - On extraction failure: force-updates yt-dlp and retries once
  - On successful play: silently checks for updates at most once per 24h
- Public reels only (no login required)
