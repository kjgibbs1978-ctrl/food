#!/usr/bin/env bash
set -euo pipefail

GRADLE_VERSION=8.9

if command -v gradle >/dev/null 2>&1; then
  gradle assembleDebug
  exit 0
fi

DIST_DIR="$PWD/.gradle-dist"
GRADLE_HOME="$DIST_DIR/gradle-$GRADLE_VERSION"
ZIP="$DIST_DIR/gradle-$GRADLE_VERSION-bin.zip"
mkdir -p "$DIST_DIR"

if [ ! -x "$GRADLE_HOME/bin/gradle" ]; then
  echo "Gradle not found; downloading Gradle $GRADLE_VERSION..."
  curl -L "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "$ZIP"
  unzip -q -o "$ZIP" -d "$DIST_DIR"
fi

"$GRADLE_HOME/bin/gradle" assembleDebug

echo
echo "APK: app/build/outputs/apk/debug/app-debug.apk"
