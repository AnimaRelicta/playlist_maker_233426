package ru.student.playlistmaker.domain

import java.util.Locale
import kotlinx.coroutines.flow.Flow

data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val durationMillis: Long,
    val artwork: Artwork,
    val artworkUrl: String? = null,
    val durationKnown: Boolean = true,
) {
    enum class Artwork { NONE, BEATLES }

    val duration: String
        get() {
            val seconds = durationMillis.coerceAtLeast(0L) / 1000
            return String.format(Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60)
        }
}

interface TracksRepository {
    suspend fun search(query: String): List<Track>
    suspend fun findById(id: Long): Track?
    val favoriteIds: Flow<Set<Long>>
    val favorites: Flow<List<Track>>
    suspend fun removeFavorite(trackId: Long)
    suspend fun toggleFavorite(trackId: Long)
}
