package io.github.XQ.TabGlass.settings.ui.components

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.XQ.TabGlass.R
import io.github.XQ.TabGlass.settings.ui.theme.UiTokens
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

private val CardHeight = 118.dp
private val GlyphSize = 116.dp
private val GlyphOffsetX = 26.dp
private val GlyphOffsetY = 30.dp

// Miuix 的颜色令牌里没有绿色系，正常状态只能写死；浅色用浅绿底配亮绿勾，深色用深绿底。
private val WorkingContainerLight = Color(0xFFDFFAE4)
private val WorkingContainerDark = Color(0xFF1A3825)
private val WorkingGlyph = Color(0xFF36D167)

/**
 * 顶部状态卡：模块装了没有、两个宿主是什么版本。
 *
 * 版式与背屏模块首页的状态卡一致——左侧两行文字，右下角一个描边圆环记号，
 * 圆环里是勾或叉。记号特意溢出卡片右下角，靠卡片的圆角裁切收边。
 */
@Composable
internal fun ModuleStatusCard(moduleInstalled: Boolean, hostSummary: String) {
    val dark = MiuixTheme.colorScheme.surface.luminance() < 0.5f
    val containerColor = when {
        !moduleInstalled -> MiuixTheme.colorScheme.errorContainer
        dark -> WorkingContainerDark
        else -> WorkingContainerLight
    }
    val contentColor = if (moduleInstalled) {
        MiuixTheme.colorScheme.onSurfaceContainer
    } else {
        MiuixTheme.colorScheme.onErrorContainer
    }
    val glyphColor = if (moduleInstalled) WorkingGlyph else MiuixTheme.colorScheme.error

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = UiTokens.CardSpacing),
        insideMargin = PaddingValues(),
        cornerRadius = UiTokens.CardCornerRadius,
        colors = CardDefaults.defaultColors(color = containerColor, contentColor = contentColor),
        pressFeedbackType = PressFeedbackType.None,
        showIndication = false,
        holdDownState = false,
        onClick = null,
        onLongPress = null
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(CardHeight)) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = GlyphOffsetX, y = GlyphOffsetY)
            ) {
                StatusGlyph(
                    activated = moduleInstalled,
                    color = glyphColor,
                    modifier = Modifier.size(GlyphSize)
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = UiTokens.ListHorizontalPadding, top = 14.dp, end = UiTokens.ListHorizontalPadding)
            ) {
                Text(
                    text = stringResource(
                        if (moduleInstalled) R.string.status_installed else R.string.status_missing
                    ),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = hostSummary,
                    modifier = Modifier.padding(top = 3.dp),
                    fontSize = 15.sp,
                    color = contentColor.copy(alpha = 0.78f),
                    maxLines = 2
                )
            }
        }
    }
}

/** 描边圆环加勾：勾是圆头折线，和背屏模块首页的状态记号同一套画法。 */
@Composable
private fun StatusGlyph(
    activated: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stroke = size.minDimension * 0.075f
        drawCircle(
            color = color,
            radius = size.minDimension / 2f - stroke / 2f,
            style = Stroke(width = stroke)
        )
        if (activated) {
            val check = Path().apply {
                moveTo(size.width * 0.30f, size.height * 0.52f)
                lineTo(size.width * 0.44f, size.height * 0.66f)
                lineTo(size.width * 0.71f, size.height * 0.35f)
            }
            drawPath(
                path = check,
                color = color,
                style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        } else {
            val inset = size.minDimension * 0.31f
            drawLine(
                color = color,
                start = Offset(inset, inset),
                end = Offset(size.width - inset, size.height - inset),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            drawLine(
                color = color,
                start = Offset(size.width - inset, inset),
                end = Offset(inset, size.height - inset),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }
}

/** 读取两个宿主应用的版本，读不到就报未安装。 */
internal fun hostSummary(context: Context): String {
    val wechat = packageVersion(context, "com.tencent.mm")
    val qq = packageVersion(context, "com.tencent.mobileqq")
    val wechatLabel = context.getString(R.string.host_wechat)
    val qqLabel = context.getString(R.string.host_qq)
    return listOfNotNull(
        wechat?.let { "$wechatLabel $it" },
        qq?.let { "$qqLabel $it" }
    ).ifEmpty { listOf(context.getString(R.string.host_none)) }.joinToString(" · ")
}

/**
 * 界面与模块已经合进同一个 APK，模块必然"在场"，所以这里不再查包是否存在，
 * 只保留勾号表示「本应用即模块」。宿主版本才是真正会变的那个信息。
 */
internal fun isModuleInstalled(@Suppress("UNUSED_PARAMETER") context: Context): Boolean = true

private fun packageVersion(context: Context, packageName: String): String? = runCatching {
    context.packageManager.getPackageInfo(packageName, 0).versionName
}.getOrNull()
