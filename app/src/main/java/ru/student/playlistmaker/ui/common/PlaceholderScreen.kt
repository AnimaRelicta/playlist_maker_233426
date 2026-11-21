package ru.student.playlistmaker.ui.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.student.playlistmaker.R
import ru.student.playlistmaker.ui.theme.PlaylistFont

@Composable
fun ScreenHeader(@StringRes title: Int, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.size(56.dp)) {
            Icon(painterResource(R.drawable.ic_back), stringResource(R.string.back), Modifier.size(48.dp))
        }
        Text(stringResource(title), Modifier.padding(start = 8.dp),
            fontFamily = PlaylistFont, fontSize = 22.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun PlaceholderScreen(@StringRes title: Int, tag: String, onBack: () -> Unit) {
    Surface(Modifier.fillMaxSize().testTag(tag)) { Box { ScreenHeader(title, onBack) } }
}
