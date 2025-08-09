#!/bin/bash

# Create TAK.gov compliant submission package
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
ROOT_FOLDER_NAME="SkyFi-ATAK-Plugin-v2"

echo "Creating TAK.gov compliant submission: $ROOT_FOLDER_NAME-$TIMESTAMP.zip"

# Clean up any previous builds
rm -rf "build/tak-submission/"
mkdir -p "build/tak-submission/$ROOT_FOLDER_NAME"

# Copy essential files for TAK.gov build
rsync -av --exclude='.git*' \
          --exclude='build/' \
          --exclude='app/build/' \
          --exclude='releases/' \
          --exclude='tak-*' \
          --exclude='SkyFi-ATAK-*.zip' \
          --exclude='SkyFi-ATAK-*.apk' \
          --exclude='ATAK-*.apk' \
          --exclude='ATAK-*.aab' \
          --exclude='*.apk' \
          --exclude='*.aab' \
          --exclude='*.log' \
          --exclude='*.pdf' \
          --exclude='*.zip' \
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
          ./ "build/tak-submission/$ROOT_FOLDER_NAME/"

# Ensure critical files are present
echo "Verifying critical files..."
ls -la "build/tak-submission/$ROOT_FOLDER_NAME/build.gradle"
ls -la "build/tak-submission/$ROOT_FOLDER_NAME/settings.gradle"
ls -la "build/tak-submission/$ROOT_FOLDER_NAME/gradlew"
ls -la "build/tak-submission/$ROOT_FOLDER_NAME/atak-gradle-takdev.jar"

# Create the zip with single root folder
cd "build/tak-submission/"
zip -r "../../$ROOT_FOLDER_NAME-TAKGOV-COMPLIANT-$TIMESTAMP.zip" "$ROOT_FOLDER_NAME/"
cd ../..

# Verify the structure
echo ""
echo "Archive created: $ROOT_FOLDER_NAME-TAKGOV-COMPLIANT-$TIMESTAMP.zip"
ls -lh "$ROOT_FOLDER_NAME-TAKGOV-COMPLIANT-$TIMESTAMP.zip"
echo ""
echo "Archive structure (first 25 entries):"
unzip -l "$ROOT_FOLDER_NAME-TAKGOV-COMPLIANT-$TIMESTAMP.zip" | head -30