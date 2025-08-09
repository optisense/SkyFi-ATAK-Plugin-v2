#!/bin/bash

echo "========================================"
echo "Building SkyFi ATAK Plugin - Local Debug"
echo "========================================"

# Clean previous builds
echo "Cleaning previous builds..."
./gradlew clean

# Build debug version (no signing required, works locally)
echo "Building CIV Debug version..."
./gradlew assembleCivDebug

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful!"
    echo ""
    echo "Debug APK location:"
    find app/build/outputs/apk -name "*debug.apk" -type f | while read apk; do
        echo "  📦 $apk"
        echo "     Size: $(ls -lh "$apk" | awk '{print $5}')"
    done
    
    echo ""
    echo "To install on device/emulator:"
    echo "  adb install -r app/build/outputs/apk/civ/debug/*.apk"
    echo ""
    echo "Note: Debug builds work without TAK.gov signing!"
else
    echo "❌ Build failed. Check error messages above."
    exit 1
fi