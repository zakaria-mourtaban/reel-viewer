package com.zakaria.reelviewer.player

import android.content.Context
import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun SetupScreen(viewModel: PlayerViewModel) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("reel_viewer_prefs", Context.MODE_PRIVATE) }
    val hasSeenTutorial = remember { prefs.getBoolean("has_seen_tutorial", false) }
    var showTutorial = remember { !hasSeenTutorial }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Reel Viewer",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Watch short-form videos from any platform without their apps",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (showTutorial) {
                TutorialVideo(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "↑ How to enable link handling in Settings",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        prefs.edit().putBoolean("has_seen_tutorial", true).apply()
                        showTutorial = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Got it")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = { viewModel.navigateToSettings() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "  Open Settings",
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "To enable link handling for specific platforms, go to Settings",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
private fun TutorialVideo(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val videoView = remember { VideoView(context) }

    LaunchedEffect(Unit) {
        val uri = Uri.parse("android.resource://${context.packageName}/${context.resources.getIdentifier("tutorial_links", "raw", context.packageName)}")
        videoView.setVideoURI(uri)
        videoView.setOnPreparedListener { mp ->
            mp.isLooping = true
            mp.setVolume(0f, 0f)
        }
        videoView.start()
    }

    DisposableEffect(Unit) {
        onDispose {
            videoView.stopPlayback()
        }
    }

    AndroidView(
        factory = { videoView },
        modifier = modifier
    )
}