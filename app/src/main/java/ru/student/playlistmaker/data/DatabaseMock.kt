package ru.student.playlistmaker.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.student.playlistmaker.domain.Playlist
import ru.student.playlistmaker.domain.Track

internal class DatabaseMock(private val catalog: TrackStorage) {
    private val tracks = catalog.tracks.associate { it.id to it.toTrack() }
    private var nextId = 3L
    private val links = mutableMapOf<Long, LinkedHashSet<Int>>()
    private val playlistState = MutableStateFlow(listOf(
        Playlist(1, "Best songs 2021", "My favorite tracks"),
        Playlist(2, "Summer Party", "Beach vibes"),
    ))
    private val favoriteState = MutableStateFlow(setOf<Int>())
    private val historyState = MutableStateFlow(listOf("Rammstein", "Queen", "Bruno Mars"))

    val playlists = playlistState.asStateFlow()
    val favorites = favoriteState.asStateFlow()
    val history = historyState.asStateFlow()

    @Synchronized
    fun create(name: String, description: String): Long? {
        val title = name.trim()
        if (title.isEmpty()) return null
        val id = nextId++
        playlistState.value += Playlist(id, title, description.trim())
        return id
    }

    @Synchronized
    fun delete(id: Long) {
        links.remove(id)
        playlistState.value = playlistState.value.filterNot { it.id == id }
    }

    @Synchronized
    fun addTrack(playlistId: Long, trackId: Int): Boolean {
        if (trackId !in tracks || playlistState.value.none { it.id == playlistId }) return false
        if (!links.getOrPut(playlistId) { linkedSetOf() }.add(trackId)) return false
        publishTracks(playlistId)
        return true
    }

    @Synchronized
    fun removeTrack(playlistId: Long, trackId: Int) {
        if (links[playlistId]?.remove(trackId) == true) publishTracks(playlistId)
    }

    private fun publishTracks(id: Long) {
        val members = links[id].orEmpty().mapNotNull(tracks::get)
        playlistState.value = playlistState.value.map { if (it.id == id) it.copy(tracks = members) else it }
    }

    fun toggleFavorite(trackId: Int) {
        if (trackId in tracks) favoriteState.update { if (trackId in it) it - trackId else it + trackId }
    }

    fun record(query: String) {
        val term = query.trim()
        if (term.isNotEmpty()) historyState.update { items ->
            (listOf(term) + items.filterNot { it.equals(term, ignoreCase = true) }).take(10)
        }
    }
}
