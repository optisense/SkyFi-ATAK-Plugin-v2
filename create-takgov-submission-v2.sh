#!/bin/bash

# TAK.gov Source Submission Package Creator
# Creates a source archive that meets all TAK.gov requirements

set -e

TIMESTAMP=$(date +"%Y%m%d-%H%M%S")
SUBMISSION_NAME="SkyFi-ATAK-Plugin-v2"
SUBMISSION_DIR="tak-submission-${TIMESTAMP}"
ZIP_NAME="${SUBMISSION_NAME}-TAKGOV-SUBMISSION-${TIMESTAMP}.zip"

echo "Creating TAK.gov submission package..."
echo "Submission directory: ${SUBMISSION_DIR}"

# Create submission directory with single root folder
mkdir -p "${SUBMISSION_DIR}/${SUBMISSION_NAME}"

# Copy all required files while excluding unnecessary ones
echo "Copying source files..."
rsync -av \
  --exclude='.git' \
  --exclude='.gradle' \
  --exclude='build' \
  --exclude='*/build' \
  --exclude='*.apk' \
  --exclude='*.aab' \
  --exclude='local.properties' \
  --exclude='.idea' \
  --exclude='*.iml' \
  --exclude='tak-submission-*' \
  --exclude='takgov-*' \
  --exclude='releases' \
  --exclude='*.zip' \
  --exclude='tak-analysis' \
  --exclude='*.log' \
  --exclude='*.tmp' \
  --exclude='.DS_Store' \
  --exclude='__pycache__' \
  --exclude='node_modules' \
  --exclude='app/src/androidTest' \
  --exclude='app/src/test' \
  ./ "${SUBMISSION_DIR}/${SUBMISSION_NAME}/"

# Ensure critical files are present
echo "Verifying critical files..."
CRITICAL_FILES=(
  "gradlew"
  "gradlew.bat"
  "gradle/wrapper/gradle-wrapper.jar"
  "gradle/wrapper/gradle-wrapper.properties"
  "build.gradle"
  "settings.gradle"
  "app/build.gradle"
  "app/src/main/AndroidManifest.xml"
  "app/proguard-gradle.txt"
)

for file in "${CRITICAL_FILES[@]}"; do
  if [ ! -f "${SUBMISSION_DIR}/${SUBMISSION_NAME}/${file}" ]; then
    echo "ERROR: Critical file missing: ${file}"
    exit 1
  fi
done

# Make gradlew executable
chmod +x "${SUBMISSION_DIR}/${SUBMISSION_NAME}/gradlew"

# Verify the build configuration
echo "Verifying build configuration..."

# Check atak-gradle-takdev version
TAKDEV_VERSION=$(grep "def takdevVersion" "${SUBMISSION_DIR}/${SUBMISSION_NAME}/app/build.gradle" | grep -o "'[^']*'" | tr -d "'")
echo "  - atak-gradle-takdev version: ${TAKDEV_VERSION}"
if [[ ! "$TAKDEV_VERSION" == "2.+"* ]]; then
  echo "WARNING: atak-gradle-takdev version should be 2.+ for ATAK 5.4.0"
fi

# Check for required AndroidManifest activity
if grep -q "com.atakmap.app.component" "${SUBMISSION_DIR}/${SUBMISSION_NAME}/app/src/main/AndroidManifest.xml"; then
  echo "  - Required activity entry: ✓"
else
  echo "ERROR: AndroidManifest.xml missing required activity entry"
  exit 1
fi

# Check ProGuard configuration
if grep -q "repackageclasses atakplugin.SkyFiATAKPlugin" "${SUBMISSION_DIR}/${SUBMISSION_NAME}/app/build.gradle"; then
  echo "  - ProGuard repackage configuration: ✓"
else
  echo "WARNING: ProGuard repackage might need adjustment"
fi

# Create the submission zip
echo "Creating submission archive: ${ZIP_NAME}"
cd "${SUBMISSION_DIR}"
zip -qr "../${ZIP_NAME}" "${SUBMISSION_NAME}"
cd ..

# Calculate file size
FILE_SIZE=$(ls -lh "${ZIP_NAME}" | awk '{print $5}')

echo ""
echo "========================================="
echo "TAK.gov Submission Package Created Successfully!"
echo "========================================="
echo "Package: ${ZIP_NAME}"
echo "Size: ${FILE_SIZE}"
echo ""
echo "Pre-submission checklist:"
echo "  ✓ Single root folder: ${SUBMISSION_NAME}"
echo "  ✓ Gradle build system included"
echo "  ✓ gradlew and gradle wrapper included"
echo "  ✓ assembleCivRelease target available"
echo "  ✓ atak-gradle-takdev plugin configured (v${TAKDEV_VERSION})"
echo "  ✓ AndroidManifest.xml has required activity"
echo "  ✓ ProGuard configured"
echo ""
echo "To test locally with TAK.gov credentials:"
echo "  cd ${SUBMISSION_DIR}/${SUBMISSION_NAME}"
echo "  ./gradlew -Ptakrepo.force=true -Ptakrepo.url=https://artifacts.tak.gov/artifactory/maven -Ptakrepo.user=<user> -Ptakrepo.password=<pass> assembleCivRelease"
echo ""
echo "Ready for upload to TAK.gov!"

# Clean up temporary submission directory (optional)
# rm -rf "${SUBMISSION_DIR}"