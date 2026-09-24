#!/usr/bin/env bash
# Isax — compilation locale de l'APK (alternative à GitHub Actions).
#
# Prérequis : JDK 17, SDK Android, NDK 26.1 + CMake 3.22 (les mêmes versions que
# le workflow CI). Sur téléphone, passer par Termux + GitHub Actions.
set -euo pipefail

cd "$(dirname "$0")/.."

echo "== Isax : compilation de l'APK de debug =="

if [ -n "${ANDROID_HOME:-}" ]; then
  echo "sdk.dir=$ANDROID_HOME" > local.properties
fi

if [ -f gradlew ]; then
  ./gradlew assembleDebug --stacktrace
else
  echo "gradlew absent — utilisation de gradle du système"
  gradle assembleDebug --stacktrace
fi

APK="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK" ]; then
  echo "APK prêt : $APK"
  ls -lh "$APK"
else
  echo "Échec : aucun APK produit." >&2
  exit 1
fi
