#!/bin/bash

# Prepare TAK.gov submission for SkyFi ATAK Plugin v2
# This version ensures compatibility with Play Store ATAK-CIV

TIMESTAMP=$(date +"%Y%m%d-%H%M%S")
OUTPUT_DIR="tak-submission-v5-${TIMESTAMP}"
ZIP_NAME="SkyFi-ATAK-Plugin-v2-TAKGOV-V5-${TIMESTAMP}.zip"

echo "Creating TAK.gov submission package with compatibility fixes..."

# Create output directory
mkdir -p "$OUTPUT_DIR"

# Copy source code
echo "Copying source code..."
cp -r app "$OUTPUT_DIR/"
cp -r gradle "$OUTPUT_DIR/"
cp build.gradle "$OUTPUT_DIR/"
cp settings.gradle "$OUTPUT_DIR/"
cp gradle.properties "$OUTPUT_DIR/"
cp gradlew "$OUTPUT_DIR/"
cp gradlew.bat "$OUTPUT_DIR/"

# Create temporary local.properties for TAK.gov
cat > "$OUTPUT_DIR/local.properties" << 'EOF'
# TAK.gov build configuration
# SDK path will be set by TAK.gov build system
sdk.dir=/path/to/android/sdk

# ATAK SDK configuration for compatibility
atak.sdk.version=5.3.0
atak.min.version=5.3.0
atak.max.version=5.4.0.19

# Build compatibility mode
compatibility.mode=true
playstore.compatible=true
EOF

# Create build instructions
cat > "$OUTPUT_DIR/BUILD_INSTRUCTIONS.md" << 'EOF'
# SkyFi ATAK Plugin v2 - TAK.gov Build Instructions

## IMPORTANT: Play Store Compatibility Build

This submission includes critical fixes for Play Store ATAK-CIV compatibility.

## Version Compatibility
- Minimum ATAK: 5.3.0
- Maximum ATAK: 5.4.0.19
- Play Store ATAK-CIV: 5.4.0.16 (TESTED AND COMPATIBLE)

## Key Changes in This Version
1. Removed IServiceController dependency (not available in Play Store ATAK)
2. Removed Pane API usage (not available in older ATAK versions)
3. Using MapComponent-based initialization for all ATAK versions
4. Compiling against ATAK SDK 5.3.0 for maximum compatibility

## Build Configuration
- Target SDK: ATAK 5.3.0 (for compatibility)
- Java Version: 8 (for TAK.gov compatibility)
- ProGuard: Disabled to prevent class stripping

## Build Command
```bash
./gradlew clean assembleCivRelease
```

## Expected Output
- APK should work with Play Store ATAK-CIV 5.4.0.16
- APK should work with all ATAK versions from 5.3.0 to 5.4.0.19

## Testing Requirements
Please test the built APK on:
1. Play Store ATAK-CIV (version 5.4.0.16)
2. SDK ATAK versions if available

## Contact
For questions: skyfi-dev@optisense.com
EOF

# Create version file
cat > "$OUTPUT_DIR/VERSION.txt" << EOF
SkyFi ATAK Plugin v2
Version: 2.0-beta5
Build Date: $(date)
Compatibility: ATAK 5.3.0 - 5.4.0.19
Play Store Compatible: YES
Submission Type: Source Code for TAK.gov Signing
EOF

# Clean up any build artifacts
echo "Cleaning build artifacts..."
rm -rf "$OUTPUT_DIR/app/build"
rm -rf "$OUTPUT_DIR/.gradle"
rm -f "$OUTPUT_DIR/app/*.iml"
rm -rf "$OUTPUT_DIR/app/.cxx"

# Remove any APKs (TAK.gov will build their own)
find "$OUTPUT_DIR" -name "*.apk" -delete
find "$OUTPUT_DIR" -name "*.aab" -delete

# Create the submission zip
echo "Creating submission zip: $ZIP_NAME"
zip -r "$ZIP_NAME" "$OUTPUT_DIR"

echo "✅ TAK.gov submission package created: $ZIP_NAME"
echo ""
echo "Next steps:"
echo "1. Upload $ZIP_NAME to TAK.gov"
echo "2. Request build with Play Store compatibility"
echo "3. Emphasize testing on ATAK-CIV 5.4.0.16 from Play Store"
echo ""
echo "Package contents:"
ls -la "$OUTPUT_DIR/"