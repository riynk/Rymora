package com.riynk.rymora.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.riynk.rymora.ui.components.MiniPlayer
import com.riynk.rymora.ui.navigation.Routes
import com.riynk.rymora.ui.screens.NowPlayingScreen
import com.riynk.rymora.ui.screens.PlaceholderScreen

private data class TabItem(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    TabItem(Routes.HOME, "Home", Icons.Rounded.Home),
    TabItem(Routes.LIBRARY, "Library", Icons.Rounded.LibraryMusic),
    TabItem(Routes.SEARCH, "Search", Icons.Rounded.Search),
    TabItem(Routes.PLAYLISTS, "Playlist", Icons.Rounded.PlaylistPlay),
    TabItem(Routes.SETTINGS, "Settings", Icons.Rounded.Settings)
)

@Composable
fun RymoraApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    var hasTrack by remember { mutableStateOf(false) }

    val isNowPlaying = currentRoute == Routes.NOW_PLAYING
    val selectedTab = tabs.firstOrNull { it.route == currentRoute }?.route

    Scaffold(
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        bottomBar = {
            if (!isNowPlaying) {
                Column {
                    if (hasTrack) {
                        MiniPlayer(
                            title = "Playing track",
                            artist = "Artist",
                            onClick = { navController.navigate(Routes.NOW_PLAYING) }
                        )
                    }
                    NavigationBar {
                        tabs.forEach { tab ->
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
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                PlaceholderScreen(
                    title = "Rymora",
                    subtitle = "Your music, your atmosphere.",
                    actionLabel = "Preview Mini Player",
                    onAction = { hasTrack = !hasTrack }
                )
            }
            composable(Routes.LIBRARY) {
                PlaceholderScreen("Library", "Your local music collection.")
            }
            composable(Routes.SEARCH) {
                PlaceholderScreen("Search", "Find tracks, albums, and artists.")
            }
            composable(Routes.PLAYLISTS) {
                PlaceholderScreen("Playlist", "Your playlists will live here.")
            }
            composable(Routes.SETTINGS) {
                PlaceholderScreen("Settings", "Make Rymora yours.")
            }
            composable(Routes.NOW_PLAYING) {
                NowPlayingScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = androidx.compose.material3.MaterialTheme.typography.headlineLarge)
        Text(
            subtitle,
            modifier = Modifier.padding(top = 8.dp),
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (actionLabel != null && onAction != null) {
            androidx.compose.material3.Button(
                onClick = onAction,
                modifier = Modifier.padding(top = 24.dp)
            ) { Text(actionLabel) }
        }
    }
}
