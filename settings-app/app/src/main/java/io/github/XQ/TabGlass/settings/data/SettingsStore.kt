package io.github.XQ.TabGlass.settings.data

import android.content.Context
import android.content.SharedPreferences

/** 本应用自己的偏好文件，模块通过 [SettingsProvider] 读取同一份内容。 */
object SettingsStore {

    fun preferences(context: Context): SharedPreferences =
        context.getSharedPreferences(GlassSettings.PREFS_NAME, Context.MODE_PRIVATE)

    fun load(context: Context): GlassSettings {
        val preferences = preferences(context)
        return GlassSettings(
            pillWidthPercent = preferences.getInt(
                GlassSettings.KEY_PILL_WIDTH_PERCENT,
                GlassSettings.DEFAULT_PILL_WIDTH_PERCENT
            ),
            pillHeightDp = preferences.getInt(
                GlassSettings.KEY_PILL_HEIGHT_DP,
                GlassSettings.DEFAULT_PILL_HEIGHT_DP
            ),
            barOffsetDp = preferences.getInt(
                GlassSettings.KEY_BAR_OFFSET_DP,
                GlassSettings.DEFAULT_BAR_OFFSET_DP
            ),
            rowPadDp = preferences.getInt(
                GlassSettings.KEY_ROW_PAD_DP,
                GlassSettings.DEFAULT_ROW_PAD_DP
            ),
            columnPadDp = preferences.getInt(
                GlassSettings.KEY_COLUMN_PAD_DP,
                GlassSettings.DEFAULT_COLUMN_PAD_DP
            )
        ).coerced()
    }

    fun save(context: Context, settings: GlassSettings) {
        val safe = settings.coerced()
        preferences(context).edit()
            .putInt(GlassSettings.KEY_PILL_WIDTH_PERCENT, safe.pillWidthPercent)
            .putInt(GlassSettings.KEY_PILL_HEIGHT_DP, safe.pillHeightDp)
            .putInt(GlassSettings.KEY_BAR_OFFSET_DP, safe.barOffsetDp)
            .putInt(GlassSettings.KEY_ROW_PAD_DP, safe.rowPadDp)
            .putInt(GlassSettings.KEY_COLUMN_PAD_DP, safe.columnPadDp)
            .apply()
    }

    fun loadThemeMode(context: Context): ThemeMode = ThemeMode.fromIndex(
        preferences(context).getInt(GlassSettings.KEY_THEME_MODE, ThemeMode.SYSTEM.index)
    )

    fun saveThemeMode(context: Context, mode: ThemeMode) {
        preferences(context).edit().putInt(GlassSettings.KEY_THEME_MODE, mode.index).apply()
    }
}
