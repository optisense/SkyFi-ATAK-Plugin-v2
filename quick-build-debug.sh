#!/bin/bash

echo "🚀 Quick Debug Build for SkyFi ATAK Plugin"
echo "=========================================="

# Use the existing unsigned APK as base and update it
cd /Users/jfuginay/Documents/dev/optisense-projects/SkyFi-ATAK-Plugin-v2

# Check if we have the compatibility wrapper compiled
if [ ! -f "app/build/intermediates/javac/civDebug/classes/com/skyfi/atak/plugin/SkyFiPluginCompatWrapper.class" ]; then
    echo "⚠️  Compatibility wrapper not compiled yet"
    echo "📦 Compiling compatibility wrapper..."
    
    # Compile just the compatibility wrapper
    javac -cp "app/libs/main.jar" \
          -d "app/build/intermediates/javac/civDebug/classes" \
          "app/src/main/java/com/skyfi/atak/plugin/SkyFiPluginCompatWrapper.java"
fi

# Create a working directory
rm -rf build/quick-debug
mkdir -p build/quick-debug

# Extract the existing unsigned APK
echo "📂 Extracting base APK..."
cd build/quick-debug
cp ../../unsigned-from-takgov.apk base.apk
unzip -q base.apk
rm base.apk

# Update plugin.xml to use compatibility wrapper
echo "✏️  Updating plugin.xml..."
cat > assets/plugin.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<plugin>
    <extension
        type="gov.tak.api.plugin.IPlugin"
        impl="com.skyfi.atak.plugin.SkyFiPluginCompatWrapper"
        singleton="true" />
</plugin>
EOF

# Repackage the APK
echo "📦 Repackaging APK..."
zip -qr ../skyfi-plugin-compat-debug-unsigned.apk .
cd ..

# Sign with debug key
echo "🔏 Signing APK with debug key..."
if [ -f "../android_keystore" ]; then
    jarsigner -verbose \
              -keystore "../android_keystore" \
              -storepass "tnttnt" \
              -keypass "tnttnt" \
              skyfi-plugin-compat-debug-unsigned.apk \
              wintec_mapping
    
    # Align the APK
    echo "📐 Aligning APK..."
    if [ -f "$ANDROID_HOME/build-tools/34.0.0/zipalign" ]; then
        $ANDROID_HOME/build-tools/34.0.0/zipalign -f -v 4 \
            skyfi-plugin-compat-debug-unsigned.apk \
            skyfi-plugin-compat-debug.apk
    else
        echo "⚠️  zipalign not found, using unaligned APK"
        mv skyfi-plugin-compat-debug-unsigned.apk skyfi-plugin-compat-debug.apk
    fi
    
    echo ""
    echo "✅ Build complete!"
    echo "📦 Output: build/skyfi-plugin-compat-debug.apk"
    echo ""
    echo "📱 To install:"
    echo "   adb install -r build/skyfi-plugin-compat-debug.apk"
else
    echo "❌ Keystore not found!"
fi