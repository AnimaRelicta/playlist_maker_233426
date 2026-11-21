package ru.student.playlistmaker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.student.playlistmaker.domain.Playlist
import ru.student.playlistmaker.domain.PlaylistsRepository
import ru.student.playlistmaker.domain.SearchHistoryRepository
import ru.student.playlistmaker.domain.TracksRepository

private val Context.historyDataStore by preferencesDataStore("search_history")

internal class SearchHistoryPreferences(private val dataStore: DataStore<Preferences>) {
    private val key = stringPreferencesKey("queries")

    val entries = dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { decode(it[key]) }

    suspend fun getEntries(): List<String> = entries.first()

    suspend fun addEntry(query: String) {
        val term = query.trim()
        if (term.isEmpty()) return
        dataStore.edit { preferences ->
            val history = decode(preferences[key]).filterNot { it.equals(term, ignoreCase = true) }
            preferences[key] = JSONArray((listOf(term) + history).take(10)).toString()
        }
    }

    private fun decode(value: String?): List<String> {
        val array = JSONArray(value ?: "[]")
        return List(array.length()) { array.getString(it) }
    }
}

internal class SearchHistoryRepositoryImpl(
    private val preferences: SearchHistoryPreferences,
) : SearchHistoryRepository {
    override val queries = preferences.entries
    override suspend fun record(query: String) = preferences.addEntry(query)
}

internal class RoomPlaylistsRepository(private val dao: LibraryDao) : PlaylistsRepository {
    override val playlists = dao.playlists().map { rows ->
        rows.groupBy { it.playlist }.map { (playlist, entries) -> playlist.toDomain(entries) }
    }

    override fun getPlaylist(id: Long) = dao.playlist(id).map { rows ->
        rows.firstOrNull()?.playlist?.toDomain(rows)
    }

    override suspend fun create(name: String, description: String): Long? {
        val title = name.trim()
        if (title.isEmpty()) return null
        return dao.create(PlaylistEntity(name = title, description = description.trim()))
    }

    override suspend fun delete(id: Long) = dao.deletePlaylist(id)

    override suspend fun addTrack(playlistId: Long, trackId: Long): Boolean =
        dao.addTrack(playlistId, trackId) != -1L

    override suspend fun removeTrack(playlistId: Long, trackId: Long) = dao.removeTrack(playlistId, trackId)

    private fun PlaylistEntity.toDomain(rows: List<PlaylistRow>) =
        Playlist(id, name, description, coverImageUri, rows.mapNotNull { it.track?.toDomain() })
}

val tracksDataModule = module {
    single { LibraryDatabase.open(androidContext()) }
    single { get<LibraryDatabase>().libraryDao() }
    single { ItunesClient() }
    single<TracksRepository> { ItunesTracksRepository(get<ItunesClient>()::search, get()) }
    single<PlaylistsRepository> { RoomPlaylistsRepository(get()) }
    single<SearchHistoryRepository> { SearchHistoryRepositoryImpl(SearchHistoryPreferences(androidContext().historyDataStore)) }
}
