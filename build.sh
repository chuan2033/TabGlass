#!/usr/bin/env bash
# 构建 TabGlass —— 一个 APK 同时是 LSPosed 模块（注入微信 / QQ，libxposed api 102）
# 和桌面上的设置界面。
#
# 用法：
#   ./build.sh               调试版（debug 签名）
#   ./build.sh release       发行版（签名取自 local.properties，见 README）
#   OUT_DIR=<目录> ./build.sh release    额外把产物复制一份到指定目录
#
# 产物默认留在 Gradle 自己的输出目录，仓库里不存任何 APK。
# 环境要求：JDK 21（PATH 或 JAVA_HOME），Android SDK 由 Gradle 定位
# （local.properties 的 sdk.dir 或 ANDROID_HOME）。
#   GRADLE_USER_HOME   Gradle 缓存目录，默认用本机 ~/.gradle
set -euo pipefail

VARIANT="${1:-debug}"
case "$VARIANT" in
    debug)
        TASK=":app:assembleDebug"
        APK_REL="app/build/outputs/apk/debug/app-debug.apk"
        SUFFIX="-debug"
        ;;
    release)
        TASK=":app:assembleRelease"
        APK_REL="app/build/outputs/apk/release/app-release.apk"
        SUFFIX=""
        ;;
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

if [ -n "${OUT_DIR:-}" ]; then
    VERSION="$(sed -n 's/.*versionName "\([^"]*\)".*/\1/p' "$PROJ/app/build.gradle")"
    mkdir -p "$OUT_DIR"
    FINAL="$OUT_DIR/TabGlass-v$VERSION$SUFFIX.apk"
    cp "$APK" "$FINAL"
    echo "copied: $FINAL"
fi
