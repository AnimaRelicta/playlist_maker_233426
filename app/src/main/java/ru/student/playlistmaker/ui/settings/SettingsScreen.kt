package ru.student.playlistmaker.ui.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ru.student.playlistmaker.R
import ru.student.playlistmaker.ui.common.ScreenHeader
import ru.student.playlistmaker.ui.theme.PlaylistColors
import ru.student.playlistmaker.ui.theme.PlaylistFont

internal fun shareIntent(message: String): Intent = Intent.createChooser(
    Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, message), null,
)

internal fun supportIntent(email: String, subject: String, body: String): Intent =
    Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + Uri.encode(email, "@") +
        "?subject=" + Uri.encode(subject) + "&body=" + Uri.encode(body)))
        .apply { if (email.isNotBlank()) putExtra(Intent.EXTRA_EMAIL, arrayOf(email)) }
        .putExtra(Intent.EXTRA_SUBJECT, subject)
        .putExtra(Intent.EXTRA_TEXT, body)

internal fun agreementIntent(url: String): Intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

@Composable
fun SettingsScreen(
    dark: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val message = stringResource(R.string.share_message)
    val email = stringResource(R.string.support_email)
    val subject = stringResource(R.string.support_subject)
    val body = stringResource(R.string.support_body)
    val agreement = stringResource(R.string.agreement_url)
    val noHandler = stringResource(R.string.no_handler)
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    fun open(intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            scope.launch { snackbar.showSnackbar(noHandler) }
        }
    }

    Surface(Modifier.fillMaxSize().testTag("settings_screen")) {
        Box {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                ScreenHeader(R.string.settings, onBack)
                Spacer(Modifier.height(24.dp))
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 61.dp)
                        .toggleable(dark, role = Role.Switch, onValueChange = onThemeChange)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SettingsLabel(R.string.dark_theme, Modifier.weight(1f))
                    Box(Modifier.size(38.dp, 24.dp), contentAlignment = Alignment.Center) {
                        Box(Modifier.size(32.dp, 12.dp).background(
                            if (dark) Color(0xFF284581) else Color(0xFFF0F1F3), CircleShape))
                        Box(Modifier.align(if (dark) Alignment.CenterEnd else Alignment.CenterStart)
                            .size(18.dp).background(if (dark) PlaylistColors.Blue else Color(0xFFCECFD2), CircleShape))
                    }
                }
                SettingsRow(R.string.share, R.drawable.ic_share) { open(shareIntent(message)) }
                SettingsRow(R.string.support, R.drawable.ic_support) { open(supportIntent(email, subject, body)) }
                SettingsRow(R.string.agreement, R.drawable.ic_chevron) { open(agreementIntent(agreement)) }
            }
            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun SettingsRow(@StringRes title: Int, @DrawableRes icon: Int, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 61.dp).clickable(role = Role.Button, onClick = onClick)
        .padding(start = 16.dp, end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        SettingsLabel(title, Modifier.weight(1f))
        Icon(painterResource(icon), null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingsLabel(@StringRes title: Int, modifier: Modifier) {
    Text(stringResource(title), modifier, fontFamily = PlaylistFont, fontSize = 16.sp)
}
