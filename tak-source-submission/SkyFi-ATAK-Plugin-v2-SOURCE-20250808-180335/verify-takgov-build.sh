#\!/bin/bash

# TAK.gov Build Verification Script
echo "================================================"
echo "TAK.gov Build Verification Script"
echo "================================================"
echo ""
echo "This will verify your plugin builds with TAK.gov credentials."
echo ""

# Prompt for credentials
read -p "Enter your TAK.gov username: " TAK_USER
read -s -p "Enter your TAK.gov password: " TAK_PASS
echo ""

# Clean and test build
echo "Testing build with TAK.gov repository..."
./gradlew clean

./gradlew -Ptakrepo.force=true \
         -Ptakrepo.url=https://artifacts.tak.gov/artifactory/maven \
         -Ptakrepo.user="${TAK_USER}" \
         -Ptakrepo.password="${TAK_PASS}" \
         assembleCivRelease

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ SUCCESS\! Build works with TAK.gov repository"
    echo "Your plugin is ready for submission\!"
    
    APK_PATH=$(find app/build/outputs/apk -name "*.apk" -type f | head -1)
    if [ -n "$APK_PATH" ]; then
        echo "APK: $APK_PATH ($(du -h "$APK_PATH" | cut -f1))"
    fi
else
    echo ""
    echo "❌ BUILD FAILED - Check credentials and errors above"
fi
