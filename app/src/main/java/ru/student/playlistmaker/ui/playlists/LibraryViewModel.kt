package ru.student.playlistmaker.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.student.playlistmaker.domain.PlaylistsRepository
import ru.student.playlistmaker.domain.TracksRepository

class LibraryViewModel(
    private val playlistsRepository: PlaylistsRepository,
    private val tracksRepository: TracksRepository,
) : ViewModel() {
    val playlists = playlistsRepository.playlists
    val favoriteIds = tracksRepository.favoriteIds
    val favorites = tracksRepository.favorites

    suspend fun track(id: Long) = tracksRepository.findById(id)
    suspend fun create(name: String, description: String, coverImageUri: String?) =
        playlistsRepository.create(name, description, coverImageUri)

    suspend fun deletePlaylist(id: Long) = playlistsRepository.delete(id)
    suspend fun mergePlaylists(sourceId: Long, targetId: Long) = playlistsRepository.merge(sourceId, targetId)

    fun removeFavorite(trackId: Long) {
        viewModelScope.launch { tracksRepository.removeFavorite(trackId) }
    }
    suspend fun addTrack(playlistId: Long, trackId: Long) = playlistsRepository.addTrack(playlistId, trackId)

    fun toggleFavorite(trackId: Long) {
        viewModelScope.launch { tracksRepository.toggleFavorite(trackId) }
    }
}
