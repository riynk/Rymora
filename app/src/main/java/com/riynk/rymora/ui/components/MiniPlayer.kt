package com.riynk.rymora.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun MiniPlayer(
    title: String,
    artist: String,
    artworkUri: Uri? = null,
    isPlaying: Boolean = false,
    progress: Float = 0f,
    onClick: () -> Unit,
    onPlayPause: () -> Unit = {},
    onNext: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 3.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
                    .padding(start = 10.dp, end = 4.dp, top = 7.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(7.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (artworkUri != null) {
                        AsyncImage(
                            model = artworkUri,
                            contentDescription = "Album artwork",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(44.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(34.dp).background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                CircleShape
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("R", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title.ifBlank { "Nothing playing" },
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1
                    )
                    Text(
                        artist.ifBlank { "Choose a track" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                IconButton(onClick = onPlayPause) {
                    Icon(
                        if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play"
                    )
                }
                IconButton(onClick = onNext) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Next")
                }
            }
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
