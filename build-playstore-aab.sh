#!/bin/bash

echo "Building Play Store AAB with correct namespace..."

# Clean previous builds
echo "Cleaning..."
./gradlew clean

# Build the AAB
echo "Building AAB..."
./gradlew bundleCivRelease

# Check if AAB was created
if [ -f "app/build/outputs/bundle/civRelease/ATAK-Plugin-SkyFi-ATAK-Plugin-v2-2.0-beta5--5.3.0-civ-release.aab" ]; then
    echo "AAB built successfully!"
    
    # Sign the AAB with Play Store key
    echo "Signing AAB with Play Store key..."
    
    # Use apksigner to sign the AAB
    /Users/jfuginay/Library/Android/sdk/build-tools/33.0.2/apksigner sign \
        --ks keystores/playstore-skyfi.keystore \
        --ks-pass pass:skyfi2024 \
        --ks-key-alias skyfi-playstore \
        --key-pass pass:skyfi2024 \
        --min-sdk-version 21 \
        --out GooglePlay-Final-Fixed.aab \
        app/build/outputs/bundle/civRelease/ATAK-Plugin-SkyFi-ATAK-Plugin-v2-2.0-beta5--5.3.0-civ-release.aab
    
    if [ -f "GooglePlay-Final-Fixed.aab" ]; then
        echo "AAB signed successfully!"
        
        # Remove code transparency if present
        echo "Removing code transparency..."
        zip -d GooglePlay-Final-Fixed.aab "BUNDLE-METADATA/com.android.tools.build.bundletool/code_transparency_signed.jwt" 2>/dev/null
        
        # Verify the package name in the AAB
        echo "Verifying package name in AAB..."
        unzip -p GooglePlay-Final-Fixed.aab base/manifest/AndroidManifest.xml | strings | grep -E "com\.(skyfi|optisense)" | head -5
        
        echo "Done! AAB ready at: GooglePlay-Final-Fixed.aab"
    else
        echo "Failed to sign AAB"
        exit 1
    fi
else
    echo "AAB build failed - file not found"
    exit 1
fi