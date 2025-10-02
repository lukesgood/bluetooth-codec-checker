#!/bin/bash

echo "Building Bluetooth Codec Checker..."

# Check if Android SDK is installed
if [ ! -d "$HOME/Android/Sdk" ]; then
    echo "Android SDK not found. Please install Android SDK first:"
    echo "1. Download command line tools from https://developer.android.com/studio#command-tools"
    echo "2. Extract to ~/Android/Sdk/cmdline-tools/latest/"
    echo "3. Run: ~/Android/Sdk/cmdline-tools/latest/bin/sdkmanager --install \"platforms;android-34\" \"build-tools;34.0.0\""
    exit 1
fi

# Build the APK
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo "Build successful!"
    echo "APK location: app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "To install on device:"
    echo "adb install app/build/outputs/apk/debug/app-debug.apk"
else
    echo "Build failed!"
    exit 1
fi
