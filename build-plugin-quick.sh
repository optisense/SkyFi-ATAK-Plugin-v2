#!/bin/bash

# Quick build script that uses local SDK without fetching remote dependencies

echo "Building SkyFi Plugin with local SDK..."

# Set up environment
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# Create a temporary gradle.properties to disable remote fetching
cat > gradle.properties.tmp << EOF
org.gradle.daemon=true
org.gradle.configureondemand=true
org.gradle.parallel=true
android.useAndroidX=true
android.enableJetifier=false
# Disable remote dependency checking
systemProp.takrepo.force=false
EOF

# Back up existing gradle.properties if it exists
if [ -f gradle.properties ]; then
    mv gradle.properties gradle.properties.backup
fi
mv gradle.properties.tmp gradle.properties

# Build the plugin
echo "Running gradle build..."
./gradlew --no-daemon assembleCivDebug -Ptakrepo.force=false || {
    echo "Build failed. Restoring gradle.properties..."
    if [ -f gradle.properties.backup ]; then
        mv gradle.properties.backup gradle.properties
    fi
    exit 1
}

# Restore gradle.properties
if [ -f gradle.properties.backup ]; then
    mv gradle.properties.backup gradle.properties
fi

# Find the built APK
APK_PATH=$(find app/build/outputs/apk/civ/debug -name "*.apk" -type f | head -1)

if [ -f "$APK_PATH" ]; then
    echo ""
    echo "✓ Build successful!"
    echo "APK location: $APK_PATH"
    echo ""
    echo "Installing to device..."
    adb install -r "$APK_PATH" && echo "✓ Plugin installed successfully!"
else
    echo "✗ APK not found after build"
    exit 1
fi