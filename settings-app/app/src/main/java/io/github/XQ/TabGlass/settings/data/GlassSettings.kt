package io.github.XQ.TabGlass.settings.data

/**
 * 底栏的几何参数。默认值与模块内的 GlassConfig 保持一致，
 * 这样设置应用没有改过任何值的时候，宿主看到的行为和模块自带默认完全相同。
 */
data class GlassSettings(
    val pillWidthPercent: Int = DEFAULT_PILL_WIDTH_PERCENT,
    val pillHeightDp: Int = DEFAULT_PILL_HEIGHT_DP,
    val barOffsetDp: Int = DEFAULT_BAR_OFFSET_DP,
    val rowPadDp: Int = DEFAULT_ROW_PAD_DP,
    val columnPadDp: Int = DEFAULT_COLUMN_PAD_DP
) {

    /** 写进 SharedPreferences 之前统一收进合法区间，滑杆与旧数据都不会越界。 */
    fun coerced(): GlassSettings = GlassSettings(
        pillWidthPercent = pillWidthPercent.coerceIn(PILL_WIDTH_RANGE),
        pillHeightDp = pillHeightDp.coerceIn(PILL_HEIGHT_RANGE),
        barOffsetDp = barOffsetDp.coerceIn(BAR_OFFSET_RANGE),
        rowPadDp = rowPadDp.coerceIn(ROW_PAD_RANGE),
        columnPadDp = columnPadDp.coerceIn(COLUMN_PAD_RANGE)
    )

    companion object {
        const val PREFS_NAME = "glass_settings"

        const val KEY_PILL_WIDTH_PERCENT = "pillWidthPercent"
        const val KEY_PILL_HEIGHT_DP = "pillHeightDp"
        const val KEY_BAR_OFFSET_DP = "barOffsetDp"
        const val KEY_ROW_PAD_DP = "rowPadDp"
        const val KEY_COLUMN_PAD_DP = "columnPadDp"
        const val KEY_THEME_MODE = "themeMode"

        const val DEFAULT_PILL_WIDTH_PERCENT = 80
        const val DEFAULT_PILL_HEIGHT_DP = 69
        const val DEFAULT_BAR_OFFSET_DP = 12
        const val DEFAULT_ROW_PAD_DP = 4
        const val DEFAULT_COLUMN_PAD_DP = 50

        val PILL_WIDTH_RANGE = 60..100
        val PILL_HEIGHT_RANGE = 56..88
        val BAR_OFFSET_RANGE = 4..32
        val ROW_PAD_RANGE = 2..12
        val COLUMN_PAD_RANGE = 24..96
    }
}

/** 主题模式与本应用界面有关，模块不读取。 */
enum class ThemeMode(val index: Int) {
    SYSTEM(0),
    LIGHT(1),
    DARK(2);

    /** 跟随系统时返回 null，由界面自己判断系统深浅。 */
    val forcedDark: Boolean?
        get() = when (this) {
            SYSTEM -> null
            LIGHT -> false
            DARK -> true
        }

    companion object {
        fun fromIndex(index: Int): ThemeMode = entries.firstOrNull { it.index == index } ?: SYSTEM
    }
}
