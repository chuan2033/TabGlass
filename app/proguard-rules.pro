# TabGlass 的 R8 规则。模块本体（hook 侧）与界面在同一个包里，
# 入口靠 META-INF/xposed/java_init.list 里的类名反射加载，必须保留。

# LSPosed 按 java_init.list 的类名加载入口，名字不能被改写。
-keep class io.github.XQ.TabGlass.LiquidGlassModule { *; }
# hook 侧其余类也被入口直接 new / 静态调用，且要处理宿主类名表；整包保留，
# 只占 dex 的 1.7%，换不来多少体积，却省掉一类难查的线上崩溃。
-keep class io.github.XQ.TabGlass.** { *; }
-keepclassmembers class io.github.XQ.TabGlass.** { *; }

# libxposed 由框架在运行时提供，编译期只是壳，不打进包里也不许警告。
-keep class io.github.libxposed.** { *; }
-dontwarn io.github.libxposed.**

# Compose / Miuix 的 consumer rules 已经够用，这里只压掉编译期缺失警告。
-dontwarn androidx.compose.**
-dontwarn top.yukonga.**
