#!/bin/bash

# Create TAK.gov submission package with fixed build configuration
# This version ensures artifacts are properly generated

TIMESTAMP=$(date +"%Y%m%d-%H%M%S")
OUTPUT_DIR="tak-submission-${TIMESTAMP}"
ZIP_NAME="SkyFi-ATAK-Plugin-v2-TAKGOV-${TIMESTAMP}.zip"

echo "======================================"
echo "TAK.gov Submission Package Creator"
echo "Version: 2.0-beta5"
echo "======================================"
echo ""

# Create output directory
echo "Creating submission directory..."
mkdir -p "$OUTPUT_DIR"

# Copy all necessary files
echo "Copying project files..."
cp -r app "$OUTPUT_DIR/"
cp -r gradle "$OUTPUT_DIR/"
cp build.gradle "$OUTPUT_DIR/"
cp settings.gradle "$OUTPUT_DIR/"
cp gradle.properties "$OUTPUT_DIR/"
cp gradlew "$OUTPUT_DIR/"
cp gradlew.bat "$OUTPUT_DIR/"

# Copy the takdev jar that TAK.gov needs
echo "Including atak-gradle-takdev.jar..."
cp atak-gradle-takdev.jar "$OUTPUT_DIR/"

# Use the final working build.gradle based on successful test template
if [ -f "app/build.gradle.final" ]; then
    echo "Using final working build configuration..."
    cp app/build.gradle.final "$OUTPUT_DIR/app/build.gradle"
elif [ -f "app/build.gradle.takgov-compat" ]; then
    echo "Using TAK.gov compatible build configuration..."
    cp app/build.gradle.takgov-compat "$OUTPUT_DIR/app/build.gradle"
elif [ -f "app/build.gradle.takgov" ]; then
    echo "Using TAK.gov specific build configuration..."
    cp app/build.gradle.takgov "$OUTPUT_DIR/app/build.gradle"
fi

# Update gradle wrapper to match successful test plugin
echo "Updating gradle wrapper to match working test configuration..."
sed -i.bak 's/gradle-7\.4\.2.*\.zip/gradle-7.6.1-all.zip/g' "$OUTPUT_DIR/gradle/wrapper/gradle-wrapper.properties"

# Create local.properties for TAK.gov
echo "Creating local.properties for TAK.gov..."
cat > "$OUTPUT_DIR/local.properties" << 'EOF'
# TAK.gov build configuration
# SDK path will be set by TAK.gov build system
sdk.dir=/path/to/android/sdk

# Plugin path
takdev.plugin=./atak-gradle-takdev.jar

# TAK repo (TAK.gov will override)
takrepo.url=https://artifacts.tak.gov/artifactory/maven
takrepo.user=takuser
takrepo.password=takpassword
EOF

# Create detailed build instructions
cat > "$OUTPUT_DIR/BUILD_INSTRUCTIONS.md" << 'EOF'
# SkyFi ATAK Plugin v2 - TAK.gov Build Instructions

## CRITICAL: Play Store ATAK Compatibility

This plugin has been specifically modified to work with Play Store ATAK-CIV version 5.4.0.16.

## Build Information
- **Plugin Version**: 2.0-beta5
- **Target ATAK**: 5.3.0 - 5.4.0.19
- **Play Store Compatible**: YES
- **Java Version**: 8

## Important Changes
1. **No IServiceController**: This interface is not available in Play Store ATAK
2. **No Pane API**: Using dropdown receivers for all UI
3. **MapComponent Based**: Plugin loads via SkyFiMapComponent
4. **Compatibility Mode**: Compiling against ATAK 5.3.0 for broader support

## Build Steps

1. Set up Android SDK path in local.properties
2. Ensure ATAK SDK dependencies are available
3. Run build command:

```bash
# For CIV build (recommended)
./gradlew clean assembleCivRelease

# For all flavors
./gradlew clean assembleRelease
```

## Expected Outputs

The build should produce APK files in:
- `app/build/outputs/apk/civ/release/`
- `app/build/outputs/apk/mil/release/`
- `app/build/outputs/apk/gov/release/`

## Testing Requirements

**IMPORTANT**: Please test the signed APK on Play Store ATAK-CIV (version 5.4.0.16)

The plugin should:
- Load without crashes
- Show toolbar icon
- Display menu items
- Handle all user interactions

## Troubleshooting

If build fails:
1. Check that atak-gradle-takdev.jar is present
2. Verify Android SDK path
3. Ensure gradle wrapper is executable

If no artifacts are produced:
1. Check build logs for errors
2. Verify all dependencies resolved
3. Ensure signing configuration is correct

## Support

Contact: skyfi-dev@optisense.com
GitHub: https://github.com/optisense/SkyFi-ATAK-Plugin-v2
EOF

# Create VERSION file
cat > "$OUTPUT_DIR/VERSION.txt" << EOF
SkyFi ATAK Plugin v2
Version: 2.0-beta5
Build Date: $(date)
Compatibility: ATAK 5.3.0 - 5.4.0.19
Play Store ATAK-CIV: COMPATIBLE
Submission ID: ${TIMESTAMP}
EOF

# Create a gradle wrapper properties file with specific version
cat > "$OUTPUT_DIR/gradle/wrapper/gradle-wrapper.properties" << 'EOF'
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-7.4.2-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
EOF

# Clean build artifacts
echo "Cleaning build artifacts..."
rm -rf "$OUTPUT_DIR/app/build"
rm -rf "$OUTPUT_DIR/.gradle"
rm -rf "$OUTPUT_DIR/build"
find "$OUTPUT_DIR" -name "*.apk" -delete
find "$OUTPUT_DIR" -name "*.aab" -delete
find "$OUTPUT_DIR" -name ".DS_Store" -delete

# Create the zip file
echo "Creating submission package..."
zip -r "$ZIP_NAME" "$OUTPUT_DIR" -x "*.DS_Store" -x "__MACOSX/*"

# Calculate checksums
echo ""
echo "Package Information:"
echo "===================="
echo "File: $ZIP_NAME"
echo "Size: $(du -h "$ZIP_NAME" | cut -f1)"
echo "MD5: $(md5 -q "$ZIP_NAME" 2>/dev/null || md5sum "$ZIP_NAME" | cut -d' ' -f1)"
echo ""

# Final instructions
echo "✅ TAK.gov submission package created successfully!"
echo ""
echo "📦 Package: $ZIP_NAME"
echo ""
echo "Next Steps:"
echo "1. Upload $ZIP_NAME to TAK.gov"
echo "2. In submission notes, emphasize:"
echo "   - Must test on Play Store ATAK-CIV 5.4.0.16"
echo "   - Plugin has been modified for compatibility"
echo "   - No IServiceController or Pane API usage"
echo "3. Request expedited build if possible"
echo ""
echo "⚠️  IMPORTANT: When TAK.gov builds complete, immediately test on Play Store ATAK!"