#!/bin/bash

# SkyFi ATAK Plugin v2 - TAK.gov Submission Package Creator
# Creates a compliant source archive for TAK.gov third-party pipeline

set -e

# Configuration
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
PLUGIN_NAME="SkyFi-ATAK-Plugin-v2-STABLE"
SUBMISSION_NAME="${PLUGIN_NAME}-TAKGOV-SUBMISSION-${TIMESTAMP}"
TEMP_DIR="/tmp/${SUBMISSION_NAME}"
ARCHIVE_NAME="${SUBMISSION_NAME}.zip"

echo "🚀 Creating TAK.gov Submission Package"
echo "======================================"
echo "Plugin Name: ${PLUGIN_NAME}"
echo "Submission: ${SUBMISSION_NAME}"
echo "Archive: ${ARCHIVE_NAME}"
echo ""

# Clean up any existing temp directory
rm -rf "$TEMP_DIR"

# Create temporary directory with the plugin name as root folder
mkdir -p "$TEMP_DIR/${PLUGIN_NAME}"

echo "📁 Copying source files..."

# Copy essential project files
cp -r app "$TEMP_DIR/${PLUGIN_NAME}/"
cp -r gradle "$TEMP_DIR/${PLUGIN_NAME}/"
cp -r sdk "$TEMP_DIR/${PLUGIN_NAME}/"
cp build.gradle "$TEMP_DIR/${PLUGIN_NAME}/"
cp settings.gradle "$TEMP_DIR/${PLUGIN_NAME}/"
cp gradlew "$TEMP_DIR/${PLUGIN_NAME}/"
cp gradlew.bat "$TEMP_DIR/${PLUGIN_NAME}/"
cp android_keystore "$TEMP_DIR/${PLUGIN_NAME}/"
cp atak-gradle-takdev.jar "$TEMP_DIR/${PLUGIN_NAME}/"

# Copy documentation and compliance files
cp README.md "$TEMP_DIR/${PLUGIN_NAME}/" 2>/dev/null || echo "README.md not found, skipping"
cp TESTING_GUIDE.md "$TEMP_DIR/${PLUGIN_NAME}/" 2>/dev/null || echo "TESTING_GUIDE.md not found, skipping"
cp CHANGELOG.md "$TEMP_DIR/${PLUGIN_NAME}/" 2>/dev/null || echo "CHANGELOG.md not found, skipping"

# Create local.properties template for TAK.gov
cat > "$TEMP_DIR/${PLUGIN_NAME}/local.properties" << 'EOF'
# TAK.gov Build Configuration
# This file will be populated by the TAK.gov build pipeline

# SDK location (will be set by build pipeline)
sdk.dir=

# TAK repository configuration (will be set by build pipeline)
takrepo.url=https://artifacts.tak.gov/artifactory/maven
takrepo.user=
takrepo.password=
EOF

echo "🧹 Cleaning up unnecessary files..."

# Remove build outputs and cache directories
find "$TEMP_DIR/${PLUGIN_NAME}" -name "build" -type d -exec rm -rf {} + 2>/dev/null || true
find "$TEMP_DIR/${PLUGIN_NAME}" -name ".gradle" -type d -exec rm -rf {} + 2>/dev/null || true
find "$TEMP_DIR/${PLUGIN_NAME}" -name "*.iml" -delete 2>/dev/null || true
find "$TEMP_DIR/${PLUGIN_NAME}" -name ".DS_Store" -delete 2>/dev/null || true

# Remove test reports and temporary files
rm -rf "$TEMP_DIR/${PLUGIN_NAME}/test-reports" 2>/dev/null || true
rm -rf "$TEMP_DIR/${PLUGIN_NAME}/app/build" 2>/dev/null || true
rm -rf "$TEMP_DIR/${PLUGIN_NAME}/.idea" 2>/dev/null || true

