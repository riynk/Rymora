package com.riynk.rymora.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val playback by playbackViewModel.state.collectAsStateWithLifecycle()
    val tracks by playbackViewModel.tracks.collectAsStateWithLifecycle()

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

    val isNowPlaying = currentRoute == Routes.NOW_PLAYING
    val selectedTab = tabs.firstOrNull { it.route == currentRoute }?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (!isNowPlaying) {
                Column {
                    if (playback.hasTrack) {
                        MiniPlayer(
                            title = playback.title,
                            artist = playback.artist,
                            isPlaying = playback.isPlaying,
                            onClick = { navController.navigate(Routes.NOW_PLAYING) },
                            onPlayPause = playbackViewModel::togglePlayPause,
                            onNext = playbackViewModel::skipNext
                        )
                    }
                    NavigationBar {
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
                    Text("Your music, your atmosphere.", modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = { scanMusic(); navController.navigate(Routes.LIBRARY) }, modifier = Modifier.padding(top = 24.dp)) {
                        Text("Scan local music")
                    }
                    Text("${tracks.size} songs found", modifier = Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            composable(Routes.LIBRARY) {
                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    Text("Library", style = MaterialTheme.typography.headlineMedium)
                    Button(onClick = ::scanMusic, modifier = Modifier.padding(vertical = 12.dp)) { Text("Refresh library") }
                    if (tracks.isEmpty()) {
                        Text("No songs found. Allow audio access, then scan your device.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        LazyColumn {
                            items(tracks, key = { it.id }) { track ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth().clickable { playbackViewModel.playTrack(track) }.padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Column(Modifier.padding(14.dp)) {
                                        Text(track.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                                        Text(track.artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            composable(Routes.SEARCH) { PlaceholderScreen("Search", "Search your local music collection.") }
            composable(Routes.PLAYLISTS) { PlaceholderScreen("Playlist", "Playlists are coming next.") }
            composable(Routes.SETTINGS) { PlaceholderScreen("Settings", "Make Rymora yours.") }
            composable(Routes.NOW_PLAYING) {
                NowPlayingScreen(
                    state = playback,
                    onBack = { navController.popBackStack() },
                    onPlayPause = playbackViewModel::togglePlayPause,
                    onNext = playbackViewModel::skipNext,
                    onPrevious = playbackViewModel::skipPrevious,
                    onSeek = playbackViewModel::seekTo
                )
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
