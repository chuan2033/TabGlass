# TabGlass · 设置界面

TabGlass 只有一个 APK：包名 `io.github.XQ.TabGlass`，既是 LSPosed 注入微信 / QQ 的模块
本体，也是桌面上点得开的设置界面。这个目录就是那个 APK 的 Gradle 工程——模块的 Java 源码
（`java/io/github/XQ/TabGlass/*.java`）和 `META-INF/xposed` 元数据都在 `app/src/main/` 下。

界面用 Miuix 组件库写成，结构、组件选型与间距取自同作者的背屏模块（`MiBackscreen`）：
`MiuixTheme` 包住整棵树，`Scaffold` + `BlurredBar` + `TopAppBar(largeTitle)` 做页面骨架，
内容统一是 `LazyColumn(horizontal = 12dp)` 里的一段 `SmallTitle` + `CardBlock`，底部是悬浮
玻璃底栏。

## 构建

```bash
./gradlew :app:assembleDebug
# 产物：app/build/outputs/apk/debug/app-debug.apk
```

仓库根目录的 `./build.sh` 就是对这条命令的包装，产物会拷成 `TabGlass-v<version>.apk`。

依赖版本与背屏模块保持一致：AGP 9.2.1、Kotlin 2.3.21、Miuix 0.9.4。`local.properties`
里的 `sdk.dir` 指向本机 Android SDK（compileSdk 37、build-tools 37.0.0）。libxposed
`api-102` 从 `../tools/xapi/classes.jar` 以 `compileOnly` 引入，运行时由框架提供。

## 参数

| 键 | 名称 | 范围 | 默认 |
|---|---|---|---|
| `pillWidthPercent` | 底栏宽度（占屏宽百分比） | 60–100 | 80 |
| `pillHeightDp` | 底栏高度 | 56–88 | 69 |
| `barOffsetDp` | 底栏下边缘到屏幕底部的距离 | 4–32 | 12 |
| `rowPadDp` | 液滴四边内缩量，同时是 tab 行左右内边距 | 2–12 | 4 |
| `columnPadDp` | 单列内容的左右最小余量 | 24–96 | 50 |

默认值与模块内 `GlassConfig` 的初值一致：设置应用从未写过任何值的时候，宿主看到的行为与
模块自带默认完全相同。

## 参数通道

模块的代码运行在微信 / QQ 进程里，读不到本应用的偏好文件，所以由本应用把当前值以只读
`ContentProvider` 的形式发布：

```
authority: io.github.XQ.TabGlass.prefs
call("get")        -> Bundle，键名与上表一致，另带 schemaVersion
query(uri, ...)    -> 一行 MatrixCursor，列名同上
```

写入只发生在本应用自己的 `SharedPreferences`，Provider 不接受 `insert` / `update` / `delete`。
模块侧的读取顺序是：宿主自己的偏好文件 → 上面的 Provider（有则以它为准）。

模块在读到 Provider 的值之后会顺手写回宿主的偏好文件，因此设置应用被系统强制停止（此时任何
应用都无法再启动它的 Provider）时，宿主仍然沿用上一次生效的值，不会掉回内置默认。

改完参数切回宿主应用，模块在主界面 `onResume` 时重新读取并就地重算底栏几何，不需要重启微信
或 QQ。

## 界面结构

| 位置 | 组件 |
|---|---|
| 顶栏 | `BlurredBar`（`progressiveTextureBlur`）+ `TopAppBar(largeTitle)` |
| 分组标题 | `SmallTitle(insideMargin = 16dp, 8dp)` |
| 卡片 | `CardBlock`：`Card`，圆角 16dp，`surfaceContainer`，底部 12dp |
| 参数行 | `SliderPreference` / `WindowDropdownPreference` / `ArrowPreference` |
| 信息行 | `InfoRow`（上下两行）、`SettingsInfoRow`（左右两端） |
| 状态卡 | `ModuleStatusCard`：描边圆环记号，勾或叉，右下角溢出后被圆角裁切 |
| 底栏 | `FloatingBottomBar`：悬浮玻璃胶囊，选中项液滴跟手 |
| 间距常量 | `UiTokens`：列表左右 12dp、卡片内边距 16dp、底栏占位 120dp |

`FloatingBottomBar` 及其依赖（`liquid/Lens`、`CombinedBackdrop`、`InnerShadow`、`Vibrancy`，
`animation/DampedDragAnimation`、`InteractiveHighlight`、`DragGestureInspector`）移植自背屏模块，
文件头的来源与许可说明保留原样。
