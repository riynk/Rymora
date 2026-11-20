package com.riynk.rymora.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.riynk.rymora.playback.LocalTrack
import com.riynk.rymora.playback.PlaybackViewModel
import com.riynk.rymora.ui.components.MiniPlayer
import com.riynk.rymora.ui.navigation.Routes
import com.riynk.rymora.ui.screens.NowPlayingScreen
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private data class MainTab(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val mainTabs = listOf(
    MainTab(Routes.HOME, "Home", Icons.Rounded.Home),
    MainTab(Routes.SEARCH, "Search", Icons.Rounded.Search),
    MainTab(Routes.LIBRARY, "Library", Icons.Rounded.LibraryMusic)
)

@Composable
fun RymoraApp(playbackViewModel: PlaybackViewModel = viewModel()) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val playback by playbackViewModel.state.collectAsStateWithLifecycle()
    val tracks by playbackViewModel.tracks.collectAsStateWithLifecycle()

    var playerProgress by remember { mutableFloatStateOf(0f) }
    var navigationBarHeightPx by remember { mutableIntStateOf(0) }
    var settleJob by remember { mutableStateOf<Job?>(null) }

    fun settlePlayer(target: Float) {
        settleJob?.cancel()
        settleJob = scope.launch {
            animate(
                initialValue = playerProgress,
                targetValue = target,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) { value, _ ->
                playerProgress = value.coerceIn(0f, 1f)
            }
        }
    }

    BackHandler(enabled = playerProgress > 0.001f) {
        settlePlayer(0f)
    }

    val audioPermission = if (Build.VERSION.SDK_INT >= 33) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) playbackViewModel.scanLocalAudio()
    }

    fun scanMusic() {
        if (ContextCompat.checkSelfPermission(context, audioPermission) == PackageManager.PERMISSION_GRANTED) {
            playbackViewModel.scanLocalAudio()
        } else {
            permissionLauncher.launch(audioPermission)
        }
    }

    val selectedTab = mainTabs.firstOrNull { it.route == currentRoute }?.route

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val rootHeightPx = with(density) { maxHeight.toPx() }
        val collapsedHeight = 84.dp
        val collapsedHeightPx = with(density) { collapsedHeight.toPx() }
        val navigationHeightPx = if (navigationBarHeightPx > 0) {
            navigationBarHeightPx.toFloat()
        } else {
            with(density) { 80.dp.toPx() }
        }
        val latestProgress = rememberUpdatedState(playerProgress)
        val latestSettle = rememberUpdatedState<(Float) -> Unit>(
            newValue = { target -> settlePlayer(target) }
        )
        val panelNestedScroll = remember(rootHeightPx) {
            object : NestedScrollConnection {
                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource
                ): Offset {
                    val progress = latestProgress.value
                    if (available.y <= 0f || progress <= 0f) return Offset.Zero

                    val consumedY = minOf(available.y, progress * rootHeightPx)
                    playerProgress = (progress - consumedY / rootHeightPx.coerceAtLeast(1f))
                        .coerceIn(0f, 1f)
                    return Offset(0f, consumedY)
                }

                override suspend fun onPostFling(
                    consumed: Velocity,
                    available: Velocity
                ): Velocity {
                    val progress = latestProgress.value
                    if (progress > 0f && progress < 1f) {
                        val collapse = available.y > 650f || progress < 0.42f
                        latestSettle.value(if (collapse) 0f else 1f)
                    }
                    return Velocity.Zero
                }
            }
        }
        val miniPlayerDragState = rememberDraggableState { delta ->
            playerProgress = (playerProgress - delta / rootHeightPx.coerceAtLeast(1f))
                .coerceIn(0f, 1f)
        }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.onSizeChanged { navigationBarHeightPx = it.height },
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    mainTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                modifier = Modifier
                    .padding(padding)
                    .padding(bottom = if (playback.hasTrack) 88.dp else 0.dp)
            ) {
                composable(Routes.HOME) {
                    HomeDestination(
                        tracks = tracks,
                        onPlayTrack = playbackViewModel::playTrack,
                        onScan = ::scanMusic,
                        onOpenLibrary = {
                            navController.navigate(Routes.LIBRARY) {
                                launchSingleTop = true
                            }
                        },
                        onOpenSettings = {
                            navController.navigate(Routes.SETTINGS) { launchSingleTop = true }
                        }
                    )
                }
                composable(Routes.SEARCH) {
                    SearchDestination(
                        tracks = tracks,
                        onPlayTrack = playbackViewModel::playTrack,
                        onOpenSettings = {
                            navController.navigate(Routes.SETTINGS) { launchSingleTop = true }
                        }
                    )
                }
                composable(Routes.LIBRARY) {
                    LibraryDestination(
                        tracks = tracks,
                        onPlayTrack = playbackViewModel::playTrack,
                        onScan = ::scanMusic,
                        onOpenSettings = {
                            navController.navigate(Routes.SETTINGS) { launchSingleTop = true }
                        }
                    )
                }
                composable(Routes.SETTINGS) {
                    SettingsDestination(
                        onBack = { navController.popBackStack() },
                        onScan = ::scanMusic
                    )
                }
            }
        }

        if (playback.hasTrack) {
            val sheetHeightPx = collapsedHeightPx +
                (rootHeightPx - collapsedHeightPx).coerceAtLeast(0f) * playerProgress
            val sheetTopPx = (rootHeightPx - navigationHeightPx - collapsedHeightPx)
                .coerceAtLeast(0f) * (1f - playerProgress)
            val sheetColor = lerp(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.background,
                playerProgress
            )
            Box(Modifier.fillMaxSize().clipToBounds()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(with(density) { sheetHeightPx.toDp() })
                        .offset { IntOffset(0, sheetTopPx.roundToInt()) }
                        .nestedScroll(panelNestedScroll),
                    shape = RoundedCornerShape(
                        topStart = 18.dp * (1f - playerProgress),
                        topEnd = 18.dp * (1f - playerProgress)
                    ),
                    color = sheetColor,
                    tonalElevation = 4.dp
                ) {
                    Box(Modifier.fillMaxSize()) {
                        if (playerProgress > 0.005f) {
                            NowPlayingScreen(
                                state = playback,
                                tracks = tracks,
                                onBack = { settlePlayer(0f) },
                                onPlayPause = playbackViewModel::togglePlayPause,
                                onNext = playbackViewModel::skipNext,
                                onPrevious = playbackViewModel::skipPrevious,
                                onSeek = playbackViewModel::seekTo,
                                onToggleShuffle = playbackViewModel::toggleShuffle,
                                onCycleRepeat = playbackViewModel::cycleRepeatMode,
                                onSelectTrack = playbackViewModel::playTrack,
                                modifier = Modifier.fillMaxSize().graphicsLayer {
                                    alpha = playerProgress
                                }
                            )
                        }

                        if (playerProgress < 0.995f) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .draggable(
                                        state = miniPlayerDragState,
                                        orientation = Orientation.Vertical,
                                        onDragStarted = { settleJob?.cancel() },
                                        onDragStopped = { velocity ->
                                            val expand = when {
                                                velocity < -1000f -> true
                                                velocity > 1000f -> false
                                                else -> playerProgress >= 0.42f
                                            }
                                            settlePlayer(if (expand) 1f else 0f)
                                        }
                                    )
                                    .graphicsLayer {
                                        alpha = 1f - playerProgress
                                        scaleX = 1f - (0.025f * playerProgress)
                                        scaleY = 1f - (0.025f * playerProgress)
                                    }
                            ) {
                                MiniPlayer(
                                    title = playback.title,
                                    artist = playback.artist,
                                    artworkUri = playback.artworkUri,
                                    isPlaying = playback.isPlaying,
                                    progress = if (playback.durationMs > 0L) {
                                        playback.positionMs.toFloat() / playback.durationMs.toFloat()
                                    } else 0f,
                                    onClick = { settlePlayer(1f) },
                                    onPlayPause = playbackViewModel::togglePlayPause,
                                    onNext = playbackViewModel::skipNext
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeDestination(
    tracks: List<LocalTrack>,
    onPlayTrack: (LocalTrack) -> Unit,
    onScan: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Rymora",
            subtitle = "Music on this device",
            onAction = onOpenSettings
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Your music, your atmosphere.", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            tracks.size.toString() + " songs found on this device",
                            modifier = Modifier.padding(top = 6.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.padding(top = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(onClick = onOpenLibrary) { Text("Open library") }
                            Button(onClick = onScan) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text(" Scan")
                            }
                        }
                    }
                }
            }
            item {
                SectionTitle("Your songs", tracks.size.toString() + " tracks")
            }
            if (tracks.isEmpty()) {
                item {
                    EmptyMessage(
                        title = "Your library is empty",
                        message = "Scan this device to find local music."
                    )
                }
            } else {
                items(tracks.take(12), key = { it.id }) { track ->
                    TrackRow(track = track, onClick = { onPlayTrack(track) })
                }
            }
            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun SearchDestination(
    tracks: List<LocalTrack>,
    onPlayTrack: (LocalTrack) -> Unit,
    onOpenSettings: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val results = remember(query, tracks) {
        val normalized = query.trim()
        if (normalized.isEmpty()) emptyList()
        else tracks.filter {
            it.title.contains(normalized, ignoreCase = true) ||
                it.artist.contains(normalized, ignoreCase = true) ||
                it.album.contains(normalized, ignoreCase = true)
        }
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Search", "Find songs in your library", onOpenSettings)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            placeholder = { Text("Songs, artists, albums") }
        )
        if (query.isBlank()) {
            EmptyMessage(
                title = "Search your music",
                message = "Search by song title, artist, or album."
            )
        } else if (results.isEmpty()) {
            EmptyMessage(
                title = "No results",
                message = "Try another song, artist, or album name."
            )
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                item { SectionTitle("Results", results.size.toString() + " matches") }
                items(results, key = { it.id }) { track ->
                    TrackRow(track = track, onClick = { onPlayTrack(track) })
                }
            }
        }
    }
}

