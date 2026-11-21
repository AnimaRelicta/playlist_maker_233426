package ru.student.playlistmaker.ui.playlists

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.student.playlistmaker.R
import ru.student.playlistmaker.domain.Playlist
import ru.student.playlistmaker.domain.Track
import ru.student.playlistmaker.ui.common.ScreenHeader
import ru.student.playlistmaker.ui.search.TrackRow
import ru.student.playlistmaker.ui.theme.PlaylistColors
import ru.student.playlistmaker.ui.theme.PlaylistFont

@Composable
fun PlaylistsScreen(playlists: List<Playlist>, onBack: () -> Unit, onNew: () -> Unit, onOpen: (Long) -> Unit) {
    Surface(Modifier.fillMaxSize().testTag("playlists_screen")) {
        Box {
            Column {
                ScreenHeader(R.string.playlists, onBack)
                LazyColumn(contentPadding = PaddingValues(bottom = 88.dp)) {
                    items(playlists, key = { it.id }) { PlaylistRow(it) { onOpen(it.id) } }
                }
            }
            FloatingActionButton(
                onClick = onNew,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 32.dp).size(51.dp),
                shape = CircleShape,
                containerColor = if (MaterialTheme.colorScheme.surface == PlaylistColors.Ink)
                    Color(0xFF515257) else Color(0xFFC4C4C4),
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
            ) {
                Icon(painterResource(R.drawable.ic_plus), stringResource(R.string.new_playlist), Modifier.size(32.dp))
            }
        }
    }
}

@Composable
internal fun PlaylistRow(playlist: Playlist, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 61.dp).clickable(onClick = onClick)
            .padding(horizontal = 13.dp), verticalAlignment = Alignment.CenterVertically,
    ) {
        PlaylistArtwork(playlist, Modifier.size(45.dp))
        Column(Modifier.padding(start = 8.dp).weight(1f)) {
            Text(playlist.name, fontSize = 16.sp, fontFamily = PlaylistFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val count = playlist.tracks.size
            Text(pluralStringResource(R.plurals.playlist_tracks, count, count),
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
    }
}

@Composable
private fun PlaylistArtwork(playlist: Playlist?, modifier: Modifier) {
    if (playlist?.id == 1L) {
        Image(painterResource(R.drawable.demo_playlist_artwork), null,
            modifier.clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
    } else {
        Box(modifier, contentAlignment = Alignment.Center) {
            Image(painterResource(R.drawable.playlist_cover_placeholder), null, Modifier.fillMaxSize(0.8f))
        }
    }
}

@Composable
fun NewPlaylistScreen(onBack: () -> Unit, onSave: (String, String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var saving by rememberSaveable { mutableStateOf(false) }
    Surface(Modifier.fillMaxSize().testTag("new_playlist_screen")) {
        Column {
            ScreenHeader(R.string.new_playlist, onBack)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
                Box(Modifier.fillMaxWidth().aspectRatio(0.92f), contentAlignment = Alignment.Center) {
                    Image(painterResource(R.drawable.playlist_cover_placeholder), null, Modifier.size(100.dp))
                }
                OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth().testTag("playlist_name"),
                    label = { Text(stringResource(R.string.playlist_name)) }, singleLine = true)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth().testTag("playlist_description"),
                    label = { Text(stringResource(R.string.playlist_description)) })
            }
            Button(
                onClick = { saving = true; onSave(name.trim(), description.trim()) },
                enabled = name.isNotBlank() && !saving,
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(disabledContainerColor = PlaylistColors.Gray, disabledContentColor = Color.White),
            ) { Text(stringResource(R.string.save_playlist)) }
        }
    }
}

@Composable
fun PlaylistDetailsScreen(playlist: Playlist?, onBack: () -> Unit, openTrack: (Track) -> Unit) {
    Surface(Modifier.fillMaxSize().testTag("playlist_details_screen")) {
        Column {
            ScreenHeader(null, onBack)
            if (playlist == null) {
                Text(stringResource(R.string.playlist_unavailable), Modifier.padding(16.dp))
            } else {
                LazyColumn {
                    item {
                        PlaylistArtwork(playlist, Modifier.fillMaxWidth().padding(horizontal = 24.dp).aspectRatio(1f))
                        Column(Modifier.padding(16.dp)) {
                            Text(playlist.name, fontFamily = PlaylistFont, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                            if (playlist.description.isNotEmpty()) Text(playlist.description)
                            val count = playlist.tracks.size
                            Text(pluralStringResource(R.plurals.playlist_tracks, count, count))
                        }
                    }
                    items(playlist.tracks, key = { it.id }) { track -> TrackRow(track) { openTrack(track) } }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlaylistPicker(playlists: List<Playlist>, onClose: () -> Unit, onSelect: (Long) -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(0.64f).testTag("playlist_picker")) {
            Text(stringResource(R.string.add_to_playlist), Modifier.fillMaxWidth().padding(bottom = 16.dp),
                textAlign = TextAlign.Center, fontFamily = PlaylistFont, fontWeight = FontWeight.Medium, fontSize = 18.sp)
            if (playlists.isEmpty()) {
                Text(stringResource(R.string.playlist_unavailable), Modifier.padding(16.dp))
            } else {
                LazyColumn {
                    items(playlists, key = { it.id }) { PlaylistRow(it) { onSelect(it.id) } }
                }
            }
        }
    }
}
