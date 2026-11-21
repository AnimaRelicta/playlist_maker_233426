package ru.student.playlistmaker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import ru.student.playlistmaker.ui.main.MainScreen
import ru.student.playlistmaker.ui.theme.PlaylistTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlaylistTheme {
                Box(Modifier.fillMaxSize().safeDrawingPadding()) {
                    MainScreen(
                        onSearch = { startActivity(Intent(this@MainActivity, SearchActivity::class.java)) },
                        onPlaylists = { startActivity(Intent(this@MainActivity, PlaylistsActivity::class.java)) },
                        onFavorites = { startActivity(Intent(this@MainActivity, FavoritesActivity::class.java)) },
                        onSettings = { startActivity(Intent(this@MainActivity, SettingsActivity::class.java)) },
                    )
                }
            }
        }
    }
}
