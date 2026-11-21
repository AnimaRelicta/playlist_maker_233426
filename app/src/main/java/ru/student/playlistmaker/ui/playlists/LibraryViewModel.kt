package ru.student.playlistmaker.ui.playlists

import androidx.lifecycle.ViewModel
import ru.student.playlistmaker.domain.PlaylistsRepository
import ru.student.playlistmaker.domain.TracksRepository

class LibraryViewModel(
    private val playlistsRepository: PlaylistsRepository,
    private val tracksRepository: TracksRepository,
) : ViewModel() {
    val playlists = playlistsRepository.playlists
    val favoriteIds = tracksRepository.favoriteIds

    fun track(id: Int) = tracksRepository.findById(id)
    fun create(name: String, description: String) = playlistsRepository.create(name, description)
    fun addTrack(playlistId: Long, trackId: Int) = playlistsRepository.addTrack(playlistId, trackId)
    fun toggleFavorite(trackId: Int) = tracksRepository.toggleFavorite(trackId)
}
