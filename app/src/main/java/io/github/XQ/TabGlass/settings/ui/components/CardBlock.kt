package io.github.XQ.TabGlass.settings.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.XQ.TabGlass.settings.ui.theme.UiTokens
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

/**
 * 页面里的卡片容器：圆角 16dp、surfaceContainer 底色、底部留 12dp。
 * 内容用 Miuix 的 preference 行填，卡片本身不参与点击。
 */
@Composable
internal fun CardBlock(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    pressFeedbackType: PressFeedbackType = PressFeedbackType.Tilt,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = UiTokens.CardSpacing),
        cornerRadius = UiTokens.CardCornerRadius,
        insideMargin = PaddingValues(),
        colors = CardDefaults.defaultColors(
            color = MiuixTheme.colorScheme.surfaceContainer,
            contentColor = MiuixTheme.colorScheme.onSurfaceContainer
        ),
        pressFeedbackType = pressFeedbackType,
        showIndication = pressFeedbackType != PressFeedbackType.None,
        holdDownState = false,
        onClick = onClick,
        onLongPress = null
    ) {
        content()
    }
}
