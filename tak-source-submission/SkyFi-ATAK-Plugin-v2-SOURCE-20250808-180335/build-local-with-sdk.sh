#!/bin/bash

echo "========================================"
echo "Building with Local ATAK SDK"
echo "========================================"

# Use the local SDK we have
export ANDROID_HOME=${ANDROID_HOME:-$HOME/Library/Android/sdk}
export PATH=$PATH:$ANDROID_HOME/platform-tools

# Clean previous builds
echo "Cleaning previous builds..."
./gradlew clean

# Build debug APK using local SDK jar
echo "Building Debug APK..."
./gradlew assembleCivDebug -x lint -x test --offline 2>&1 | tee build.log

# Check if build succeeded
if grep -q "BUILD SUCCESSFUL" build.log; then
    echo ""
    echo "✅ Build successful!"
    echo ""
    
    # Find all APKs
    echo "Generated APKs:"
    find app/build/outputs/apk -name "*.apk" -type f 2>/dev/null | while read apk; do
        echo "  📦 $(basename $apk)"
        echo "     Path: $apk"
        echo "     Size: $(ls -lh "$apk" | awk '{print $5}')"
        
        # Check if it's signed
        if jarsigner -verify "$apk" 2>/dev/null | grep -q "jar verified"; then
            echo "     Signed: ✅ Yes"
        else
            echo "     Signed: ❌ No (debug signature)"
        fi
    done
    
    echo ""
    echo "To install on device:"
    echo "  adb install -r app/build/outputs/apk/civ/debug/*.apk"
    echo ""
    echo "Note: Debug builds work in ATAK without official signing!"
else
    echo ""
    echo "❌ Build failed. Checking errors..."
    grep -E "ERROR|FAILED|Exception" build.log | head -10
fi