@Composable
private fun LibraryDestination(
    tracks: List<LocalTrack>,
    onPlayTrack: (LocalTrack) -> Unit,
    onScan: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var category by remember { mutableStateOf("Songs") }
    val categories = listOf("Songs", "Albums", "Artists", "Playlists")

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Library", "Your local collection", onOpenSettings)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { item ->
                FilterChip(
                    selected = category == item,
                    onClick = { category = item },
                    label = { Text(item) }
                )
            }
        }

        when (category) {
            "Songs" -> {
                if (tracks.isEmpty()) {
                    EmptyMessage(
                        title = "No songs yet",
                        message = "Scan your device to add local audio to Rymora."
                    )
                    Button(onClick = onScan, modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text("Scan music")
                    }
                } else {
                    LazyColumn(Modifier.fillMaxSize()) {
                        item { SectionTitle("All songs", tracks.size.toString() + " tracks") }
                        items(tracks, key = { it.id }) { track ->
                            TrackRow(track = track, onClick = { onPlayTrack(track) })
                        }
                    }
                }
            }
            "Albums" -> {
                val groups = tracks.groupBy { it.album.ifBlank { "Unknown album" } }.toList()
                    .sortedBy { it.first.lowercase() }
                if (groups.isEmpty()) {
                    EmptyMessage("No albums", "Albums from your device will appear here.")
                } else {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(groups, key = { it.first }) { group ->
                            CollectionRow(
                                title = group.first,
                                subtitle = group.second.firstOrNull()?.artist.orEmpty(),
                                count = group.second.size,
                                icon = Icons.Rounded.Album,
                                onClick = { group.second.firstOrNull()?.let(onPlayTrack) }
                            )
                        }
                    }
                }
            }
            "Artists" -> {
                val groups = tracks.groupBy { it.artist.ifBlank { "Unknown artist" } }.toList()
                    .sortedBy { it.first.lowercase() }
                if (groups.isEmpty()) {
                    EmptyMessage("No artists", "Artists from your device will appear here.")
                } else {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(groups, key = { it.first }) { group ->
                            CollectionRow(
                                title = group.first,
                                subtitle = "Artist",
                                count = group.second.size,
                                icon = Icons.Rounded.Person,
                                onClick = { group.second.firstOrNull()?.let(onPlayTrack) }
                            )
                        }
                    }
                }
            }
            else -> EmptyMessage(
                title = "No playlists yet",
                message = "Your playlists will appear here when playlist management is added."
            )
        }
    }
}

