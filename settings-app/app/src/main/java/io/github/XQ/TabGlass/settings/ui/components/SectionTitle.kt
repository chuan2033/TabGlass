package io.github.XQ.TabGlass.settings.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import io.github.XQ.TabGlass.settings.ui.theme.UiTokens
import top.yukonga.miuix.kmp.basic.SmallTitle

/** 卡片前的分组标题。左侧留白与卡片内文字对齐。 */
@Composable
internal fun SectionTitle(text: String) {
    SmallTitle(
        text = text,
        insideMargin = PaddingValues(UiTokens.ListHorizontalPadding, UiTokens.SectionTitleMargin)
    )
}
