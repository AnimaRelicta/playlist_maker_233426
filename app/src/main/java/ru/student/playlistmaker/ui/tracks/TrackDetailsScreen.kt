package ru.student.playlistmaker.ui.tracks

import androidx.compose.foundation.Image
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.launch
import ru.student.playlistmaker.domain.Playlist
import ru.student.playlistmaker.ui.playlists.PlaylistPicker
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
    val url = track.artworkUrl
    if (url != null) {
        val request = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .diskCachePolicy(CachePolicy.DISABLED)
            .build()
        Box(modifier.clip(RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
            ArtworkPlaceholder()
            AsyncImage(request, contentDescription = null, modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop)
        }
    } else if (track.artwork == Track.Artwork.BEATLES) {
        Image(painterResource(R.drawable.beatles_artwork), null,
            modifier.clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
    } else {
        Box(modifier, contentAlignment = Alignment.Center) {
            ArtworkPlaceholder()
        }
    }
}

@Composable
private fun ArtworkPlaceholder() {
    Icon(painterResource(R.drawable.ic_track_placeholder), null, Modifier.fillMaxSize(0.62f),
        tint = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
fun TrackDetailsScreen(
    track: Track?, onBack: () -> Unit, favorite: Boolean = false,
    playlists: List<Playlist> = emptyList(), onFavorite: () -> Unit = {},
    onAdd: (Long) -> Boolean = { false },
) {
    var showPicker by rememberSaveable(track?.id) { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val added = stringResource(R.string.track_added)
    val duplicate = stringResource(R.string.track_already_added)
    Surface(Modifier.fillMaxSize().testTag("track_details")) {
        Box {
            Column {
                ScreenHeader(null, onBack)
                if (track == null) {
                    Text(stringResource(R.string.track_unavailable), Modifier.padding(16.dp))
                } else {
                    Column(Modifier.verticalScroll(rememberScrollState()).padding(top = 26.dp, bottom = 24.dp)) {
                        Column(Modifier.padding(horizontal = 24.dp)) {
                            TrackArtwork(track, Modifier.fillMaxWidth().aspectRatio(1f))
                            Spacer(Modifier.height(24.dp))
                            Text(track.title.ifBlank { stringResource(R.string.unknown_track) }, fontFamily = PlaylistFont, fontWeight = FontWeight.Medium, fontSize = 22.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(track.artist.ifBlank { stringResource(R.string.unknown_artist) }, fontFamily = PlaylistFont, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Spacer(Modifier.height(52.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                TrackAction(R.drawable.ic_add_playlist, R.string.add_to_playlist) { showPicker = true }
                                TrackAction(if (favorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite,
                                    if (favorite) R.string.remove_favorite else R.string.add_favorite,
                                    if (favorite) PlaylistColors.Blue else Color.White, onFavorite)
                            }
                        }
                        Spacer(Modifier.height(30.dp))
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.duration), fontSize = 13.sp, color = PlaylistColors.Gray)
                            Text(if (track.durationKnown) track.duration else stringResource(R.string.unknown_duration), fontSize = 13.sp)
                        }
                    }
                }
            }
            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
        }
    }
    if (showPicker) {
        PlaylistPicker(playlists, { showPicker = false }) { id ->
            showPicker = false
            val message = if (onAdd(id)) added else duplicate
            scope.launch { snackbar.showSnackbar(message) }
        }
    }
}

@Composable
private fun TrackAction(icon: Int, label: Int, tint: Color = Color.White, onClick: () -> Unit) {
    IconButton(onClick = onClick,
        modifier = Modifier.size(51.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f), CircleShape)) {
        Icon(painterResource(icon), stringResource(label), Modifier.size(24.dp), tint = tint)
    }
}
