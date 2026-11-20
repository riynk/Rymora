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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.onSizeChanged
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.riynk.rymora.playback.PlaybackViewModel
import com.riynk.rymora.ui.components.MiniPlayer
import com.riynk.rymora.ui.navigation.Routes
import com.riynk.rymora.ui.screens.NowPlayingScreen
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private data class TabItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val tabs = listOf(
    TabItem(Routes.HOME, "Home", Icons.Rounded.Home),
    TabItem(Routes.LIBRARY, "Library", Icons.Rounded.LibraryMusic),
    TabItem(Routes.SEARCH, "Search", Icons.Rounded.Search),
    TabItem(Routes.PLAYLISTS, "Playlist", Icons.Rounded.PlaylistPlay),
    TabItem(Routes.SETTINGS, "Settings", Icons.Rounded.Settings)
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

    val selectedTab = tabs.firstOrNull { it.route == currentRoute }?.route

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val rootHeightPx = with(density) { maxHeight.toPx() }
        val collapsedHeight = 84.dp
        val collapsedHeightPx = with(density) { collapsedHeight.toPx() }
        val navigationHeightPx = if (navigationBarHeightPx > 0) {
            navigationBarHeightPx.toFloat()
        } else {
            with(density) { 80.dp.toPx() }
        }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.onSizeChanged { navigationBarHeightPx = it.height }
                ) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
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
                modifier = Modifier.padding(padding)
            ) {
                composable(Routes.HOME) {
                    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
                        Text("RYMORA", style = MaterialTheme.typography.headlineLarge)
                        Text(
                            "Your music, your atmosphere.",
                            modifier = Modifier.padding(top = 8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { scanMusic(); navController.navigate(Routes.LIBRARY) },
                            modifier = Modifier.padding(top = 24.dp)
                        ) {
                            Text("Scan local music")
                        }
                        Text(
                            "${tracks.size} songs found",
                            modifier = Modifier.padding(top = 12.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                composable(Routes.LIBRARY) {
                    Column(Modifier.fillMaxSize().padding(16.dp)) {
                        Text("Library", style = MaterialTheme.typography.headlineMedium)
                        Button(onClick = ::scanMusic, modifier = Modifier.padding(vertical = 12.dp)) {
                            Text("Refresh library")
                        }
                        if (tracks.isEmpty()) {
                            Text(
                                "No songs found. Allow audio access, then scan your device.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            LazyColumn {
                                items(tracks, key = { it.id }) { track ->
                                    Surface(
                                        modifier = Modifier.fillMaxWidth()
                                            .clickable { playbackViewModel.playTrack(track) }
                                            .padding(vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = MaterialTheme.shapes.medium
                                    ) {
                                        Column(Modifier.padding(14.dp)) {
                                            Text(track.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                                            Text(
                                                track.artist,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                composable(Routes.SEARCH) {
                    PlaceholderScreen("Search", "Search your local music collection.")
                }
                composable(Routes.PLAYLISTS) {
                    PlaceholderScreen("Playlist", "Playlists are coming next.")
                }
                composable(Routes.SETTINGS) {
                    PlaceholderScreen("Settings", "Make Rymora yours.")
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
            val dragState = rememberDraggableState { delta ->
                playerProgress = (playerProgress - delta / rootHeightPx.coerceAtLeast(1f))
                    .coerceIn(0f, 1f)
            }

            Box(Modifier.fillMaxSize().clipToBounds()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(with(density) { sheetHeightPx.toDp() })
                        .offset { IntOffset(0, sheetTopPx.roundToInt()) }
                        .draggable(
                            state = dragState,
                            orientation = Orientation.Vertical,
                            onDragStarted = { settleJob?.cancel() },
                            onDragStopped = { velocity ->
                                val expand = when {
                                    velocity < -1100f -> true
                                    velocity > 1100f -> false
                                    else -> playerProgress >= 0.38f
                                }
                                settlePlayer(if (expand) 1f else 0f)
                            }
                        ),
                    shape = RoundedCornerShape(
                        topStart = 22.dp * (1f - playerProgress),
                        topEnd = 22.dp * (1f - playerProgress)
                    ),
                    color = sheetColor,
                    tonalElevation = 6.dp
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
                                    .graphicsLayer {
                                        alpha = 1f - playerProgress
                                        scaleX = 1f - (0.035f * playerProgress)
                                        scaleY = 1f - (0.035f * playerProgress)
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
private fun PlaceholderScreen(title: String, subtitle: String) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text(title, style = MaterialTheme.typography.headlineLarge)
        Text(subtitle, modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
