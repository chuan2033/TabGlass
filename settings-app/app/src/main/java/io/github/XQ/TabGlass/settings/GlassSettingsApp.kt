package io.github.XQ.TabGlass.settings

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsControllerCompat
import io.github.XQ.TabGlass.settings.data.SettingsStore
import io.github.XQ.TabGlass.settings.ui.GlassHomeScreen
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme

/**
 * 应用的根容器：负责主题、系统栏样式和参数状态。
 *
 * 参数改动先更新界面状态（预览页实时跟随），松手提交时才写入偏好文件。
 */
@Composable
internal fun GlassSettingsApp() {
    val context = LocalContext.current
    var settings by remember { mutableStateOf(SettingsStore.load(context)) }
    var themeMode by remember { mutableStateOf(SettingsStore.loadThemeMode(context)) }

    val isDark = themeMode.forcedDark ?: isSystemInDarkTheme()
    LaunchedEffect(isDark) {
        val activity = context as? ComponentActivity ?: return@LaunchedEffect
        activity.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { isDark },
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { isDark }
        )
        activity.window.isNavigationBarContrastEnforced = false
        WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
            isAppearanceLightStatusBars = !isDark
            isAppearanceLightNavigationBars = !isDark
        }
    }

    MiuixTheme(colors = if (isDark) darkColorScheme() else lightColorScheme()) {
        GlassHomeScreen(
            settings = settings,
            themeMode = themeMode,
            onSettingsChange = { settings = it },
            onSettingsCommit = { SettingsStore.save(context, it) },
            onThemeModeChange = {
                themeMode = it
                SettingsStore.saveThemeMode(context, it)
            }
        )
    }
}
