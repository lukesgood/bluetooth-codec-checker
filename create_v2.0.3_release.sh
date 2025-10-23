#!/bin/bash

echo "🚀 Creating Bluetooth Codec Checker v2.0.3 Release"

# Build info
VERSION="v2.0.3"
APK_FILE="bluetooth-codec-checker-v2.0.3.apk"
RELEASE_NOTES="RELEASE_v2.0.3.txt"

echo "✅ Version: $VERSION"
echo "✅ APK: $APK_FILE ($(ls -lh $APK_FILE | awk '{print $5}'))"
echo "✅ Release Notes: $RELEASE_NOTES"
echo "✅ Git Tag: $(git tag --list $VERSION)"

echo ""
echo "📋 Release Summary:"
echo "- Near Real-Time Bluetooth signal analysis"
echo "- Enhanced codec detection (aptX Adaptive, LDAC)"
echo "- 1-second UI updates, 5-second scan cycles"
echo "- Multi-method detection with log analysis"
echo "- Auto-recovery system with 0.5s failure recovery"

echo ""
echo "🔧 Manual GitHub Release Steps:"
echo "1. Go to: https://github.com/lukesgood/bluetooth-codec-checker/releases/new"
echo "2. Tag: $VERSION"
echo "3. Title: Bluetooth Codec Checker $VERSION - Near Real-Time Analysis"
echo "4. Upload: $APK_FILE"
echo "5. Description: Copy from $RELEASE_NOTES"

echo ""
echo "📱 Installation:"
echo "adb install $APK_FILE"
