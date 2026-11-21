package ru.student.playlistmaker.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import ru.student.playlistmaker.domain.PlaylistsRepository
import ru.student.playlistmaker.domain.SearchHistoryRepository
import org.koin.dsl.module
import ru.student.playlistmaker.domain.Track
import ru.student.playlistmaker.domain.TracksRepository

internal data class TrackDto(val id: Int, val title: String, val artist: String, val millis: Long, val hasArtwork: Boolean)

internal class TrackStorage {
    val tracks = listOf(
        TrackDto(1, "Yesterday (Remastered 2009)", "The Beatles", 125000, true),
        TrackDto(2, "Here Comes The Sun (Remastered 2009)", "The Beatles", 187000, true),
        TrackDto(3, "No Reply", "The Beatles", 152000, false),
        TrackDto(4, "Let It Be", "The Beatles", 243000, true),
        TrackDto(5, "Girl", "The Beatles", 138000, true),
        TrackDto(6, "Michelle", "The Beatles", 167000, true),
        TrackDto(7, "Eleanor Rigby", "The Beatles", 198000, true),
        TrackDto(8, "Come Together", "The Beatles", 259000, true),
    )
}

internal interface TracksClient {
    suspend fun load(): List<TrackDto>
    fun findById(id: Int): TrackDto?
}

internal class LocalTracksClient(private val storage: TrackStorage) : TracksClient {
    override suspend fun load(): List<TrackDto> {
        delay(3000)
        return storage.tracks
    }

    override fun findById(id: Int): TrackDto? = storage.tracks.firstOrNull { it.id == id }
}

internal class LocalTracksRepository(
    private val client: TracksClient,
    private val database: DatabaseMock,
) : TracksRepository {
    override val favoriteIds = database.favorites
    override val favorites = favoriteIds.map { ids -> ids.mapNotNull(::findById) }
    override fun toggleFavorite(trackId: Int) = database.toggleFavorite(trackId)

    override suspend fun search(query: String): List<Track> {
        val term = query.trim()
        if (term.isEmpty()) return emptyList()
        return client.load().filter {
            it.title.contains(term, ignoreCase = true) || it.artist.contains(term, ignoreCase = true)
        }.map { it.toTrack() }
    }

    override fun findById(id: Int): Track? = client.findById(id)?.toTrack()

}

internal fun TrackDto.toTrack() = Track(
    id, title, artist, millis, if (hasArtwork) Track.Artwork.BEATLES else Track.Artwork.NONE,
)

val tracksDataModule = module {
    single { TrackStorage() }
    single<TracksClient> { LocalTracksClient(get()) }
    single { DatabaseMock(get()) }
    single<TracksRepository> { LocalTracksRepository(get(), get()) }
    single<PlaylistsRepository> {
        val db = get<DatabaseMock>()
        object : PlaylistsRepository {
            override val playlists = db.playlists
            override fun create(name: String, description: String) = db.create(name, description)
            override fun delete(id: Long) = db.delete(id)
            override fun addTrack(playlistId: Long, trackId: Int) = db.addTrack(playlistId, trackId)
            override fun removeTrack(playlistId: Long, trackId: Int) = db.removeTrack(playlistId, trackId)
        }
    }
    single<SearchHistoryRepository> {
        val db = get<DatabaseMock>()
        object : SearchHistoryRepository {
            override val queries = db.history
            override fun record(query: String) = db.record(query)
        }
    }
}
