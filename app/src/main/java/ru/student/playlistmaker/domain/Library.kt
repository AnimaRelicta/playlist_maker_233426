package ru.student.playlistmaker.domain

import kotlinx.coroutines.flow.Flow
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
    val playlists: Flow<List<Playlist>>
    fun getPlaylist(id: Long): Flow<Playlist?> =
        playlists.map { items -> items.firstOrNull { it.id == id } }.distinctUntilChanged()
    suspend fun create(name: String, description: String, coverImageUri: String? = null): Long?
    suspend fun delete(id: Long)
    suspend fun merge(sourceId: Long, targetId: Long): Boolean
    suspend fun addTrack(playlistId: Long, trackId: Long): Boolean
    suspend fun removeTrack(playlistId: Long, trackId: Long)
}

interface SearchHistoryRepository {
    val queries: Flow<List<String>>
    suspend fun record(query: String)
}
