package ru.student.playlistmaker

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.view.drawToBitmap
import androidx.navigation.compose.rememberNavController
import java.io.File
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import ru.student.playlistmaker.domain.TracksRepository
import ru.student.playlistmaker.ui.PlaylistHost
import ru.student.playlistmaker.ui.search.*
import ru.student.playlistmaker.ui.theme.PlaylistTheme
import ru.student.playlistmaker.ui.tracks.TrackDetailsScreen

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ru-w360dp-h800dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Sprint5UiTest {
    @org.junit.After
    fun stopDependencyGraph() = org.koin.core.context.stopKoin()

    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun searchRouteUsesKoinAndOpensSelectedDetails() {
        useDemoSearch()
        GlobalContext.get().declare<CoroutineDispatcher>(Dispatchers.Main)
        compose.setContent { PlaylistTheme(false) { PlaylistHost(rememberNavController(), false, {}) } }
        compose.onNodeWithTag("menu_search").performClick()
        compose.onNodeWithTag("search_input").performTextInput("yEsTeR")
        compose.onNodeWithContentDescription("Найти треки").performClick()
        compose.onNodeWithTag("search_loading").assertIsDisplayed()
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(java.time.Duration.ofSeconds(3))
        compose.onNodeWithTag("track_1").assertIsDisplayed()
        compose.onNodeWithTag("track_1").performClick()
        compose.onNodeWithTag("track_details").assertIsDisplayed()
        compose.onNodeWithText("Yesterday (Remastered 2009)").assertIsDisplayed()
        compose.onNodeWithText("The Beatles").assertIsDisplayed()
        compose.onNodeWithText("02:05").assertIsDisplayed()
        compose.onNodeWithContentDescription("Назад").performClick()
        compose.onNodeWithTag("track_1").assertIsDisplayed()
        compose.onNodeWithContentDescription("Очистить поиск").performClick()
        compose.onNodeWithTag("track_1").assertDoesNotExist()
    }

    @Test fun renderSearchResultsLight() = render(false, "search-results") {
        val repository = GlobalContext.get().get<TracksRepository>()
        val tracks = (1..8).map { checkNotNull(repository.findById(it)) }
        SearchContent(SearchState("The Beatles", SearchResult.Success(tracks)), {}, {}, {}, {})
    }

    @Test fun renderEmptyDark() = render(true, "search-empty") {
        SearchContent(SearchState("Unknown", SearchResult.Success(emptyList())), {}, {}, {}, {})
    }

    @Test fun renderFailureLightAndCheckRetry() {
        var retries = 0
        render(false, "search-failure") {
            SearchContent(SearchState("Queen", SearchResult.Fail(null)), {}, { retries++ }, {}, {})
        }
        compose.onNodeWithText("Проблемы со связью").assertIsDisplayed()
        compose.onNodeWithText("Обновить").performClick()
        org.junit.Assert.assertEquals(1, retries)
    }

    @Test fun renderTrackDetailsDark() = render(true, "track-details") {
        TrackDetailsScreen(GlobalContext.get().get<TracksRepository>().findById(1), onBack = {})
    }

    private fun render(dark: Boolean, name: String, screen: @androidx.compose.runtime.Composable () -> Unit) {
        compose.setContent { PlaylistTheme(dark, screen) }
        val bitmap = compose.runOnIdle {
            compose.activity.findViewById<android.view.View>(android.R.id.content).drawToBitmap()
        }
        val directory = File("build/reports/screenshots").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use {
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
