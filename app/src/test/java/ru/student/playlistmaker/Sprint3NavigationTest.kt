package ru.student.playlistmaker

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Sprint3NavigationTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test fun mainActivityIsLauncher() {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            .setPackage(context.packageName)
        assertEquals(MainActivity::class.java.name, context.packageManager.resolveActivity(intent, 0)?.activityInfo?.name)
    }

    @Test fun searchOpensAndCloses() = checkDestination(SearchActivity::class.java)
    @Test fun settingsOpensAndCloses() = checkDestination(SettingsActivity::class.java)
    @Test fun playlistsOpenAndClose() = checkDestination(PlaylistsActivity::class.java)
    @Test fun favoritesOpenAndClose() = checkDestination(FavoritesActivity::class.java)

    private fun checkDestination(destination: Class<out ComponentActivity>) {
        val declaration = context.packageManager.getActivityInfo(ComponentName(context, destination), 0)
        assertFalse(declaration.exported)
        Robolectric.buildActivity(destination).setup().use { controller ->
            val activity = controller.get()
            assertFalse(activity.isFinishing)
            activity.onBackPressedDispatcher.onBackPressed()
            assertTrue(activity.isFinishing)
        }
    }
}
