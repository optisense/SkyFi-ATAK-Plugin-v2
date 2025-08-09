#!/bin/bash

# Script to prepare source package for TAK.gov submission
# This creates a clean source archive that meets all TAK.gov requirements

set -e

echo "============================================"
echo "Preparing TAK.gov Source Submission Package"
echo "============================================"

# Configuration
TIMESTAMP=$(date +"%Y%m%d-%H%M%S")
PROJECT_NAME="SkyFi-ATAK-Plugin-v2"
OUTPUT_DIR="tak-submission-${TIMESTAMP}"
ZIP_NAME="${PROJECT_NAME}-TAKGOV-SUBMISSION-${TIMESTAMP}.zip"

echo "Creating output directory: ${OUTPUT_DIR}"
rm -rf "${OUTPUT_DIR}"
mkdir -p "${OUTPUT_DIR}/${PROJECT_NAME}"

echo "Copying essential source files..."

# Copy gradle files
cp -r gradle "${OUTPUT_DIR}/${PROJECT_NAME}/"
cp gradlew "${OUTPUT_DIR}/${PROJECT_NAME}/"
cp gradlew.bat "${OUTPUT_DIR}/${PROJECT_NAME}/"
cp settings.gradle "${OUTPUT_DIR}/${PROJECT_NAME}/"

# Write a TAK.gov-compatible root build.gradle (AGP 7.4.2 works with Gradle 7.6.1)
cat > "${OUTPUT_DIR}/${PROJECT_NAME}/build.gradle" << 'EOF'
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:7.4.2'
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
EOF

# Copy the TAK.gov-specific app/build.gradle (minimal deps, no bundle block)
mkdir -p "${OUTPUT_DIR}/${PROJECT_NAME}/app"
cp app/build.gradle.takgov "${OUTPUT_DIR}/${PROJECT_NAME}/app/build.gradle"

# Create gradle.properties without MaxPermSize option
cat > "${OUTPUT_DIR}/${PROJECT_NAME}/gradle.properties" << 'EOF'
# Gradle properties for TAK.gov build
org.gradle.daemon=true
org.gradle.configureondemand=true
org.gradle.parallel=true
org.gradle.jvmargs=-Xmx4096m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8
android.useAndroidX=true
android.enableJetifier=false

# TAK.gov will provide credentials for their Maven repository
systemProp.takrepo.force=true
EOF

# Include atak-gradle-takdev.jar for offline SDK resolution
if [ -f "atak-gradle-takdev.jar" ]; then
  cp atak-gradle-takdev.jar "${OUTPUT_DIR}/${PROJECT_NAME}/"
else
  echo "WARNING: atak-gradle-takdev.jar not found; TAK.gov will need network access to fetch SDK"
fi

# Copy app directory structure
echo "Copying app source code..."

# Copy source directories
cp -r app/src "${OUTPUT_DIR}/${PROJECT_NAME}/app/"

# Copy proguard files
cp app/proguard-gradle.txt "${OUTPUT_DIR}/${PROJECT_NAME}/app/" 2>/dev/null || echo "No proguard-gradle.txt found"

# Create proguard-gradle-repackage.txt with proper content
echo "-repackageclasses atakplugin.SkyFiATAKPlugin" > "${OUTPUT_DIR}/${PROJECT_NAME}/app/proguard-gradle-repackage.txt"

# Copy libs if they exist
if [ -d "app/libs" ]; then
    cp -r app/libs "${OUTPUT_DIR}/${PROJECT_NAME}/app/"
fi

# Create local.properties template for TAK.gov
cat > "${OUTPUT_DIR}/${PROJECT_NAME}/local.properties" << 'EOF'
# TAK.gov will populate these values
sdk.dir=/path/to/android/sdk
takrepo.url=https://artifacts.tak.gov/artifactory/maven
takrepo.user=YOUR_USERNAME
takrepo.password=YOUR_PASSWORD
takrepo.force=true
EOF

# Create a README for TAK.gov
cat > "${OUTPUT_DIR}/${PROJECT_NAME}/README_TAKGOV.txt" << 'EOF'
SkyFi ATAK Plugin v2.0 - TAK.gov Build Instructions
====================================================

This source package has been prepared for the TAK.gov build pipeline.

Build Requirements:
- Java 17 (compatible with TAK.gov environment)
- Android SDK
- Gradle 7.5
- atak-gradle-takdev plugin version 2.+ (latest)

Build Command:
./gradlew -Ptakrepo.force=true \
          -Ptakrepo.url=https://artifacts.tak.gov/artifactory/maven \
          -Ptakrepo.user=<user> \
          -Ptakrepo.password=<pass> \
          assembleCivRelease

Where <user> and <pass> are your artifacts.tak.gov credentials.

The source structure complies with TAK.gov requirements:
- Single root folder containing all source
- Gradle build system
- assembleCivRelease target defined
- Uses atak-gradle-takdev plugin for SDK resolution
- AndroidManifest.xml contains required intent-filter for plugin discovery
- Proguard repackaging configured as "atakplugin.SkyFiATAKPlugin"

Note: This package does not include signing configuration.
TAK.gov will sign the APK using their official certificates.
EOF

# Clean up unnecessary files
echo "Cleaning up unnecessary files..."
find "${OUTPUT_DIR}/${PROJECT_NAME}" -name ".DS_Store" -delete 2>/dev/null || true
find "${OUTPUT_DIR}/${PROJECT_NAME}" -name "*.iml" -delete 2>/dev/null || true
find "${OUTPUT_DIR}/${PROJECT_NAME}" -name ".idea" -type d -exec rm -rf {} + 2>/dev/null || true
find "${OUTPUT_DIR}/${PROJECT_NAME}" -name "build" -type d -exec rm -rf {} + 2>/dev/null || true
find "${OUTPUT_DIR}/${PROJECT_NAME}" -name ".gradle" -type d -exec rm -rf {} + 2>/dev/null || true

# Remove any APK files that might be present
find "${OUTPUT_DIR}/${PROJECT_NAME}" -name "*.apk" -delete 2>/dev/null || true
find "${OUTPUT_DIR}/${PROJECT_NAME}" -name "*.aab" -delete 2>/dev/null || true

# Create the zip archive
echo "Creating zip archive: ${ZIP_NAME}"
cd "${OUTPUT_DIR}"
zip -r "../${ZIP_NAME}" "${PROJECT_NAME}"
cd ..

# Verify the package
echo ""
echo "Verifying package structure..."
unzip -l "${ZIP_NAME}" | head -20

echo ""
echo "============================================"
echo "TAK.gov Submission Package Created!"
echo "============================================"
echo "Package: ${ZIP_NAME}"
echo "Size: $(du -h ${ZIP_NAME} | cut -f1)"
echo ""
echo "This package is ready for submission to TAK.gov"
echo ""
echo "Before submission, verify that you can build locally with:"
echo "./gradlew -Ptakrepo.force=true -Ptakrepo.url=https://artifacts.tak.gov/artifactory/maven -Ptakrepo.user=<user> -Ptakrepo.password=<pass> assembleCivRelease"
echo ""
echo "Note: You need valid TAK.gov credentials to test the build."
echo "============================================"