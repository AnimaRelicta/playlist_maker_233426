package ru.student.playlistmaker.ui.playlists

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import ru.student.playlistmaker.R
import ru.student.playlistmaker.domain.Playlist
import ru.student.playlistmaker.domain.Track
import ru.student.playlistmaker.ui.common.ScreenHeader
import ru.student.playlistmaker.ui.search.TrackRow
import ru.student.playlistmaker.ui.theme.PlaylistColors
import ru.student.playlistmaker.ui.theme.PlaylistFont

@Composable
fun PlaylistsScreen(
    playlists: List<Playlist>,
    onBack: () -> Unit,
    onNew: () -> Unit,
    onOpen: (Long) -> Unit,
    onDelete: (suspend (Long) -> Unit)?,
    onMerge: (suspend (Long, Long) -> Boolean)?,
) {
    var deleteId by rememberSaveable { mutableStateOf<Long?>(null) }
    var mergeId by rememberSaveable { mutableStateOf<Long?>(null) }
    var mergeMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    Surface(Modifier.fillMaxSize().testTag("playlists_screen")) {
        Box {
            Column {
                ScreenHeader(R.string.playlists, onBack)
                LazyColumn(contentPadding = PaddingValues(bottom = 88.dp)) {
                    items(playlists, key = { it.id }) { playlist ->
                        val onLongClick: (() -> Unit)? = when {
                            onDelete != null -> ({ deleteId = playlist.id })
                            onMerge != null -> ({ mergeId = playlist.id })
                            else -> null
                        }
                        PlaylistRow(playlist, onLongClick = onLongClick) { onOpen(playlist.id) }
                    }
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
    if (onDelete != null) playlists.firstOrNull { it.id == deleteId }?.let { playlist ->
        ConfirmLibraryAction(stringResource(R.string.confirm_delete_playlist, playlist.name), { deleteId = null }) {
            deleteId = null
            scope.launch { onDelete(playlist.id) }
        }
    }
    if (onMerge != null) playlists.firstOrNull { it.id == mergeId }?.let { source ->
        val targets = playlists.filterNot { it.id == source.id }
        PlaylistPicker(targets, { mergeId = null }, title = R.string.choose_merge_target, emptyMessage = R.string.no_merge_targets) { id ->
            val target = targets.first { it.id == id }
            mergeId = null
            scope.launch {
                if (onMerge(source.id, target.id)) {
                    mergeMessage = context.getString(R.string.playlists_merged, source.name, target.name)
                }
            }
        }
    }
    mergeMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { mergeMessage = null },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { mergeMessage = null }) { Text(stringResource(R.string.ok)) } },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PlaylistRow(playlist: Playlist, onLongClick: (() -> Unit)? = null, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 61.dp).combinedClickable(onClick = onClick, onLongClick = onLongClick)
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
    val uri = playlist?.coverImageUri
    if (uri != null) {
        SelectedCover(uri, modifier)
    } else if (playlist?.id == 1L) {
        Image(painterResource(R.drawable.demo_playlist_artwork), null,
            modifier.clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
    } else {
        Box(modifier, contentAlignment = Alignment.Center) {
            Image(painterResource(R.drawable.playlist_cover_placeholder), null, Modifier.fillMaxSize(0.8f))
        }
    }
}

@Composable
private fun SelectedCover(uri: String, modifier: Modifier) {
    val placeholder = painterResource(R.drawable.playlist_cover_placeholder)
    AsyncImage(
        model = Uri.parse(uri),
        contentDescription = null,
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
        placeholder = placeholder,
        error = placeholder,
        contentScale = ContentScale.Crop,
    )
}

@Composable
fun NewPlaylistScreen(onBack: () -> Unit, onSave: suspend (String, String, String?) -> Boolean) {
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var cover by rememberSaveable { mutableStateOf<String?>(null) }
    var coverError by rememberSaveable { mutableStateOf(false) }
    val resolver = LocalContext.current.contentResolver
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            cover = uri.toString()
            coverError = false
        }
    }
    val scope = rememberCoroutineScope()
    Surface(Modifier.fillMaxSize().testTag("new_playlist_screen")) {
        Column {
            ScreenHeader(R.string.new_playlist, onBack)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
                Box(
                    Modifier.fillMaxWidth().aspectRatio(0.92f)
                        .clickable(enabled = !saving, onClickLabel = stringResource(R.string.choose_playlist_cover)) {
                            picker.launch(arrayOf("image/*"))
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    val uri = cover
                    if (uri == null) {
                        Image(painterResource(R.drawable.playlist_cover_placeholder),
                            stringResource(R.string.playlist_cover), Modifier.size(100.dp))
                    } else {
                        SelectedCover(uri, Modifier.fillMaxWidth().aspectRatio(1f))
                    }
                }
                if (coverError) {
                    Text(stringResource(R.string.playlist_cover_unavailable), color = MaterialTheme.colorScheme.error)
                }
                OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth().testTag("playlist_name"),
                    label = { Text(stringResource(R.string.playlist_name)) }, singleLine = true)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth().testTag("playlist_description"),
                    label = { Text(stringResource(R.string.playlist_description)) })
            }
            Button(
                onClick = {
                    saving = true
                    scope.launch {
                        try {
                            cover?.let { resolver.takePersistableUriPermission(Uri.parse(it), Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                            if (onSave(name.trim(), description.trim(), cover)) onBack()
                        } catch (_: SecurityException) {
                            coverError = true
                        } finally {
                            saving = false
                        }
                    }
                },
                enabled = name.isNotBlank() && !saving,
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(disabledContainerColor = PlaylistColors.Gray, disabledContentColor = Color.White),
            ) { Text(stringResource(R.string.save_playlist)) }
        }
    }
}

@Composable
fun PlaylistDetailsScreen(
    playlist: Playlist?,
    onBack: () -> Unit,
    openTrack: (Track) -> Unit,
    onDelete: (suspend () -> Unit)?,
    onRemoveTrack: (suspend (Long) -> Unit)?,
) {
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var removeId by rememberSaveable { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()
    Surface(Modifier.fillMaxSize().testTag("playlist_details_screen")) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { ScreenHeader(null, onBack) }
                if (playlist != null && onDelete != null) {
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(painterResource(R.drawable.ic_delete), stringResource(R.string.delete_playlist))
                    }
                }
            }
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
                            val totalMinutes = playlist.tracks.sumOf { it.durationMillis } / 60000
                            Text(stringResource(R.string.playlist_length, totalMinutes,
                                pluralStringResource(R.plurals.playlist_tracks, count, count)))
                        }
                    }
                    items(playlist.tracks, key = { it.id }) { track ->
                        TrackRow(track, onLongClick = onRemoveTrack?.let { { removeId = track.id } }) { openTrack(track) }
                    }
                }
            }
        }
    }
    if (confirmDelete && playlist != null && onDelete != null) {
        ConfirmLibraryAction(stringResource(R.string.confirm_delete_playlist, playlist.name), { confirmDelete = false }) {
            confirmDelete = false
            scope.launch {
                onDelete()
                onBack()
            }
        }
    }
    if (onRemoveTrack != null) playlist?.tracks?.firstOrNull { it.id == removeId }?.let { track ->
        ConfirmLibraryAction(stringResource(R.string.confirm_remove_playlist_track, track.title), { removeId = null }) {
            removeId = null
            scope.launch { onRemoveTrack(track.id) }
        }
    }
}

@Composable
private fun ConfirmLibraryAction(message: String, onCancel: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancel,
        text = { Text(message) },
        dismissButton = { TextButton(onClick = onCancel) { Text(stringResource(R.string.cancel_action)) } },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.confirm_delete)) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlaylistPicker(
    playlists: List<Playlist>,
    onClose: () -> Unit,
    title: Int = R.string.add_to_playlist,
    emptyMessage: Int = R.string.playlist_unavailable,
    onSelect: (Long) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(0.64f).testTag("playlist_picker")) {
            Text(stringResource(title), Modifier.fillMaxWidth().padding(bottom = 16.dp),
                textAlign = TextAlign.Center, fontFamily = PlaylistFont, fontWeight = FontWeight.Medium, fontSize = 18.sp)
            if (playlists.isEmpty()) {
                Text(stringResource(emptyMessage), Modifier.padding(16.dp))
            } else {
                LazyColumn {
                    items(playlists, key = { it.id }) { PlaylistRow(it) { onSelect(it.id) } }
                }
            }
        }
    }
}
