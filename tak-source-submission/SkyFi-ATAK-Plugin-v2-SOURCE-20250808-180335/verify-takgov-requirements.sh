#!/bin/bash

# Script to verify TAK.gov third-party pipeline requirements before submission

echo "======================================"
echo "TAK.gov Requirements Verification"
echo "======================================"

ERRORS=0
WARNINGS=0

# Function to check requirement
check_requirement() {
    local description="$1"
    local condition="$2"
    local is_error="${3:-true}"
    
    echo -n "Checking: $description... "
    
    if eval "$condition"; then
        echo "✓ PASS"
        return 0
    else
        if [ "$is_error" = "true" ]; then
            echo "✗ FAIL"
            ((ERRORS++))
        else
            echo "⚠ WARNING"
            ((WARNINGS++))
        fi
        return 1
    fi
}

# Check Gradle files exist
check_requirement "Gradle build system present" "[ -f build.gradle ] && [ -f settings.gradle ]"
check_requirement "Gradle wrapper present" "[ -f gradlew ] && [ -f gradlew.bat ]"
check_requirement "Gradle folder present" "[ -d gradle ]"

# Check ATAK gradle plugin version
echo -n "Checking: ATAK gradle plugin version... "
if grep -q "takdevVersion = '2\.+'" app/build.gradle; then
    echo "✓ PASS (2.+)"
else
    echo "✗ FAIL (must be 2.+ for ATAK 5.4)"
    ((ERRORS++))
fi

# Check ProGuard repackage configuration
echo -n "Checking: ProGuard repackage configuration... "
if grep -q "repackageclasses atakplugin.SkyFiATAKPlugin" app/build.gradle; then
    echo "✓ PASS"
else
    echo "✗ FAIL"
    ((ERRORS++))
fi

# Check AndroidManifest.xml
echo -n "Checking: AndroidManifest.xml intent-filter... "
if grep -q '<action android:name="com.atakmap.app.component"' app/src/main/AndroidManifest.xml; then
    echo "✓ PASS"
else
    echo "✗ FAIL"
    ((ERRORS++))
fi

# Check for atak-gradle-takdev.jar
check_requirement "ATAK gradle plugin JAR present" "[ -f atak-gradle-takdev.jar ]" false

# Test assembleCivRelease target exists
echo -n "Checking: assembleCivRelease target... "
if ./gradlew tasks --all 2>/dev/null | grep -q "assembleCivRelease"; then
    echo "✓ PASS"
else
    echo "⚠ WARNING (couldn't verify, but should exist)"
    ((WARNINGS++))
fi

# Check for common issues
echo ""
echo "Additional checks:"

# Check for hardcoded paths
echo -n "Checking: No hardcoded paths in build.gradle... "
if grep -E "(C:|/Users/|/home/)" app/build.gradle; then
    echo "⚠ WARNING (found potential hardcoded paths)"
    ((WARNINGS++))
else
    echo "✓ PASS"
fi

# Check compileSdkVersion
echo -n "Checking: compileSdkVersion... "
SDK_VERSION=$(grep -oP 'compileSdkVersion\s+\K\d+' app/build.gradle)
if [ "$SDK_VERSION" -ge 30 ]; then
    echo "✓ PASS (SDK $SDK_VERSION)"
else
    echo "⚠ WARNING (SDK $SDK_VERSION may be too old)"
    ((WARNINGS++))
fi

echo ""
echo "======================================"
echo "Verification Summary:"
echo "======================================"
echo "Errors:   $ERRORS"
echo "Warnings: $WARNINGS"
echo ""

if [ $ERRORS -eq 0 ]; then
    echo "✓ All requirements met! Ready for TAK.gov submission."
    echo ""
    echo "To create submission archive, run:"
    echo "./prepare-takgov-submission.sh"
    exit 0
else
    echo "✗ Fix the errors above before submitting to TAK.gov"
    exit 1
fi