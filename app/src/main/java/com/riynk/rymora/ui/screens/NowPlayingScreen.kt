package com.riynk.rymora.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.riynk.rymora.playback.PlaybackUiState

@Composable
fun NowPlayingScreen(
    state: PlaybackUiState,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit
) {
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(state.isPlaying) {
        if (state.isPlaying) {
            while (true) {
                rotation.animateTo(
                    targetValue = rotation.value + 360f,
                    animationSpec = tween(durationMillis = 12_000, easing = LinearEasing)
                )
            }
        }
    }

    val duration = state.durationMs.coerceAtLeast(0L)
    val position = state.positionMs.coerceIn(0L, duration)

    Column(
        modifier = Modifier.fillMaxSize()
            .pointerInput(onBack) {
                var dragDistance = 0f
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        dragDistance += dragAmount
                    },
                    onDragEnd = {
                        if (dragDistance > 72.dp.toPx()) onBack()
                        dragDistance = 0f
                    },
                    onDragCancel = { dragDistance = 0f }
                )
            }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowDownward, contentDescription = "Collapse player")
            }
            Text("NOW PLAYING", style = MaterialTheme.typography.labelLarge)
            Text(
                "  •  RYMORA",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Box(
            modifier = Modifier.size(290.dp).graphicsLayer { rotationZ = rotation.value }
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(250.dp).border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(205.dp).border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.size(150.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f), CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("R", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Box(
                modifier = Modifier.size(18.dp).background(MaterialTheme.colorScheme.background, CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                if (state.hasTrack) state.title else "No track selected",
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1
            )
            Text(
                if (state.hasTrack) state.artist else "Choose a song from your library",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Slider(
                value = if (duration > 0L) position.toFloat() else 0f,
                onValueChange = { onSeek(it.toLong()) },
                valueRange = 0f..duration.coerceAtLeast(1L).toFloat(),
                enabled = state.hasTrack && duration > 0L,
                modifier = Modifier.padding(top = 16.dp)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTime(position), style = MaterialTheme.typography.labelSmall)
                Text(formatTime(duration), style = MaterialTheme.typography.labelSmall)
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious, enabled = state.hasPrevious) {
                    Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous")
                }
                IconButton(onClick = onPlayPause, enabled = state.hasTrack) {
                    Icon(
                        if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (state.isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(48.dp)
                    )
                }
                IconButton(onClick = onNext, enabled = state.hasNext) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Next")
                }
            }
        }
    }
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = (milliseconds / 1000L).coerceAtLeast(0L)
    return "${totalSeconds / 60}:${(totalSeconds % 60).toString().padStart(2, '0')}"
}
