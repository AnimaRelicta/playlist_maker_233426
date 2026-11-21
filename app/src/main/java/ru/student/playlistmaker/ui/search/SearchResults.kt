package ru.student.playlistmaker.ui.search

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.student.playlistmaker.R
import ru.student.playlistmaker.domain.Track
import ru.student.playlistmaker.ui.theme.PlaylistColors
import ru.student.playlistmaker.ui.theme.PlaylistFont
import ru.student.playlistmaker.ui.tracks.TrackArtwork

@Composable
internal fun SearchResults(result: SearchResult, onRetry: () -> Unit, openTrack: (Track) -> Unit, modifier: Modifier) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        when (result) {
            SearchResult.Initial -> Unit
            SearchResult.Searching -> CircularProgressIndicator(
                modifier = Modifier.padding(top = 32.dp).size(36.dp).testTag("search_loading"),
            )
            is SearchResult.Success -> if (result.tracks.isEmpty()) {
                SearchMessage(false, onRetry)
            } else {
                LazyColumn(contentPadding = PaddingValues(top = 24.dp, bottom = 8.dp)) {
                    items(result.tracks, key = { it.id }) { TrackRow(it) { openTrack(it) } }
                }
            }
            is SearchResult.Fail -> SearchMessage(true, onRetry)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TrackRow(track: Track, onLongClick: (() -> Unit)? = null, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 61.dp).testTag("track_${track.id}")
        .combinedClickable(role = Role.Button, onClick = onClick, onLongClick = onLongClick).padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically) {
        TrackArtwork(track, Modifier.size(45.dp))
        Column(Modifier.weight(1f).padding(start = 8.dp, end = 8.dp)) {
            Text(track.title.ifBlank { stringResource(R.string.unknown_track) }, fontFamily = PlaylistFont, fontSize = 16.sp, maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            val artist = track.artist.ifBlank { stringResource(R.string.unknown_artist) }
            val duration = if (track.durationKnown) track.duration else stringResource(R.string.unknown_duration)
            Text(stringResource(R.string.track_metadata, artist, duration),
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = PlaylistFont,
                fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(painterResource(R.drawable.ic_chevron), null, Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SearchMessage(failed: Boolean, onRetry: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 112.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        TrackStatusIllustration(failed)
        Spacer(Modifier.height(20.dp))
        Text(stringResource(if (failed) R.string.search_failure else R.string.search_empty),
            fontFamily = PlaylistFont, fontSize = 18.sp, fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center)
        if (failed) {
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.search_failure_description), fontSize = 16.sp, textAlign = TextAlign.Center)
            TextButton(onClick = onRetry) { Text(stringResource(R.string.search_retry)) }
        }
    }
}

@Composable
internal fun TrackStatusIllustration(failed: Boolean) {
    val note = MaterialTheme.colorScheme.onSurface
    Canvas(Modifier.size(102.dp, 114.dp)) {
        scale(size.width / 102f, size.height / 114f, pivot = Offset.Zero) {
            val beam = Path().apply {
                moveTo(25f, 26f); lineTo(102f, 0f); lineTo(102f, 91f)
                lineTo(96f, 91f); lineTo(96f, 29f); lineTo(31f, 40f)
                lineTo(31f, 73f); lineTo(25f, 73f); close()
            }
            drawPath(beam, note)
            drawOval(note, Offset(0f, 53f), Size(31f, 41f))
            drawCircle(if (failed) PlaylistColors.Blue else Color(0xFFFFD653), 34.5f, Offset(62.5f, 60f))
            if (failed) {
                for (radius in listOf(10f, 17f, 24f)) {
                    drawArc(Color.White, 224f, 92f, false, Offset(62f - radius, 71f - radius),
                        Size(2 * radius, 2 * radius), style = Stroke(3f, cap = StrokeCap.Round))
                }
                drawCircle(Color.White, 2.5f, Offset(62f, 72f))
                drawLine(Color.White, Offset(45f, 44f), Offset(79f, 79f), 4f, StrokeCap.Round)
            } else {
                drawCircle(PlaylistColors.Ink, 4.5f, Offset(51f, 53f))
                drawCircle(PlaylistColors.Ink, 4.5f, Offset(73f, 53f))
                drawPath(Path().apply { moveTo(60f, 75f); quadraticTo(68f, 69f, 79f, 70f) },
                    PlaylistColors.Ink, style = Stroke(4f, cap = StrokeCap.Round))
            }
            drawOval(note, Offset(71f, 72f), Size(31f, 41f))
        }
    }
}
