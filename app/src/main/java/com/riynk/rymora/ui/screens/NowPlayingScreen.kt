package com.riynk.rymora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.riynk.rymora.playback.LocalTrack
import com.riynk.rymora.playback.PlaybackUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    state: PlaybackUiState,
    tracks: List<LocalTrack>,
    onCollapse: () -> Unit,
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
    val active = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(onCollapse) {
                var downwardDrag = 0f
                var dismissed = false
                detectVerticalDragGestures(
                    onDragStart = {
                        downwardDrag = 0f
                        dismissed = false
                    },
                    onVerticalDrag = { change, amount ->
                        if (amount > 0f && !dismissed) {
                            downwardDrag += amount
                            if (downwardDrag > 120f) {
                                dismissed = true
                                onCollapse()
                            }
                            change.consume()
                        }
                    }
                )
            }
    ) {
        if (state.artworkUri != null) {
            AsyncImage(
                model = state.artworkUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().blur(54.dp)
            )
        }
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background.copy(alpha = 0.34f),
                        MaterialTheme.colorScheme.background.copy(alpha = 0.94f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
        )

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
        ) {
            val artworkSize = minOf(maxWidth - 52.dp, maxHeight * 0.40f).coerceAtLeast(176.dp)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onCollapse) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close player")
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("PLAYING FROM YOUR LIBRARY", style = MaterialTheme.typography.labelSmall, color = muted, letterSpacing = 1.1.sp)
                        Text("Now Playing", style = MaterialTheme.typography.titleMedium)
                    }
                    IconButton(onClick = { showQueue = true }) {
                        Icon(Icons.Rounded.QueueMusic, contentDescription = "Open queue")
                    }
                }

                Spacer(Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.size(artworkSize),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
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
                            Icon(Icons.Rounded.Album, contentDescription = null, modifier = Modifier.size(84.dp), tint = active)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            state.title.ifBlank { "Nothing playing" },
                            style = MaterialTheme.typography.headlineSmall,
                            maxLines = 1
                        )
                        Text(
                            state.artist.ifBlank { "Choose a song from your library" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = muted,
                            maxLines = 1
                        )
                    }
                    IconButton(onClick = { showQueue = true }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "More playback options", tint = muted)
                    }
                }

                Spacer(Modifier.height(12.dp))
                Slider(
                    value = position.toFloat(),
                    onValueChange = { onSeek(it.toLong()) },
                    valueRange = 0f..duration.coerceAtLeast(1L).toFloat(),
                    enabled = state.hasTrack && duration > 0L,
                    colors = SliderDefaults.colors(
                        thumbColor = active,
                        activeTrackColor = active,
                        inactiveTrackColor = muted.copy(alpha = 0.30f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatTime(position), style = MaterialTheme.typography.labelSmall, color = muted)
                    Text("-${formatTime((duration - position).coerceAtLeast(0L))}", style = MaterialTheme.typography.labelSmall, color = muted)
                }

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onToggleShuffle) {
                        Icon(Icons.Rounded.Shuffle, contentDescription = "Shuffle", tint = if (state.shuffleEnabled) active else muted)
                    }
                    IconButton(onClick = onPrevious, enabled = state.hasPrevious) {
                        Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(34.dp))
                    }
                    Surface(
                        onClick = onPlayPause,
                        enabled = state.hasTrack,
                        shape = CircleShape,
                        color = active,
                        modifier = Modifier.size(68.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (state.isPlaying) "Pause" else "Play",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }
                    IconButton(onClick = onNext, enabled = state.hasNext) {
                        Icon(Icons.Rounded.SkipNext, contentDescription = "Next", modifier = Modifier.size(34.dp))
                    }
                    IconButton(onClick = onCycleRepeat) {
                        Icon(
                            if (state.repeatMode == Player.REPEAT_MODE_ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                            contentDescription = "Repeat",
                            tint = if (state.repeatMode == Player.REPEAT_MODE_OFF) muted else active
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                Surface(
                    onClick = { showQueue = true },
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.QueueMusic, contentDescription = null, tint = active)
                        Column(modifier = Modifier.weight(1f).padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Up next", style = MaterialTheme.typography.titleSmall)
                            Text("${tracks.size} songs in queue", style = MaterialTheme.typography.bodySmall, color = muted)
                        }
                        Text("VIEW", style = MaterialTheme.typography.labelMedium, color = active, letterSpacing = 0.8.sp)
                    }
                }
                Spacer(Modifier.height(18.dp))
            }
        }
    }

    if (showQueue) {
        ModalBottomSheet(
            onDismissRequest = { showQueue = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
                Text("Up next", style = MaterialTheme.typography.headlineSmall)
                Text("The current playback queue", style = MaterialTheme.typography.bodySmall, color = muted)
                Spacer(Modifier.height(12.dp))
                LazyColumn(Modifier.fillMaxWidth().height(380.dp)) {
                    items(tracks, key = { it.id }) { track ->
                        val selected = track.uri.toString() == state.mediaId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(13.dp))
                                .clickable { onSelectTrack(track); showQueue = false }
                                .background(if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f) else Color.Transparent)
                                .padding(horizontal = 9.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (track.artworkUri != null) {
                                AsyncImage(
                                    model = track.artworkUri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(9.dp))
                                )
                            } else {
                                Box(
                                    Modifier.size(44.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(9.dp)),
                                    contentAlignment = Alignment.Center
                                ) { Icon(Icons.Rounded.Album, contentDescription = null, tint = active) }
                            }
                            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                Text(track.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, color = if (selected) active else MaterialTheme.colorScheme.onSurface)
                                Text(track.artist, style = MaterialTheme.typography.bodySmall, maxLines = 1, color = muted)
                            }
                            Text(formatTime(track.durationMs), style = MaterialTheme.typography.labelSmall, color = muted)
                        }
                    }
                }
                Spacer(Modifier.height(22.dp))
            }
        }
    }
}

private fun formatTime(milliseconds: Long): String {
    val seconds = (milliseconds / 1000L).coerceAtLeast(0L)
    return "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"
}
