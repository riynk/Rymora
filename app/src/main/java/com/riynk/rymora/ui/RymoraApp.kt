package com.riynk.rymora.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.zIndex
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
import com.riynk.rymora.ui.screens.HomeScreen
import com.riynk.rymora.ui.screens.LibraryScreen
import com.riynk.rymora.ui.screens.NowPlayingScreen
import com.riynk.rymora.ui.screens.SearchScreen
import com.riynk.rymora.ui.screens.SettingsScreen

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
    val playback by playbackViewModel.state.collectAsStateWithLifecycle()
    val tracks by playbackViewModel.tracks.collectAsStateWithLifecycle()
    var playerExpanded by remember { mutableStateOf(false) }
    var playerDragging by remember { mutableStateOf(false) }
    var dragExpansion by remember { mutableStateOf(0f) }
    val animatedExpansion by animateFloatAsState(targetValue = if (playerExpanded) 1f else 0f, animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessLow), label = "player expansion")
    val expansion = if (playerDragging) dragExpansion else animatedExpansion

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

    LaunchedEffect(audioPermission) {
        if (ContextCompat.checkSelfPermission(context, audioPermission) == PackageManager.PERMISSION_GRANTED) {
            playbackViewModel.scanLocalAudio()
        } else {
            permissionLauncher.launch(audioPermission)
        }
    }

    BackHandler(enabled = playerExpanded) {
        playerExpanded = false
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                Column {
                    if (playback.hasTrack && !playerExpanded) {
                        MiniPlayer(
                            title = playback.title,
                            artist = playback.artist,
                            artworkUri = playback.artworkUri,
                            isPlaying = playback.isPlaying,
                            progress = if (playback.durationMs > 0L) {
                                playback.positionMs.toFloat() / playback.durationMs.toFloat()
                            } else 0f,
                            onClick = { playerExpanded = true },
                            onPlayPause = playbackViewModel::togglePlayPause,
                            onNext = playbackViewModel::skipNext,
                            expansionProgress = expansion,
                            onDragProgress = { progress -> playerDragging = true; dragExpansion = progress },
                            onDragEnd = { playerExpanded = dragExpansion > 0.38f; playerDragging = false }
                        )
                    }
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp
                    ) {
                        mainTabs.forEach { tab ->
                            NavigationBarItem(
                                selected = currentRoute == tab.route,
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
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        tracks = tracks,
                        onPlayTrack = playbackViewModel::playTrack,
                        onPlayAll = {
                            if (!playback.shuffleEnabled) playbackViewModel.toggleShuffle()
                            tracks.firstOrNull()?.let(playbackViewModel::playTrack)
                        },
                        onOpenLibrary = {
                            navController.navigate(Routes.LIBRARY) { launchSingleTop = true }
                        },
                        onOpenSettings = {
                            navController.navigate(Routes.SETTINGS) { launchSingleTop = true }
                        },
                        onScan = ::scanMusic
                    )
                }
                composable(Routes.SEARCH) {
                    SearchScreen(
                        tracks = tracks,
                        onPlayTrack = playbackViewModel::playTrack
                    )
                }
                composable(Routes.LIBRARY) {
                    LibraryScreen(
                        tracks = tracks,
                        onPlayTrack = playbackViewModel::playTrack,
                        onScan = ::scanMusic
                    )
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        onScan = ::scanMusic
                    )
                }
            }
        }

        if (expansion > 0f || playerExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f)
                    .graphicsLayer {
                        alpha = expansion
                        scaleX = 0.92f + 0.08f * expansion
                        scaleY = 0.90f + 0.10f * expansion
                        translationY = size.height * (1f - expansion) * 0.12f
                    }
                    .clip(RoundedCornerShape((30f * (1f - expansion)).dp))
            ) {
                NowPlayingScreen(
                    state = playback,
                    tracks = tracks,
                    onCollapse = { playerExpanded = false },
                    onPlayPause = playbackViewModel::togglePlayPause,
                    onNext = playbackViewModel::skipNext,
                    onPrevious = playbackViewModel::skipPrevious,
                    onSeek = playbackViewModel::seekTo,
                    onToggleShuffle = playbackViewModel::toggleShuffle,
                    onCycleRepeat = playbackViewModel::cycleRepeatMode,
                    onSelectTrack = playbackViewModel::playTrack,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
