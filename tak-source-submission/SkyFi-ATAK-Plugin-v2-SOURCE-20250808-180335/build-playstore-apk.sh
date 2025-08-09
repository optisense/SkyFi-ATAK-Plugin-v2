#!/bin/bash

echo "======================================"
echo "Building SkyFi ATAK Plugin APK for Google Play Store"
echo "======================================"
echo ""

# Clean build directory
rm -rf app/build

# Build signed APK
echo "Building signed APK..."
./gradlew assembleCivRelease --no-daemon --stacktrace

echo ""
echo "Build attempt complete."
echo ""

# Check if APK was created
if ls app/build/outputs/apk/civ/release/*.apk 1> /dev/null 2>&1; then
    echo "Success! APK created:"
    ls -la app/build/outputs/apk/civ/release/*.apk
    
    # Copy to root directory
    cp app/build/outputs/apk/civ/release/*.apk ./skyfi-atak-playstore-signed.apk
    echo ""
    echo "APK copied to: ./skyfi-atak-playstore-signed.apk"
    echo ""
    echo "This APK is signed with your Play Store upload key and can be:"
    echo "1. Tested locally on devices"
    echo "2. Uploaded to Google Play Console (though AAB is preferred)"
else
    echo "Build failed. No APK found."
    echo ""
    echo "Common issues:"
    echo "- Gradle version incompatibility"
    echo "- Missing dependencies"
    echo "- Keystore configuration issues"
fi