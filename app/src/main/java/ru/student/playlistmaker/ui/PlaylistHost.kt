package ru.student.playlistmaker.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import org.koin.core.parameter.parametersOf
import ru.student.playlistmaker.ui.playlists.PlaylistViewModel
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
import ru.student.playlistmaker.BuildConfig
import ru.student.playlistmaker.ui.playlists.FavoritesScreen
import ru.student.playlistmaker.ui.main.MainScreen
import ru.student.playlistmaker.ui.search.SearchScreen
import ru.student.playlistmaker.ui.settings.SettingsScreen

enum class PlaylistScreen { MAIN, SEARCH, SETTINGS, PLAYLISTS, FAVORITES, DETAILS, NEW_PLAYLIST, PLAYLIST_DETAILS }

enum class FinalVariant { DELETE_PLAYLIST, REMOVE_TRACK, MERGE_PLAYLISTS }

@Composable
fun PlaylistHost(
    navController: NavHostController,
    dark: Boolean,
    finalVariant: FinalVariant = FinalVariant.valueOf(BuildConfig.FINAL_VARIANT),
    onThemeChange: (Boolean) -> Unit,
) {
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
            arguments = listOf(navArgument("trackId") { type = NavType.LongType })) { entry ->
            val vm: LibraryViewModel = koinViewModel()
            val trackId = requireNotNull(entry.arguments).getLong("trackId")
            val track by produceState<Track?>(null, trackId) { value = vm.track(trackId) }
            val favorites by vm.favoriteIds.collectAsStateWithLifecycle(emptySet())
            val playlists by vm.playlists.collectAsStateWithLifecycle(emptyList())
            TrackDetailsScreen(track, back, track?.id in favorites, playlists,
                onFavorite = { track?.let { vm.toggleFavorite(it.id) } },
                onAdd = { id -> track?.let { vm.addTrack(id, it.id) } == true })
        }
        composable(PlaylistScreen.SETTINGS.name) { SettingsScreen(dark, onThemeChange, back) }
        composable(PlaylistScreen.PLAYLISTS.name) {
            val vm: LibraryViewModel = koinViewModel()
            val playlists by vm.playlists.collectAsStateWithLifecycle(emptyList())
            PlaylistsScreen(
                playlists, back, { open(PlaylistScreen.NEW_PLAYLIST) },
                onOpen = { id -> navController.navigate("${PlaylistScreen.PLAYLIST_DETAILS.name}/$id") },
                onDelete = (vm::deletePlaylist).takeIf { finalVariant == FinalVariant.DELETE_PLAYLIST },
                onMerge = (vm::mergePlaylists).takeIf { finalVariant == FinalVariant.MERGE_PLAYLISTS },
            )
        }
        composable(PlaylistScreen.NEW_PLAYLIST.name) {
            val vm: LibraryViewModel = koinViewModel()
            NewPlaylistScreen(back) { name, description, cover -> vm.create(name, description, cover) != null }
        }
        composable("${PlaylistScreen.PLAYLIST_DETAILS.name}/{playlistId}",
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType })) { entry ->
            val id = requireNotNull(entry.arguments).getLong("playlistId")
            val vm: PlaylistViewModel = koinViewModel { parametersOf(id) }
            val playlist by vm.playlist.collectAsStateWithLifecycle(null)
            PlaylistDetailsScreen(
                playlist, back, ::openTrack,
                onDelete = (vm::delete).takeIf { finalVariant == FinalVariant.DELETE_PLAYLIST },
                onRemoveTrack = (vm::removeTrack).takeIf { finalVariant == FinalVariant.REMOVE_TRACK },
            )
        }
        composable(PlaylistScreen.FAVORITES.name) {
            val vm: LibraryViewModel = koinViewModel()
            val favorites by vm.favorites.collectAsStateWithLifecycle(emptyList())
            FavoritesScreen(favorites, back, ::openTrack, vm::removeFavorite)
        }
    }
}
