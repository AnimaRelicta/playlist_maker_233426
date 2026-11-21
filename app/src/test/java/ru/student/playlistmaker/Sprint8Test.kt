package ru.student.playlistmaker

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import ru.student.playlistmaker.data.DatabaseMock
import ru.student.playlistmaker.data.LocalTracksClient
import ru.student.playlistmaker.data.LocalTracksRepository
import ru.student.playlistmaker.data.TrackStorage
import ru.student.playlistmaker.domain.PlaylistsRepository
import ru.student.playlistmaker.ui.PlaylistHost
import ru.student.playlistmaker.ui.search.SearchResult
import ru.student.playlistmaker.ui.search.SearchViewModel
import ru.student.playlistmaker.ui.theme.PlaylistTheme
import android.os.Looper
import java.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
class Sprint8DataTest {
    @Test fun playlistLinksAreUniqueAndDeletionLeavesCatalogIntact() {
        val storage = TrackStorage()
        val db = DatabaseMock(storage)
        val repository = LocalTracksRepository(LocalTracksClient(storage), db)
        val id = checkNotNull(db.create("  Road trip  ", " Long drive "))
        assertNull(db.create("  ", "ignored"))
        assertFalse(db.addTrack(id, 999))
        assertTrue(db.addTrack(id, 1))
        assertFalse(db.addTrack(id, 1))
        assertTrue(db.addTrack(2, 1))
        assertEquals("Road trip", db.playlists.value.last().name)
        assertEquals("Long drive", db.playlists.value.last().description)
        assertEquals(listOf(1), db.playlists.value.last().tracks.map { it.id })
        db.removeTrack(2, 1)
        assertTrue(db.playlists.value[1].tracks.isEmpty())
        db.delete(id)
        assertEquals(2, db.playlists.value.size)
        assertNotNull(repository.findById(1))
    }

    @Test fun automaticSearchDebouncesAndRecordsOnlyCompletedQuery() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val storage = TrackStorage()
        val db = DatabaseMock(storage)
        val history = object : ru.student.playlistmaker.domain.SearchHistoryRepository {
            override val queries = db.history
            override fun record(query: String) = db.record(query)
        }
        val vm = SearchViewModel(LocalTracksRepository(LocalTracksClient(storage), db), dispatcher, history)
        try {
            vm.changeQuery("Beat")
            advanceTimeBy(300)
            vm.changeQuery("Michelle")
            advanceTimeBy(499)
            assertEquals(SearchResult.Initial, vm.state.value.result)
            advanceTimeBy(1)
            runCurrent()
            assertEquals(SearchResult.Searching, vm.state.value.result)
            advanceUntilIdle()
            assertEquals(listOf("Michelle"), (vm.state.value.result as SearchResult.Success).tracks.map { it.title })
            assertEquals("Michelle", db.history.value.first())
            assertFalse("Beat" in db.history.value)
            vm.searchFromHistory("MICHELLE")
            advanceUntilIdle()
            assertEquals("MICHELLE", db.history.value.first())
            assertEquals(1, db.history.value.count { it.equals("Michelle", true) })
            vm.changeQuery("Beatles")
            advanceTimeBy(500)
            runCurrent()
            vm.changeQuery("")
            advanceUntilIdle()
            assertEquals(SearchResult.Initial, vm.state.value.result)
            assertFalse("Beatles" in db.history.value)
        } finally {
            Dispatchers.resetMain()
        }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ru-w360dp-h800dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Sprint8UiTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    @After fun stopDependencyGraph() = stopKoin()

    @Test fun createPlaylistThenAddTrackFromSearchAndReopenIt() {
        GlobalContext.get().declare<CoroutineDispatcher>(Dispatchers.Main)
        compose.setContent { PlaylistTheme(false) { PlaylistHost(rememberNavController(), false, {}) } }
        compose.onNodeWithTag("menu_playlists").performClick()
        compose.onNodeWithText("Best songs 2021").assertIsDisplayed()
        compose.onNodeWithContentDescription("Новый плейлист").performClick()
        compose.onNodeWithText("Сохранить").assertIsNotEnabled()
        compose.onNodeWithTag("playlist_name").performTextInput("Road trip")
        compose.onNodeWithTag("playlist_description").performTextInput("Favorite tracks")
        compose.onNodeWithText("Сохранить").performClick()
        compose.onNodeWithText("Road trip").assertIsDisplayed()
        compose.onNodeWithContentDescription("Назад").performClick()
        compose.onNodeWithTag("menu_search").performClick()
        compose.onNodeWithTag("search_input").performClick()
        compose.onNodeWithText("Queen").assertIsDisplayed()
        compose.onNodeWithTag("search_input").performTextInput("Michelle")
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(500))
        compose.onNodeWithTag("search_loading").assertIsDisplayed()
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(3))
        compose.onNodeWithTag("track_6").performClick()
        compose.onNodeWithContentDescription("Добавить в избранное").performClick()
        compose.onNodeWithContentDescription("Убрать из избранного").assertIsDisplayed()
        compose.onNodeWithContentDescription("Добавить в плейлист").performClick()
        compose.onNodeWithTag("playlist_picker").assertIsDisplayed()
        compose.onNodeWithText("Road trip").performClick()
        compose.onNodeWithTag("playlist_picker").assertDoesNotExist()
        compose.onNodeWithContentDescription("Назад").performClick()
        compose.onNodeWithContentDescription("Назад").performClick()
        compose.onNodeWithTag("menu_playlists").performClick()
        compose.onNodeWithText("Road trip").performClick()
        compose.onNodeWithTag("playlist_details_screen").assertIsDisplayed()
        compose.onNodeWithText("Favorite tracks").assertIsDisplayed()
        compose.onNodeWithText("Michelle").assertIsDisplayed()
        val playlist = GlobalContext.get().get<PlaylistsRepository>().playlists.value.last()
        assertEquals(listOf(6), playlist.tracks.map { it.id })
    }
}
