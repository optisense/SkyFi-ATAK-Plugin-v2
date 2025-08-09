#!/bin/bash

echo "======================================"
echo "Building SkyFi ATAK Plugin for Google Play Store"
echo "======================================"
echo ""

# Clean previous builds
echo "Cleaning previous builds..."
./gradlew clean

# Build the App Bundle (AAB) for Play Store
echo ""
echo "Building App Bundle (AAB) for Play Store..."
./gradlew bundleCivRelease

# Also build APK for testing
echo ""
echo "Building APK for testing..."
./gradlew assembleCivRelease

echo ""
echo "======================================"
echo "Build Complete!"
echo "======================================"
echo ""
echo "Output files:"
echo ""

# List AAB files
if ls app/build/outputs/bundle/civRelease/*.aab 1> /dev/null 2>&1; then
    echo "App Bundle (AAB) for Play Store upload:"
    ls -la app/build/outputs/bundle/civRelease/*.aab
    echo ""
fi

# List APK files
if ls app/build/outputs/apk/civ/release/*.apk 1> /dev/null 2>&1; then
    echo "APK for testing:"
    ls -la app/build/outputs/apk/civ/release/*.apk
    echo ""
fi

echo "Next steps:"
echo "1. Upload the AAB file to Google Play Console"
echo "2. Google Play will sign it with their distribution key"
echo "3. Test the APK locally if needed"
echo ""
echo "Upload to Play Console:"
echo "  - Go to Release Management → App releases"
echo "  - Create a new release or update existing"
echo "  - Upload the .aab file from app/build/outputs/bundle/civRelease/"