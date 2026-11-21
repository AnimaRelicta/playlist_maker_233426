package ru.student.playlistmaker.ui.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.student.playlistmaker.R
import ru.student.playlistmaker.ui.theme.PlaylistColors
import ru.student.playlistmaker.ui.theme.PlaylistFont

@Composable
fun MainScreen(
    onSearch: () -> Unit,
    onPlaylists: () -> Unit,
    onFavorites: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().background(PlaylistColors.Blue).testTag("main_screen")) {
        Box(Modifier.fillMaxWidth().height(70.dp)) {
            Text(stringResource(R.string.app_name),
                modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                color = androidx.compose.ui.graphics.Color.White,
                fontFamily = PlaylistFont, fontWeight = FontWeight.Medium,
                fontSize = 22.sp, lineHeight = 26.sp)
        }
        Surface(modifier = Modifier.fillMaxWidth().weight(1f).testTag("main_menu"),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(top = 8.dp)) {
                MenuRow(R.string.search, R.drawable.ic_search, "menu_search", onSearch)
                MenuRow(R.string.playlists, R.drawable.ic_playlists, "menu_playlists", onPlaylists)
                MenuRow(R.string.favorites, R.drawable.ic_favorite, "menu_favorites", onFavorites)
                MenuRow(R.string.settings, R.drawable.ic_settings, "menu_settings", onSettings)
            }
        }
    }
}

@Composable
private fun MenuRow(@StringRes title: Int, @DrawableRes icon: Int, tag: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(66.dp).testTag(tag)
        .clickable(role = Role.Button, onClick = onClick).padding(horizontal = 28.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
            val iconSize = if (icon == R.drawable.ic_favorite) Modifier.size(20.dp, 18.4.dp)
                else Modifier.size(24.dp)
            Icon(painterResource(icon), contentDescription = null, modifier = iconSize)
        }
        Spacer(Modifier.width(8.dp))
        Text(stringResource(title), Modifier.weight(1f), fontFamily = PlaylistFont,
            fontSize = 22.sp, lineHeight = 26.sp, fontWeight = FontWeight.Medium)
        Icon(painterResource(R.drawable.ic_chevron), contentDescription = null,
            modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
