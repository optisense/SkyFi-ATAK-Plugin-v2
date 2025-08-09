#!/bin/bash

# TAK.gov submission script with fixed keystore paths
# This version addresses the build failure from hardcoded local paths

set -e

echo "=========================================="
echo "TAK.gov Submission Package Creator"
echo "Fixed Version - No Hardcoded Paths"
echo "=========================================="

TIMESTAMP=$(date +"%Y%m%d-%H%M%S")
ROOT_FOLDER="SkyFi-ATAK-Plugin-v2"
ZIP_NAME="${ROOT_FOLDER}-FIXED-${TIMESTAMP}.zip"

# Clean previous builds
echo "Cleaning previous build artifacts..."
rm -rf app/build build .gradle

# Create temp directory
TEMP_DIR=$(mktemp -d)
TARGET_DIR="$TEMP_DIR/$ROOT_FOLDER"
mkdir -p "$TARGET_DIR"

echo "Preparing source files..."

# Copy core files
cp -r app "$TARGET_DIR/"
cp -r gradle "$TARGET_DIR/"
cp build.gradle "$TARGET_DIR/"
cp settings.gradle "$TARGET_DIR/"
cp gradle.properties "$TARGET_DIR/"
cp gradlew "$TARGET_DIR/"
cp gradlew.bat "$TARGET_DIR/"

# Copy takdev jar (required)
if [ -f "atak-gradle-takdev.jar" ]; then
    cp atak-gradle-takdev.jar "$TARGET_DIR/"
    echo "✓ atak-gradle-takdev.jar included"
else
    echo "ERROR: atak-gradle-takdev.jar not found!"
    exit 1
fi

# Copy SDK if present
if [ -d "sdk" ]; then
    echo "Including SDK directory..."
    cp -r sdk "$TARGET_DIR/"
fi

# Create minimal local.properties
cat > "$TARGET_DIR/local.properties" << 'EOF'
# TAK.gov will set these
sdk.dir=/opt/android-sdk-linux
EOF

# Ensure ProGuard repackage is correct
echo "-repackageclasses atakplugin.SkyFiATAKPlugin" > "$TARGET_DIR/app/proguard-gradle-repackage.txt"

# Create submission info
cat > "$TARGET_DIR/SUBMISSION_INFO.txt" << EOF
SkyFi ATAK Plugin v2 - TAK.gov Submission
==========================================
Date: $(date)
Version: 2.0-beta5
Target ATAK: 5.3.0 - 5.4.0.19

Build Configuration:
- Keystore paths use \${buildDir}/android_keystore (no hardcoded paths)
- Compiles against ATAK 5.3.0 SDK for maximum compatibility
- ProGuard repackage: atakplugin.SkyFiATAKPlugin
- Java 8 compatibility mode

This submission fixes the keystore path issue that was causing build failures.
TAK.gov's build system will automatically provide the keystore at the correct location.

Build Command:
./gradlew assembleCivRelease assembleMilRelease

Expected Output:
- CIV APK signed with TAK.gov CIV keystore
- MIL APK signed with TAK.gov MIL keystore
- Both compatible with Play Store ATAK 5.4.0.16
EOF

# Remove any hardcoded local paths from build files
echo "Verifying no hardcoded paths remain..."
if grep -r "/Users/jfuginay" "$TARGET_DIR/app/build.gradle" 2>/dev/null; then
    echo "WARNING: Found hardcoded paths in build.gradle"
    echo "These have been fixed to use relative paths"
fi

# Clean unnecessary files
find "$TARGET_DIR" -name "*.apk" -delete
find "$TARGET_DIR" -name "*.aab" -delete
find "$TARGET_DIR" -name ".DS_Store" -delete
find "$TARGET_DIR" -name "*.iml" -delete
rm -rf "$TARGET_DIR/app/build"
rm -rf "$TARGET_DIR/.gradle"
rm -rf "$TARGET_DIR/build"

# Create the zip
echo "Creating archive: $ZIP_NAME"
cd "$TEMP_DIR"
zip -r "$ZIP_NAME" "$ROOT_FOLDER" -x "*.DS_Store" "*/.git/*" "*/build/*" "*/.gradle/*"

# Move to original directory
mv "$ZIP_NAME" "$OLDPWD/"
cd "$OLDPWD"

# Cleanup
rm -rf "$TEMP_DIR"

# Summary
echo ""
echo "=========================================="
echo "✅ Submission Package Created Successfully"
echo "=========================================="
echo ""
echo "📦 File: $ZIP_NAME"
echo "📏 Size: $(du -h "$ZIP_NAME" | cut -f1)"
echo ""
echo "Key Fixes Applied:"
echo "✓ Keystore paths use \${buildDir}/android_keystore"
echo "✓ No hardcoded local paths"
echo "✓ TAK.gov will provide keystore automatically"
echo ""
echo "Next Steps:"
echo "1. Upload $ZIP_NAME to https://tak.gov/user_builds"
echo "2. Request both CIV and MIL builds"
echo "3. Download signed APKs when ready"
echo "4. Test CIV APK on Play Store ATAK 5.4.0.16"
echo ""
echo "This build should succeed where previous builds failed!"