#!/usr/bin/env bash
# Pre-fetch the only external dependency, libxposed api 102, into $TOOL_ROOT/xapi
# (default ./tools). The Android SDK and the JDK are taken from the host, so
# there is nothing else to download.
#
#   ANDROID_HOME  where build.sh looks for platforms/ + build-tools/
#   JAVA_HOME     where build.sh looks for javac/java
#
# build.sh downloads this AAR itself when it is missing; running this script
# first is only useful if you want the fetch to happen up front.
set -euo pipefail

PROJ="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TOOL_ROOT="${TOOL_ROOT:-$PROJ/tools}"
mkdir -p "$TOOL_ROOT/xapi" "$TOOL_ROOT"

PY=""
for c in python3 python py; do command -v "$c" >/dev/null 2>&1 && { PY="$c"; break; }; done
[ -n "$PY" ] || { echo "error: python not found" >&2; exit 1; }

AAR="$TOOL_ROOT/api-102.aar"
if [ ! -s "$AAR" ]; then
    echo "  fetching api-102.0.0.aar"
    curl -sSLf -o "$AAR" \
        https://repo1.maven.org/maven2/io/github/libxposed/api/102.0.0/api-102.0.0.aar
fi
"$PY" - "$AAR" "$TOOL_ROOT/xapi" <<'PYUNZIP'
import sys, zipfile
zipfile.ZipFile(sys.argv[1]).extract("classes.jar", sys.argv[2])
PYUNZIP

echo "done. $TOOL_ROOT/xapi/classes.jar"
echo "now run ./build.sh (it uses the host SDK: ${ANDROID_HOME:-<unset>})"
