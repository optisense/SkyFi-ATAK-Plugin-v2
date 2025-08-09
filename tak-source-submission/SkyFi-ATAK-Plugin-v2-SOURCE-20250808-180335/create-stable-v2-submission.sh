#!/bin/bash

# Create STABLE v2.0 TAK.gov submission package
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
ROOT_FOLDER_NAME="SkyFi-ATAK-Plugin-v2"

echo "=============================================="
echo "Creating STABLE v2.0 TAK.gov Submission"
echo "AI features removed for stable release"
echo "=============================================="

# Clean up any previous builds
rm -rf "build/tak-stable-v2/"
mkdir -p "build/tak-stable-v2/$ROOT_FOLDER_NAME"

# Copy essential files EXCEPT vulnerable components and AI features
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
          --exclude='**/ai/*' \
          --exclude='**/AI*' \
          --exclude='ic_ai_*' \
          ./ "build/tak-stable-v2/$ROOT_FOLDER_NAME/"

echo ""
echo "✅ STABLE v2.0 Features:"
echo "  - Archive Search & Ordering"
echo "  - AOI (Area of Interest) Management"  
echo "  - COG (Cloud Optimized GeoTIFF) Layer Support"
echo "  - Shape Selection & Drawing Tools"
echo "  - Image Cache & Preview Management"
echo "  - TAK Server Integration"
echo "  - Satellite Feasibility Calculations"
echo ""
echo "❌ Removed for Stability:"
echo "  - AI/ML features (commented out)"
echo "  - Vulnerable atak-gradle-takdev.jar"
echo "  - Experimental features"
echo ""

# Create the zip
cd "build/tak-stable-v2/"
zip -r "../../$ROOT_FOLDER_NAME-STABLE-v2.0-$TIMESTAMP.zip" "$ROOT_FOLDER_NAME/"
cd ../..

# Verify the structure
echo "Archive created: $ROOT_FOLDER_NAME-STABLE-v2.0-$TIMESTAMP.zip"
ls -lh "$ROOT_FOLDER_NAME-STABLE-v2.0-$TIMESTAMP.zip"
echo ""
echo "Verification:"
echo "  - No AI references:"
unzip -l "$ROOT_FOLDER_NAME-STABLE-v2.0-$TIMESTAMP.zip" | grep -i "ai\|artificial" | wc -l | xargs -I {} echo "    {} AI files (should be 0)"
echo "  - No vulnerable jar:"
unzip -l "$ROOT_FOLDER_NAME-STABLE-v2.0-$TIMESTAMP.zip" | grep "atak-gradle-takdev.jar" || echo "    ✅ No vulnerable jar found"
echo ""
echo "Ready for TAK.gov submission!"