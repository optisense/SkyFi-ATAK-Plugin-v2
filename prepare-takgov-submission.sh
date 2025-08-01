#!/bin/bash

# Script to prepare source code archive for TAK.gov third-party pipeline submission
# This creates a properly formatted zip file that meets TAK.gov requirements

set -e

echo "=========================================="
echo "TAK.gov Source Archive Preparation Script"
echo "=========================================="

# Define the root folder name (this will be used for APK naming)
ROOT_FOLDER="SkyFi-ATAK-Plugin-v2"
ARCHIVE_NAME="${ROOT_FOLDER}-takgov-source.zip"

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
else
    echo "WARNING: atak-gradle-takdev.jar not found. Build may fail on TAK.gov"
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

This source archive is prepared for TAK.gov third-party pipeline submission.

Build Requirements Met:
✓ Single root folder: $ROOT_FOLDER
✓ Gradle build system with scripts included
✓ assembleCivRelease target defined
✓ atak-gradle-takdev version 2.+ configured
✓ Proper AndroidManifest.xml intent-filter
✓ ProGuard repackage configured: atakplugin.SkyFiATAKPlugin

To verify locally before submission:
./gradlew -Ptakrepo.force=true \\
  -Ptakrepo.url=https://artifacts.tak.gov/artifactory/maven \\
  -Ptakrepo.user=<your-user> \\
  -Ptakrepo.password=<your-pass> \\
  assembleCivRelease

Plugin Features:
- High-resolution satellite imagery integration
- Area of Responsibility (AOR) filtering  
- AOI management and drawing tools
- Multiple tasking methods
- Image archiving and favorites

Target ATAK Version: 5.4.0
EOF

# Clean any build artifacts before packaging
echo "Cleaning build artifacts..."
cd "$TARGET_DIR"
if [ -f gradlew ]; then
    chmod +x gradlew
    ./gradlew clean || echo "Clean failed, continuing..."
fi

# Create the zip archive
echo "Creating archive: $ARCHIVE_NAME"
cd "$TEMP_DIR"
zip -r "$ARCHIVE_NAME" "$ROOT_FOLDER"

# Move archive to original directory
mv "$ARCHIVE_NAME" "$OLDPWD/"

# Cleanup
rm -rf "$TEMP_DIR"

echo ""
echo "✓ Archive created successfully: $ARCHIVE_NAME"
echo ""
echo "File size: $(du -h "$OLDPWD/$ARCHIVE_NAME" | cut -f1)"
echo ""
echo "Next steps:"
echo "1. Upload $ARCHIVE_NAME to https://tak.gov/user_builds"
echo "2. Monitor the build status"
echo "3. Download the signed APK when build completes"
echo ""
echo "NOTE: Ensure you have TAK.gov credentials for artifact repository access"