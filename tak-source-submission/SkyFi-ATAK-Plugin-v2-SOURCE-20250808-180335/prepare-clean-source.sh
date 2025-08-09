#!/bin/bash

# Script to prepare clean source archive for TAK.gov submission
# This script removes build artifacts and creates a clean source archive

set -e

echo "=== Preparing SkyFi ATAK Plugin v2 for TAK.gov Submission ==="

# Define the project root
PROJECT_ROOT="/Users/jfuginay/Documents/dev/SkyFi-ATAK-Plugin-v2"
CLEAN_SOURCE_DIR="/tmp/skyfi-atak-plugin-clean"
ARCHIVE_NAME="skyfi-atak-plugin-v2-source-clean.tar.gz"

# Clean previous clean build if it exists
if [ -d "$CLEAN_SOURCE_DIR" ]; then
    echo "Removing previous clean source directory..."
    rm -rf "$CLEAN_SOURCE_DIR"
fi

# Create clean source directory
echo "Creating clean source copy..."
mkdir -p "$CLEAN_SOURCE_DIR"

# Copy source files, excluding build artifacts and other unwanted files
rsync -av \
    --exclude='*.apk' \
    --exclude='*.aar' \
    --exclude='*.so' \
    --exclude='*.jar' \
    --exclude='build/' \
    --exclude='*/build/' \
    --exclude='.gradle/' \
    --exclude='*/.gradle/' \
    --exclude='gradle/daemon/' \
    --exclude='artifacts/' \
    --exclude='dev-bundle/' \
    --exclude='skyfi-atak-bundle/' \
    --exclude='*.zip' \
    --exclude='android_keystore' \
    --exclude='local.properties' \
    --exclude='.git/' \
    --exclude='.github/' \
    --exclude='*.iml' \
    --exclude='.idea/' \
    --exclude='*.DS_Store' \
    --exclude='SkyFi-ATAK-Plugin-main/' \
    --exclude='SkyFi-ATAK-Plugin-main.zip' \
    "$PROJECT_ROOT/" "$CLEAN_SOURCE_DIR/"

# Create the clean source archive
echo "Creating clean source archive..."
cd /tmp
tar -czf "$ARCHIVE_NAME" skyfi-atak-plugin-clean/

echo ""
echo "=== Clean Source Archive Created ==="
echo "Archive: /tmp/$ARCHIVE_NAME"
echo "Size: $(du -h /tmp/$ARCHIVE_NAME | cut -f1)"
echo ""
echo "Contents verification:"
tar -tzf "/tmp/$ARCHIVE_NAME" | head -20
echo "... (showing first 20 files)"
echo ""

# Calculate archive size and file count
FILE_COUNT=$(tar -tzf "/tmp/$ARCHIVE_NAME" | wc -l)
echo "Total files in archive: $FILE_COUNT"

echo ""
echo "=== TAK.gov Submission Ready ==="
echo "✓ Build artifacts removed"
echo "✓ Clean source archive created at: /tmp/$ARCHIVE_NAME"
echo "✓ Ready for TAK.gov third-party pipeline submission"

# Cleanup temporary directory
rm -rf "$CLEAN_SOURCE_DIR"

echo ""
echo "Note: Upload the archive /tmp/$ARCHIVE_NAME to TAK.gov submission system"