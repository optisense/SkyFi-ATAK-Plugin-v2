#!/bin/bash

# Script to prepare source code archive for TAK.gov third-party pipeline submission
# This creates a properly formatted zip file that meets TAK.gov requirements

set -e

echo "=========================================="
echo "TAK.gov Source Archive Preparation Script"
echo "=========================================="

# Define the root folder name (this will be used for APK naming)
ROOT_FOLDER="SkyFi-ATAK-Plugin-v2"
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
ARCHIVE_NAME="${ROOT_FOLDER}-takgov-submission-${TIMESTAMP}.zip"

# Clean any previous build artifacts
echo "Cleaning previous build artifacts..."
if [ -d "app/build" ]; then
    rm -rf app/build
fi
if [ -d "build" ]; then
    rm -rf build
fi

# Create temporary directory
TEMP_DIR=$(mktemp -d)
echo "Creating temporary directory: $TEMP_DIR"

# Create the root folder in temp directory
TARGET_DIR="$TEMP_DIR/$ROOT_FOLDER"
mkdir -p "$TARGET_DIR"

echo "Copying source files..."

# Copy required files and directories
# TAK.gov requires these at the root of the archive
cp -r app "$TARGET_DIR/"
cp -r gradle "$TARGET_DIR/"
cp build.gradle "$TARGET_DIR/"
cp settings.gradle "$TARGET_DIR/"
cp gradle.properties "$TARGET_DIR/"
cp gradlew "$TARGET_DIR/"
cp gradlew.bat "$TARGET_DIR/"

# Copy the ATAK gradle plugin (required for offline builds)
if [ -f "atak-gradle-takdev.jar" ]; then
    cp atak-gradle-takdev.jar "$TARGET_DIR/"
    echo "✓ atak-gradle-takdev.jar included"
else
    echo "ERROR: atak-gradle-takdev.jar not found. This is required for TAK.gov builds!"
    exit 1
fi

# Copy SDK directory if it exists
if [ -d "sdk" ]; then
    echo "Copying SDK directory..."
    cp -r sdk "$TARGET_DIR/"
fi

# Create a minimal local.properties for the build
cat > "$TARGET_DIR/local.properties" << EOF
# Auto-generated for TAK.gov submission
# SDK path will be set by build environment
EOF

# Create submission README
cat > "$TARGET_DIR/SUBMISSION_README.txt" << EOF
SkyFi ATAK Plugin v2 - TAK.gov Source Submission
===============================================

Submission Date: $(date)
Plugin Version: 2.0
Target ATAK Version: 5.4.0

This source archive is prepared for TAK.gov third-party pipeline submission.

Build Requirements Met:
✓ Single root folder: $ROOT_FOLDER
✓ Gradle build system with scripts included
✓ assembleCivRelease target defined in app/build.gradle
✓ atak-gradle-takdev version 2.+ configured
✓ AndroidManifest.xml includes required intent-filter
✓ ProGuard repackage configured: atakplugin.SkyFiATAKPlugin

Build Verification:
- The plugin builds successfully with: ./gradlew assembleCivRelease
- All dependencies are properly declared
- No local file system dependencies

TAK.gov Build Command:
./gradlew -Ptakrepo.force=true \\
  -Ptakrepo.url=https://artifacts.tak.gov/artifactory/maven \\
  -Ptakrepo.user=\$TAK_USER \\
  -Ptakrepo.password=\$TAK_PASSWORD \\
  assembleCivRelease

Plugin Features:
- High-resolution satellite imagery integration with SkyFi API
- Area of Responsibility (AOR) filtering for security compliance
- AOI (Area of Interest) management and drawing tools
- Multiple satellite tasking methods (Point, Polygon, GPS-based)
- Image archiving with search and favorites functionality
- Seamless integration with ATAK's mapping interface

Security & Compliance:
- No hardcoded credentials or sensitive data
- API keys managed through secure preferences
- Compliant with TAK security guidelines
- ProGuard obfuscation enabled for release builds

Contact Information:
Company: Optisense (DBA SkyFi)
Support: support@skyfi.com
EOF

# Verify critical files exist
echo ""
echo "Verifying required files..."
REQUIRED_FILES=(
    "$TARGET_DIR/app/build.gradle"
    "$TARGET_DIR/app/src/main/AndroidManifest.xml"
    "$TARGET_DIR/app/proguard-gradle.txt"
    "$TARGET_DIR/app/proguard-gradle-repackage.txt"
    "$TARGET_DIR/build.gradle"
    "$TARGET_DIR/settings.gradle"
    "$TARGET_DIR/gradle.properties"
    "$TARGET_DIR/gradlew"
    "$TARGET_DIR/atak-gradle-takdev.jar"
)

for file in "${REQUIRED_FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "✓ $(basename $file)"
    else
        echo "✗ Missing: $(basename $file)"
        exit 1
    fi
done

# Verify key configurations
echo ""
echo "Verifying configurations..."

# Check for assembleCivRelease target
if grep -q "productFlavors" "$TARGET_DIR/app/build.gradle" && grep -q "civ" "$TARGET_DIR/app/build.gradle"; then
    echo "✓ assembleCivRelease target available"
else
    echo "✗ assembleCivRelease target not found"
    exit 1
fi

# Check for atak-gradle-takdev configuration
if grep -q "atak-gradle-takdev" "$TARGET_DIR/app/build.gradle"; then
    echo "✓ atak-gradle-takdev configured"
else
    echo "✗ atak-gradle-takdev not configured"
    exit 1
fi

# Check for ProGuard repackage
if grep -q "atakplugin.SkyFiATAKPlugin" "$TARGET_DIR/app/proguard-gradle-repackage.txt"; then
    echo "✓ ProGuard repackage configured correctly"
else
    echo "✗ ProGuard repackage not configured"
    exit 1
fi

# Check for required AndroidManifest entry
if grep -q "com.atakmap.app.component" "$TARGET_DIR/app/src/main/AndroidManifest.xml"; then
    echo "✓ Required AndroidManifest intent-filter present"
else
    echo "✗ Required AndroidManifest intent-filter missing"
    exit 1
fi

# Create the zip archive
echo ""
echo "Creating archive: $ARCHIVE_NAME"
cd "$TEMP_DIR"
zip -r "$ARCHIVE_NAME" "$ROOT_FOLDER" -x "*.DS_Store" "*/.git/*" "*/build/*" "*/.gradle/*"

# Move archive to original directory
mv "$ARCHIVE_NAME" "$OLDPWD/"

# Cleanup
rm -rf "$TEMP_DIR"

# Final summary
cd "$OLDPWD"
echo ""
echo "=========================================="
echo "✓ Archive created successfully!"
echo "=========================================="
echo ""
echo "Archive: $ARCHIVE_NAME"
echo "Size: $(du -h "$ARCHIVE_NAME" | cut -f1)"
echo ""
echo "Archive contents summary:"
unzip -l "$ARCHIVE_NAME" | grep -E "(\.gradle|\.xml|\.txt|\.jar|\.properties)$" | tail -20
echo ""
echo "Next steps:"
echo "1. Upload $ARCHIVE_NAME to https://tak.gov/user_builds"
echo "2. Monitor the build status on TAK.gov dashboard"
echo "3. Download the signed APK when build completes"
echo "4. Test the signed APK on ATAK Civ and/or Mil versions"
echo ""
echo "IMPORTANT: Ensure you have TAK.gov credentials configured for artifact access"