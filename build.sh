#!/usr/bin/env bash
# 构建 TabGlass —— 一个 APK 同时是 LSPosed 模块（注入微信 / QQ，libxposed api 102）
# 和桌面上的设置界面。
#
# 模块本体是纯 Java，但界面是 Kotlin + Compose + Miuix，所以构建走 Gradle；
# 这个脚本只是给出一条命令、并把产物按版本号命名到仓库根目录。
#
# 环境要求：JDK 21（PATH 或 JAVA_HOME），Android SDK 由 Gradle 定位
# （local.properties 的 sdk.dir 或 ANDROID_HOME）。
#   GRADLE_USER_HOME   Gradle 缓存目录，默认用本机 ~/.gradle
#   VERSION            覆盖写进文件名的版本号
set -euo pipefail

PROJ="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SDK_GRADLE="${GRADLE_USER_HOME:-${HOME}/.gradle}"

cd "$PROJ"
GRADLE_USER_HOME="$SDK_GRADLE" ./gradlew --quiet :app:assembleDebug

APK="$PROJ/app/build/outputs/apk/debug/app-debug.apk"
VERSION="${VERSION:-$(sed -n 's/.*versionName "\([^"]*\)".*/\1/p' "$PROJ/app/build.gradle")}"
FINAL="$PROJ/TabGlass-v$VERSION.apk"

cp "$APK" "$FINAL"
echo "built: $FINAL ($(wc -c < "$FINAL") bytes)"
