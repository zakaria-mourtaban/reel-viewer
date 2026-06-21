package com.zakaria.reelviewer.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zakaria.reelviewer.data.ReelRepository
import com.zakaria.reelviewer.data.ReelResult
import com.zakaria.reelviewer.util.LinkHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ScreenMode { SETUP, PLAYER }

data class PlayerState(
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val videoUrl: String? = null,
    val title: String? = null,
    val error: String? = null,
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ReelRepository(application)
    private var lastUrl: String? = null

    private val _screenMode = MutableStateFlow(ScreenMode.SETUP)
    val screenMode: StateFlow<ScreenMode> = _screenMode.asStateFlow()

    private val _isDefaultHandler = MutableStateFlow(false)
    val isDefaultHandler: StateFlow<Boolean> = _isDefaultHandler.asStateFlow()

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    fun checkDefaultStatus() {
        _isDefaultHandler.value = LinkHandler.isDefaultForInstagramReels(getApplication())
    }

    fun onSettingsReturned() {
        checkDefaultStatus()
    }

    fun loadReel(url: String) {
        _screenMode.value = ScreenMode.PLAYER
        lastUrl = url
        viewModelScope.launch {
            _state.value = PlayerState(isLoading = true)

            var result = repository.getReelStreamUrl(url)

            if (result is ReelResult.Error) {
                _state.value = PlayerState(
                    isLoading = true,
                    statusMessage = "Extractor may be outdated — updating yt-dlp…",
                )
                repository.updateYtDlp()
                result = repository.getReelStreamUrl(url)
            }

            when (result) {
                is ReelResult.Success -> {
                    _state.value = PlayerState(
                        isLoading = false,
                        videoUrl = result.url,
                        title = result.title,
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

    fun retry() {
        lastUrl?.let { loadReel(it) }
    }
}
