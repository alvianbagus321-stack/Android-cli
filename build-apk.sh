#!/usr/bin/env bash
set -euo pipefail

if command -v gradle >/dev/null 2>&1; then
  gradle --no-daemon assembleDebug
else
  echo "Gradle tidak ditemukan. Buka project ini di Android Studio atau jalankan GitHub Actions: Build Android APK." >&2
  exit 1
fi

echo "APK: app/build/outputs/apk/debug/app-debug.apk"
