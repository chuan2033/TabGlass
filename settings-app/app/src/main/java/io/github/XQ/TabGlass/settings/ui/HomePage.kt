package io.github.XQ.TabGlass.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.github.XQ.TabGlass.R
import io.github.XQ.TabGlass.settings.data.GlassSettings
import io.github.XQ.TabGlass.settings.data.ThemeMode
import io.github.XQ.TabGlass.settings.ui.components.CardBlock
import io.github.XQ.TabGlass.settings.ui.components.ModuleStatusCard
import io.github.XQ.TabGlass.settings.ui.components.SectionTitle
import io.github.XQ.TabGlass.settings.ui.components.hostSummary
import io.github.XQ.TabGlass.settings.ui.components.isModuleInstalled
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.utils.PressFeedbackType

/**
 * 主页：模块状态与界面偏好。底栏的几何参数放在预览页，调的时候能直接看到比例变化。
 */
@Composable
internal fun HomePage(
    themeMode: ThemeMode,
    onSettingsChange: (GlassSettings) -> Unit,
    onSettingsCommit: (GlassSettings) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    val context = LocalContext.current
    val moduleInstalled = remember { isModuleInstalled(context) }
    val hosts = remember { hostSummary(context) }

    Column {
        ModuleStatusCard(moduleInstalled = moduleInstalled, hostSummary = hosts)

        SectionTitle(stringResource(R.string.home_theme_title))
        CardBlock(pressFeedbackType = PressFeedbackType.None) {
            WindowDropdownPreference(
                items = listOf(
                    stringResource(R.string.theme_system),
                    stringResource(R.string.theme_light),
                    stringResource(R.string.theme_dark)
                ),
                selectedIndex = themeMode.index,
                title = stringResource(R.string.home_theme_mode_title),
                onSelectedIndexChange = { onThemeModeChange(ThemeMode.fromIndex(it)) }
            )
            ArrowPreference(
                title = stringResource(R.string.home_reset_title),
                summary = stringResource(R.string.home_reset_summary),
                onClick = {
                    val defaults = GlassSettings()
                    onSettingsChange(defaults)
                    onSettingsCommit(defaults)
                }
            )
        }
    }
}
