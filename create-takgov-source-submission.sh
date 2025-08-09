#!/bin/bash

# Create TAK.gov source code submission package
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
SUBMISSION_NAME="SkyFi-ATAK-Plugin-v2-SOURCE-$TIMESTAMP"

echo "📦 Creating TAK.gov Source Code Submission Package"
echo "=================================================="
echo ""

# Clean up any previous submissions
rm -rf "tak-source-submission/"
mkdir -p "tak-source-submission/$SUBMISSION_NAME"

echo "📁 Copying source files..."

# Copy only the essential source files (no binaries)
rsync -av --exclude='.git*' \
          --exclude='build/' \
          --exclude='app/build/' \
          --exclude='*.apk' \
          --exclude='*.aab' \
          --exclude='*.jar' \
          --exclude='*.zip' \
          --exclude='*.log' \
          --exclude='releases/' \
          --exclude='sdk/' \
          --exclude='ATAK-*' \
          --exclude='atak-*' \
          --exclude='tak-*' \
          --exclude='SkyFi-*.zip' \
          --exclude='.gradle/' \
          --exclude='.idea/' \
          ./ "tak-source-submission/$SUBMISSION_NAME/"

# Ensure the compatibility wrapper is included
echo "✅ Ensuring compatibility wrapper is included..."
if [ -f "app/src/main/java/com/skyfi/atak/plugin/SkyFiPluginCompatWrapper.java" ]; then
    echo "   Compatibility wrapper found and included"
else
    echo "   ⚠️ Warning: Compatibility wrapper not found"
fi

# Create README for TAK.gov
cat > "tak-source-submission/$SUBMISSION_NAME/README_TAKGOV.txt" << 'EOF'
SkyFi ATAK Plugin v2.0 - Source Code Submission
================================================

This package contains the complete source code for the SkyFi ATAK Plugin v2.0.

IMPORTANT NOTES FOR TAK.GOV BUILD:
-----------------------------------
1. This plugin has been updated to work with ATAK 5.4.0.16
2. The IServiceController dependency has been removed for compatibility
3. Please use the SkyFiPluginCompatWrapper class as the main plugin entry point
4. The plugin.xml has been configured to use the compatibility wrapper

BUILD INSTRUCTIONS:
-------------------
1. Use Java 17 for compilation
2. Target ATAK version: 5.4.0.16
3. The plugin should be signed with TAK.gov certificates for distribution

KEY FILES:
----------
- app/src/main/java/com/skyfi/atak/plugin/SkyFiPluginCompatWrapper.java (Main entry point)
- app/src/main/assets/plugin.xml (Plugin configuration)
- app/build.gradle (Build configuration)

CONTACT:
--------
Developer: SkyFi / Optisense
Email: jfuginay@optisense.ai
Date: $(date)

EOF

# Create build instructions
cat > "tak-source-submission/$SUBMISSION_NAME/BUILD_INSTRUCTIONS.md" << 'EOF'
# Build Instructions for TAK.gov

## Prerequisites
- Java 17
- Android SDK
- ATAK SDK 5.4.0.16

## Build Steps

1. Set up your environment:
   ```bash
   export JAVA_HOME=/path/to/java17
   ```

2. Configure local.properties with TAK.gov credentials:
   ```
   takrepo.url=https://artifacts.tak.gov/artifactory/maven
   takrepo.user=YOUR_USERNAME
   takrepo.password=YOUR_PASSWORD
   ```

3. Build the unsigned APK:
   ```bash
   ./gradlew assembleCivUnsigned
   ```

4. Sign with TAK.gov certificates

## Important Notes

- The plugin uses `SkyFiPluginCompatWrapper` for ATAK 5.4.0.16 compatibility
- IServiceController dependency has been removed
- Target SDK version is set to 5.4.0.16 in build.gradle

EOF

# Verify the package structure
echo ""
echo "📋 Package contents:"
echo "-------------------"
ls -la "tak-source-submission/$SUBMISSION_NAME/" | head -20

# Create the zip file
echo ""
echo "🗜️ Creating ZIP archive..."
cd "tak-source-submission/"
zip -r "../$SUBMISSION_NAME.zip" "$SUBMISSION_NAME/" -x "*.DS_Store" "*/build/*" "*/.gradle/*"
cd ..

# Check the final package
echo ""
echo "✅ Submission package created successfully!"
echo ""
echo "📦 Package details:"
ls -lh "$SUBMISSION_NAME.zip"
echo ""
echo "📤 Ready for submission to TAK.gov"
echo "   File: $SUBMISSION_NAME.zip"
echo ""
echo "Next steps:"
echo "1. Upload to TAK.gov build system"
echo "2. They will build and sign the plugin"
echo "3. Download the signed APK when ready"