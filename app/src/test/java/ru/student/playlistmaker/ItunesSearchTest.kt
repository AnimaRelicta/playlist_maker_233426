package ru.student.playlistmaker

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.json.JSONException
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import ru.student.playlistmaker.data.LibraryDatabase
import ru.student.playlistmaker.data.RoomPlaylistsRepository
import ru.student.playlistmaker.data.ItunesTracksRepository
import ru.student.playlistmaker.data.itunesSearchUrl
import ru.student.playlistmaker.data.parseItunesSongs

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class ItunesSearchTest {
    @Test fun queryIsEncodedAsOneParameter() {
        val url = itunesSearchUrl("  AC/DC & рок + 42  ")
        val uri = android.net.Uri.parse(url)
        assertEquals("https", uri.scheme)
        assertEquals("itunes.apple.com", uri.host)
        assertEquals("AC/DC & рок + 42", uri.getQueryParameter("term"))
        assertEquals("song", uri.getQueryParameter("entity"))
    }

    @Test fun parserHandlesMissingFieldsAndRejectsMalformedResponse() {
        val songs = parseItunesSongs("""
            {"results":[null,{},{"trackId":4100000000,"trackName":null,"artistName":" ",
                "artworkUrl100":"file:///tmp/img"},{"trackId":14,"trackName":"Song","artistName":"Band",
                "trackTimeMillis":61000,"artworkUrl100":"http://is1-ssl.mzstatic.com/art.jpg"},
                {"trackId":14,"trackName":"Duplicate"}]}
        """.trimIndent())
        assertEquals(2, songs.size)
        assertEquals(4100000000L, songs.first().appleId)
        assertEquals("", songs.first().title)
        assertNull(songs.first().durationMillis)
        assertNull(songs.first().artworkUrl)
        assertEquals("https://is1-ssl.mzstatic.com/art.jpg", songs.last().artworkUrl)
        assertTrue(parseItunesSongs("""{"results":[]}""").isEmpty())
        assertThrows(JSONException::class.java) { parseItunesSongs("""{"error":"offline"}""") }
    }

    @Test fun remoteIdentitySurvivesRepeatedSearchAndPlaylistRoundTrip() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val songs = parseItunesSongs("""{"results":[{"trackId":4100000000,"trackName":"Song",
            "artistName":"Band","trackTimeMillis":61000}]}""")
        val (saved, playlistId, secondId) = withDatabase(context) { database ->
            val repository = ItunesTracksRepository({ songs }, database.libraryDao())
            val playlists = RoomPlaylistsRepository(database.libraryDao())
            val first = repository.search("  song  ").single()
            assertEquals(-4100000000L, first.id)
            assertEquals(first.id, repository.search("band").single().id)
            assertEquals("01:01", first.duration)
            assertNull(playlists.create("  ", "ignored"))
            val id = checkNotNull(playlists.create("  Road trip  ", " Long drive "))
            val second = checkNotNull(playlists.create("Second", ""))
            assertFalse(playlists.addTrack(id, 999))
            assertFalse(playlists.addTrack(Long.MAX_VALUE, first.id))
            assertTrue(playlists.addTrack(id, first.id))
            assertFalse(playlists.addTrack(id, first.id))
            assertTrue(playlists.addTrack(second, first.id))
            repository.toggleFavorite(first.id)
            Triple(first, id, second)
        }
        withDatabase(context) { database ->
            val repository = ItunesTracksRepository({ error("Network must not be used") }, database.libraryDao())
            val playlists = RoomPlaylistsRepository(database.libraryDao())
            val playlist = checkNotNull(playlists.getPlaylist(playlistId).first())
            assertEquals("Road trip", playlist.name)
            assertEquals("Long drive", playlist.description)
            assertEquals(listOf(saved), playlist.tracks)
            assertEquals(saved, repository.findById(saved.id))
            assertEquals(listOf(saved), repository.favorites.first())
            playlists.delete(playlistId)
            assertNull(playlists.getPlaylist(playlistId).first())
            assertEquals(listOf(saved), playlists.getPlaylist(secondId).first()?.tracks)
            playlists.removeTrack(secondId, saved.id)
            assertEquals(emptyList<Any>(), playlists.getPlaylist(secondId).first()?.tracks)
            assertEquals(listOf(saved), repository.favorites.first())
            repository.toggleFavorite(saved.id)
            assertTrue(repository.favorites.first().isEmpty())
        }
    }

    @Test fun mergesPlaylistsWithoutDuplicatesAndKeepsTargetCoverAndFavorites() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val cover = "content://covers/target"
        val (targetId, trackIds) = withDatabase(context) { database ->
            val songs = (1L..4L).map { id ->
                ru.student.playlistmaker.data.ItunesSong(id, "Track $id", "Artist", 61000L, null)
            }
            val dao = database.libraryDao()
            val tracks = ItunesTracksRepository({ songs }, dao)
            val ids = tracks.search("tracks").map { it.id }
            val playlists = RoomPlaylistsRepository(dao)
            val source = checkNotNull(playlists.create("Source", ""))
            val target = checkNotNull(playlists.create("Target", "Description", cover))
            val other = checkNotNull(playlists.create("Other", ""))
            for (id in ids.drop(1)) assertTrue(playlists.addTrack(source, id))
            for (id in ids.take(2)) assertTrue(playlists.addTrack(target, id))
            assertTrue(playlists.addTrack(other, ids.last()))
            tracks.toggleFavorite(ids.last())

            assertFalse(playlists.merge(source, source))
            assertFalse(playlists.merge(source, Long.MAX_VALUE))
            assertFalse(playlists.merge(Long.MAX_VALUE, target))
            assertEquals(ids.drop(1), playlists.getPlaylist(source).first()?.tracks?.map { it.id })
            assertTrue(playlists.merge(source, target))
            assertNull(playlists.getPlaylist(source).first())
            assertEquals(ids, playlists.getPlaylist(target).first()?.tracks?.map { it.id })
            playlists.removeTrack(target, ids.last())
            assertEquals(listOf(ids.last()), playlists.getPlaylist(other).first()?.tracks?.map { it.id })
            target to ids
        }
        withDatabase(context) { database ->
            val dao = database.libraryDao()
            val playlists = RoomPlaylistsRepository(dao)
            val tracks = ItunesTracksRepository({ error("Network must not be used") }, dao)
            val target = checkNotNull(playlists.getPlaylist(targetId).first())
            assertEquals("Target", target.name)
            assertEquals("Description", target.description)
            assertEquals(cover, target.coverImageUri)
            assertEquals(trackIds.dropLast(1), target.tracks.map { it.id })
            assertEquals(setOf(trackIds.last()), tracks.favoriteIds.first())
            tracks.removeFavorite(trackIds.last())
            tracks.removeFavorite(trackIds.last())
            assertTrue(tracks.favoriteIds.first().isEmpty())
            assertNotNull(tracks.findById(trackIds.last()))
        }
    }

    private suspend fun <T> withDatabase(context: Context, block: suspend (LibraryDatabase) -> T): T {
        val database = LibraryDatabase.open(context)
        return try {
            block(database)
        } finally {
            database.close()
        }
    }
}
