package ru.student.playlistmaker.data

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import ru.student.playlistmaker.domain.Track
import ru.student.playlistmaker.domain.TracksRepository
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder

internal data class ItunesSong(
    val appleId: Long,
    val title: String,
    val artist: String,
    val durationMillis: Long?,
    val artworkUrl: String?,
)

internal fun itunesSearchUrl(query: String): String {
    val term = URLEncoder.encode(query.trim(), "UTF-8")
    return "https://itunes.apple.com/search?term=$term&media=music&entity=song&country=US&limit=50"
}

private fun JSONObject.text(key: String): String =
    (opt(key) as? String)?.trim().orEmpty()

private fun artworkUrl(value: String): String? = runCatching {
    val uri = URI(value)
    if (uri.host.isNullOrEmpty() || uri.userInfo != null) return null
    when (uri.scheme?.lowercase()) {
        "https" -> uri.toString()
        "http" -> "https:" + uri.rawSchemeSpecificPart
        else -> null
    }
}.getOrNull()

internal fun parseItunesSongs(json: String): List<ItunesSong> {
    val results = JSONObject(json).getJSONArray("results")
    val songs = ArrayList<ItunesSong>(results.length())
    for (index in 0 until results.length()) {
        val item = results.optJSONObject(index) ?: continue
        val id = item.optLong("trackId", 0)
        if (id <= 0) continue
        songs += ItunesSong(
            appleId = id,
            title = item.text("trackName"),
            artist = item.text("artistName"),
            durationMillis = item.optLong("trackTimeMillis", -1).takeIf { it >= 0 },
            artworkUrl = artworkUrl(item.text("artworkUrl100")),
        )
    }
    return songs.distinctBy { it.appleId }
}

internal class ItunesClient {
    fun search(query: String): List<ItunesSong> {
        val connection = URI(itunesSearchUrl(query)).toURL().openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = 8000
            connection.readTimeout = 10000
            connection.setRequestProperty("Accept", "application/json")
            val status = connection.responseCode
            if (status !in 200..299) throw IOException("iTunes HTTP $status")
            val body = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            parseItunesSongs(body)
        } finally {
            connection.disconnect()
        }
    }
}

internal class ItunesTracksRepository(
    private val searchRemote: (String) -> List<ItunesSong>,
    private val dao: LibraryDao,
) : TracksRepository {
    override val favorites = dao.favorites().map { tracks -> tracks.map { it.toDomain() } }
    override val favoriteIds = favorites.map { tracks -> tracks.mapTo(mutableSetOf()) { it.id } }

    override suspend fun search(query: String): List<Track> {
        val term = query.trim()
        if (term.isEmpty()) return emptyList()
        val songs = runInterruptible { searchRemote(term) }
        currentCoroutineContext().ensureActive()
        val tracks = songs.map { song ->
            Track(-song.appleId, song.title, song.artist, song.durationMillis ?: 0L, Track.Artwork.NONE,
                artworkUrl = song.artworkUrl, durationKnown = song.durationMillis != null)
        }
        dao.saveTracks(tracks.map { it.toEntity() })
        return tracks
    }

    override suspend fun findById(id: Long): Track? = dao.findTrack(id)?.toDomain()
    override suspend fun removeFavorite(trackId: Long) {
        dao.removeFavorite(trackId)
    }

    override suspend fun toggleFavorite(trackId: Long) = dao.toggleFavorite(trackId)
}
