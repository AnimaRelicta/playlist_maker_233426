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
    suspend fun create(name: String, description: String) = playlistsRepository.create(name, description)
    suspend fun addTrack(playlistId: Long, trackId: Long) = playlistsRepository.addTrack(playlistId, trackId)

    fun toggleFavorite(trackId: Long) {
        viewModelScope.launch { tracksRepository.toggleFavorite(trackId) }
    }
}
