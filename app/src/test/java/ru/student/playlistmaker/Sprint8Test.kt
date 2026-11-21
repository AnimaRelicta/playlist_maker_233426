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
import kotlinx.coroutines.flow.MutableStateFlow
import ru.student.playlistmaker.domain.SearchHistoryRepository
import ru.student.playlistmaker.data.LocalTracksClient
import ru.student.playlistmaker.data.LocalTracksRepository
import ru.student.playlistmaker.data.TrackStorage
import ru.student.playlistmaker.ui.PlaylistHost
import ru.student.playlistmaker.ui.search.SearchResult
import ru.student.playlistmaker.ui.search.SearchViewModel
import ru.student.playlistmaker.ui.theme.PlaylistTheme
import android.os.Looper
import java.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
class Sprint8DataTest {
    @Test fun automaticSearchDebouncesAndRecordsOnlyCompletedQuery() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val storage = TrackStorage()
        val recordedQueries = MutableStateFlow(emptyList<String>())
        val history = object : SearchHistoryRepository {
            override val queries = recordedQueries
            override suspend fun record(query: String) { recordedQueries.value = recordedQueries.value + query }
        }
        val vm = SearchViewModel(LocalTracksRepository(LocalTracksClient(storage)), dispatcher, history)
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
            assertEquals("Michelle", recordedQueries.value.last())
            assertFalse("Beat" in recordedQueries.value)
            vm.searchFromHistory("MICHELLE")
            advanceUntilIdle()
            assertEquals("MICHELLE", recordedQueries.value.last())
            assertEquals(listOf("Michelle", "MICHELLE"), recordedQueries.value)
            vm.changeQuery("Beatles")
            advanceTimeBy(500)
            runCurrent()
            vm.changeQuery("")
            advanceUntilIdle()
            assertEquals(SearchResult.Initial, vm.state.value.result)
            assertFalse("Beatles" in recordedQueries.value)
        } finally {
            Dispatchers.resetMain()
        }
    }
}

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ru-w360dp-h800dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Sprint8UiTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    @After fun stopDependencyGraph() = stopKoin()

    @Test fun createPlaylistThenAddTrackFromSearchAndReopenIt() {
        useDemoSearch()
        GlobalContext.get().declare<CoroutineDispatcher>(Dispatchers.Main)
        compose.setContent { PlaylistTheme(false) { PlaylistHost(rememberNavController(), false, {}) } }
        compose.onNodeWithTag("menu_playlists").performClick()
        compose.waitUntilExactlyOneExists(hasText("Best songs 2021"), 10_000)
        compose.onNodeWithContentDescription("Новый плейлист").performClick()
        compose.onNodeWithText("Сохранить").assertIsNotEnabled()
        compose.onNodeWithTag("playlist_name").performTextInput("Road trip")
        compose.onNodeWithTag("playlist_description").performTextInput("Favorite tracks")
        compose.onNodeWithText("Сохранить").performClick()
        compose.waitUntilExactlyOneExists(hasText("Road trip"), 10_000)
        compose.onNodeWithContentDescription("Назад").performClick()
        compose.onNodeWithTag("menu_search").performClick()
        compose.onNodeWithTag("search_input").performClick()
        compose.onNodeWithTag("search_input").performTextInput("Michelle")
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(500))
        compose.onNodeWithTag("search_loading").assertIsDisplayed()
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(3))
        compose.waitUntilExactlyOneExists(hasTestTag("track_6"), 10_000)
        compose.onNodeWithTag("track_6").performClick()
        compose.onNodeWithContentDescription("Добавить в избранное").performClick()
        compose.waitUntilExactlyOneExists(hasContentDescription("Убрать из избранного"), 10_000)
        compose.onNodeWithContentDescription("Добавить в плейлист").performClick()
        compose.onNodeWithTag("playlist_picker").assertIsDisplayed()
        compose.onNodeWithText("Road trip").performClick()
        compose.onNodeWithTag("playlist_picker").assertDoesNotExist()
        compose.onNodeWithContentDescription("Назад").performClick()
        compose.onNodeWithContentDescription("Назад").performClick()
        compose.onNodeWithTag("menu_playlists").performClick()
        compose.onNodeWithText("Road trip").performClick()
        compose.onNodeWithTag("playlist_details_screen").assertIsDisplayed()
        compose.waitUntilExactlyOneExists(hasText("Favorite tracks"), 10_000)
        compose.waitUntilExactlyOneExists(hasText("Michelle"), 10_000)
        compose.onNodeWithContentDescription("Назад").performClick()
        compose.onNodeWithContentDescription("Назад").performClick()
        compose.onNodeWithTag("menu_favorites").performClick()
        compose.waitUntilExactlyOneExists(hasText("Michelle"), 10_000)
        compose.onNodeWithText("Michelle").performClick()
        compose.waitUntilExactlyOneExists(hasContentDescription("Убрать из избранного"), 10_000)
    }
}
