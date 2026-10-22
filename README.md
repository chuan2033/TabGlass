# TabGlass

为微信和 QQ 打造的 iOS 26 风格液态玻璃底部导航栏 · LSPosed 模块（libxposed API 102）

> 基于 [liuran001/WeChat-LiquidGlass](https://github.com/liuran001/WeChat-LiquidGlass) 修改：
> 重做了 pill 几何（宽高按屏宽比例、液滴四边等距），拆出 Miuix 设置应用把参数变成可调，
> 包名与版本线独立维护。原始实现与设计思路见上游仓库。

## ✨ 特性

- 🫧 **单 pass AGSL 透镜管线**：圆角矩形 SDF 折射、边缘色散、重力传感器高光
- 💧 **玻璃滴选中动效**：切换标签时液滴滑动、回弹；**支持横向拖拽切换**，跟手拉伸、松手吸附
- 🎯 **对齐 KernelSU 管理器**：悬浮 pill 宽度贴合内容、4dp 轻模糊 + 容器色垫底、
  按压时液滴放大 `78/56` 并淡入折射、焦点标签放大 `1.1×`
- 🔍 **背景实时跟随**：玻璃折射的是当前页面的真实内容，切页即变
- 🧩 **完整保留原生功能**：红点、未读数、图标渐变、长按菜单全部走应用自己的实现
- 🪟 **双宿主**：微信与 QQ 共用一套安装器，差异集中在 `HostApp` 一张表里
- 🌗 **深浅色跟随**：读取应用 uiMode，辅以标签色众数探测交叉校验
- ♿ **降级兜底**：Android 12 保留 GPU 模糊与液滴，只少了边缘折射（AGSL 需 13）；
  Android 12 以下退化为轻磨砂；native 模糊库不可用时自动切纯 Java 实现

## 📦 安装

1. 准备支持 **libxposed API 102** 的 LSPosed 环境
2. 安装 APK，在 LSPosed 中启用（作用域已限定 `com.tencent.mm` 和 `com.tencent.mobileqq`，
   只勾选想改造的那个也可以）
3. **强制停止**微信 / QQ 后重新打开

> 已测试：微信 `8.0.77 (3160)`  QQ `9.2.85 (13860)` `9.1.52 (9054)`

## ⚙️ 内置设置界面

一个 APK 两个身份：注入微信 / QQ 进程的模块本体，和桌面上点得开的设置界面。后者用 Miuix
写成，结构与同作者的背屏模块一致：`Scaffold` + `BlurredBar` + 大标题、`SmallTitle` +
`CardBlock` 分组、悬浮玻璃底栏。它调五个几何参数（宽度、高度、底部间距、液滴内缩、列最小
留白），改完切回微信或 QQ 即生效，不用重装。

参数由界面写进本应用自己的 SharedPreferences，再经只读 `ContentProvider`
（`io.github.XQ.TabGlass.prefs`）交给注入侧——模块代码跑在微信进程里，
`getSharedPreferences` 拿到的是微信的存储，读不到自己 App 的。模块读到之后会写回宿主
自己的偏好文件作为兜底。细节见 [`settings-app/README.md`](settings-app/README.md)。

## 🔍 工作原理

两个应用的主界面结构几乎是双胞胎——内容区铺满全屏，底栏以 `layout_gravity=bottom`
浮在它上面，同处一个 FrameLayout——所以安装器只有一套：

```
Hook Instrumentation.callActivityOnResume
  └─ 主界面 Activity：按类名定位底栏（两家资源 ID 都被 AndResGuard 混淆，不可用）
       └─ 布局手术：把底栏搬进 LiquidGlassHostLayout
            ├─ 玻璃层 LiquidGlassView（backdrop = 底栏原本的全屏兄弟）
            ├─ 液滴层 LiquidGlassView（跟随选中项，可拖拽）
            └─ 应用原生底栏（背景已清空）
```

应用相关的部分全在 [`HostApp`](settings-app/app/src/main/java/io/github/XQ/TabGlass/HostApp.java) 里：

| | 微信 | QQ |
|---|---|---|
| 包名 | `com.tencent.mm` | `com.tencent.mobileqq` |
| 主界面 Activity | `ui.LauncherUI` | `activity.SplashActivity` |
| 底栏 | `ui.LauncherUIBottomTabView` | `widget.QQTabWidget`（默认）<br>`widget.QQTabLayout`（灰度） |
| 切页信号 | `setTo(int)` | `setCurrentTab(int)` |
| backdrop | `CustomViewPager` | `tabcontent` / `ViewPager2` |
| 额外处理 | — | 隐藏自带的 `QQBlurViewWrapper` |

QQ 同一个安装包里带了新旧两套底栏，服务端开关 `tab_layout_9065_116522266`
默认关闭，即绝大多数用户跑的是 `QQTabWidget`（`android.widget.TabWidget` 子类）。
两套都在表里，运行时按实际存在的那个走。

日志 tag：`LiquidGlass`

## 🛠️ 构建

模块本体是纯 Java，但设置界面是 Kotlin + Compose + Miuix，所以构建走 Gradle。`build.sh`
现在只是它的一层薄包装——老的 javac + d8 + aapt2 手工链跑不了 Compose 编译器：

```bash
./build.sh
```

等价于 `cd settings-app && ./gradlew :app:assembleDebug`，再把产物拷到根目录，输出
`TabGlass-vX.Y.Z.apk`（debug 签名，约 32MB，大头是 Compose + Miuix 运行时）。

| 依赖 | 来源 |
|---|---|
| Android SDK 37 / build-tools 37.0.0 | `settings-app/local.properties` 的 `sdk.dir`，或 `ANDROID_HOME` |
| JDK 21 | `JAVA_HOME`，否则用 PATH 上的 |
| AGP 9.2.1 / Kotlin 2.3.21 / Miuix 0.9.4 | Gradle 从 Maven 取 |
| libxposed `api-102` | `tools/xapi/classes.jar`，`compileOnly`；`./setup-tools.sh` 从 Maven 的 `api-102.0.0.aar` 解出来 |

**最低系统是 Android 13（API 33）**，由 `miuix-blur` 的下限决定——模块单独编译时曾是 26，
合进界面之后 Android 12L 及以下装不上。

### 可调参数

| 参数 | 位置 | 默认 |
|---|---|---|
| 折射高度 / 边缘带 | `GlassTuner.onSize()` | 24dp（同 KernelSU `lens()`） |
| 模糊强度 | `tuneGlass()` | 4dp（同 KernelSU `blur()`） |
| pill 宽度（占屏宽百分比，列宽均分） | `GlassConfig.pillWidthPercent` | 80%（0 = 回归上游按内容收缩） |
| 每列最小留白（只作下限保护） | `GlassConfig.columnPadDp` | 50dp（上游 32） |
| 行左右内边距（同时是液滴四边内缩量） | `GlassConfig.rowPadDp` | 4dp（上游 4） |
| pill 高度（两家统一） | `GlassConfig.pillHeightDp` | 69dp（原生栏更高时不缩小） |
| 底部抬升 | `GlassConfig.barOffsetDp` | 12dp |
| 按压放大 / 焦点放大 | `DropletDragController` / `DropletPanel` | 78/56 · 1.1 |
| 拖拽形变上限 | `DropletDragController.STRETCH_LIMIT` | 0.2 |

pill 的尺寸只在这些几何参数上变化：tab 栏本身的高度、图标与文字尺寸都保持宿主原样，
pill 变高时 tab 是垂直居中放进去的（`tabLp` 的 `CENTER_VERTICAL`），
所以图标不会被拉伸或缩放，点击热区也不变。

**宽度按屏宽算，不再按内容算。** 上游让 pill 贴住标签，微信 4 个 tab 得到 962px，
QQ 3 个 tab 只有 665px，同一台机器上会读成两条不一样的栏。改成 80% 屏宽、列宽均分后，
两家都是 958/959px，多出来的空间平均分到各列，`columnPadDp` 退化成「一列不能窄于
内容 + 该值」的下限保护。

**`rowPadDp` 不要单独调大。** 液滴的宽度正好是一个 tab 列，所以最左/最右一列到 pill
边缘的距离就是 `rowPadDp`，而液滴的上下内缩量也取这个值（`syncDropletSize` 里的
`inset`）。把它调成 10dp 而上下仍留 4dp，选中最左的「微信」时会明显看出左边空得多、
上下贴得紧。要让 pill 更宽请调 `pillWidthPercent`。

**高度是绝对 dp，不是「原生 + N」。** 微信停靠栏 152px、QQ 141px，用相对值会让两家
差出一截，所以直接钉死 69dp，谁的原生内容更高就取谁。剩下约 10px 的差异来自宿主
自己的 density（这台机器上微信 2.72、QQ 2.62，同一段 69dp 画出来 190px vs 180px），
属于各自 UI 的等比缩放，不追像素级一致的话不用管。

在 1200×2608 的机器上实测（微信 / QQ）：pill **958×190px / 959×180px**，
列宽 234px / 313px，液滴四边内缩均 4dp；改动前是 758×144px / 665×141px。

### 液滴

静止时是一层 10% 的纯色块；按住/拖拽时按 `pressProgress` 淡入透镜：

```
lens(refractionHeight = 10dp × p, refractionAmount = 14dp × p,
     depthEffect = true, chromaticAberration = 0.5)
```

液滴折射的是 **页面 + 一份单独绘制的 1.1× 标签副本**（KernelSU 的
`CombinedBackdrop`）。拖拽时看到的放大图标正是这份副本经折射后的样子，
因此液滴层必须压在真实标签**之上**，而真实标签保持原尺寸——两边都缩放会叠加两次。

### 动画

滑动/按压完全复刻 KernelSU 的 `DampedDragAnimation`：五条独立弹簧，参数照搬。

| 弹簧 | 阻尼比 / 刚度 | 驱动 |
|---|---|---|
| value | 1.0 / 1000 | 位置（**以标签为单位**，非像素） |
| velocity | 0.5 / 300 | 归一化速度 → 拉伸形变 |
| press | 1.0 / 1000 | 按压进度 |
| scaleX | 0.6 / 250 | 横向缩放 |
| scaleY | 0.7 / 250 | 纵向缩放 |

两处易被忽略但决定手感的细节：位置以标签为单位追踪，速度按标签数归一化，
因此不同屏幕密度手感一致；`release()` 会**等液滴接近目标**才回落缩放，
这样甩动读起来是一个连贯动作而非"滑动 + 单独缩回"。

点击切换也走同一条路径（press → 移动 → release），与拖拽完全一致。

## 🙏 致谢

- [liuran001/WeChat-LiquidGlass](https://github.com/liuran001/WeChat-LiquidGlass) — MIT，本项目的基础
- [sjtt2/HeyBox-LiquidGlass](https://github.com/sjtt2/HeyBox-LiquidGlass) — MIT，本项目的灵感来源
- [tiann/KernelSU](https://github.com/tiann/KernelSU) — 拖拽形变的速度曲线参考其 `FloatingBottomBar`
- [libxposed/api](https://github.com/libxposed/api) — Apache-2.0

## 📄 License

[MIT](LICENSE) — 原始代码 © 2026 liuran001，修改部分 © 2026 chuan2033
