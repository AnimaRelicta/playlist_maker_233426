package ru.student.playlistmaker.ui.playlists

import androidx.lifecycle.ViewModel
import ru.student.playlistmaker.domain.PlaylistsRepository

class PlaylistViewModel(repository: PlaylistsRepository, playlistId: Long) : ViewModel() {
    val playlist = repository.getPlaylist(playlistId)
}
