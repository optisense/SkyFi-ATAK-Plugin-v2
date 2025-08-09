#\!/bin/bash

echo "📦 Creating Clean TAK.gov Submission (No SDK)"
echo "=============================================="

TIMESTAMP=$(date +%Y%m%d-%H%M%S)
SUBMISSION_NAME="SkyFi-ATAK-Plugin-v2-CLEAN-$TIMESTAMP"

# Clean previous submission
rm -rf tak-submission-clean
mkdir -p tak-submission-clean/$SUBMISSION_NAME

echo "📁 Copying source files (excluding SDK)..."
rsync -av --exclude='.git*' \
          --exclude='build/' \
          --exclude='app/build/' \
          --exclude='*.apk' \
          --exclude='*.aab' \
          --exclude='*.jar' \
          --exclude='*.zip' \
          --exclude='releases/' \
          --exclude='tak-*' \
          --exclude='ATAK-CIV-*' \
          --exclude='atak-*' \
          --exclude='.gradle/' \
          --exclude='.idea/' \
          --exclude='sdk/' \
          --exclude='SDK/' \
          ./ "tak-submission-clean/$SUBMISSION_NAME/"

echo "✏️ Ensuring correct Gradle version..."
cat > "tak-submission-clean/$SUBMISSION_NAME/gradle/wrapper/gradle-wrapper.properties" << 'EOF'
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-6.9.1-all.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
EOF

echo "✏️ Ensuring plugin.xml uses compatibility wrapper..."
cat > "tak-submission-clean/$SUBMISSION_NAME/app/src/main/assets/plugin.xml" << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<plugin>
    <extension
        type="gov.tak.api.plugin.IPlugin"
        impl="com.skyfi.atak.plugin.SkyFiPluginCompatWrapper"
        singleton="true" />
</plugin>
EOF

echo "📝 Creating build instructions..."
cat > "tak-submission-clean/$SUBMISSION_NAME/BUILD_INSTRUCTIONS.txt" << 'EOF'
TAK.gov Build Instructions
==========================

Requirements:
- Gradle 6.9.1
- JDK 17
- ATAK SDK 5.4.0.16

Critical: Use SkyFiPluginCompatWrapper (NOT SkyFiPluginWrapper)
This avoids IServiceController dependency issue.

Build command:
./gradlew clean assembleCivRelease
EOF

echo "🗜️ Creating ZIP..."
cd tak-submission-clean
zip -r "../$SUBMISSION_NAME.zip" "$SUBMISSION_NAME/"
cd ..

echo "✅ Clean package created: $SUBMISSION_NAME.zip"
ls -lh "$SUBMISSION_NAME.zip"
