package com.zakaria.reelviewer.player

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zakaria.reelviewer.util.LinkHandler
import com.zakaria.reelviewer.util.LinkStatus
import com.zakaria.reelviewer.util.PlatformInfo
import com.zakaria.reelviewer.util.PlatformRegistry

@Composable
fun SettingsScreen(viewModel: PlayerViewModel) {
    val context = LocalContext.current
    val cacheSizeBytes by viewModel.cacheSizeBytes.collectAsState()
    val ytDlpVersion by viewModel.ytDlpVersion.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val linkStatuses by viewModel.platformLinkStatuses.collectAsState()
    val diagnostics by viewModel.diagnostics.collectAsState()
    val isRunningDiagnostics by viewModel.isRunningDiagnostics.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SettingsHeader(title = "Settings", onBack = { viewModel.navigateBack() })

            SectionLinkHandling(
                linkStatuses = linkStatuses,
                onOpenLinkSettings = {
                    if (context is Activity) {
                        LinkHandler.openDefaultLinkSettings(context)
                    }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SectionYtDlp(
                version = ytDlpVersion,
                isChecking = updateState.isChecking,
                message = updateState.message,
                onCheckUpdate = { viewModel.checkForUpdates() }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SectionCache(
                cacheSizeBytes = cacheSizeBytes,
                onClearCache = { viewModel.clearCache() }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SectionDiagnostics(
                diagnostics = diagnostics,
                isRunning = isRunningDiagnostics,
                onRunDiagnostics = { viewModel.runDiagnostics() },
                onRetry = { viewModel.runDiagnostics() },
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SectionAbout()

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun SectionLinkHandling(
    linkStatuses: Map<String, LinkStatus>,
    onOpenLinkSettings: () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "Link Handling",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Link handling must be enabled per platform in Android settings. " +
                "This is a limitation imposed by the platforms we are trying to access " +
                "with the app and nothing can be done to circumvent this.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(12.dp))

        val sortedPlatforms = PlatformRegistry.platforms.sortedWith(
            compareBy<PlatformInfo> {
                when (linkStatuses[it.name]) {
                    LinkStatus.NONE -> 2
                    LinkStatus.DISABLED -> 1
                    LinkStatus.ENABLED -> 0
                    else -> 3
                }
            }.thenBy { it.name }
        )

        sortedPlatforms.forEach { platform ->
            val status = linkStatuses[platform.name]
            PlatformDropdown(platform, status)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onOpenLinkSettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "  Open Link Settings",
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun PlatformDropdown(
    platform: PlatformInfo,
    status: LinkStatus?,
) {
    var expanded by remember { mutableStateOf(false) }
    val isGreyed = status == LinkStatus.NONE
    val alpha = if (isGreyed) 0.4f else 1.0f

    val statusColor = when (status) {
        LinkStatus.ENABLED -> Color(0xFF4CAF50)
        LinkStatus.DISABLED -> Color(0xFFE53935)
        LinkStatus.NONE -> Color.Gray
        else -> Color.Gray
    }

    Column(modifier = Modifier.alpha(alpha)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { if (!isGreyed) expanded = !expanded }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = if (isGreyed) Color.Gray else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = platform.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isGreyed) Color.Gray else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 8.dp).weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(statusColor, CircleShape)
            )
        }
        AnimatedVisibility(
            visible = expanded && !isGreyed,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.padding(start = 32.dp, bottom = 8.dp)) {
                platform.domains.forEach { domain ->
                    Text(
                        text = "  $domain",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionYtDlp(
    version: String?,
    isChecking: Boolean,
    message: String?,
    onCheckUpdate: () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "yt-dlp Extractor",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Version: ${version ?: "Unknown"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Automatically checked daily",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onCheckUpdate,
            enabled = !isChecking,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isChecking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                )
                Text(text = "  Checking…", modifier = Modifier.padding(start = 4.dp))
            } else {
                Icon(
                    imageVector = Icons.Filled.Update,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(text = "  Check for Updates", modifier = Modifier.padding(start = 4.dp))
            }
        }
        if (message != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun SectionCache(
    cacheSizeBytes: Long,
    onClearCache: () -> Unit,
) {
    val sizeText = formatCacheSize(cacheSizeBytes)
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "Cache",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Storage,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "  Current size: $sizeText",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Cache automatically clears when it exceeds 1 GB. " +
                "Recently viewed videos are kept for faster downloads.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onClearCache,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(text = "  Clear Cache", modifier = Modifier.padding(start = 4.dp))
        }
    }
}

@Composable
private fun SectionDiagnostics(
    diagnostics: Map<String, PlatformDiagnostic>,
    isRunning: Boolean,
    onRunDiagnostics: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "Diagnostics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Test video extraction from each platform. Tap a failed platform to re-run.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onRunDiagnostics,
            enabled = !isRunning,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isRunning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                )
                Text(text = "  Testing...", modifier = Modifier.padding(start = 4.dp))
            } else {
                Icon(
                    imageVector = Icons.Filled.Update,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(text = "  Run Diagnostics", modifier = Modifier.padding(start = 4.dp))
            }
        }

        if (diagnostics.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            PlatformRegistry.platforms.forEach { platform ->
                val diag = diagnostics[platform.name] ?: return@forEach
                DiagnosticRow(diag, onRetry)
            }
        }
    }
}

@Composable
private fun DiagnosticRow(
    diag: PlatformDiagnostic,
    onRetry: () -> Unit,
) {
    val statusColor = when (diag.status) {
        DiagnosticStatus.PASS -> Color(0xFF4CAF50)
        DiagnosticStatus.FAIL -> Color(0xFFFFA726)
        DiagnosticStatus.RUNNING -> Color(0xFF2196F3)
        DiagnosticStatus.IDLE -> Color.Gray
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (diag.status == DiagnosticStatus.FAIL) it.clickable { onRetry() } else it }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (diag.status == DiagnosticStatus.RUNNING) {
            CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                strokeWidth = 1.5.dp,
            )
        } else {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(statusColor, CircleShape)
            )
        }
        Text(
            text = "  ${diag.platform.name}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (diag.status == DiagnosticStatus.IDLE) {
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.onBackground
            },
            modifier = Modifier.weight(1f)
        )
        if (diag.message != null && diag.status != DiagnosticStatus.IDLE) {
            Text(
                text = diag.message,
                style = MaterialTheme.typography.labelSmall,
                color = if (diag.status == DiagnosticStatus.FAIL) {
                    Color(0xFFFFA726).copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                },
                maxLines = 2,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
    }
}

@Composable
private fun SectionAbout() {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "About",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "  Reel Viewer v1.4.0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Watch short-form videos from 9 platforms without their apps",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
    }
}

private fun formatCacheSize(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 * 1024 -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
        bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
        bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}