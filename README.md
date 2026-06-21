# Reel Viewer

An Android app that lets you watch short-form videos from any platform without their apps installed. When you tap a video link from Instagram, TikTok, YouTube Shorts, Facebook, Twitter/X, Snapchat, Pinterest, Twitch, or Dailymotion, Reel Viewer opens it directly — no share menu, no copy-paste. It plays the video in a native player with standard controls (seekbar, play/pause, fullscreen, looping, speed).

## How it works

1. You tap a supported video link in any app
2. Android shows an "Open with" dialog with Reel Viewer as an option (tap "Always" to make it the default for that link type)
3. The app uses [youtubedl-android](https://github.com/yausername/youtubedl-android) (a yt-dlp wrapper) to extract the direct video URL
4. The video streams in a native [Media3 ExoPlayer](https://developer.android.com/media/media3/exoplayer) with full player controls — playback starts as soon as the first buffer arrives, no need to download the full video first

## Supported platforms

| Platform | URL patterns |
|----------|-------------|
| Instagram Reels | `instagram.com/reel/*`, `/reels/*`, `/tv/*`, `instagr.am/reel/*` |
| TikTok | `tiktok.com/@*/video/*`, `tiktok.com/t/*`, `vm.tiktok.com/*`, `vt.tiktok.com/*` |
| YouTube Shorts | `youtube.com/shorts/*`, `m.youtube.com/shorts/*` |
| Facebook Reels | `facebook.com/reel/*`, `facebook.com/share/v/*`, `facebook.com/share/r/*`, `fb.watch/*` |
| Twitter/X | `twitter.com/*/status/*`, `x.com/*/status/*` |
| Snapchat Spotlight | `snapchat.com/spotlight/*` |
| Pinterest | `pinterest.com/pin/*`, `pin.it/*` |
| Twitch Clips | `twitch.tv/*/clip/*`, `clips.twitch.tv/*` |
| Dailymotion | `dailymotion.com/video/*`, `dai.ly/*` |

## Features

- **Deep-link routing** — automatically intercepts supported video links from 9 platforms
- **Native video controls** — seekbar, play/pause, fullscreen, looping, playback speed
- **Fast streaming** — optimized ExoPlayer buffer starts playback with just 1 second of buffered data
- **Auto-updating extractor** — if a platform changes their page and extraction breaks, the app automatically updates yt-dlp (NIGHTLY channel) and retries. On successful plays, it silently checks for updates at most once per 24h.
- **Public videos only** — no login, no credentials, no cookies
- **Looping playback** — videos loop by default, just like native apps

## Requirements

- Android 7.0 (API 24) or higher
- Internet connection

## Building from source

1. Clone this repo
2. Open in Android Studio (Hedgehog 2023.1.1 or newer)
3. Let Gradle sync — it will download youtubedl-android and all dependencies from Maven Central
4. Run on a device or emulator (API 24+)

```bash
# Or build from the command line
./gradlew assembleDebug
```

The debug APK will be at `app/build/outputs/apk/debug/`.

## Installing a release

Download the appropriate APK from the [Releases](../../releases) page:

| APK | Devices |
|-----|---------|
| `app-universal-release.apk` | All devices (largest file) |
| `app-arm64-v8a-release.apk` | Modern phones (most common) |
| `app-armeabi-v7a-release.apk` | Older 32-bit ARM phones |
| `app-x86_64-release.apk` | Emulators / x86 tablets |

Enable "Install from unknown sources" for your browser/file manager, then install the APK.

> **First link tap:** Android will show an "Open with" dialog listing Reel Viewer and your browser. Tap **"Always"** to make all future Instagram reel links open directly in Reel Viewer with no prompt.

## Release signing setup

To build signed release APKs via GitHub Actions, you need a keystore. This is a one-time setup:

### 1. Generate a keystore locally

```bash
keytool -genkeypair -v \
  -keystore release.keystore \
  -alias reelviewer \
  -keyalg RSA -keysize 2048 -validity 10000
```

Enter a keystore password and key password when prompted (they can be the same).

### 2. Add GitHub repository secrets

Go to your repo → **Settings → Secrets and variables → Actions → New repository secret** and add these four:

| Secret name | Value |
|-------------|-------|
| `KEYSTORE_BASE64` | Base64-encoded keystore file (see below) |
| `KEYSTORE_PASSWORD` | The keystore password you chose |
| `KEY_ALIAS` | `reelviewer` (or whatever alias you used) |
| `KEY_PASSWORD` | The key password you chose |

To base64-encode the keystore:

```bash
# Linux / macOS
base64 -i release.keystore | tr -d '\n'

# Windows (PowerShell)
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release.keystore"))
```

Copy the entire output string as the `KEYSTORE_BASE64` secret value.

### 3. Create a release

```bash
git tag v1.0.0
git push origin v1.0.0
```

The GitHub Actions `release.yml` workflow will build signed APKs (universal + per-ABI) and publish them to the Releases tab automatically.

## Caveats

- **Instagram ToS:** Extracting reel URLs programmatically may violate Instagram's Terms of Service. This app is for personal use only.
- **Extractors can break:** Instagram occasionally changes their page structure. When this happens, yt-dlp's NIGHTLY channel usually has a fix within a day. The app auto-updates the extractor on failure and in the background.
- **APK size:** The app bundles Python + yt-dlp (~30–40 MB per ABI). Using the per-ABI APKs cuts download size roughly in half.
- **App Links auto-verify:** True auto-verification (no dialog at all) would require hosting a `.well-known/assetlinks.json` file on instagram.com, which we can't do. The first link tap shows a one-time "Always" dialog; after that it's seamless.
- **Private/age-gated reels:** Not supported (public reels only, no login).

## License

GPL-3.0 — inherited from [youtubedl-android](https://github.com/yausername/youtubedl-android). See [LICENSE](LICENSE).
