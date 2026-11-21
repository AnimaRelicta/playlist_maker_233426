package ru.student.playlistmaker

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import ru.student.playlistmaker.data.LibraryDao
import ru.student.playlistmaker.data.TrackStorage
import ru.student.playlistmaker.data.toEntity
import ru.student.playlistmaker.data.toTrack
import ru.student.playlistmaker.domain.PlaylistsRepository
import ru.student.playlistmaker.ui.FinalVariant
import ru.student.playlistmaker.ui.PlaylistHost
import ru.student.playlistmaker.ui.theme.PlaylistTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ru-w360dp-h800dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Sprint11UiTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    @After fun stopDependencyGraph() = stopKoin()

    @Test fun variantOneDeletesPlaylistsFromDetailsAndList() {
        seedAndOpen(FinalVariant.DELETE_PLAYLIST)
        compose.onNodeWithText("Исходный").performClick()
        compose.waitUntilExactlyOneExists(hasTestTag("track_1"), 10_000)
        compose.onNodeWithTag("track_1").assert(hasNoLongClick())
        compose.onNodeWithContentDescription("Удалить плейлист").performClick()
        compose.onNodeWithText("Удалить").performClick()
        compose.waitUntilExactlyOneExists(hasTestTag("playlists_screen"), 10_000)
        compose.waitUntilDoesNotExist(hasText("Исходный"), 10_000)

        compose.onNodeWithText("Целевой").performTouchInput { longClick() }
        compose.onNodeWithText("Удалить плейлист «Целевой»?").assertIsDisplayed()
        compose.onNodeWithText("Удалить").performClick()
        compose.waitUntilDoesNotExist(hasText("Целевой"), 10_000)
    }

    @Test fun variantTwoRemovesTrackAndLeavesPlaylistOpen() {
        seedAndOpen(FinalVariant.REMOVE_TRACK)
        compose.onNodeWithText("Исходный").assert(hasNoLongClick()).performClick()
        compose.waitUntilExactlyOneExists(hasTestTag("track_1"), 10_000)
        compose.onNodeWithContentDescription("Удалить плейлист").assertDoesNotExist()
        compose.onNodeWithTag("track_1").performTouchInput { longClick() }
        compose.onNodeWithText("Отмена").performClick()
        compose.onNodeWithTag("track_1").assertIsDisplayed()
        compose.onNodeWithTag("track_1").performTouchInput { longClick() }
        compose.onNodeWithText("Удалить").performClick()
        compose.waitUntilDoesNotExist(hasTestTag("track_1"), 10_000)
        compose.onNodeWithTag("playlist_details_screen").assertIsDisplayed()
        compose.onNodeWithText("Исходный").assertIsDisplayed()
    }

    @Test fun variantThreeMergesIntoAnotherPlaylist() {
        seedAndOpen(FinalVariant.MERGE_PLAYLISTS)
        compose.onNodeWithText("Исходный").performTouchInput { longClick() }
        compose.onNodeWithText("Выберите плейлист для объединения").assertIsDisplayed()
        val picker = hasAnyAncestor(hasTestTag("playlist_picker"))
        compose.onNode(hasText("Исходный") and picker).assertDoesNotExist()
        compose.onNode(hasText("Целевой") and picker).performClick()
        compose.waitUntilExactlyOneExists(hasText("«Исходный» объединён с «Целевой»"), 10_000)
        compose.onNodeWithText("ОК").performClick()
        compose.waitUntilDoesNotExist(hasText("Исходный"), 10_000)
        compose.onNodeWithTag("playlists_screen").assertIsDisplayed()
        compose.onNodeWithText("Целевой").performClick()
        compose.waitUntilExactlyOneExists(hasTestTag("track_1"), 10_000)
        compose.onNodeWithContentDescription("Удалить плейлист").assertDoesNotExist()
        compose.onNodeWithTag("track_1").assert(hasNoLongClick())
    }

    private fun hasNoLongClick() =
        SemanticsMatcher.keyNotDefined(androidx.compose.ui.semantics.SemanticsActions.OnLongClick)

    private fun seedAndOpen(variant: FinalVariant) {
        val koin = GlobalContext.get()
        val repository = koin.get<PlaylistsRepository>()
        val track = TrackStorage().tracks.first().toTrack()
        runBlocking {
            koin.get<LibraryDao>().saveTracks(listOf(track.toEntity()))
            val source = checkNotNull(repository.create("Исходный", ""))
            checkNotNull(repository.create("Целевой", ""))
            repository.addTrack(source, track.id)
        }
        compose.setContent { PlaylistTheme(false) { PlaylistHost(rememberNavController(), false, variant) {} } }
        compose.onNodeWithTag("menu_playlists").performClick()
        compose.waitUntilExactlyOneExists(hasText("Исходный"), 10_000)
    }
}
