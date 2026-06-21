package com.zakaria.reelviewer.player

import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.zakaria.reelviewer.data.VideoCache

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun ReelPlayerScreen(viewModel: PlayerViewModel) {
    val state by viewModel.state.collectAsState()
    val downloadState by viewModel.downloadState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var playerViewRef = remember { mutableStateOf<PlayerView?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val exoPlayer = remember {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(1000, 5000, 1000, 1000)
            .setTargetBufferBytes(DefaultLoadControl.DEFAULT_TARGET_BUFFER_BYTES)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(VideoCache.get())
            .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(cacheDataSourceFactory)

        ExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
                repeatMode = Player.REPEAT_MODE_ONE
            }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                Lifecycle.Event.ON_STOP -> exoPlayer.pause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    LaunchedEffect(state.videoUrl) {
        state.videoUrl?.let { url ->
            exoPlayer.setMediaItem(MediaItem.fromUri(url))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
            exoPlayer.playbackParameters = PlaybackParameters(playbackSpeed)
        }
    }

    LaunchedEffect(playbackSpeed) {
        exoPlayer.playbackParameters = PlaybackParameters(playbackSpeed)
    }

    LaunchedEffect(downloadState.message) {
        downloadState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearDownloadMessage()
        }
    }

    LaunchedEffect(downloadState.error) {
        downloadState.error?.let {
            snackbarHostState.showSnackbar("Download failed: $it")
            viewModel.clearDownloadMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when {
            state.isLoading -> {
                LoadingContent(statusMessage = state.statusMessage, platform = state.platform)
            }
            state.error != null -> {
                ErrorContent(
                    message = state.error!!,
                    onRetry = { viewModel.retry() }
                )
            }
            state.videoUrl != null -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = true
                                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                                setKeepContentOnPlayerReset(true)
                                controllerHideOnTouch = true
                                setControllerShowTimeoutMs(4000)
                                hideController()
                                playerViewRef.value = this

                                setOnTouchListener { _, event ->
                                    handleHoldToPause(event, exoPlayer)
                                    false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    PlayerOverlay(
                        viewModel = viewModel,
                        playbackSpeed = playbackSpeed,
                        onSpeedChange = { speed ->
                            playbackSpeed = speed
                        },
                        onShare = {
                            state.originalUrl?.let { url ->
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, url)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share link"))
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            else -> {
                IdleContent()
            }
        }

        if (downloadState.isDownloading) {
            DownloadProgressOverlay(progress = downloadState.progress)
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

private val holdHandler = Handler(Looper.getMainLooper())
private var isHolding = false
private var wasPlayingBeforeHold = false

private fun handleHoldToPause(event: MotionEvent, player: Player) {
    when (event.action) {
        MotionEvent.ACTION_DOWN -> {
            isHolding = false
            holdHandler.postDelayed({
                isHolding = true
                wasPlayingBeforeHold = player.isPlaying
                if (player.isPlaying) player.pause()
            }, 300)
        }
        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
            holdHandler.removeCallbacksAndMessages(null)
            if (isHolding) {
                if (wasPlayingBeforeHold) player.play()
                isHolding = false
            }
        }
    }
}

@Composable
private fun PlayerOverlay(
    viewModel: PlayerViewModel,
    playbackSpeed: Float,
    onSpeedChange: (Float) -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Top,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            val speedLabel = when (playbackSpeed) {
                0.5f -> "0.5x"
                1.5f -> "1.5x"
                2.0f -> "2x"
                else -> "1x"
            }
            IconButton(onClick = {
                val next = when (playbackSpeed) {
                    0.5f -> 1.0f
                    1.0f -> 1.5f
                    1.5f -> 2.0f
                    else -> 0.5f
                }
                onSpeedChange(next)
            }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Speed, contentDescription = "Speed", tint = Color.White)
                    Text(speedLabel, color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
            }
            IconButton(onClick = onShare) {
                Icon(Icons.Filled.Share, contentDescription = "Share", tint = Color.White)
            }
            IconButton(onClick = { viewModel.downloadCurrentVideo() }) {
                Icon(Icons.Filled.Download, contentDescription = "Download", tint = Color.White)
            }
            IconButton(onClick = { viewModel.navigateToSettings() }) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.White)
            }
        }
    }
}

@Composable
private fun DownloadProgressOverlay(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                progress = { progress },
                color = Color.White,
                strokeWidth = 6.dp,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (progress > 0f) "${(progress * 100).toInt()}%" else "Preparing…",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Downloading…",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun LoadingContent(statusMessage: String?, platform: String?) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = Color.White)
        if (platform != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading from $platform…",
                color = Color.White,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
        if (statusMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = statusMessage,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Refresh,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = Color.White,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun IdleContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tap a short-form video link to play it",
            color = Color.White,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Instagram • TikTok • YouTube Shorts • Facebook\nTwitter/X • Snapchat • Pinterest • Twitch • Dailymotion",
            color = Color.White.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}
