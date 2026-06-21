package com.zakaria.reelviewer.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zakaria.reelviewer.data.DownloadManager
import com.zakaria.reelviewer.data.DownloadResult
import com.zakaria.reelviewer.data.ReelRepository
import com.zakaria.reelviewer.data.ReelResult
import com.zakaria.reelviewer.data.UpdateResult
import com.zakaria.reelviewer.data.VideoCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ReelRepository(application)
    private val downloadManager = DownloadManager(application)
    private var lastUrl: String? = null

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

    init {
        refreshCacheSize()
        refreshYtDlpVersion()
    }

    fun navigateToSettings() {
        _screenMode.value = ScreenMode.SETTINGS
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
        _screenMode.value = ScreenMode.PLAYER
        lastUrl = url
        viewModelScope.launch {
            _state.value = PlayerState(isLoading = true)

            var result = repository.getStreamUrl(url)

            if (result is ReelResult.Error) {
                _state.value = PlayerState(
                    isLoading = true,
                    statusMessage = "Extractor may be outdated — updating yt-dlp…",
                )
                repository.updateYtDlp()
                result = repository.getStreamUrl(url)
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
}
