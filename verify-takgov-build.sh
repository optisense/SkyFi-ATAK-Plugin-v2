#!/bin/bash

# Script to verify TAK.gov build configuration and detect potential issues
# Run this before submitting to TAK.gov

set -e

echo "================================================"
echo "TAK.gov Build Configuration Verification Script"
echo "================================================"
echo ""

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Track issues
ISSUES_FOUND=0

echo "1. Checking Java Configuration..."
echo "--------------------------------"

# Check Java version in build.gradle
if grep -q "JavaVersion.VERSION_1_8" app/build.gradle; then
    echo -e "${GREEN}✓${NC} Java 8 compatibility configured (correct for TAK.gov)"
elif grep -q "JavaVersion.VERSION_11" app/build.gradle; then
    echo -e "${RED}✗${NC} Java 11 found - TAK.gov may have issues with this"
    echo "  Recommendation: Use JavaVersion.VERSION_1_8"
    ((ISSUES_FOUND++))
elif grep -q "JavaVersion.VERSION_17" app/build.gradle; then
    echo -e "${RED}✗${NC} Java 17 found - TAK.gov build uses Java 17 but plugin should target Java 8"
    echo "  Recommendation: Use JavaVersion.VERSION_1_8 for compatibility"
    ((ISSUES_FOUND++))
fi

echo ""
echo "2. Checking Bundle Configuration..."
echo "-----------------------------------"

# Check for AAB prevention
if grep -q "enableSplit = false" app/build.gradle; then
    echo -e "${GREEN}✓${NC} AAB split prevention configured"
else
    echo -e "${YELLOW}⚠${NC} AAB split prevention not found - TAK.gov might generate AAB instead of APK"
    echo "  Recommendation: Add bundle configuration to disable splits"
fi

echo ""
echo "3. Checking ProGuard Configuration..."
echo "-------------------------------------"

# Check ProGuard files exist
if [ -f "app/proguard-gradle.txt" ]; then
    echo -e "${GREEN}✓${NC} ProGuard rules file exists"
    
    # Check for critical rules
    if grep -q "com.skyfi.atak.plugin.SkyFiPlugin" app/proguard-gradle.txt; then
        echo -e "${GREEN}✓${NC} Plugin class keep rules found"
    else
        echo -e "${YELLOW}⚠${NC} Plugin class not explicitly kept in ProGuard"
        echo "  Recommendation: Add -keep rules for SkyFiPlugin"
    fi
    
    if grep -q "androidx" app/proguard-gradle.txt; then
        echo -e "${GREEN}✓${NC} AndroidX rules found"
    else
        echo -e "${YELLOW}⚠${NC} AndroidX rules not found - may cause runtime crashes"
    fi
else
    echo -e "${RED}✗${NC} ProGuard rules file missing!"
    ((ISSUES_FOUND++))
fi

echo ""
echo "4. Checking Plugin Implementation..."
echo "------------------------------------"

# Check for Looper.prepare() bug
if grep -q "Looper.prepare()" app/src/main/java/com/skyfi/atak/plugin/SkyFiPlugin.java; then
    echo -e "${RED}✗${NC} Looper.prepare() found in plugin - this will cause crashes!"
    echo "  This is a critical bug that must be fixed"
    ((ISSUES_FOUND++))
else
    echo -e "${GREEN}✓${NC} No Looper.prepare() calls found"
fi

# Check for onStart implementation
if grep -q "public void onStart()" app/src/main/java/com/skyfi/atak/plugin/SkyFiPlugin.java; then
    echo -e "${GREEN}✓${NC} onStart() method implemented"
else
    echo -e "${RED}✗${NC} onStart() method not found!"
    ((ISSUES_FOUND++))
fi

# Check for logging in onStart
if grep -q "Log.d.*onStart" app/src/main/java/com/skyfi/atak/plugin/SkyFiPlugin.java; then
    echo -e "${GREEN}✓${NC} Logging added to onStart() for debugging"
else
    echo -e "${YELLOW}⚠${NC} No logging in onStart() - consider adding for debugging"
fi

echo ""
echo "5. Checking AndroidManifest..."
echo "------------------------------"

# Check for required intent filter
if grep -q "com.atakmap.app.component" app/src/main/AndroidManifest.xml; then
    echo -e "${GREEN}✓${NC} Required ATAK intent filter present"
else
    echo -e "${RED}✗${NC} Required ATAK intent filter missing!"
    ((ISSUES_FOUND++))
fi

# Check for plugin-api metadata
if grep -q "plugin-api" app/src/main/AndroidManifest.xml; then
    echo -e "${GREEN}✓${NC} Plugin API metadata present"
else
    echo -e "${RED}✗${NC} Plugin API metadata missing!"
    ((ISSUES_FOUND++))
fi

echo ""
echo "6. Checking Dependencies..."
echo "---------------------------"

# Check for problematic dependencies
if grep -q "okhttp3:okhttp:4.12" app/build.gradle; then
    echo -e "${YELLOW}⚠${NC} OkHttp 4.12 found - may have Kotlin 1.9 dependency issues"
    echo "  Consider downgrading to 4.10.0 or 3.14.9"
fi

if grep -q "retrofit2:retrofit:2.11" app/build.gradle; then
    echo -e "${YELLOW}⚠${NC} Retrofit 2.11 found - may have compatibility issues"
    echo "  Consider downgrading to 2.9.0"
fi

echo ""
echo "7. Checking Build Configuration..."
echo "----------------------------------"

# Check for namespace declaration
if grep -q "namespace 'com.skyfi.atak.plugin'" app/build.gradle; then
    echo -e "${GREEN}✓${NC} Namespace properly declared"
else
    echo -e "${YELLOW}⚠${NC} Namespace not declared - required for newer Gradle versions"
fi

# Check for signing configuration
if grep -q "signingConfig signingConfigs.debug" app/build.gradle; then
    echo -e "${YELLOW}⚠${NC} Debug signing configured - TAK.gov will override this"
    echo "  This is OK for local testing but TAK.gov will use their own signing"
fi

echo ""
echo "8. Testing Local Build..."
echo "-------------------------"

# Try to build locally
echo "Attempting local build..."
if ./gradlew assembleCivRelease --no-daemon 2>&1 | grep -q "BUILD SUCCESSFUL"; then
    echo -e "${GREEN}✓${NC} Local build successful"
    
    # Check if APK was created (not AAB)
    if [ -f "app/build/outputs/apk/civ/release/"*.apk ]; then
        echo -e "${GREEN}✓${NC} APK file generated (not AAB)"
    else
        echo -e "${RED}✗${NC} No APK found - might be generating AAB instead"
        ((ISSUES_FOUND++))
    fi
else
    echo -e "${RED}✗${NC} Local build failed - fix errors before submitting to TAK.gov"
    ((ISSUES_FOUND++))
fi

echo ""
echo "================================================"
echo "Verification Complete"
echo "================================================"
echo ""

if [ $ISSUES_FOUND -eq 0 ]; then
    echo -e "${GREEN}✓ No critical issues found!${NC}"
    echo "Your build configuration appears ready for TAK.gov submission."
else
    echo -e "${RED}✗ Found $ISSUES_FOUND critical issue(s) that need fixing${NC}"
    echo "Please address the issues above before submitting to TAK.gov."
    exit 1
fi

echo ""
echo "Next steps:"
echo "1. Run: ./create-takgov-submission.sh"
echo "2. Upload the generated zip to TAK.gov"
echo "3. Monitor build status on TAK.gov dashboard"
echo "4. Test the signed APK when ready"