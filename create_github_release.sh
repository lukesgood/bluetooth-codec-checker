#!/bin/bash

# Manual steps to create GitHub release
echo "=== Create GitHub Release v1.4 ==="
echo ""
echo "1. Go to: https://github.com/lukesgood/bluetooth-codec-checker/releases/new"
echo "2. Select existing tag: v1.4"
echo "3. Title: v1.4 - Real Data Only (No More Fake Information)"
echo "4. Copy description from release_text.txt"
echo "5. Upload APK: bluetooth-codec-checker-v1.4.apk"
echo "6. Click 'Publish release'"
echo ""
echo "Files ready:"
echo "- APK: $(ls -lh bluetooth-codec-checker-v1.4.apk 2>/dev/null || echo 'bluetooth-codec-checker-v1.4.apk (not found)')"
echo "- Description: release_text.txt"
echo ""
echo "Opening GitHub releases page..."

# Open the release creation page
python3 -c "import webbrowser; webbrowser.open('https://github.com/lukesgood/bluetooth-codec-checker/releases/new?tag=v1.4&title=v1.4%20-%20Real%20Data%20Only%20(No%20More%20Fake%20Information)')" 2>/dev/null || echo "Could not open browser automatically"

echo ""
echo "Release page should open in your browser."
echo "Copy the description from release_text.txt and upload the APK file."
