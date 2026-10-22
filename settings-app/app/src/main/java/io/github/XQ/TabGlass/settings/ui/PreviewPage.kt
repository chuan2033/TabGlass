package io.github.XQ.TabGlass.settings.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.XQ.TabGlass.R
import io.github.XQ.TabGlass.settings.data.GlassSettings
import io.github.XQ.TabGlass.settings.ui.components.CardBlock
import io.github.XQ.TabGlass.settings.ui.components.SectionTitle
import io.github.XQ.TabGlass.settings.ui.components.SettingsInfoRow
import io.github.XQ.TabGlass.settings.ui.theme.UiTokens
import kotlin.math.roundToInt
import top.yukonga.miuix.kmp.preference.SliderPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

/**
 * 预览页：上半按手机实际宽度把参数画成一条底栏，下半就是这几个参数本身。
 * 画布只表达尺寸与层次，不呈现折射与模糊；拖动滑杆时预览同步变化，松手才写入偏好。
 */
@Composable
internal fun PreviewPage(
    settings: GlassSettings,
    onSettingsChange: (GlassSettings) -> Unit,
    onSettingsCommit: (GlassSettings) -> Unit
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.toFloat().coerceAtLeast(1f)
    val colors = MiuixTheme.colorScheme
    val pillWidthDp = screenWidthDp * settings.pillWidthPercent / 100f

    Column {
        SectionTitle(stringResource(R.string.preview_title))
        CardBlock(pressFeedbackType = PressFeedbackType.None) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .padding(UiTokens.ListHorizontalPadding)
            ) {
                val scale = size.width / screenWidthDp
                val pillWidth = size.width * settings.pillWidthPercent / 100f
                val pillHeight = settings.pillHeightDp * scale
                val bottomGap = settings.barOffsetDp * scale
                val inset = settings.rowPadDp * scale
                val columnWidth = (pillWidth - inset * 2f) / PREVIEW_COLUMN_COUNT
                val left = (size.width - pillWidth) / 2f
                val bottom = size.height - bottomGap
                val top = bottom - pillHeight
                val pageRadius = 14.dp.toPx()

                // 页面底色，用来说明底栏是浮在内容之上的一层。
                drawRoundRect(
                    color = colors.surface,
                    topLeft = Offset.Zero,
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(pageRadius, pageRadius)
                )

                // 几行示意内容，只为了让人看出底栏压住的是页面本身。
                val rowHeight = 10.dp.toPx()
                val rowLeft = 16.dp.toPx()
                val rowRight = size.width - 16.dp.toPx()
                val rowGap = 14.dp.toPx()
                var rowY = 16.dp.toPx()
                val widths = floatArrayOf(0.62f, 0.45f, 0.68f, 0.38f)
                for (fraction in widths) {
                    if (rowY + rowHeight > top - rowGap) {
                        break
                    }
                    drawRoundRect(
                        color = colors.onSurface.copy(alpha = 0.07f),
                        topLeft = Offset(rowLeft, rowY),
                        size = Size((rowRight - rowLeft) * fraction, rowHeight),
                        cornerRadius = CornerRadius(rowHeight / 2f, rowHeight / 2f)
                    )
                    rowY += rowHeight + rowGap
                }

                // 胶囊本体。
                val pillRadius = pillHeight / 2f
                drawRoundRect(
                    color = colors.surfaceContainer,
                    topLeft = Offset(left, top),
                    size = Size(pillWidth, pillHeight),
                    cornerRadius = CornerRadius(pillRadius, pillRadius)
                )
                drawRoundRect(
                    color = colors.onSurface.copy(alpha = 0.08f),
                    topLeft = Offset(left, top),
                    size = Size(pillWidth, pillHeight),
                    cornerRadius = CornerRadius(pillRadius, pillRadius),
                    style = Stroke(width = 1.dp.toPx())
                )

                // 选中项的液滴，落在第一列。
                val dropletWidth = columnWidth
                val dropletHeight = pillHeight - inset * 2f
                drawRoundRect(
                    color = colors.primary.copy(alpha = 0.16f),
                    topLeft = Offset(left + inset, top + inset),
                    size = Size(dropletWidth, dropletHeight),
                    cornerRadius = CornerRadius(dropletHeight / 2f, dropletHeight / 2f)
                )
            }
        }

        CardBlock(pressFeedbackType = PressFeedbackType.None) {
            SettingsInfoRow(
                label = stringResource(R.string.preview_caption_width),
                value = stringResource(R.string.common_dp, pillWidthDp.roundToInt())
            )
            SettingsInfoRow(
                label = stringResource(R.string.preview_caption_height),
                value = stringResource(R.string.common_dp, settings.pillHeightDp)
            )
            SettingsInfoRow(
                label = stringResource(R.string.preview_caption_inset),
                value = stringResource(R.string.common_dp, settings.rowPadDp)
            )
        }

        SectionTitle(stringResource(R.string.bar_size_title))
        CardBlock(pressFeedbackType = PressFeedbackType.None) {
            SliderPreference(
                value = settings.pillWidthPercent.toFloat(),
                onValueChange = {
                    onSettingsChange(settings.copy(pillWidthPercent = it.roundToInt()))
                },
                title = stringResource(R.string.bar_pill_width_title),
                valueText = stringResource(R.string.common_percent, settings.pillWidthPercent),
                valueRange = GlassSettings.PILL_WIDTH_RANGE.range(),
                onValueChangeFinished = { onSettingsCommit(settings) }
            )
            SliderPreference(
                value = settings.pillHeightDp.toFloat(),
                onValueChange = {
                    onSettingsChange(settings.copy(pillHeightDp = it.roundToInt()))
                },
                title = stringResource(R.string.bar_pill_height_title),
                valueText = stringResource(R.string.common_dp, settings.pillHeightDp),
                valueRange = GlassSettings.PILL_HEIGHT_RANGE.range(),
                onValueChangeFinished = { onSettingsCommit(settings) }
            )
            SliderPreference(
                value = settings.barOffsetDp.toFloat(),
                onValueChange = {
                    onSettingsChange(settings.copy(barOffsetDp = it.roundToInt()))
                },
                title = stringResource(R.string.bar_offset_title),
                valueText = stringResource(R.string.common_dp, settings.barOffsetDp),
                valueRange = GlassSettings.BAR_OFFSET_RANGE.range(),
                onValueChangeFinished = { onSettingsCommit(settings) }
            )
        }

        SectionTitle(stringResource(R.string.bar_inset_title))
        CardBlock(pressFeedbackType = PressFeedbackType.None) {
            SliderPreference(
                value = settings.rowPadDp.toFloat(),
                onValueChange = {
                    onSettingsChange(settings.copy(rowPadDp = it.roundToInt()))
                },
                title = stringResource(R.string.bar_row_pad_title),
                valueText = stringResource(R.string.common_dp, settings.rowPadDp),
                valueRange = GlassSettings.ROW_PAD_RANGE.range(),
                onValueChangeFinished = { onSettingsCommit(settings) }
            )
            SliderPreference(
                value = settings.columnPadDp.toFloat(),
                onValueChange = {
                    onSettingsChange(settings.copy(columnPadDp = it.roundToInt()))
                },
                title = stringResource(R.string.bar_column_pad_title),
                valueText = stringResource(R.string.common_dp, settings.columnPadDp),
                valueRange = GlassSettings.COLUMN_PAD_RANGE.range(),
                onValueChangeFinished = { onSettingsCommit(settings) }
            )
        }
    }
}

private fun IntRange.range(): ClosedFloatingPointRange<Float> = first.toFloat()..last.toFloat()

/** 预览按四个标签列画，与微信底栏的列数一致。 */
private const val PREVIEW_COLUMN_COUNT = 4
