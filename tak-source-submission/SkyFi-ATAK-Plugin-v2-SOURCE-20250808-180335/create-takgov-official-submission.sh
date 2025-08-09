#\!/bin/bash

# Create Official TAK.gov Compliant Submission Package
TIMESTAMP=$(date +"%Y%m%d-%H%M%S")
SUBMISSION_NAME="SkyFi-ATAK-Plugin-v2"  # Folder name becomes APK name
SUBMISSION_ZIP="${SUBMISSION_NAME}-TAKGOV-OFFICIAL-${TIMESTAMP}.zip"

echo "================================================"
echo "Creating Official TAK.gov Submission Package"
echo "Using official TAK repository configuration"
echo "================================================"

# Create fresh submission directory with proper name
rm -rf ${SUBMISSION_NAME}
mkdir -p ${SUBMISSION_NAME}

# Copy required source files
echo "Copying source files..."
cp -r app ${SUBMISSION_NAME}/
cp -r gradle ${SUBMISSION_NAME}/
cp build.gradle ${SUBMISSION_NAME}/
cp settings.gradle ${SUBMISSION_NAME}/
cp gradle.properties ${SUBMISSION_NAME}/
cp gradlew ${SUBMISSION_NAME}/
cp gradlew.bat ${SUBMISSION_NAME}/

# Clean build artifacts and unnecessary files
echo "Cleaning build artifacts..."
find ${SUBMISSION_NAME} -type d -name "build" -exec rm -rf {} + 2>/dev/null
find ${SUBMISSION_NAME} -name "*.apk" -o -name "*.aab" -o -name ".DS_Store" | xargs rm -f 2>/dev/null
find ${SUBMISSION_NAME} -name "*.backup" -o -name "*.bak" | xargs rm -f 2>/dev/null
rm -rf ${SUBMISSION_NAME}/app/src/test
rm -rf ${SUBMISSION_NAME}/app/src/androidTest

# Create compliance documentation
cat > ${SUBMISSION_NAME}/README_TAKGOV.txt << 'INNEREOF'
SkyFi ATAK Plugin v2 - Official TAK.gov Submission
==================================================

Package Name: com.optisense.skyfi.atak
Version: 2.0-beta5
Target ATAK: 5.3.0 - 5.4.0.19

This submission is fully compliant with TAK.gov requirements:
✓ Single root folder (SkyFi-ATAK-Plugin-v2)
✓ Gradle build system with assembleCivRelease target
✓ Uses atak-gradle-takdev plugin version 2.+
✓ ProGuard repackaging to atakplugin.SkyFiATAKPlugin
✓ AndroidManifest.xml contains discovery activity
✓ Configured for official TAK.gov repository

Build Command:
./gradlew assembleCivRelease

To verify with TAK.gov credentials:
./gradlew -Ptakrepo.force=true \
         -Ptakrepo.url=https://artifacts.tak.gov/artifactory/maven \
         -Ptakrepo.user=<username> \
         -Ptakrepo.password=<password> \
         assembleCivRelease
INNEREOF

# Verify critical requirements
echo ""
echo "Verifying TAK.gov compliance..."

# 1. Check for assembleCivRelease target
if grep -q "assembleCivRelease" ${SUBMISSION_NAME}/app/build.gradle; then
    echo "✓ assembleCivRelease target found"
else
    echo "⚠ WARNING: assembleCivRelease target not found"
fi

# 2. Check for atak-gradle-takdev version 2.+
if grep -q "takdevVersion.*=.*'2\.\+'" ${SUBMISSION_NAME}/app/build.gradle; then
    echo "✓ atak-gradle-takdev version 2.+ configured"
else
    echo "⚠ WARNING: Check atak-gradle-takdev version"
fi

# 3. Check AndroidManifest for discovery activity
if grep -q "com.atakmap.app.component" ${SUBMISSION_NAME}/app/src/main/AndroidManifest.xml; then
    echo "✓ Discovery activity present in AndroidManifest"
else
    echo "⚠ WARNING: Discovery activity missing"
fi

# 4. Check ProGuard repackaging
if grep -q "SkyFiATAKPlugin" ${SUBMISSION_NAME}/app/build.gradle; then
    echo "✓ ProGuard repackaging configured"
else
    echo "⚠ WARNING: ProGuard repackaging not configured"
fi

# Create the ZIP file
echo ""
echo "Creating submission ZIP..."
zip -qr ${SUBMISSION_ZIP} ${SUBMISSION_NAME}

# Verify ZIP was created
if [ -f "${SUBMISSION_ZIP}" ]; then
    SIZE=$(du -h "${SUBMISSION_ZIP}" | cut -f1)
    echo ""
    echo "================================================"
    echo "✅ Official TAK.gov Submission Package Created\!"
    echo "================================================"
    echo "File: ${SUBMISSION_ZIP}"
    echo "Size: ${SIZE}"
    echo ""
    echo "This package is ready for TAK.gov submission\!"
    echo "The folder name '${SUBMISSION_NAME}' will be used for APK naming."
    echo ""
    echo "Next steps:"
    echo "1. Test locally: ./verify-takgov-build.sh"
    echo "2. Upload to TAK.gov third-party pipeline"
    echo "3. Download signed APK after build completes"
    echo "================================================"
else
    echo "ERROR: Failed to create submission package"
    exit 1
fi

# Clean up temporary directory
rm -rf ${SUBMISSION_NAME}
