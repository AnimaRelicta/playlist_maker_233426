package ru.student.playlistmaker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import ru.student.playlistmaker.ui.common.PlaceholderScreen
import ru.student.playlistmaker.ui.theme.PlaylistTheme

abstract class PlaceholderActivity : ComponentActivity() {
    @get:StringRes protected abstract val titleResource: Int
    protected abstract val screenTag: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlaylistTheme {
                Box(Modifier.safeDrawingPadding()) {
                    PlaceholderScreen(titleResource, screenTag, onBack = { finish() })
                }
            }
        }
    }
}
