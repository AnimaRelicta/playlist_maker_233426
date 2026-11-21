package ru.student.playlistmaker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import ru.student.playlistmaker.ui.PlaylistHost
import ru.student.playlistmaker.ui.theme.PlaylistTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settings = getSharedPreferences("appearance", MODE_PRIVATE)
        setContent {
            var preference by rememberSaveable {
                mutableStateOf(if (settings.contains("dark")) settings.getBoolean("dark", false) else null)
            }
            val dark = preference ?: isSystemInDarkTheme()
            PlaylistTheme(darkTheme = dark) {
                Box(Modifier.fillMaxSize().safeDrawingPadding()) {
                    PlaylistHost(rememberNavController(), dark) {
                        preference = it
                        settings.edit().putBoolean("dark", it).apply()
                    }
                }
            }
        }
    }
}
