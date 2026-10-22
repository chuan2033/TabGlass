package io.github.XQ.TabGlass.settings.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.XQ.TabGlass.R
import io.github.XQ.TabGlass.settings.data.GlassSettings
import io.github.XQ.TabGlass.settings.data.ThemeMode
import io.github.XQ.TabGlass.settings.ui.components.BlurredBar
import io.github.XQ.TabGlass.settings.ui.components.FloatingBottomBar
import io.github.XQ.TabGlass.settings.ui.theme.UiTokens
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NavigationItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Show
import top.yukonga.miuix.kmp.icon.extended.Tune
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

/** 三个页面：主页看状态、预览调参数并看比例、关于看版本。 */
internal enum class HomeTab(val labelRes: Int, val icon: ImageVector) {
    HOME(R.string.nav_home, MiuixIcons.Tune),
    PREVIEW(R.string.nav_preview, MiuixIcons.Show),
    ABOUT(R.string.nav_about, MiuixIcons.Info)
}

/** 悬浮底栏离屏幕底部的距离，与背屏模块的主界面一致。 */
private val FloatingBarBottomPadding = 16.dp
private val FloatingBarOffsetY = 12.dp

@Composable
internal fun GlassHomeScreen(
    settings: GlassSettings,
    themeMode: ThemeMode,
    onSettingsChange: (GlassSettings) -> Unit,
    onSettingsCommit: (GlassSettings) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    val tabs = HomeTab.entries
    val pagerState = rememberPagerState { tabs.size }
    val scope = rememberCoroutineScope()
    // 每个页面的滚动位置分开保存，切页回来不会跳回顶部。
    val listStates = List(tabs.size) { rememberLazyListState() }
    val surfaceColor = MiuixTheme.colorScheme.surface
    // 悬浮底栏的取样层：整块内容与底栏共用一个，模糊才能采到页面本身。
    val bottomBarBackdrop = rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(bottomBarBackdrop),
            beyondViewportPageCount = tabs.size - 1
        ) { page ->
            TabPage(
                tab = tabs[page],
                listState = listStates[page],
                settings = settings,
                themeMode = themeMode,
                onSettingsChange = onSettingsChange,
                onSettingsCommit = onSettingsCommit,
                onThemeModeChange = onThemeModeChange
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = FloatingBarBottomPadding)
                .offset(y = FloatingBarOffsetY)
        ) {
            FloatingBottomBar(
                items = tabs.map {
                    NavigationItem(stringResource(it.labelRes), it.icon)
                },
                selectedIndex = pagerState.currentPage,
                onItemClick = { index ->
                    scope.launch { pagerState.animateScrollToPage(index) }
                },
                backdrop = bottomBarBackdrop,
                isBlurActive = true
            )
        }
    }
}

@Composable
private fun TabPage(
    tab: HomeTab,
    listState: LazyListState,
    settings: GlassSettings,
    themeMode: ThemeMode,
    onSettingsChange: (GlassSettings) -> Unit,
    onSettingsCommit: (GlassSettings) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    val scrollBehavior = MiuixScrollBehavior(rememberTopAppBarState())
    val surfaceColor = MiuixTheme.colorScheme.surface
    // 每个页面各自持有一份采样层，切换动画期间不会互相覆盖坐标。
    val backdrop = rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
    val title = stringResource(tab.labelRes)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = surfaceColor,
        topBar = {
            BlurredBar(backdrop) {
                TopAppBar(
                    title = title,
                    largeTitle = title,
                    color = Color.Transparent,
                    scrollBehavior = scrollBehavior
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .scrollEndHaptic()
                    .overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(horizontal = UiTokens.ContentHorizontalPadding),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding()
                )
            ) {
                item {
                    when (tab) {
                        HomeTab.HOME -> HomePage(
                            themeMode = themeMode,
                            onSettingsChange = onSettingsChange,
                            onSettingsCommit = onSettingsCommit,
                            onThemeModeChange = onThemeModeChange
                        )
                        HomeTab.PREVIEW -> PreviewPage(
                            settings = settings,
                            onSettingsChange = onSettingsChange,
                            onSettingsCommit = onSettingsCommit
                        )
                        HomeTab.ABOUT -> AboutPage()
                    }
                }
                item {
                    // 悬浮底栏压在内容之上，最后一项要留出它的高度与下方间距。
                    Spacer(
                        Modifier
                            .height(UiTokens.FloatingBarContentSpacer)
                            .navigationBarsPadding()
                    )
                }
            }
        }
    }
}
