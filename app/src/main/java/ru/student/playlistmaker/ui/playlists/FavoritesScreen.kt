package ru.student.playlistmaker.ui.playlists

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.student.playlistmaker.R
import ru.student.playlistmaker.domain.Track
import ru.student.playlistmaker.ui.common.ScreenHeader
import ru.student.playlistmaker.ui.search.TrackRow
import ru.student.playlistmaker.ui.search.TrackStatusIllustration

@Composable
fun FavoritesScreen(tracks: List<Track>, onBack: () -> Unit, openTrack: (Track) -> Unit) {
    Surface(Modifier.fillMaxSize().testTag("favorites_screen")) {
        Column {
            ScreenHeader(R.string.favorites, onBack)
            if (tracks.isEmpty()) {
                Column(
                    Modifier.fillMaxWidth().padding(top = 160.dp, start = 24.dp, end = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    TrackStatusIllustration(failed = false)
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.favorites_empty), style = MaterialTheme.typography.titleMedium)
                }
            } else {
                LazyColumn {
                    items(tracks, key = { it.id }) { track -> TrackRow(track) { openTrack(track) } }
                }
            }
        }
    }
}
