package ru.student.playlistmaker

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
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

}
