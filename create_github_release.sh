#!/bin/bash

# Manual steps to create GitHub release
echo "=== Create GitHub Release v1.6 ==="
echo ""
echo "1. Go to: https://github.com/lukesgood/bluetooth-codec-checker/releases/new"
echo "2. Select existing tag: v1.6"
echo "3. Title: v1.6 - Enhanced Codec Detection Accuracy with OS-Level Detection Only"
echo "4. Copy description from RELEASE_v1.6.txt"
echo "5. Upload APK: app/build/outputs/apk/debug/app-debug.apk"
echo "6. Click 'Publish release'"
echo ""
echo "Files ready:"
echo "- APK: $(ls -lh app/build/outputs/apk/debug/app-debug.apk 2>/dev/null || echo 'app-debug.apk (not found)')"
echo "- Description: RELEASE_v1.6.txt"
echo ""
echo "Opening GitHub releases page..."

# Open the release creation page
python3 -c "import webbrowser; webbrowser.open('https://github.com/lukesgood/bluetooth-codec-checker/releases/new?tag=v1.6&title=v1.6%20-%20Enhanced%20Codec%20Detection%20Accuracy%20with%20OS-Level%20Detection%20Only')" 2>/dev/null || echo "Could not open browser automatically"

echo ""
echo "Release page should open in your browser."
echo "Copy the description from RELEASE_v1.6.txt and upload the APK file."
