#!/data/data/com.termux/files/usr/bin/bash
# Isax — push + build APK depuis Termux (sans ordinateur).
# Usage : bash scripts/termux_build.sh <TON_USER_GITHUB>
set -e
USER_GH="${1:?Usage: termux_build.sh <user_github>}"
REPO="isax-launcher"
pkg install -y git gh 2>/dev/null || true
if [ ! -d .git ]; then
  git init; git add .; git commit -m "Isax v0.2"
  git branch -M main
  git remote add origin "https://github.com/$USER_GH/$REPO.git"
fi
git push -u origin main
echo "-> Workflow declenche. Suivi :"
gh run watch
echo "-> Telechargement APK :"
gh run download -n isax-apk || true
APK=$(find . -name "*.apk" -path "*isax-apk*" | head -1 || true)
[ -n "$APK" ] && termux-open "$APK"
