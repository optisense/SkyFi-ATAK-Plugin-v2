#!/bin/bash

# Script to create a development bundle with ATAK 5.4 and SkyFi Plugin v2
# This bundle includes matching debug-signed APKs for development/testing

set -e

echo "============================================="
echo "Creating ATAK + SkyFi Plugin Development Bundle"
echo "============================================="

# Define paths and names
BUNDLE_NAME="SkyFi-ATAK-Dev-Bundle-$(date +%Y%m%d)"
BUNDLE_DIR="dev-bundle"
SDK_PATH="sdk/ATAK-CIV-5.4.0.18-SDK"

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# Create bundle directory
echo -e "${YELLOW}Creating bundle directory...${NC}"
rm -rf "$BUNDLE_DIR"
mkdir -p "$BUNDLE_DIR"

# Check if ATAK APK exists
if [ ! -f "$SDK_PATH/atak.apk" ]; then
    echo -e "${RED}ERROR: ATAK APK not found at $SDK_PATH/atak.apk${NC}"
    exit 1
fi

# Copy ATAK APK
echo -e "${GREEN}✓ Copying ATAK 5.4 CIV APK...${NC}"
cp "$SDK_PATH/atak.apk" "$BUNDLE_DIR/ATAK-5.4.0-CIV-debug.apk"

# Copy the debug keystore for reference
echo -e "${GREEN}✓ Copying debug keystore...${NC}"
cp "$SDK_PATH/android_keystore" "$BUNDLE_DIR/"

# Build the plugin
echo -e "${YELLOW}Building SkyFi Plugin (debug)...${NC}"
./gradlew clean assembleCivDebug

# Find and copy the plugin APK
PLUGIN_APK=$(find app/build/outputs/apk/civ/debug -name "*.apk" -type f | head -1)
if [ -f "$PLUGIN_APK" ]; then
    echo -e "${GREEN}✓ Plugin built successfully${NC}"
    cp "$PLUGIN_APK" "$BUNDLE_DIR/SkyFi-ATAK-Plugin-v2-debug.apk"
else
    echo -e "${RED}ERROR: Plugin APK not found${NC}"
    exit 1
fi

# Create installation script
echo -e "${YELLOW}Creating installation script...${NC}"
cat > "$BUNDLE_DIR/install.sh" << 'EOF'
#!/bin/bash

# Installation script for ATAK + SkyFi Plugin Development Bundle

echo "========================================"
echo "ATAK + SkyFi Plugin Installation Script"
echo "========================================"

# Check if adb is available
if ! command -v adb &> /dev/null; then
    echo "ERROR: adb not found. Please install Android SDK Platform Tools"
    echo "Download from: https://developer.android.com/studio/releases/platform-tools"
    exit 1
fi

# Check if device is connected
echo "Checking for connected devices..."
adb devices | grep -q device$ || {
    echo "ERROR: No Android device found"
    echo "Please connect your device and enable USB debugging"
    exit 1
}

# Uninstall existing versions (if any)
echo "Removing existing installations (if any)..."
adb uninstall com.atakmap.app.civ 2>/dev/null || true
adb uninstall com.skyfi.atak.plugin 2>/dev/null || true

# Install ATAK
echo "Installing ATAK 5.4.0 CIV (debug signed)..."
adb install -r ATAK-5.4.0-CIV-debug.apk || {
    echo "ERROR: Failed to install ATAK"
    exit 1
}

# Install Plugin
echo "Installing SkyFi Plugin v2..."
adb install -r SkyFi-ATAK-Plugin-v2-debug.apk || {
    echo "ERROR: Failed to install plugin"
    exit 1
}

echo ""
echo "✓ Installation complete!"
echo ""
echo "Next steps:"
echo "1. Launch ATAK on your device"
echo "2. Go to Settings → Tool Preferences → Plugin Management"
echo "3. You should see 'SkyFi ATAK Plugin' loaded"
echo "4. Access the plugin from the tools menu"
echo ""
echo "NOTE: This is a DEBUG build for development only"
echo "Both APKs are signed with the same debug certificate"
EOF

chmod +x "$BUNDLE_DIR/install.sh"

# Create installation batch file for Windows
cat > "$BUNDLE_DIR/install.bat" << 'EOF'
@echo off
echo ========================================
echo ATAK + SkyFi Plugin Installation Script
echo ========================================

REM Check if adb is available
where adb >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: adb not found. Please install Android SDK Platform Tools
    echo Download from: https://developer.android.com/studio/releases/platform-tools
    pause
    exit /b 1
)

