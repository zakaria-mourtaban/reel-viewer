package com.zakaria.reelviewer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.zakaria.reelviewer.player.PlayerViewModel
import com.zakaria.reelviewer.player.ReelPlayerScreen
import com.zakaria.reelviewer.player.ScreenMode
import com.zakaria.reelviewer.player.SettingsScreen
import com.zakaria.reelviewer.player.SetupScreen
import com.zakaria.reelviewer.ui.theme.ReelViewerTheme

class MainActivity : ComponentActivity() {
    private val viewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        setContent {
            ReelViewerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val screenMode by viewModel.screenMode.collectAsState()
                    when (screenMode) {
                        ScreenMode.PLAYER -> ReelPlayerScreen(viewModel = viewModel)
                        ScreenMode.SETUP -> SetupScreen(viewModel = viewModel)
                        ScreenMode.SETTINGS -> SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.navigateBack()
            }
        })
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshCacheSize()
    }

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        viewModel.loadReel(uri.toString())
    }
}
