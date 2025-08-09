#!/bin/bash

# TAK.gov Build Verification Script
# Tests the submission package with TAK.gov credentials

set -e

if [ $# -ne 3 ]; then
    echo "Usage: $0 <submission.zip> <tak_username> <tak_password>"
    exit 1
fi

ZIP_FILE="$1"
TAK_USER="$2"
TAK_PASS="$3"

echo "Extracting and testing TAK.gov submission..."

# Extract
TEMP_DIR="test-$(date +%Y%m%d-%H%M%S)"
unzip -q "$ZIP_FILE" -d "$TEMP_DIR"

# Find the plugin directory
PLUGIN_DIR=$(find "$TEMP_DIR" -type d -name "SkyFi-ATAK-Plugin-v2" | head -1)

if [ -z "$PLUGIN_DIR" ]; then
    echo "Error: Plugin directory not found in submission"
    exit 1
fi

cd "$PLUGIN_DIR"

# Test build
echo "Testing build with TAK.gov repository..."
./gradlew clean
./gradlew -Ptakrepo.force=true \
         -Ptakrepo.url=https://artifacts.tak.gov/artifactory/maven \
         -Ptakrepo.user="$TAK_USER" \
         -Ptakrepo.password="$TAK_PASS" \
         assembleCivRelease

if [ -f app/build/outputs/apk/civ/release/*.apk ]; then
    echo "✓ Build successful!"
    echo "APK generated at: $(ls app/build/outputs/apk/civ/release/*.apk)"
else
    echo "✗ Build failed - no APK generated"
    exit 1
fi

cd ../..
rm -rf "$TEMP_DIR"

echo "Verification complete - submission package is valid!"