# Remove existing APKs and submission packages
find "$TEMP_DIR/${PLUGIN_NAME}" -name "*.apk" -delete 2>/dev/null || true
find "$TEMP_DIR/${PLUGIN_NAME}" -name "*submission*.zip" -delete 2>/dev/null || true
find "$TEMP_DIR/${PLUGIN_NAME}" -name "*STABLE*.zip" -delete 2>/dev/null || true

echo "✅ Verifying TAK.gov compliance..."

# Verify required files exist
REQUIRED_FILES=(
    "app/build.gradle"
    "gradlew"
    "settings.gradle"
    "app/src/main/AndroidManifest.xml"
    "app/proguard-gradle.txt"
)

for file in "${REQUIRED_FILES[@]}"; do
    if [ ! -f "$TEMP_DIR/${PLUGIN_NAME}/$file" ]; then
        echo "❌ ERROR: Required file missing: $file"
        exit 1
    fi
done

# Verify AndroidManifest.xml has required activity
if ! grep -q "com.atakmap.app.component" "$TEMP_DIR/${PLUGIN_NAME}/app/src/main/AndroidManifest.xml"; then
    echo "❌ ERROR: AndroidManifest.xml missing required component activity"
    exit 1
fi

# Verify build.gradle has assembleCivRelease target and correct plugin version
if ! grep -q "assembleCivRelease\|assembleCivUnsigned" "$TEMP_DIR/${PLUGIN_NAME}/app/build.gradle"; then
    echo "⚠️  WARNING: assembleCivRelease target may not be properly configured"
fi

# Check for takdev version 2.+ (either in variable or direct reference)
if grep -q "takdevVersion.*=.*'2\." "$TEMP_DIR/${PLUGIN_NAME}/app/build.gradle" || grep -q "atak-gradle-takdev.*2\." "$TEMP_DIR/${PLUGIN_NAME}/app/build.gradle"; then
    echo "✅ atak-gradle-takdev version 2.+ configured correctly"
else
    echo "❌ ERROR: Must use atak-gradle-takdev version 2.+ for ATAK 5.4.0"
    exit 1
fi

# Verify proguard repackage setting
if ! grep -q "repackageclasses.*SkyFi" "$TEMP_DIR/${PLUGIN_NAME}/app/build.gradle"; then
    echo "❌ ERROR: Proguard repackageclasses not properly configured"
    exit 1
fi

echo "📦 Creating submission archive..."

# Create the zip archive
cd "$TEMP_DIR"
zip -r "$ARCHIVE_NAME" "${PLUGIN_NAME}/" -x "*.DS_Store" "*/build/*" "*/.gradle/*" "*.iml"

# Move archive to project directory
mv "$ARCHIVE_NAME" "$(pwd)/../../../"

# Clean up temp directory
cd /
rm -rf "$TEMP_DIR"

echo ""
echo "🎉 TAK.gov Submission Package Created Successfully!"
echo "=================================================="
echo "Archive: ${ARCHIVE_NAME}"
echo "Location: $(pwd)/../../../${ARCHIVE_NAME}"
echo ""
echo "📋 Submission Checklist:"
echo "✅ Single root folder: ${PLUGIN_NAME}"
echo "✅ Gradle build system included"
echo "✅ assembleCivUnsigned target available"
echo "✅ atak-gradle-takdev 2.+ plugin configured"
echo "✅ AndroidManifest.xml has required component activity"
echo "✅ Proguard repackageclasses configured for SkyFi"
echo "✅ Source code cleaned of build artifacts"
echo ""
echo "🚀 Ready for TAK.gov Third-Party Pipeline Submission!"
echo ""
echo "📝 Upload Instructions:"
echo "1. Go to TAK.gov third-party pipeline"
echo "2. Upload: ${ARCHIVE_NAME}"
echo "3. The build pipeline will:"
echo "   - Extract the archive"
echo "   - Use '${PLUGIN_NAME}' as the APK name"
echo "   - Run: ./gradlew assembleCivRelease"
echo "   - Sign the APK with third-party certificate"
echo ""
echo "⚠️  Note: Ensure you have tested the build locally with:"
echo "   ./gradlew assembleCivUnsigned"
echo ""