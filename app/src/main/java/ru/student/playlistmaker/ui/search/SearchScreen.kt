package ru.student.playlistmaker.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.student.playlistmaker.R
import ru.student.playlistmaker.ui.common.ScreenHeader
import ru.student.playlistmaker.ui.theme.PlaylistColors
import ru.student.playlistmaker.ui.theme.PlaylistFont

@Composable
fun SearchScreen(onBack: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val dark = MaterialTheme.colorScheme.surface == PlaylistColors.Ink
    val placeholderColor = if (dark) PlaylistColors.Ink else PlaylistColors.Gray
    val style = TextStyle(color = PlaylistColors.Ink, fontSize = 16.sp, fontFamily = PlaylistFont)
    val title = stringResource(R.string.search)
    Surface(Modifier.fillMaxSize().testTag("search_screen")) {
        Column {
            ScreenHeader(R.string.search, onBack)
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.padding(horizontal = 16.dp).padding(top = 8.dp)
                    .fillMaxWidth().testTag("search_input").semantics { contentDescription = title },
                singleLine = true,
                textStyle = style,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                cursorBrush = SolidColor(PlaylistColors.Blue),
                decorationBox = { input ->
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 36.dp)
                            .background(if (dark) Color.White else Color(0xFFE6E8EB), RoundedCornerShape(8.dp)),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(painterResource(R.drawable.ic_search_field), null,
                            Modifier.padding(start = 12.dp, end = 8.dp).size(16.dp), tint = placeholderColor)
                        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            if (query.isEmpty()) Text(title, style = style.copy(color = placeholderColor))
                            input()
                        }
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }, modifier = Modifier.size(40.dp, 36.dp)) {
                                Icon(painterResource(R.drawable.ic_clear_field), stringResource(R.string.clear_search),
                                    Modifier.size(40.dp, 36.dp), tint = placeholderColor)
                            }
                        } else {
                            Spacer(Modifier.width(12.dp))
                        }
                    }
                },
            )
        }
    }
}
