package ru.student.playlistmaker.ui.tracks

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.student.playlistmaker.R
import ru.student.playlistmaker.domain.Track
import ru.student.playlistmaker.ui.common.ScreenHeader
import ru.student.playlistmaker.ui.theme.PlaylistColors
import ru.student.playlistmaker.ui.theme.PlaylistFont

@Composable
fun TrackArtwork(track: Track, modifier: Modifier) {
    if (track.artwork == Track.Artwork.BEATLES) {
        Image(painterResource(R.drawable.beatles_artwork), null,
            modifier.clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
    } else {
        Box(modifier, contentAlignment = Alignment.Center) {
            Icon(painterResource(R.drawable.ic_track_placeholder), null,
                Modifier.fillMaxSize(0.62f), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun TrackDetailsScreen(track: Track?, onBack: () -> Unit) {
    Surface(Modifier.fillMaxSize().testTag("track_details")) {
        Column {
            ScreenHeader(null, onBack)
            if (track == null) {
                Text(stringResource(R.string.track_unavailable), Modifier.padding(16.dp))
            } else {
                Column(Modifier.verticalScroll(rememberScrollState()).padding(top = 26.dp, bottom = 24.dp)) {
                    Column(Modifier.padding(horizontal = 24.dp)) {
                        TrackArtwork(track, Modifier.fillMaxWidth().aspectRatio(1f))
                        Spacer(Modifier.height(24.dp))
                        Text(track.title, fontFamily = PlaylistFont, fontWeight = FontWeight.Medium, fontSize = 22.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(track.artist, fontFamily = PlaylistFont, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Spacer(Modifier.height(52.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            InactiveTrackAction(R.drawable.ic_add_playlist, R.string.add_to_playlist)
                            InactiveTrackAction(R.drawable.ic_favorite, R.string.favorites)
                        }
                    }
                    Spacer(Modifier.height(30.dp))
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.duration), fontSize = 13.sp, color = PlaylistColors.Gray)
                        Text(track.duration, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun InactiveTrackAction(icon: Int, label: Int) {
    IconButton(onClick = {}, enabled = false,
        modifier = Modifier.size(51.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f), CircleShape)) {
        Icon(painterResource(icon), stringResource(label), Modifier.size(24.dp), tint = Color.White)
    }
}
