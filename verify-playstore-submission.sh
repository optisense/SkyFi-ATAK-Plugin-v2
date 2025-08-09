#!/bin/bash

# Verification script for TAK.gov Play Store submission package
# This script checks that the submission package meets all requirements

set -e

echo "TAK.gov Play Store Submission Package Verification"
echo "==================================================="
echo ""

# Check if submission directory exists
SUBMISSION_DIR=$(ls -d tak-submission-playstore-* 2>/dev/null | head -n 1)

if [ -z "$SUBMISSION_DIR" ]; then
    echo "❌ No submission directory found. Run create-takgov-playstore-submission.sh first."
    exit 1
fi

echo "Checking submission: $SUBMISSION_DIR"
echo ""

# Initialize counters
PASS=0
FAIL=0

# Function to check requirement
check_requirement() {
    local description="$1"
    local condition="$2"
    
    echo -n "Checking: $description... "
    if eval "$condition"; then
        echo "✅ PASS"
        ((PASS++))
    else
        echo "❌ FAIL"
        ((FAIL++))
    fi
}

# Navigate to submission directory
cd "$SUBMISSION_DIR/SkyFi-ATAK-Plugin-v2"

# Check directory structure
check_requirement "app directory exists" "[ -d app ]"
check_requirement "src/main/java exists" "[ -d app/src/main/java ]"
check_requirement "gradle wrapper exists" "[ -f gradlew ]"
check_requirement "build.gradle exists" "[ -f build.gradle ]"
check_requirement "app/build.gradle exists" "[ -f app/build.gradle ]"

# Check package structure
check_requirement "com.optisense.skyfi.atak package exists" \
    "[ -d app/src/main/java/com/optisense/skyfi/atak ]"

# Check for Play Store plugin
check_requirement "SkyFiPlayStorePlugin.java exists" \
    "[ -f app/src/main/java/com/optisense/skyfi/atak/playstore/SkyFiPlayStorePlugin.java ]"

# Check AndroidManifest.xml
check_requirement "AndroidManifest.xml exists" "[ -f app/src/main/AndroidManifest.xml ]"
check_requirement "AndroidManifest uses correct package" \
    "grep -q 'package=\"com.optisense.skyfi.atak\"' app/src/main/AndroidManifest.xml"

# Check plugin.xml
check_requirement "plugin.xml exists" "[ -f app/src/main/assets/plugin.xml ]"
check_requirement "plugin.xml references SkyFiPlayStorePlugin" \
    "grep -q 'SkyFiPlayStorePlugin' app/src/main/assets/plugin.xml"

# Check build.gradle configuration
check_requirement "namespace is com.optisense.skyfi.atak" \
    "grep -q \"namespace 'com.optisense.skyfi.atak'\" app/build.gradle"
check_requirement "applicationId is com.optisense.skyfi.atak" \
    "grep -q 'applicationId \"com.optisense.skyfi.atak\"' app/build.gradle"

# Check for no backup files
check_requirement "No .bak files" "[ -z \"$(find . -name '*.bak' 2>/dev/null)\" ]"
check_requirement "No .backup files" "[ -z \"$(find . -name '*.backup' 2>/dev/null)\" ]"

# Check for no keystore files
check_requirement "No keystore files" "[ -z \"$(find . -name '*.keystore' -o -name '*.jks' 2>/dev/null)\" ]"

# Check for no build artifacts
check_requirement "No build directories" "[ ! -d app/build ] && [ ! -d build ]"
check_requirement "No .class files" "[ -z \"$(find . -name '*.class' 2>/dev/null)\" ]"

# Check documentation
check_requirement "README_TAKGOV.txt exists" "[ -f README_TAKGOV.txt ]"
check_requirement "BUILD_INSTRUCTIONS.md exists" "[ -f BUILD_INSTRUCTIONS.md ]"
check_requirement "VERSION.txt exists" "[ -f VERSION.txt ]"

# Check version
check_requirement "Version is 2.0-beta5" "grep -q '2.0-beta5' VERSION.txt"

# Check dependencies in build.gradle
check_requirement "OkHttp dependency present" \
    "grep -q 'com.squareup.okhttp3:okhttp' app/build.gradle"
check_requirement "Retrofit dependency present" \
    "grep -q 'com.squareup.retrofit2:retrofit' app/build.gradle"

# Check ProGuard configuration
check_requirement "proguard-gradle.txt exists" "[ -f app/proguard-gradle.txt ]"
check_requirement "proguard-gradle-repackage.txt exists" "[ -f app/proguard-gradle-repackage.txt ]"

# Check for TAK.gov specific configuration
check_requirement "local.properties exists" "[ -f local.properties ]"
check_requirement "gradle.properties exists" "[ -f gradle.properties ]"

# Check Java source files are properly packaged
check_requirement "Main plugin classes present" \
    "[ -f app/src/main/java/com/optisense/skyfi/atak/SkyFiPlugin.java ]"
check_requirement "API client present" \
    "[ -f app/src/main/java/com/optisense/skyfi/atak/APIClient.java ]"

# Return to original directory
cd ../..

echo ""
echo "==================================================="
echo "Verification Results:"
echo "==================================================="
echo "✅ Passed: $PASS"
echo "❌ Failed: $FAIL"
echo ""

if [ $FAIL -eq 0 ]; then
    echo "🎉 SUCCESS! Package is ready for TAK.gov submission."
    echo ""
    echo "Submission package: $SUBMISSION_DIR"
    echo "ZIP file should be: SkyFi-ATAK-Plugin-v2-TAKGOV-PLAYSTORE-*.zip"
    echo ""
    echo "Next steps:"
    echo "1. Submit the ZIP file to TAK.gov"
    echo "2. Request Play Store compatible signing"
    echo "3. Specify target: Google Play Store ATAK-CIV"
else
    echo "⚠️  WARNING! Package has issues that need to be fixed."
    echo "Please review the failed checks above."
fi

echo ""