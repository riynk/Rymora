package com.riynk.rymora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.riynk.rymora.playback.LocalTrack
import com.riynk.rymora.playback.PlaybackUiState

@Composable
fun NowPlayingScreen(
    state: PlaybackUiState,
    tracks: List<LocalTrack>,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onSelectTrack: (LocalTrack) -> Unit,
    modifier: Modifier = Modifier
) {
    var showQueue by remember { mutableStateOf(false) }
    val duration = state.durationMs.coerceAtLeast(0L)
    val position = state.positionMs.coerceIn(0L, duration)
    val activeTint = Color(0xFFFF6B55)
    val inactiveTint = Color.White.copy(alpha = 0.68f)

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF171416))) {
        if (state.artworkUri != null) {
            AsyncImage(
                model = state.artworkUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().blur(48.dp).graphicsLayer { alpha = 0.48f }
            )
        }
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xCC241C20),
                        Color(0xF2171416),
                        Color(0xFF111014)
                    )
                )
            )
        )

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowDownward, contentDescription = "Collapse player", tint = Color.White)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("NOW PLAYING", color = Color.White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text("RYMORA MIX", color = Color.White.copy(alpha = 0.58f), style = MaterialTheme.typography.labelSmall)
                }
                IconButton(onClick = { showQueue = true }) {
                    Icon(Icons.Rounded.QueueMusic, contentDescription = "Up next", tint = Color.White)
                }
            }

            Spacer(Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    .aspectRatio(1f),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.07f),
                shadowElevation = 18.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (state.artworkUri != null) {
                        AsyncImage(
                            model = state.artworkUri,
                            contentDescription = "Album artwork",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            Modifier.fillMaxSize().background(
                                Brush.linearGradient(listOf(Color(0xFF4B2635), Color(0xFF171D32)))
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("R", color = Color.White, style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (state.hasTrack) state.title else "No track selected",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        if (state.hasTrack) state.artist else "Choose a song from your library",
                        color = Color.White.copy(alpha = 0.68f),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1
                    )
                }
                Text("♪", color = activeTint, style = MaterialTheme.typography.headlineMedium)
            }

            Slider(
                value = if (duration > 0L) position.toFloat() else 0f,
                onValueChange = { onSeek(it.toLong()) },
                valueRange = 0f..duration.coerceAtLeast(1L).toFloat(),
                enabled = state.hasTrack && duration > 0L,
                modifier = Modifier.padding(top = 18.dp),
                colors = androidx.compose.material3.SliderDefaults.colors(
                    thumbColor = activeTint,
                    activeTrackColor = activeTint,
                    inactiveTrackColor = Color.White.copy(alpha = 0.24f)
                )
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTime(position), color = Color.White.copy(alpha = 0.65f), style = MaterialTheme.typography.labelSmall)
                Text("-${formatTime((duration - position).coerceAtLeast(0L))}", color = Color.White.copy(alpha = 0.65f), style = MaterialTheme.typography.labelSmall)
            }

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleShuffle) {
                    Icon(Icons.Rounded.Shuffle, contentDescription = "Shuffle", tint = if (state.shuffleEnabled) activeTint else inactiveTint)
                }
                IconButton(onClick = onPrevious, enabled = state.hasPrevious) {
                    Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(34.dp))
                }
                Surface(
                    onClick = onPlayPause,
                    enabled = state.hasTrack,
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(68.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (state.isPlaying) "Pause" else "Play",
                            tint = Color(0xFF171416),
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
                IconButton(onClick = onNext, enabled = state.hasNext) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(34.dp))
                }
                IconButton(onClick = onCycleRepeat) {
                    Icon(
                        if (state.repeatMode == Player.REPEAT_MODE_ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                        contentDescription = "Repeat",
                        tint = if (state.repeatMode == Player.REPEAT_MODE_OFF) inactiveTint else activeTint
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Surface(
                onClick = { showQueue = true },
                color = Color.White.copy(alpha = 0.09f),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.QueueMusic, contentDescription = null, tint = Color.White)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text("Up next", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("${tracks.size} lagu dalam antrean", color = Color.White.copy(alpha = 0.62f), style = MaterialTheme.typography.bodySmall)
                    }
                    Text("Lihat", color = activeTint, style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(Modifier.height(26.dp))
        }
    }

    if (showQueue) {
        ModalBottomSheet(
            onDismissRequest = { showQueue = false },
            containerColor = Color(0xFF211D20),
            contentColor = Color.White
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
                Text("Up next", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Antrean pemutaran", color = Color.White.copy(alpha = 0.62f), style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(12.dp))
                LazyColumn(modifier = Modifier.fillMaxWidth().height(360.dp)) {
                    items(tracks, key = { it.id }) { track ->
                        val selected = track.uri.toString() == state.mediaId
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                .background(if (selected) Color.White.copy(alpha = 0.10f) else Color.Transparent)
                                .clickable { onSelectTrack(track); showQueue = false }
                                .padding(horizontal = 10.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier.size(42.dp).clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF40323A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (selected) "♫" else "♪", color = activeTint)
                            }
                            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                Text(track.title, color = if (selected) activeTint else Color.White, maxLines = 1, fontWeight = FontWeight.Medium)
                                Text(track.artist, color = Color.White.copy(alpha = 0.62f), maxLines = 1, style = MaterialTheme.typography.bodySmall)
                            }
                            Text(formatTime(track.durationMs), color = Color.White.copy(alpha = 0.55f), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = (milliseconds / 1000L).coerceAtLeast(0L)
    return "${totalSeconds / 60}:${(totalSeconds % 60).toString().padStart(2, '0')}"
}
