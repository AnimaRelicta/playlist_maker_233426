package ru.student.playlistmaker

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.json.JSONException
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import ru.student.playlistmaker.data.DatabaseMock
import ru.student.playlistmaker.data.ItunesTracksRepository
import ru.student.playlistmaker.data.TrackStorage
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
        val database = DatabaseMock(TrackStorage())
        val songs = parseItunesSongs("""{"results":[{"trackId":4100000000,"trackName":"Song",
            "artistName":"Band","trackTimeMillis":61000}]}""")
        val repository = ItunesTracksRepository({ songs }, database)
        val first = repository.search("  song  ").single()
        assertEquals(first.id, repository.search("band").single().id)
        assertEquals("01:01", first.duration)
        assertTrue(database.addTrack(1, first.id))
        assertFalse(database.addTrack(1, first.id))
        assertEquals(first, repository.findById(database.playlists.value.first().tracks.single().id))
        repository.toggleFavorite(first.id)
        assertEquals(listOf(first), repository.favorites.first())
    }
}
