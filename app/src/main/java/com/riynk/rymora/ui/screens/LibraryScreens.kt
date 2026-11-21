package com.riynk.rymora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.riynk.rymora.playback.LocalTrack

@Composable
fun HomeScreen(
    tracks: List<LocalTrack>,
    onPlayTrack: (LocalTrack) -> Unit,
    onPlayAll: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenSettings: () -> Unit,
    onScan: () -> Unit
) {
    val albumCount = remember(tracks) { tracks.map { it.album }.filter { it.isNotBlank() }.distinct().size }
    val artistCount = remember(tracks) { tracks.map { it.artist }.filter { it.isNotBlank() }.distinct().size }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("RYMORA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("Your music space", style = MaterialTheme.typography.headlineMedium)
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                }
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Made for your library", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "${tracks.size} songs  ·  $albumCount albums  ·  $artistCount artists",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = onPlayAll, enabled = tracks.isNotEmpty()) {
                            Icon(Icons.Rounded.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(7.dp))
                            Text("Shuffle all")
                        }
                        TextButton(onClick = onOpenLibrary) { Text("Open library") }
                    }
                }
            }
        }

        if (tracks.isEmpty()) {
            item {
                EmptyLibraryCard(onScan = onScan)
            }
        } else {
            item {
                SectionHeading(
                    title = "Recently added",
                    action = "See all",
                    onAction = onOpenLibrary
                )
            }
            items(tracks.take(12), key = { it.id }) { track ->
                TrackListItem(track = track, onClick = { onPlayTrack(track) })
            }
        }
    }
}

@Composable
fun SearchScreen(
    tracks: List<LocalTrack>,
    onPlayTrack: (LocalTrack) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val results = remember(tracks, query) {
        if (query.isBlank()) emptyList()
        else tracks.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.artist.contains(query, ignoreCase = true) ||
                it.album.contains(query, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Find your sound", style = MaterialTheme.typography.headlineMedium)
            Text("Search songs, artists and albums on this device", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            placeholder = { Text("What do you want to hear?") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = if (query.isNotEmpty()) {
                { TextButton(onClick = { query = "" }) { Text("Clear") } }
            } else null
        )
        if (query.isBlank()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(42.dp), tint = MaterialTheme.colorScheme.primary)
                    Text("Search your offline library", style = MaterialTheme.typography.titleMedium)
                    Text("${tracks.size} tracks indexed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else if (results.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No matches found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                items(results, key = { it.id }) { track ->
                    TrackListItem(track = track, onClick = { onPlayTrack(track) })
                }
            }
        }
    }
}

@Composable
fun LibraryScreen(
    tracks: List<LocalTrack>,
    onPlayTrack: (LocalTrack) -> Unit,
    onScan: () -> Unit
) {
    var selected by rememberSaveable { mutableStateOf("Songs") }
    val albums = remember(tracks) { tracks.groupBy { it.album.ifBlank { "Unknown album" } }.toList().sortedBy { it.first.lowercase() } }
    val artists = remember(tracks) { tracks.groupBy { it.artist.ifBlank { "Unknown artist" } }.toList().sortedBy { it.first.lowercase() } }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 18.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Your library", style = MaterialTheme.typography.headlineMedium)
                Text("${tracks.size} tracks stored on this device", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onScan) { Icon(Icons.Rounded.Refresh, contentDescription = "Scan device") }
        }
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Songs", "Albums", "Artists").forEach { tab ->
                FilterChip(
                    selected = selected == tab,
                    onClick = { selected = tab },
                    label = { Text(tab) }
                )
            }
        }
        if (tracks.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { EmptyLibraryCard(onScan = onScan) }
        } else {
            when (selected) {
                "Songs" -> LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 14.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    items(tracks, key = { it.id }) { track -> TrackListItem(track, onClick = { onPlayTrack(track) }) }
                }
                "Albums" -> LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    items(albums, key = { it.first }) { (name, albumTracks) ->
                        CollectionListItem(
                            title = name,
                            subtitle = "${albumTracks.size} tracks",
                            artwork = albumTracks.firstOrNull()?.artworkUri?.toString(),
                            fallback = Icons.Rounded.Album,
                            onClick = { albumTracks.firstOrNull()?.let(onPlayTrack) }
                        )
                    }
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    items(artists, key = { it.first }) { (name, artistTracks) ->
                        CollectionListItem(
                            title = name,
                            subtitle = "${artistTracks.size} tracks",
                            artwork = artistTracks.firstOrNull()?.artworkUri?.toString(),
                            fallback = Icons.Rounded.Person,
                            onClick = { artistTracks.firstOrNull()?.let(onPlayTrack) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onScan: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, contentDescription = "Back") }
                Column(Modifier.padding(start = 6.dp)) {
                    Text("Settings", style = MaterialTheme.typography.headlineMedium)
                    Text("Make Rymora yours", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            SettingsGroup("Appearance", "Interface and visual identity") {
                SettingsEntry(Icons.Rounded.Tune, "Obsidian Aurora", "Dark surfaces with artwork-led accents")
                SettingsEntry(Icons.Rounded.Album, "Artwork", "Album artwork is used throughout the player")
            }
        }
        item {
            SettingsGroup("Music library", "Manage audio stored on your device") {
                SettingsAction(Icons.Rounded.Refresh, "Scan device", "Find newly added audio files", "Scan", onScan)
                SettingsEntry(Icons.Rounded.FolderOpen, "Offline library", "Your music is indexed locally on this device")
            }
        }
        item {
            SettingsGroup("Playback", "How Rymora plays your music") {
                SettingsEntry(Icons.Rounded.PlayArrow, "Background playback", "MediaSession keeps playback controls available outside the app")
                SettingsEntry(Icons.Rounded.LibraryMusic, "Playback queue", "Play tracks from your local library")
            }
        }
        item {
            SettingsGroup("About", "Application information") {
                SettingsEntry(Icons.Rounded.MusicNote, "Rymora", "Offline-first Android music player · Version 0.1.0")
            }
        }
    }
}

@Composable
private fun SectionHeading(title: String, action: String, onAction: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        TextButton(onClick = onAction) { Text(action) }
    }
}

@Composable
private fun TrackListItem(track: LocalTrack, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ArtworkTile(track.artworkUri?.toString(), Modifier.size(52.dp), Icons.Rounded.MusicNote)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(track.title, style = MaterialTheme.typography.titleSmall, maxLines = 1)
            Text("${track.artist}  ·  ${formatDuration(track.durationMs)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
        Icon(Icons.Rounded.PlayArrow, contentDescription = "Play ${track.title}", tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CollectionListItem(
    title: String,
    subtitle: String,
    artwork: String?,
    fallback: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        ArtworkTile(artwork, Modifier.size(54.dp), fallback)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ArtworkTile(
    artwork: String?,
    modifier: Modifier,
    fallback: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Box(contentAlignment = Alignment.Center) {
            if (artwork != null) {
                AsyncImage(model = artwork, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Icon(fallback, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun EmptyLibraryCard(onScan: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Rounded.LibraryMusic, contentDescription = null, modifier = Modifier.size(44.dp), tint = MaterialTheme.colorScheme.primary)
        Text("Your library is empty", style = MaterialTheme.typography.titleMedium)
        Text("Scan this device to find your local audio files.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick = onScan) {
            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(7.dp))
            Text("Scan music")
        }
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.padding(start = 4.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(20.dp)) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsEntry(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    action: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(action, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    }
}

private fun formatDuration(durationMs: Long): String {
    val seconds = (durationMs / 1000L).coerceAtLeast(0L)
    return "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"
}

