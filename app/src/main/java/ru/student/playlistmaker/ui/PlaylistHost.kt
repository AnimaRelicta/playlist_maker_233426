package ru.student.playlistmaker.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.student.playlistmaker.ui.playlists.LibraryViewModel
import ru.student.playlistmaker.ui.playlists.NewPlaylistScreen
import ru.student.playlistmaker.ui.playlists.PlaylistsScreen
import ru.student.playlistmaker.ui.playlists.PlaylistDetailsScreen
import org.koin.androidx.compose.koinViewModel
import ru.student.playlistmaker.domain.Track
import ru.student.playlistmaker.ui.tracks.TrackDetailsScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ru.student.playlistmaker.R
import ru.student.playlistmaker.ui.common.PlaceholderScreen
import ru.student.playlistmaker.ui.main.MainScreen
import ru.student.playlistmaker.ui.search.SearchScreen
import ru.student.playlistmaker.ui.settings.SettingsScreen

enum class PlaylistScreen { MAIN, SEARCH, SETTINGS, PLAYLISTS, FAVORITES, DETAILS, NEW_PLAYLIST, PLAYLIST_DETAILS }

@Composable
fun PlaylistHost(navController: NavHostController, dark: Boolean, onThemeChange: (Boolean) -> Unit) {
    fun open(screen: PlaylistScreen) {
        navController.navigate(screen.name) { launchSingleTop = true }
    }
    fun openTrack(track: Track) {
        navController.navigate("${PlaylistScreen.DETAILS.name}/${track.id}") { launchSingleTop = true }
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
            SearchScreen(koinViewModel(), back, ::openTrack)
        }
        composable("${PlaylistScreen.DETAILS.name}/{trackId}",
            arguments = listOf(navArgument("trackId") { type = NavType.IntType })) { entry ->
            val vm: LibraryViewModel = koinViewModel()
            val track = entry.arguments?.getInt("trackId")?.let(vm::track)
            val favorites by vm.favoriteIds.collectAsStateWithLifecycle(emptySet())
            val playlists by vm.playlists.collectAsStateWithLifecycle()
            TrackDetailsScreen(track, back, track?.id in favorites, playlists,
                onFavorite = { track?.let { vm.toggleFavorite(it.id) } },
                onAdd = { id -> track?.let { vm.addTrack(id, it.id) } == true })
        }
        composable(PlaylistScreen.SETTINGS.name) { SettingsScreen(dark, onThemeChange, back) }
        composable(PlaylistScreen.PLAYLISTS.name) {
            val vm: LibraryViewModel = koinViewModel()
            val playlists by vm.playlists.collectAsStateWithLifecycle()
            PlaylistsScreen(playlists, back, { open(PlaylistScreen.NEW_PLAYLIST) }) { id ->
                navController.navigate("${PlaylistScreen.PLAYLIST_DETAILS.name}/$id")
            }
        }
        composable(PlaylistScreen.NEW_PLAYLIST.name) {
            val vm: LibraryViewModel = koinViewModel()
            NewPlaylistScreen(back) { name, description -> if (vm.create(name, description) != null) back() }
        }
        composable("${PlaylistScreen.PLAYLIST_DETAILS.name}/{playlistId}",
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType })) { entry ->
            val vm: LibraryViewModel = koinViewModel()
            val playlists by vm.playlists.collectAsStateWithLifecycle()
            val id = entry.arguments?.getLong("playlistId")
            PlaylistDetailsScreen(playlists.firstOrNull { it.id == id }, back, ::openTrack)
        }
        composable(PlaylistScreen.FAVORITES.name) { PlaceholderScreen(R.string.favorites, "favorites_screen", back) }
    }
}
