#!/usr/bin/env bash
# 构建 TabGlass —— 一个 APK 同时是 LSPosed 模块（注入微信 / QQ，libxposed api 102）
# 和桌面上的设置界面。
#
# 用法：
#   ./build.sh               调试版
#   ./build.sh release       发行版
#
# 产物留在 Gradle 自己的输出目录，仓库和别处都不放 APK：
#   debug   app/build/outputs/apk/debug/app-debug.apk
#   release app/build/outputs/apk/release/app-release.apk
# 两个变体用的是同一把钥匙（app/release-key.jks），可以互相覆盖安装。
#
# 环境要求：JDK 21（PATH 或 JAVA_HOME），Android SDK 由 Gradle 定位
# （local.properties 的 sdk.dir 或 ANDROID_HOME）。
#   GRADLE_USER_HOME   Gradle 缓存目录，默认用本机 ~/.gradle
set -euo pipefail

VARIANT="${1:-debug}"
case "$VARIANT" in
    debug)   TASK=":app:assembleDebug";   APK_REL="app/build/outputs/apk/debug/app-debug.apk" ;;
    release) TASK=":app:assembleRelease"; APK_REL="app/build/outputs/apk/release/app-release.apk" ;;
    *)
        echo "用法：./build.sh [debug|release]" >&2
        exit 2
        ;;
esac

PROJ="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SDK_GRADLE="${GRADLE_USER_HOME:-${HOME}/.gradle}"

cd "$PROJ"
GRADLE_USER_HOME="$SDK_GRADLE" ./gradlew --quiet "$TASK"

APK="$PROJ/$APK_REL"
echo "built: $APK ($(wc -c < "$APK") bytes)"
