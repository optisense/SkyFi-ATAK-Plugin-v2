#!/bin/bash

# SkyFi ATAK Plugin v2 - Regression Test Runner
# Run this before committing any changes to ensure no regressions

set -e

echo "=================================================="
echo "🧪 SkyFi ATAK Plugin v2 - Regression Test Suite"
echo "=================================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check Java version
echo "📋 Checking environment..."
java -version
echo ""

# Clean previous test results
echo "🧹 Cleaning previous test results..."
./gradlew clean
echo ""

# Run unit tests
echo "🔬 Running unit tests..."
if ./gradlew test; then
    echo -e "${GREEN}✅ Unit tests passed${NC}"
else
    echo -e "${RED}❌ Unit tests failed${NC}"
    echo "Please fix failing tests before committing!"
    exit 1
fi
echo ""

# Run regression test suite specifically
echo "🔄 Running regression test suite..."
if ./gradlew test --tests "com.skyfi.atak.plugin.RegressionTestSuite"; then
    echo -e "${GREEN}✅ Regression tests passed${NC}"
else
    echo -e "${RED}❌ Regression tests failed${NC}"
    echo "Regression detected! Please ensure your changes don't break existing functionality."
    exit 1
fi
echo ""

# Check for critical classes
echo "🔍 Verifying critical components..."
CRITICAL_CLASSES=(
    "SkyFiPlugin"
    "APIClient"
    "TaskingOrderFragment"
    "AOIManager"
    "CoordinateInputDialog"
    "ImageCacheManager"
)

for class in "${CRITICAL_CLASSES[@]}"; do
    if find app/src/main -name "${class}.java" | grep -q .; then
        echo -e "  ✓ ${class} found"
    else
        echo -e "${RED}  ✗ ${class} missing!${NC}"
        exit 1
    fi
done
echo -e "${GREEN}✅ All critical components present${NC}"
echo ""

# Build test APK
echo "🔨 Building test APK..."
if ./gradlew assembleCivDebug; then
    echo -e "${GREEN}✅ Test APK built successfully${NC}"
    
    # Check APK size
    APK_PATH="app/build/outputs/apk/civ/debug/"
    if [ -d "$APK_PATH" ]; then
        APK_SIZE=$(du -sh $APK_PATH/*.apk | cut -f1)
        echo "  📦 APK Size: $APK_SIZE"
    fi
else
    echo -e "${RED}❌ APK build failed${NC}"
    exit 1
fi
echo ""

# Check for ProGuard issues
echo "🛡️ Checking ProGuard configuration..."
if grep -q "^# -applymapping" app/proguard-gradle.txt; then
    echo -e "${GREEN}✅ ProGuard mapping commented out (correct for local builds)${NC}"
else
    echo -e "${YELLOW}⚠️  ProGuard mapping may cause issues${NC}"
fi
echo ""

# Generate test report
echo "📊 Generating test report..."
./gradlew jacocoTestReport || true
if [ -f "app/build/reports/tests/test/index.html" ]; then
    echo "  📄 Test report available at: app/build/reports/tests/test/index.html"
fi
echo ""

# Summary
echo "=================================================="
echo "📈 REGRESSION TEST SUMMARY"
echo "=================================================="
echo -e "${GREEN}✅ All regression tests passed!${NC}"
echo ""
echo "Your changes appear to be safe. You can proceed with:"
echo "  1. Committing your changes"
echo "  2. Creating a pull request"
echo "  3. Deploying to test environment"
echo ""
echo "Remember to:"
echo "  - Update CHANGELOG.md with your changes"
echo "  - Add new tests for any new features"
echo "  - Run integration tests on a physical device"
echo "=================================================="