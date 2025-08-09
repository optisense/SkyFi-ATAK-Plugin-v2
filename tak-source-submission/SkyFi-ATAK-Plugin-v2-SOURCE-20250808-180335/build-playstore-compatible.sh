#!/bin/bash

echo "======================================"
echo "Building SkyFi ATAK Plugin for Google Play Store"
echo "Using compatible Gradle configuration"
echo "======================================"
echo ""

# Backup current build.gradle
cp build.gradle build.gradle.backup

# Use Play Store compatible build configuration
cp build.gradle.playstore build.gradle

# Clean previous builds
echo "Cleaning previous builds..."
./gradlew clean

# Build the App Bundle (AAB) for Play Store
echo ""
echo "Building App Bundle (AAB) for Play Store..."
./gradlew bundleCivRelease

# Check if AAB was created
if [ $? -eq 0 ]; then
    echo ""
    echo "Also building APK for testing..."
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
        
        # Copy to root for easy access
        cp app/build/outputs/bundle/civRelease/*.aab ./skyfi-atak-playstore.aab
        echo "Copied to: ./skyfi-atak-playstore.aab"
        echo ""
    fi
    
    # List APK files
    if ls app/build/outputs/apk/civ/release/*.apk 1> /dev/null 2>&1; then
        echo "APK for testing:"
        ls -la app/build/outputs/apk/civ/release/*.apk
        echo ""
        
        # Copy to root for easy access
        cp app/build/outputs/apk/civ/release/*.apk ./skyfi-atak-playstore.apk
        echo "Copied to: ./skyfi-atak-playstore.apk"
        echo ""
    fi
    
    echo "Next steps:"
    echo "1. Upload skyfi-atak-playstore.aab to Google Play Console"
    echo "2. Google Play will sign it with their distribution key"
    echo "3. Test skyfi-atak-playstore.apk locally if needed"
else
    echo "Build failed. Check the error messages above."
fi

# Restore original build.gradle
mv build.gradle.backup build.gradle

echo ""
echo "Original build.gradle restored."