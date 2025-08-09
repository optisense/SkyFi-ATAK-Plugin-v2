#!/bin/bash

echo "=============================================="
echo "Building SkyFi ATAK Plugin with TAK.gov Access"
echo "=============================================="

# Note: Replace these with your actual TAK.gov artifacts credentials
# These are different from git.tak.gov credentials!
TAK_USER="${TAK_USER:-your_tak_username}"
TAK_PASS="${TAK_PASS:-your_tak_password}"

echo "Using TAK.gov repository for SDK dependencies..."
echo ""

# Clean build
./gradlew clean

# Build CIV Debug (for local testing)
echo "Building CIV Debug with TAK.gov SDK..."
./gradlew -Ptakrepo.force=true \
          -Ptakrepo.url=https://artifacts.tak.gov/artifactory/maven \
          -Ptakrepo.user="$TAK_USER" \
          -Ptakrepo.password="$TAK_PASS" \
          assembleCivDebug

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Debug build successful with TAK.gov SDK!"
    
    # Try release build (unsigned)
    echo ""
    echo "Building CIV Release (unsigned)..."
    ./gradlew -Ptakrepo.force=true \
              -Ptakrepo.url=https://artifacts.tak.gov/artifactory/maven \
              -Ptakrepo.user="$TAK_USER" \
              -Ptakrepo.password="$TAK_PASS" \
              assembleCivRelease
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "✅ Release build successful!"
        echo ""
        echo "Build outputs:"
        find app/build/outputs/apk -name "*.apk" -type f | while read apk; do
            echo "  📦 $apk"
            echo "     Size: $(ls -lh "$apk" | awk '{print $5}')"
        done
        
        echo ""
        echo "⚠️  Important Notes:"
        echo "  - Debug builds can be installed and tested locally"
        echo "  - Release builds are UNSIGNED and won't load in production ATAK"
        echo "  - Only TAK.gov can create officially signed releases"
        echo ""
        echo "To install debug build:"
        echo "  adb install -r app/build/outputs/apk/civ/debug/*.apk"
    else
        echo "❌ Release build failed"
    fi
else
    echo ""
    echo "❌ Debug build failed"
    echo ""
    echo "Troubleshooting:"
    echo "1. Check your TAK.gov credentials (artifacts.tak.gov, not git.tak.gov)"
    echo "2. Ensure you have proper access level"
    echo "3. Try with local SDK fallback: ./gradlew assembleCivDebug"
fi