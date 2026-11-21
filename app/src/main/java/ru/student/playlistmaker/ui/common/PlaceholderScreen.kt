package ru.student.playlistmaker.ui.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.student.playlistmaker.R
import ru.student.playlistmaker.ui.theme.PlaylistFont

/** Sprint 3 destination: the real search/settings UI is introduced in sprint 4. */
@Composable
fun PlaceholderScreen(@StringRes title: Int, tag: String, onBack: () -> Unit) {
    Surface(Modifier.fillMaxSize().testTag(tag)) {
        Row(Modifier.fillMaxWidth().height(56.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.size(56.dp)) {
                Icon(painterResource(R.drawable.ic_back), stringResource(R.string.back), Modifier.size(48.dp))
            }
            Text(stringResource(title), Modifier.padding(top = 16.dp),
                fontFamily = PlaylistFont, fontSize = 22.sp, fontWeight = FontWeight.Medium)
        }
    }
}
