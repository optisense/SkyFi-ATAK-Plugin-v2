#!/bin/bash

# Quick fix and build script
echo "Building with JAVA_HOME set..."

export JAVA_HOME=/opt/homebrew/opt/openjdk@11/libexec/openjdk.jdk/Contents/Home

# Build the APK
./gradlew assembleCivDebug --no-daemon

if [ $? -eq 0 ]; then
    echo "Build successful!"
    # Install on device
    adb install -r app/build/outputs/apk/civ/debug/ATAK-Plugin-SkyFi-ATAK-Plugin-v2-*.apk
else
    echo "Build failed!"
    exit 1
fi