REM Check if device is connected
echo Checking for connected devices...
adb devices | findstr /r "device$" >nul
if %errorlevel% neq 0 (
    echo ERROR: No Android device found
    echo Please connect your device and enable USB debugging
    pause
    exit /b 1
)

REM Uninstall existing versions
echo Removing existing installations if any...
adb uninstall com.atakmap.app.civ >nul 2>&1
adb uninstall com.skyfi.atak.plugin >nul 2>&1

REM Install ATAK
echo Installing ATAK 5.4.0 CIV debug signed...
adb install -r ATAK-5.4.0-CIV-debug.apk
if %errorlevel% neq 0 (
    echo ERROR: Failed to install ATAK
    pause
    exit /b 1
)

REM Install Plugin
echo Installing SkyFi Plugin v2...
adb install -r SkyFi-ATAK-Plugin-v2-debug.apk
if %errorlevel% neq 0 (
    echo ERROR: Failed to install plugin
    pause
    exit /b 1
)

echo.
echo Installation complete!
echo.
echo Next steps:
echo 1. Launch ATAK on your device
echo 2. Go to Settings - Tool Preferences - Plugin Management
echo 3. You should see SkyFi ATAK Plugin loaded
echo 4. Access the plugin from the tools menu
echo.
echo NOTE: This is a DEBUG build for development only
pause
EOF

# Create README
cat > "$BUNDLE_DIR/README.md" << EOF
# ATAK + SkyFi Plugin Development Bundle

This bundle contains debug-signed versions of ATAK 5.4.0 CIV and the SkyFi Plugin v2 for development and testing purposes.

## Contents

- **ATAK-5.4.0-CIV-debug.apk** - ATAK 5.4.0 CIV signed with debug certificate
- **SkyFi-ATAK-Plugin-v2-debug.apk** - SkyFi Plugin signed with matching debug certificate
- **android_keystore** - The debug keystore used to sign both APKs
- **install.sh** - Installation script for macOS/Linux
- **install.bat** - Installation script for Windows

## Requirements

- Android device with Android 5.0 (API 21) or higher
- USB debugging enabled on device
- ADB (Android Debug Bridge) installed on computer
- Device connected via USB

## Installation

### macOS/Linux:
\`\`\`bash
chmod +x install.sh
./install.sh
\`\`\`

### Windows:
\`\`\`cmd
install.bat
\`\`\`

### Manual Installation:
\`\`\`bash
adb install -r ATAK-5.4.0-CIV-debug.apk
adb install -r SkyFi-ATAK-Plugin-v2-debug.apk
\`\`\`

## Important Notes

⚠️ **Development Use Only** - These are debug builds not suitable for operational use
⚠️ **Matching Signatures** - Both APKs are signed with the same debug certificate
⚠️ **Data Loss** - Installing these will remove any existing ATAK installation

## Plugin Features

- High-resolution satellite imagery integration
- Area of Responsibility (AOR) filtering
- AOI management and drawing tools
- Multiple tasking methods (pindrop, lat/lon, MGRS)
- Image archiving and favorites
- Metadata sorting and filtering

## Troubleshooting

1. **"App not installed" error**: Uninstall existing ATAK/plugin first
2. **Plugin not showing**: Restart ATAK after installation
3. **Certificate errors**: Both APKs must be from this bundle

## Build Information

- Build Date: $(date +"%Y-%m-%d %H:%M:%S")
- ATAK Version: 5.4.0.18 CIV
- Plugin Version: 2.0
- Target SDK: 34
- Min SDK: 26
EOF

# Create the zip bundle
echo -e "${YELLOW}Creating zip bundle...${NC}"
cd "$BUNDLE_DIR"
zip -r "../${BUNDLE_NAME}.zip" .
cd ..

# Get file sizes
BUNDLE_SIZE=$(du -h "${BUNDLE_NAME}.zip" | cut -f1)

echo ""
echo -e "${GREEN}✓ Development bundle created successfully!${NC}"
echo ""
echo "Bundle: ${BUNDLE_NAME}.zip (${BUNDLE_SIZE})"
echo "Contents:"
echo "  - ATAK 5.4.0 CIV (debug signed)"
echo "  - SkyFi Plugin v2 (debug signed)"
echo "  - Installation scripts"
echo "  - Debug keystore"
echo ""
echo "This bundle can be uploaded to GitHub releases for easy distribution"