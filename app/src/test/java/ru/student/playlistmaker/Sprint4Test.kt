package ru.student.playlistmaker

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.core.view.drawToBitmap
import androidx.navigation.compose.rememberNavController
import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import ru.student.playlistmaker.ui.PlaylistHost
import ru.student.playlistmaker.ui.search.SearchContent
import ru.student.playlistmaker.ui.search.SearchState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import ru.student.playlistmaker.ui.settings.*
import ru.student.playlistmaker.ui.theme.PlaylistTheme

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ru-w360dp-h800dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Sprint4Test {
    @org.junit.After
    fun stopDependencyGraph() = org.koin.core.context.stopKoin()

    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun allScreensOpenAndReturn() {
        compose.setContent { PlaylistTheme(false) { PlaylistHost(rememberNavController(), false, onThemeChange = {}) } }
        listOf("search", "settings", "playlists", "favorites").forEach { screen ->
            compose.onNodeWithTag("menu_$screen").performClick()
            compose.onNodeWithTag("${screen}_screen").assertIsDisplayed()
            compose.onNodeWithContentDescription("Назад").performClick()
            compose.onNodeWithTag("main_screen").assertIsDisplayed()
        }
    }

    @Test fun inputCanBeCleared() {
        compose.setContent {
            var state by androidx.compose.runtime.remember { mutableStateOf(SearchState()) }
            PlaylistTheme(false) { SearchContent(state, { state = SearchState(it) }, {}, {}, {}) }
        }
        compose.onAllNodesWithText("Поиск").assertCountEquals(2)
        val field = compose.onNodeWithTag("search_input")
        field.assertTopPositionInRootIsEqualTo(64.dp).assertHeightIsEqualTo(36.dp)
        field.performTextInput("Queen")
        field.assertTextEquals("Queen")
        compose.onNodeWithContentDescription("Очистить поиск").performClick()
        compose.onNodeWithText("Queen").assertDoesNotExist()
        compose.onAllNodesWithText("Поиск").assertCountEquals(2)
    }

    @Test fun themeSwitchUpdatesSettings() {
        val dark = mutableStateOf(false)
        compose.setContent { PlaylistTheme(dark.value) { SettingsScreen(dark.value, { dark.value = it }, {}) } }
        compose.onNodeWithText("Темная тема").assertIsOff().performClick().assertIsOn()
    }

    @Test fun externalIntentsHaveExpectedPayloads() {
        val share = shareIntent("Playlist Maker")
        assertEquals(Intent.ACTION_CHOOSER, share.action)
        val send = share.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
        assertEquals(Intent.ACTION_SEND, send?.action)
        assertEquals("text/plain", send?.type)
        assertEquals("Playlist Maker", send?.getStringExtra(Intent.EXTRA_TEXT))
        val mail = supportIntent("student@example.org", "Тема & вопрос", "Текст +")
        assertEquals(Intent.ACTION_SENDTO, mail.action)
        assertEquals("mailto", mail.data?.scheme)
        assertArrayEquals(arrayOf("student@example.org"), mail.getStringArrayExtra(Intent.EXTRA_EMAIL))
        assertEquals("Тема & вопрос", mail.getStringExtra(Intent.EXTRA_SUBJECT))
        assertEquals("Текст +", mail.getStringExtra(Intent.EXTRA_TEXT))
        assertTrue(mail.dataString.orEmpty().contains("%26"))
        val agreement = agreementIntent("https://yandex.ru/legal/practicum_offer/")
        assertEquals(Intent.ACTION_VIEW, agreement.action)
        assertEquals("yandex.ru", agreement.data?.host)
    }

    @Test fun renderSearchLight() = render(false, false)
    @Test fun renderSearchDark() = render(true, false)
    @Test fun renderSettingsLight() = render(false, true)
    @Test fun renderSettingsDark() = render(true, true)

    private fun render(dark: Boolean, settings: Boolean) {
        compose.setContent {
            PlaylistTheme(dark) {
                if (settings) SettingsScreen(dark, {}, {}) else SearchContent(SearchState(), {}, {}, {}, {})
            }
        }
        val bitmap = compose.runOnIdle {
            compose.activity.findViewById<android.view.View>(android.R.id.content).drawToBitmap()
        }
        val name = (if (settings) "settings" else "search") + (if (dark) "-dark" else "-light")
        val directory = File("build/reports/screenshots").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use {
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
        assertEquals(if (dark) 0xFF1A1B22.toInt() else 0xFFFFFFFF.toInt(),
            bitmap.getPixel(bitmap.width / 2, bitmap.height - 10))
    }
}
