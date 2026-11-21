package ru.student.playlistmaker.ui.playlists

import androidx.lifecycle.ViewModel
import ru.student.playlistmaker.domain.PlaylistsRepository

class PlaylistViewModel(private val repository: PlaylistsRepository, private val playlistId: Long) : ViewModel() {
    val playlist = repository.getPlaylist(playlistId)

    suspend fun delete() = repository.delete(playlistId)
    suspend fun removeTrack(trackId: Long) = repository.removeTrack(playlistId, trackId)
}
