#\!/bin/bash

echo "📦 Preparing TAK.gov Submission with Fixed Configuration"
echo "========================================================"
echo ""
echo "Requirements: Gradle 6.9.1, JDK 17"
echo ""

TIMESTAMP=$(date +%Y%m%d-%H%M%S)
SUBMISSION_NAME="SkyFi-ATAK-Plugin-v2-FIXED-$TIMESTAMP"

# Clean previous submission
rm -rf tak-submission-fixed
mkdir -p tak-submission-fixed/$SUBMISSION_NAME

echo "📁 Copying source files..."
rsync -av --exclude='.git*' \
          --exclude='build/' \
          --exclude='app/build/' \
          --exclude='*.apk' \
          --exclude='*.aab' \
          --exclude='*.jar' \
          --exclude='*.zip' \
          --exclude='releases/' \
          --exclude='tak-*' \
          --exclude='.gradle/' \
          --exclude='.idea/' \
          ./ "tak-submission-fixed/$SUBMISSION_NAME/"

echo "✏️ Fixing Gradle wrapper version..."
cat > "tak-submission-fixed/$SUBMISSION_NAME/gradle/wrapper/gradle-wrapper.properties" << 'EOF'
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-6.9.1-all.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
EOF

echo "✏️ Updating plugin.xml to use compatibility wrapper..."
cat > "tak-submission-fixed/$SUBMISSION_NAME/app/src/main/assets/plugin.xml" << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<plugin>
    <extension
        type="gov.tak.api.plugin.IPlugin"
        impl="com.skyfi.atak.plugin.SkyFiPluginCompatWrapper"
        singleton="true" />
</plugin>
EOF

echo "📝 Creating build instructions..."
cat > "tak-submission-fixed/$SUBMISSION_NAME/TAKGOV_BUILD.md" << 'EOF'
# TAK.gov Build Instructions

## Requirements
- Gradle 6.9.1 
- JDK 17

## Important
- Use SkyFiPluginCompatWrapper (NO IServiceController)
- Target: ATAK 5.4.0.16
EOF

echo ""
echo "🗜️ Creating submission ZIP..."
cd tak-submission-fixed
zip -r "../$SUBMISSION_NAME.zip" "$SUBMISSION_NAME/"
cd ..

echo "✅ Package: $SUBMISSION_NAME.zip"
ls -lh "$SUBMISSION_NAME.zip"
