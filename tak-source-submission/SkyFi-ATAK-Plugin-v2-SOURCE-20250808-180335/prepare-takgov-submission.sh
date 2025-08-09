#!/bin/bash

echo "================================================"
echo "Preparing SkyFi Plugin for TAK.gov Submission"
echo "================================================"

# Configuration
SUBMISSION_DIR="tak-submission-$(date +%Y%m%d-%H%M%S)"
PLUGIN_NAME="SkyFi-ATAK-Plugin-v2"
SDK_VERSION="5.3.0.12"

# Create submission directory
echo "Creating submission directory: $SUBMISSION_DIR"
mkdir -p "$SUBMISSION_DIR/$PLUGIN_NAME"

# Copy source files
echo "Copying source files..."
cp -r app "$SUBMISSION_DIR/$PLUGIN_NAME/"
cp -r gradle "$SUBMISSION_DIR/$PLUGIN_NAME/"
cp build.gradle "$SUBMISSION_DIR/$PLUGIN_NAME/"
cp settings.gradle "$SUBMISSION_DIR/$PLUGIN_NAME/"
cp gradle.properties "$SUBMISSION_DIR/$PLUGIN_NAME/"
cp gradlew "$SUBMISSION_DIR/$PLUGIN_NAME/"
cp gradlew.bat "$SUBMISSION_DIR/$PLUGIN_NAME/"

# Copy documentation
echo "Copying documentation..."
cp README.md "$SUBMISSION_DIR/$PLUGIN_NAME/" 2>/dev/null || echo "# SkyFi ATAK Plugin v2" > "$SUBMISSION_DIR/$PLUGIN_NAME/README.md"

# Create local.properties for TAK.gov build environment
echo "Creating local.properties..."
cat > "$SUBMISSION_DIR/$PLUGIN_NAME/local.properties" << EOF
# TAK.gov build configuration
sdk.dir=/opt/android-sdk
ndk.dir=/opt/android-ndk
# TAK SDK location (will be configured by TAK.gov)
takdev.plugin=/path/to/atak-gradle-takdev.jar
EOF

# Update build.gradle for TAK.gov environment
echo "Updating build configuration for TAK.gov..."
sed -i.bak 's|/Users/jfuginay/Downloads/ATAK-CIV-.*-SDK|${rootDir}/sdk/ATAK-SDK|g' "$SUBMISSION_DIR/$PLUGIN_NAME/app/build.gradle"
rm "$SUBMISSION_DIR/$PLUGIN_NAME/app/build.gradle.bak"

# Create build instructions
echo "Creating build instructions..."
cat > "$SUBMISSION_DIR/BUILD_INSTRUCTIONS.md" << 'EOF'
# SkyFi ATAK Plugin v2 - Build Instructions

## Overview
This plugin provides satellite tasking capabilities for ATAK users.

## Compatibility
- Minimum ATAK Version: 5.3.0
- Maximum ATAK Version: 5.4.0.19
- Target ATAK Version: 5.4.0
- Tested with Play Store ATAK-CIV: 5.4.0.16

## Build Requirements
- Android SDK: API Level 33
- Gradle: 7.4.2
- Java: 17
- ATAK SDK: 5.3.0 or higher

## Build Steps

1. Extract the plugin source to your build directory
2. Place the ATAK SDK in `sdk/ATAK-SDK/` directory
3. Configure local.properties with your SDK paths
4. Build the plugin:
   ```bash
   ./gradlew clean assembleCivRelease
   ```

## Important Notes

- The plugin uses a compatibility layer to work with ATAK versions 5.3.0 through 5.4.0.19
- IServiceController is optional and the plugin falls back to legacy initialization if not available
- The plugin has been tested with Play Store ATAK-CIV version 5.4.0.16
- All API integrations are handled through dropdown receivers for maximum compatibility

## Plugin Features

- Satellite tasking and ordering
- AOI (Area of Interest) management
- Archive imagery browsing
- Real-time satellite feasibility analysis
- Integration with SkyFi API services

## Signing

The plugin should be signed with the official TAK.gov signing certificate for distribution.

## Support

For questions or issues, contact: support@skyfi.com
EOF

# Create compatibility notes
echo "Creating compatibility notes..."
cat > "$SUBMISSION_DIR/COMPATIBILITY_NOTES.md" << 'EOF'
# Compatibility Notes for TAK.gov Review

## API Compatibility Approach

The plugin implements a compatibility layer to work across ATAK versions 5.3.0 to 5.4.0.19:

1. **No IServiceController Dependency**: The plugin does not use IServiceController, which is not available in Play Store ATAK 5.4.0.16.

2. **MapComponent-based Initialization**: Uses SkyFiMapComponent (extends DropDownMapComponent) as the entry point, which is compatible with all ATAK versions.

3. **Dropdown Receivers**: All UI interactions use dropdown receivers instead of the newer Pane API for maximum compatibility.

4. **Runtime API Detection**: The CompatibilityHelper class detects available APIs at runtime and adapts accordingly.

5. **Menu Integration**: Uses menu.xml in assets folder for toolbar integration, which works across all versions.

## Tested Configurations

- ATAK SDK 5.3.0.12: ✓ Builds and runs
- ATAK SDK 5.4.0.18: ✓ Builds and runs
- ATAK SDK 5.4.0.19: ✓ Builds and runs
- Play Store ATAK-CIV 5.4.0.16: ✓ Compatible (using 5.3.0 SDK for compilation)

## Known Issues Resolved

- NoClassDefFoundError for IServiceController: Removed dependency
- Plugin loading failures on Play Store version: Fixed with compatibility layer
- Duplicate toolbar icons: Fixed initialization sequence

## ProGuard Configuration

The plugin includes comprehensive ProGuard rules to:
- Keep all plugin entry points
- Suppress warnings for optional APIs
- Preserve necessary class structures for reflection
EOF

# Clean up unnecessary files
echo "Cleaning up unnecessary files..."
find "$SUBMISSION_DIR" -name ".DS_Store" -delete
find "$SUBMISSION_DIR" -name "*.iml" -delete
find "$SUBMISSION_DIR" -name ".idea" -type d -exec rm -rf {} + 2>/dev/null
find "$SUBMISSION_DIR" -name "build" -type d -exec rm -rf {} + 2>/dev/null
find "$SUBMISSION_DIR" -name ".gradle" -type d -exec rm -rf {} + 2>/dev/null

# Create the final zip
echo "Creating submission archive..."
cd "$SUBMISSION_DIR"
zip -r "../$SUBMISSION_DIR.zip" . -x "*.DS_Store" -x "__MACOSX/*"
cd ..

echo "================================================"
echo "TAK.gov submission prepared successfully!"
echo "Submission archive: $SUBMISSION_DIR.zip"
echo "================================================"
echo ""
echo "Next steps:"
echo "1. Review the submission in $SUBMISSION_DIR/"
echo "2. Upload $SUBMISSION_DIR.zip to TAK.gov"
echo "3. Include the BUILD_INSTRUCTIONS.md and COMPATIBILITY_NOTES.md in your submission notes"
echo ""