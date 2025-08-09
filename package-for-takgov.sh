#\!/bin/bash

# Quick TAK.gov submission package creator
TIMESTAMP=$(date +"%Y%m%d-%H%M%S")
SUBMISSION_NAME="SkyFi-ATAK-Plugin-v2-ABSTRACTPLUGIN-${TIMESTAMP}"
SUBMISSION_DIR="tak-submission-${TIMESTAMP}"

echo "Creating TAK.gov submission with AbstractPlugin pattern..."

# Create submission directory
rm -rf ${SUBMISSION_DIR}
mkdir -p ${SUBMISSION_DIR}

# Copy source files
cp -r app ${SUBMISSION_DIR}/
cp -r gradle ${SUBMISSION_DIR}/
cp build.gradle ${SUBMISSION_DIR}/
cp settings.gradle ${SUBMISSION_DIR}/
cp gradle.properties ${SUBMISSION_DIR}/
cp gradlew ${SUBMISSION_DIR}/
cp gradlew.bat ${SUBMISSION_DIR}/

# Clean build artifacts
find ${SUBMISSION_DIR} -type d -name "build" -exec rm -rf {} + 2>/dev/null
find ${SUBMISSION_DIR} -name "*.apk" -o -name "*.aab" -o -name ".DS_Store" | xargs rm -f 2>/dev/null

# Create README
cat > ${SUBMISSION_DIR}/README.txt << 'INNEREOF'
SkyFi ATAK Plugin v2 - Uses AbstractPlugin Pattern (like Meshtastic)
Package: com.optisense.skyfi.atak
Version: 2.0-beta5
Build: ./gradlew assembleCivRelease
INNEREOF

# Create ZIP
cd ${SUBMISSION_DIR}
zip -qr ../${SUBMISSION_NAME}.zip . -x "*.DS_Store"
cd ..

# Verify and report
if [ -f "${SUBMISSION_NAME}.zip" ]; then
    SIZE=$(du -h "${SUBMISSION_NAME}.zip" | cut -f1)
    echo "✓ Created: ${SUBMISSION_NAME}.zip (${SIZE})"
    echo "Ready for TAK.gov submission\!"
    
    # Show what's included
    echo ""
    echo "Key files using AbstractPlugin pattern:"
    unzip -l ${SUBMISSION_NAME}.zip | grep -E "(plugin.xml|SkyFiLifecycle|SkyFiTool|SkyFiMapComponent)" | head -10
else
    echo "ERROR: Failed to create package"
fi

# Cleanup
rm -rf ${SUBMISSION_DIR}
