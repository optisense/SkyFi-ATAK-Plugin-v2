#!/bin/bash

echo "==================================="
echo "Quick ATAK + SkyFi Install Helper"
echo "==================================="

# Check if Homebrew is installed
if ! command -v brew &> /dev/null; then
    echo "Homebrew not found. Installing Homebrew first..."
    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
fi

# Check if ADB is installed
if ! command -v adb &> /dev/null; then
    echo "ADB not found. Installing Android Platform Tools..."
    brew install --cask android-platform-tools
    
    # Add to PATH if needed
    export PATH="$PATH:/opt/homebrew/bin"
fi

# Verify ADB is now available
if ! command -v adb &> /dev/null; then
    echo "ERROR: ADB installation failed"
    echo "Please install manually: brew install --cask android-platform-tools"
    exit 1
fi

echo "✓ ADB is installed"

# Check for connected devices
echo ""
echo "Checking for connected Android devices..."
adb devices -l

# Wait for device if not connected
if ! adb devices | grep -q "device$"; then
    echo ""
    echo "No device found. Please:"
    echo "1. Connect your Android device via USB"
    echo "2. Enable Developer Options (tap Build Number 7 times in Settings > About)"
    echo "3. Enable USB Debugging in Developer Options"
    echo "4. Accept the authorization prompt on your device"
    echo ""
    read -p "Press Enter when ready..."
    
    # Check again
    if ! adb devices | grep -q "device$"; then
        echo "ERROR: Still no device found"
        exit 1
    fi
fi

echo ""
echo "✓ Device connected!"
echo ""

# Uninstall existing versions
echo "Removing any existing ATAK/Plugin installations..."
adb uninstall com.atakmap.app.civ 2>/dev/null || true
adb uninstall com.skyfi.atak.plugin 2>/dev/null || true

# Install ATAK
echo "Installing ATAK 5.4.0 CIV (debug signed)..."
ATAK_APK="sdk/ATAK-CIV-5.4.0.18-SDK/atak.apk"
if [ -f "$ATAK_APK" ]; then
    adb install -r "$ATAK_APK"
    echo "✓ ATAK installed successfully"
else
    echo "ERROR: ATAK APK not found at $ATAK_APK"
    exit 1
fi

# Build and install plugin
echo ""
echo "Building SkyFi Plugin..."
if [ -f "./gradlew" ]; then
    ./gradlew assembleCivDebug
    
    # Find and install the plugin APK
    PLUGIN_APK=$(find app/build/outputs/apk/civ/debug -name "*.apk" -type f | head -1)
    if [ -f "$PLUGIN_APK" ]; then
        echo "Installing SkyFi Plugin..."
        adb install -r "$PLUGIN_APK"
        echo "✓ Plugin installed successfully"
    else
        echo "ERROR: Plugin APK not found after build"
        exit 1
    fi
else
    echo "ERROR: gradlew not found"
    exit 1
fi

echo ""
echo "======================================="
echo "✓ Installation Complete!"
echo "======================================="
echo ""
echo "Next steps:"
echo "1. Launch ATAK on your device"
echo "2. Go to Settings → Tool Preferences → Plugin Management"
echo "3. Verify 'SkyFi ATAK Plugin' is loaded"
echo "4. Access the plugin from the tools menu"
echo ""
echo "Both APKs are debug-signed and compatible with each other."