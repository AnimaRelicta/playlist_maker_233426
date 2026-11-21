package ru.student.playlistmaker.domain

import java.util.Locale

data class Track(val id: Int, val title: String, val artist: String, val durationMillis: Long, val artwork: Artwork) {
    enum class Artwork { NONE, BEATLES }

    val duration: String
        get() {
            val seconds = durationMillis.coerceAtLeast(0L) / 1000
            return String.format(Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60)
        }
}

interface TracksRepository {
    suspend fun search(query: String): List<Track>
    fun findById(id: Int): Track?
}