@Composable
private fun SettingsDestination(
    onBack: () -> Unit,
    onScan: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
            }
            Column(Modifier.padding(start = 8.dp)) {
                Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Rymora", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable(onClick = onScan),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Refresh, contentDescription = null)
                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                    Text("Scan device", fontWeight = FontWeight.SemiBold)
                    Text("Refresh your local music library", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Text(
            "Rymora • Local music player",
            modifier = Modifier.padding(20.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ScreenHeader(
    title: String,
    subtitle: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 14.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onAction) {
            Icon(Icons.Rounded.Settings, contentDescription = "Settings")
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TrackRow(track: LocalTrack, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                if (track.artworkUri != null) {
                    AsyncImage(
                        model = track.artworkUri,
                        contentDescription = "Album artwork",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Column(Modifier.weight(1f)) {
                Text(track.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, fontWeight = FontWeight.Medium)
                Text(
                    track.artist + " • " + track.album,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Text(
                formatTrackDuration(track.durationMs),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CollectionRow(
    title: String,
    subtitle: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(count.toString() + " songs", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyMessage(title: String, message: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            message,
            modifier = Modifier.padding(top = 6.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatTrackDuration(milliseconds: Long): String {
    val seconds = (milliseconds / 1000L).coerceAtLeast(0L)
    return (seconds / 60L).toString() + ":" + (seconds % 60L).toString().padStart(2, '0')
}
