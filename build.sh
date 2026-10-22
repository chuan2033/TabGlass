#!/usr/bin/env bash
# Build TabGlass — one APK that is both an LSPosed module for WeChat / QQ
# (libxposed api 102) and the settings UI you launch from the home screen.
#
# The module body is plain Java, but the UI is Kotlin + Compose + Miuix, so the
# build has to go through Gradle now; the old hand-rolled javac + d8 + aapt2
# chain cannot run the Compose compiler. This script stays as the single entry
# point so `./build.sh` still does the right thing.
#
# Requirements: JDK 21 on PATH or JAVA_HOME, and the Android SDK located by
# Gradle (local.properties `sdk.dir` or ANDROID_HOME).
#   GRADLE_USER_HOME   Gradle cache, defaults to the machine's own
#   VERSION            override the version stamped into the file name
set -euo pipefail

PROJ="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GRADLE="${PROJ}/settings-app"
SDK_GRADLE="${GRADLE_USER_HOME:-${HOME}/.gradle}"

cd "$GRADLE"
GRADLE_USER_HOME="$SDK_GRADLE" ./gradlew --quiet :app:assembleDebug

APK="$GRADLE/app/build/outputs/apk/debug/app-debug.apk"
VERSION="${VERSION:-$(sed -n 's/.*versionName "\([^"]*\)".*/\1/p' "$GRADLE/app/build.gradle")}"
FINAL="$PROJ/TabGlass-v$VERSION.apk"

cp "$APK" "$FINAL"
echo "built: $FINAL ($(wc -c < "$FINAL") bytes)"
