package ru.student.playlistmaker

import androidx.activity.ComponentActivity
import androidx.core.view.drawToBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import ru.student.playlistmaker.ui.main.MainScreen
import ru.student.playlistmaker.ui.theme.PlaylistTheme

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ru-w360dp-h800dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MainScreenTest {
    @org.junit.After
    fun stopDependencyGraph() = org.koin.core.context.stopKoin()

    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun allFourRowsAreAccessibleAndDispatchTheirOwnAction() {
        val clicks = mutableListOf<String>()
        compose.setContent {
            PlaylistTheme(darkTheme = false) {
                MainScreen({ clicks += "search" }, { clicks += "playlists" },
                    { clicks += "favorites" }, { clicks += "settings" })
            }
        }
        listOf("Поиск", "Плейлисты", "Избранное", "Настройки").forEach {
            compose.onNodeWithText(it).assertIsDisplayed().assertHasClickAction().performClick()
        }
        assertEquals(listOf("search", "playlists", "favorites", "settings"), clicks)
    }

    @Test fun lightScreenMatchesReferenceGeometryAndProducesScreenshot() = render(false, "main-light")
    @Test fun darkScreenMatchesReferenceGeometryAndProducesScreenshot() = render(true, "main-dark")

    private fun render(dark: Boolean, name: String) {
        compose.setContent { PlaylistTheme(darkTheme = dark) { MainScreen({}, {}, {}, {}) } }
        compose.onNodeWithTag("main_menu").assertTopPositionInRootIsEqualTo(70.dp)
        compose.onNodeWithTag("menu_search").assertTopPositionInRootIsEqualTo(78.dp).assertHeightIsEqualTo(66.dp)
        compose.onNodeWithTag("menu_playlists").assertTopPositionInRootIsEqualTo(144.dp)
        compose.onNodeWithTag("menu_favorites").assertTopPositionInRootIsEqualTo(210.dp)
        compose.onNodeWithTag("menu_settings").assertTopPositionInRootIsEqualTo(276.dp)
        val bitmap = compose.runOnIdle {
            compose.activity.findViewById<android.view.View>(android.R.id.content).drawToBitmap()
        }
        val directory = File("build/reports/screenshots").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use {
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
        assertEquals(0xFF3772E7.toInt(), bitmap.getPixel(100, 2))
        val background = if (dark) 0xFF1A1B22.toInt() else 0xFFFFFFFF.toInt()
        assertEquals(background, bitmap.getPixel(bitmap.width / 2, bitmap.height - 10))
    }
}
