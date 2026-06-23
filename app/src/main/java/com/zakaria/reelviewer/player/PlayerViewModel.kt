package com.zakaria.reelviewer.player

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zakaria.reelviewer.data.DownloadManager
import com.zakaria.reelviewer.data.DownloadResult
import com.zakaria.reelviewer.data.ReelRepository
import com.zakaria.reelviewer.data.ReelResult
import com.zakaria.reelviewer.data.UpdateResult
import com.zakaria.reelviewer.data.VideoCache
import com.zakaria.reelviewer.util.LinkStatus
import com.zakaria.reelviewer.util.PlatformInfo
import com.zakaria.reelviewer.util.PlatformRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

enum class ScreenMode { SETUP, PLAYER, SETTINGS }

data class PlayerState(
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val videoUrl: String? = null,
    val originalUrl: String? = null,
    val title: String? = null,
    val platform: String? = null,
    val error: String? = null,
)

data class DownloadState(
    val isDownloading: Boolean = false,
    val progress: Float = 0f,
    val message: String? = null,
    val error: String? = null,
)

data class UpdateState(
    val isChecking: Boolean = false,
    val message: String? = null,
)

enum class DiagnosticStatus { IDLE, RUNNING, PASS, FAIL }

data class PlatformDiagnostic(
    val platform: PlatformInfo,
    val status: DiagnosticStatus = DiagnosticStatus.IDLE,
    val message: String? = null,
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ReelRepository(application)
    private val downloadManager = DownloadManager(application)
    private var lastUrl: String? = null
    private var openedFromLink = false

    private val _screenMode = MutableStateFlow(ScreenMode.SETUP)
    val screenMode: StateFlow<ScreenMode> = _screenMode.asStateFlow()

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val _downloadState = MutableStateFlow(DownloadState())
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    private val _updateState = MutableStateFlow(UpdateState())
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _cacheSizeBytes = MutableStateFlow(0L)
    val cacheSizeBytes: StateFlow<Long> = _cacheSizeBytes.asStateFlow()

    private val _ytDlpVersion = MutableStateFlow<String?>(null)
    val ytDlpVersion: StateFlow<String?> = _ytDlpVersion.asStateFlow()

    private val _debugOutput = MutableStateFlow<String?>(null)
    val debugOutput: StateFlow<String?> = _debugOutput.asStateFlow()

    private val _platformLinkStatuses = MutableStateFlow<Map<String, LinkStatus>>(emptyMap())
    val platformLinkStatuses: StateFlow<Map<String, LinkStatus>> = _platformLinkStatuses.asStateFlow()

    private val _diagnostics = MutableStateFlow<Map<String, PlatformDiagnostic>>(emptyMap())
    val diagnostics: StateFlow<Map<String, PlatformDiagnostic>> = _diagnostics.asStateFlow()

    private val _isRunningDiagnostics = MutableStateFlow(false)
    val isRunningDiagnostics: StateFlow<Boolean> = _isRunningDiagnostics.asStateFlow()

    init {
        refreshCacheSize()
        refreshYtDlpVersion()
    }

    fun checkLinkStatuses() {
        val context = getApplication<Application>()
        val statuses = mutableMapOf<String, LinkStatus>()
        for (platform in PlatformRegistry.platforms) {
            statuses[platform.name] = checkPlatformLinkStatus(context, platform.testUrl)
        }
        _platformLinkStatuses.value = statuses
    }

    private fun checkPlatformLinkStatus(context: Context, testUrl: String): LinkStatus {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(testUrl))
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
        return if (resolved?.activityInfo?.packageName == context.packageName) {
            LinkStatus.ENABLED
        } else if (resolved != null) {
            LinkStatus.DISABLED
        } else {
            LinkStatus.NONE
        }
    }

    fun navigateToSettings() {
        _screenMode.value = ScreenMode.SETTINGS
        checkLinkStatuses()
    }

    fun navigateBack() {
        when (_screenMode.value) {
            ScreenMode.SETTINGS -> {
                _screenMode.value = if (lastUrl != null) ScreenMode.PLAYER else ScreenMode.SETUP
            }
            else -> {}
        }
    }

    fun refreshCacheSize() {
        _cacheSizeBytes.value = VideoCache.sizeBytes()
    }

    fun clearCache() {
        VideoCache.clear()
        refreshCacheSize()
    }

    fun refreshYtDlpVersion() {
        _ytDlpVersion.value = repository.getVersionName() ?: repository.getVersion()
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            _updateState.value = UpdateState(isChecking = true, message = null)
            val result = repository.updateYtDlp()
            when (result) {
                is UpdateResult.Updated -> {
                    _updateState.value = UpdateState(isChecking = false, message = "Updated to ${result.version ?: "latest"}")
                    refreshYtDlpVersion()
                }
                is UpdateResult.AlreadyUpToDate -> {
                    _updateState.value = UpdateState(isChecking = false, message = "Already up to date")
                }
                is UpdateResult.Error -> {
                    _updateState.value = UpdateState(isChecking = false, message = result.message)
                }
            }
        }
    }

    fun loadReel(url: String) {
        openedFromLink = true
        _screenMode.value = ScreenMode.PLAYER
        lastUrl = url
        viewModelScope.launch {
            _state.value = PlayerState(isLoading = true)
            _debugOutput.value = null

            var result = repository.getStreamUrl(url)

            if (result is ReelResult.Error && result.verboseOutput != null) {
                _debugOutput.value = result.verboseOutput
            }

            if (result is ReelResult.Error) {
                _state.value = PlayerState(
                    isLoading = true,
                    statusMessage = "Extractor may be outdated — updating yt-dlp…",
                )
                repository.updateYtDlp()
                result = repository.getStreamUrl(url)
                if (result is ReelResult.Error && result.verboseOutput != null) {
                    _debugOutput.value = result.verboseOutput
                }
            }

            when (result) {
                is ReelResult.Success -> {
                    _state.value = PlayerState(
                        isLoading = false,
                        videoUrl = result.url,
                        originalUrl = url,
                        title = result.title,
                        platform = result.platform,
                    )
                    repository.maybeBackgroundUpdate()
                }
                is ReelResult.Error -> {
                    _state.value = PlayerState(
                        isLoading = false,
                        error = result.message,
                    )
                }
            }
        }
    }

    fun loadVerboseOutput() {
        val url = lastUrl ?: return
        viewModelScope.launch {
            _debugOutput.value = "Loading verbose output…"
            val output = repository.getVerboseOutput(url)
            _debugOutput.value = output
        }
    }

    fun clearDebugOutput() {
        _debugOutput.value = null
    }

    fun downloadCurrentVideo() {
        val state = _state.value
        val videoUrl = state.videoUrl ?: return
        val title = state.title
        viewModelScope.launch {
            _downloadState.value = DownloadState(isDownloading = true, progress = 0f, message = "Downloading…")
            val result = downloadManager.download(videoUrl, title) { progress ->
                _downloadState.value = DownloadState(isDownloading = true, progress = progress, message = "Downloading…")
            }
            when (result) {
                is DownloadResult.Success -> {
                    _downloadState.value = DownloadState(isDownloading = false, progress = 1f, message = "Saved to Downloads")
                    refreshCacheSize()
                }
                is DownloadResult.Error -> {
                    _downloadState.value = DownloadState(isDownloading = false, error = result.message)
                }
            }
        }
    }

    fun clearDownloadMessage() {
        _downloadState.value = DownloadState()
    }

    fun retry() {
        lastUrl?.let { loadReel(it) }
    }

    fun runDiagnostics() {
        viewModelScope.launch {
            _isRunningDiagnostics.value = true
            _diagnostics.value = PlatformRegistry.platforms.associate {
                it.name to PlatformDiagnostic(it, DiagnosticStatus.RUNNING)
            }

            for (platform in PlatformRegistry.platforms) {
                val result = withTimeoutOrNull(30_000L) {
                    repository.getStreamUrl(platform.testVideoUrl)
                }

                val diagnostic = when {
                    result == null -> PlatformDiagnostic(
                        platform, DiagnosticStatus.FAIL,
                        "Timed out after 30s"
                    )
                    result is ReelResult.Success -> PlatformDiagnostic(
                        platform, DiagnosticStatus.PASS,
                        "OK — URL extracted"
                    )
                    result is ReelResult.Error -> {
                        val msg = result.message
                            .lineSequence()
                            .firstOrNull { it.contains("ERROR", ignoreCase = true) || it.isNotBlank() }
                            ?.replace("ERROR: ", "")
                            ?: result.message
                        PlatformDiagnostic(platform, DiagnosticStatus.FAIL, msg)
                    }
                    else -> PlatformDiagnostic(platform, DiagnosticStatus.FAIL, "Unknown error")
                }

                _diagnostics.value = _diagnostics.value + (platform.name to diagnostic)
            }

            _isRunningDiagnostics.value = false
        }
    }

    fun wasOpenedFromLink(): Boolean = openedFromLink
}