package ru.student.playlistmaker.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.student.playlistmaker.R
import ru.student.playlistmaker.domain.Track
import ru.student.playlistmaker.ui.common.ScreenHeader
import ru.student.playlistmaker.ui.theme.PlaylistColors
import ru.student.playlistmaker.ui.theme.PlaylistFont

@Composable
fun SearchScreen(viewModel: SearchViewModel, onBack: () -> Unit, openTrack: (Track) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SearchContent(state, viewModel::changeQuery, viewModel::search, onBack, openTrack)
}

@Composable
internal fun SearchContent(
    state: SearchState,
    changeQuery: (String) -> Unit,
    search: () -> Unit,
    onBack: () -> Unit,
    openTrack: (Track) -> Unit,
) {
    val query = state.query
    val focus = LocalFocusManager.current
    val submit = {
        if (query.isNotBlank()) {
            focus.clearFocus()
            search()
        }
    }
    val dark = MaterialTheme.colorScheme.surface == PlaylistColors.Ink
    val hintColor = if (dark) PlaylistColors.Ink else PlaylistColors.Gray
    val style = TextStyle(color = PlaylistColors.Ink, fontSize = 16.sp, fontFamily = PlaylistFont)
    val title = stringResource(R.string.search)
    Surface(Modifier.fillMaxSize().testTag("search_screen")) {
        Column {
            ScreenHeader(R.string.search, onBack)
            BasicTextField(
                value = query,
                onValueChange = changeQuery,
                modifier = Modifier.padding(horizontal = 16.dp).padding(top = 8.dp)
                    .fillMaxWidth().testTag("search_input").semantics { contentDescription = title },
                singleLine = true,
                textStyle = style,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { submit() }),
                cursorBrush = SolidColor(PlaylistColors.Blue),
                decorationBox = { input ->
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 36.dp)
                            .background(if (dark) Color.White else Color(0xFFE6E8EB), RoundedCornerShape(8.dp)),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = submit, enabled = query.isNotBlank(), modifier = Modifier.size(36.dp)) {
                            Icon(painterResource(R.drawable.ic_search_field), stringResource(R.string.start_search),
                                Modifier.size(16.dp).offset(x = 2.dp), tint = hintColor)
                        }
                        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            if (query.isEmpty()) Text(title, style = style.copy(color = hintColor))
                            input()
                        }
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { changeQuery("") }, modifier = Modifier.size(40.dp, 36.dp)) {
                                Icon(painterResource(R.drawable.ic_clear_field), stringResource(R.string.clear_search),
                                    Modifier.size(40.dp, 36.dp), tint = hintColor)
                            }
                        } else {
                            Spacer(Modifier.width(12.dp))
                        }
                    }
                },
            )
            SearchResults(state.result, submit, openTrack, Modifier.weight(1f))
        }
    }
}
