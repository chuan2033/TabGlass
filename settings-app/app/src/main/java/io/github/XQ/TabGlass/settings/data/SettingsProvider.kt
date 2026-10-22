package io.github.XQ.TabGlass.settings.data

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Bundle

/**
 * 模块读取参数的只读入口。
 *
 * 模块的代码运行在微信 / QQ 进程里，拿不到本应用的 SharedPreferences，
 * 所以由本应用把当前值以 Bundle 的形式返回。只处理读取，不接受写入：
 * 参数只能在本应用的界面里改。
 */
class SettingsProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle {
        require(method == METHOD_GET) { "unsupported method: $method" }
        val context = context ?: error("provider created without a context")
        val settings = SettingsStore.load(context)
        return Bundle().apply {
            putInt(KEY_SCHEMA_VERSION, SCHEMA_VERSION)
            putInt(GlassSettings.KEY_PILL_WIDTH_PERCENT, settings.pillWidthPercent)
            putInt(GlassSettings.KEY_PILL_HEIGHT_DP, settings.pillHeightDp)
            putInt(GlassSettings.KEY_BAR_OFFSET_DP, settings.barOffsetDp)
            putInt(GlassSettings.KEY_ROW_PAD_DP, settings.rowPadDp)
            putInt(GlassSettings.KEY_COLUMN_PAD_DP, settings.columnPadDp)
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val context = context ?: error("provider created without a context")
        val settings = SettingsStore.load(context)
        val columns = projection ?: COLUMNS
        val cursor = MatrixCursor(columns)
        val values = mapOf(
            KEY_SCHEMA_VERSION to SCHEMA_VERSION,
            GlassSettings.KEY_PILL_WIDTH_PERCENT to settings.pillWidthPercent,
            GlassSettings.KEY_PILL_HEIGHT_DP to settings.pillHeightDp,
            GlassSettings.KEY_BAR_OFFSET_DP to settings.barOffsetDp,
            GlassSettings.KEY_ROW_PAD_DP to settings.rowPadDp,
            GlassSettings.KEY_COLUMN_PAD_DP to settings.columnPadDp
        )
        cursor.addRow(columns.map { values[it] })
        return cursor
    }

    override fun getType(uri: Uri): String = "vnd.android.cursor.item/vnd.$AUTHORITY.settings"

    override fun insert(uri: Uri, values: ContentValues?): Uri? =
        throw UnsupportedOperationException("settings are written in the app only")

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = throw UnsupportedOperationException("settings are written in the app only")

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int =
        throw UnsupportedOperationException("settings are written in the app only")

    companion object {
        const val AUTHORITY = "io.github.XQ.TabGlass.prefs"
        const val METHOD_GET = "get"
        const val SCHEMA_VERSION = 1

        const val KEY_SCHEMA_VERSION = "schemaVersion"

        private val COLUMNS = arrayOf(
            KEY_SCHEMA_VERSION,
            GlassSettings.KEY_PILL_WIDTH_PERCENT,
            GlassSettings.KEY_PILL_HEIGHT_DP,
            GlassSettings.KEY_BAR_OFFSET_DP,
            GlassSettings.KEY_ROW_PAD_DP,
            GlassSettings.KEY_COLUMN_PAD_DP
        )

        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/settings")
    }
}
