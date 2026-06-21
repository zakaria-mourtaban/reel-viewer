# Reel Viewer

An Android app that lets you watch Instagram reels without having the Instagram app installed. When you tap an `instagram.com/reel/...` link from any app (WhatsApp, Discord, browser, etc.), Reel Viewer opens it directly — no share menu, no copy-paste. It plays the reel in a native video player with standard controls (seekbar, play/pause, fullscreen, looping, speed).

## How it works

1. You tap an Instagram reel link in any app
2. Android offers Reel Viewer as the handler (tap "Always" on first use to make it seamless)
3. The app uses [youtubedl-android](https://github.com/yausername/youtubedl-android) (a yt-dlp wrapper) to extract the direct video URL
4. The video plays in a native [Media3 ExoPlayer](https://developer.android.com/media/media3/exoplayer) with full player controls

## Features

- **Deep-link routing** — automatically intercepts `instagram.com/reel/*` and `/reels/*` links
- **Native video controls** — seekbar, play/pause, fullscreen, looping, playback speed
- **Auto-updating extractor** — if Instagram changes their page and extraction breaks, the app automatically updates yt-dlp (NIGHTLY channel) and retries. On successful plays, it silently checks for updates at most once per 24h.
- **Public reels only** — no login, no credentials, no cookies
- **Looping playback** — reels loop by default, just like the Instagram app

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
