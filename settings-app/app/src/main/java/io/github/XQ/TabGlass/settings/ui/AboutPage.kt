package io.github.XQ.TabGlass.settings.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.github.XQ.TabGlass.BuildConfig
import io.github.XQ.TabGlass.R
import io.github.XQ.TabGlass.settings.ui.components.CardBlock
import io.github.XQ.TabGlass.settings.ui.components.InfoRow
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.utils.PressFeedbackType

private const val PROJECT_URL = "https://github.com/chuan2033/TabGlass"
private const val UPSTREAM_URL = "https://github.com/liuran001/WeChat-LiquidGlass"

@Composable
internal fun AboutPage() {
    val context = LocalContext.current

    fun open(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    Column {
        CardBlock(pressFeedbackType = PressFeedbackType.None) {
            InfoRow(
                label = stringResource(R.string.about_version),
                value = BuildConfig.VERSION_NAME
            )
            ArrowPreference(
                title = stringResource(R.string.about_repo_title),
                summary = stringResource(R.string.about_repo_summary),
                onClick = { open(PROJECT_URL) }
            )
            ArrowPreference(
                title = stringResource(R.string.about_upstream_title),
                summary = stringResource(R.string.about_upstream_summary),
                onClick = { open(UPSTREAM_URL) }
            )
            ArrowPreference(
                title = stringResource(R.string.about_license_title),
                summary = stringResource(R.string.about_license_summary),
                onClick = { open("$PROJECT_URL/blob/main/LICENSE") }
            )
        }
    }
}
