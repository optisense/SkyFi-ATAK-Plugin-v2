#!/bin/bash

# Build and Install SkyFi ATAK Plugin
set -e

echo "========================================="
echo "SkyFi ATAK Plugin Build & Install Script"
echo "========================================="

# Set Java 17
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.16/libexec/openjdk.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

echo "Using Java version:"
java -version 2>&1 | head -1

# Clean previous builds
echo ""
echo "Cleaning previous build artifacts..."
rm -rf app/build
rm -rf .gradle/buildOutputCleanup
./gradlew clean --no-daemon 2>/dev/null || true

# Ensure keystore is in place
echo "Setting up keystore..."
mkdir -p app/build
cp android_keystore app/build/

# Build the plugin
echo ""
echo "Building plugin (this may take a few minutes)..."
./gradlew --no-daemon assembleCivDebug

# Check if build succeeded
if [ ! -f app/build/outputs/apk/civ/debug/*.apk ]; then
    echo "ERROR: Build failed - no APK generated"
    exit 1
fi

# Find the APK
APK_PATH=$(find app/build/outputs/apk/civ/debug -name "*.apk" | head -1)
echo ""
echo "Build successful!"
echo "APK: $APK_PATH"
echo "Size: $(ls -lh "$APK_PATH" | awk '{print $5}')"

# Check if device is connected
echo ""
echo "Checking for connected Android device..."
if ! adb devices | grep -q "device$"; then
    echo "ERROR: No Android device connected"
    echo "Please connect your device and enable USB debugging"
    exit 1
fi

DEVICE=$(adb devices | grep device$ | head -1 | awk '{print $1}')
echo "Found device: $DEVICE"

# Uninstall old version
echo ""
echo "Removing old plugin version..."
adb uninstall com.skyfi.atak.plugin 2>/dev/null || echo "No previous version installed"

# Install new version
echo ""
echo "Installing new plugin..."
adb install -r "$APK_PATH"

# Clear logcat
adb logcat -c

echo ""
echo "========================================="
echo "Plugin installed successfully!"
echo "========================================="
echo ""
echo "Next steps:"
echo "1. Open ATAK on your device"
echo "2. Look for the SkyFi plugin icon in the toolbar"
echo "3. Check logs with: adb logcat | grep -i skyfi"
echo ""
echo "To monitor plugin loading:"
echo "  adb logcat | grep -E 'SkyFi|PluginLoader|plugin.*skyfi' -i"