#!/bin/bash

# Create SECURE TAK.gov submission (no vulnerable jars)
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
ROOT_FOLDER_NAME="SkyFi-ATAK-Plugin-v2"

echo "================================================"
echo "Creating SECURE TAK.gov Submission"
echo "Removing vulnerable components for security"
echo "================================================"

# Clean up any previous builds
rm -rf "build/tak-secure/"
mkdir -p "build/tak-secure/$ROOT_FOLDER_NAME"

# Copy essential files EXCEPT vulnerable components
rsync -av --exclude='.git*' \
          --exclude='build/' \
          --exclude='app/build/' \
          --exclude='releases/' \
          --exclude='tak-*' \
          --exclude='BULLETPROOF-*' \
          --exclude='skyfi-takgov-*' \
          --exclude='SkyFi-ATAK-*.zip' \
          --exclude='*.apk' \
          --exclude='*.aab' \
          --exclude='*.log' \
          --exclude='*.pdf' \
          --exclude='dev-bundle/' \
          --exclude='reference-docs/' \
          --exclude='skyfi-atak-bundle/' \
          --exclude='.DS_Store' \
          --exclude='test-reports/' \
          --exclude='takgov-build-analysis/' \
          --exclude='.claude/' \
          --exclude='ATAK-CIV-*-SDK/' \
          --exclude='atak-plugin-*/' \
          --exclude='helloworld/' \
          --exclude='sdk/' \
          --exclude='atak-gradle-takdev.jar' \
          --exclude='dependency-check-report.*' \
          --exclude='fortify_*' \
          ./ "build/tak-secure/$ROOT_FOLDER_NAME/"

# IMPORTANT: Do NOT include the vulnerable atak-gradle-takdev.jar
# TAK.gov will use their own secure version

echo ""
echo "⚠️  SECURITY NOTES:"
echo "  - Removed atak-gradle-takdev.jar (has 11 CVEs)"
echo "  - TAK.gov will use their own secure version"
echo "  - Removed duplicate BULLETPROOF submission folders"
echo ""

# Create the zip
cd "build/tak-secure/"
zip -r "../../$ROOT_FOLDER_NAME-SECURE-TAKGOV-$TIMESTAMP.zip" "$ROOT_FOLDER_NAME/"
cd ../..

# Verify the structure
echo "Archive created: $ROOT_FOLDER_NAME-SECURE-TAKGOV-$TIMESTAMP.zip"
ls -lh "$ROOT_FOLDER_NAME-SECURE-TAKGOV-$TIMESTAMP.zip"
echo ""
echo "Verifying no vulnerable components:"
unzip -l "$ROOT_FOLDER_NAME-SECURE-TAKGOV-$TIMESTAMP.zip" | grep -E "atak-gradle-takdev|BULLETPROOF" || echo "✅ Clean - no vulnerable components found"
echo ""
echo "First 30 files in archive:"
unzip -l "$ROOT_FOLDER_NAME-SECURE-TAKGOV-$TIMESTAMP.zip" | head -35