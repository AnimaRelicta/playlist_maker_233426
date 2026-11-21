package ru.student.playlistmaker.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import org.koin.compose.koinInject
import org.koin.androidx.compose.koinViewModel
import ru.student.playlistmaker.domain.TracksRepository
import ru.student.playlistmaker.ui.tracks.TrackDetailsScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ru.student.playlistmaker.R
import ru.student.playlistmaker.ui.common.PlaceholderScreen
import ru.student.playlistmaker.ui.main.MainScreen
import ru.student.playlistmaker.ui.search.SearchScreen
import ru.student.playlistmaker.ui.settings.SettingsScreen

enum class PlaylistScreen { MAIN, SEARCH, SETTINGS, PLAYLISTS, FAVORITES, DETAILS }

@Composable
fun PlaylistHost(navController: NavHostController, dark: Boolean, onThemeChange: (Boolean) -> Unit) {
    fun open(screen: PlaylistScreen) {
        navController.navigate(screen.name) { launchSingleTop = true }
    }
    val back: () -> Unit = { navController.popBackStack() }
    NavHost(navController, startDestination = PlaylistScreen.MAIN.name) {
        composable(PlaylistScreen.MAIN.name) {
            MainScreen(
                onSearch = { open(PlaylistScreen.SEARCH) },
                onPlaylists = { open(PlaylistScreen.PLAYLISTS) },
                onFavorites = { open(PlaylistScreen.FAVORITES) },
                onSettings = { open(PlaylistScreen.SETTINGS) },
            )
        }
        composable(PlaylistScreen.SEARCH.name) {
            SearchScreen(koinViewModel(), back) { track ->
                navController.navigate("${PlaylistScreen.DETAILS.name}/${track.id}")
            }
        }
        composable("${PlaylistScreen.DETAILS.name}/{trackId}",
            arguments = listOf(navArgument("trackId") { type = NavType.IntType })) { entry ->
            val repository = koinInject<TracksRepository>()
            val track = entry.arguments?.getInt("trackId")?.let(repository::findById)
            TrackDetailsScreen(track, back)
        }
        composable(PlaylistScreen.SETTINGS.name) { SettingsScreen(dark, onThemeChange, back) }
        composable(PlaylistScreen.PLAYLISTS.name) { PlaceholderScreen(R.string.playlists, "playlists_screen", back) }
        composable(PlaylistScreen.FAVORITES.name) { PlaceholderScreen(R.string.favorites, "favorites_screen", back) }
    }
}
