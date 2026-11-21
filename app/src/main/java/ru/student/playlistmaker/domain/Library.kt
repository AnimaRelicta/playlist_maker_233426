package ru.student.playlistmaker.domain

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged

data class Playlist(
    val id: Long,
    val name: String,
    val description: String,
    val coverImageUri: String? = null,
    val tracks: List<Track> = emptyList(),
)

interface PlaylistsRepository {
    val playlists: StateFlow<List<Playlist>>
    fun getPlaylist(id: Long): kotlinx.coroutines.flow.Flow<Playlist?> =
        playlists.map { items -> items.firstOrNull { it.id == id } }.distinctUntilChanged()
    fun create(name: String, description: String): Long?
    fun delete(id: Long)
    fun addTrack(playlistId: Long, trackId: Int): Boolean
    fun removeTrack(playlistId: Long, trackId: Int)
}

interface SearchHistoryRepository {
    val queries: StateFlow<List<String>>
    fun record(query: String)
}
