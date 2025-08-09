#!/bin/bash

################################################################################
# Test script for Play Store ATAK Plugin
# Verifies the build doesn't contain SDK dependencies
################################################################################

set -e

echo "=================================================="
echo "Testing Play Store ATAK Plugin Build"
echo "=================================================="

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

APK_PATH="playstore-builds/app-debug.apk"

# Check if APK exists
if [ ! -f "$APK_PATH" ]; then
    echo -e "${RED}Error: APK not found at $APK_PATH${NC}"
    echo "Please run ./build-playstore-only.sh first"
    exit 1
fi

echo -e "${YELLOW}Extracting and analyzing APK...${NC}"

# Create temp directory
TEMP_DIR=$(mktemp -d)
trap "rm -rf $TEMP_DIR" EXIT

# Extract APK
unzip -q "$APK_PATH" -d "$TEMP_DIR"

# Check for SDK-specific classes
echo -e "${YELLOW}Checking for SDK dependencies...${NC}"

# List of forbidden classes/packages
FORBIDDEN=(
    "gov/tak/api"
    "IPlugin"
    "IServiceController"
    "PluginNativeLoader"
    "SkyFiPluginWrapper"
    "SkyFiPluginBridge"
)

FOUND_ISSUES=0

for pattern in "${FORBIDDEN[@]}"; do
    if find "$TEMP_DIR" -type f -name "*.dex" -exec sh -c "strings {} | grep -q '$pattern'" \; 2>/dev/null; then
        echo -e "${RED}✗ Found forbidden reference: $pattern${NC}"
        FOUND_ISSUES=$((FOUND_ISSUES + 1))
    else
        echo -e "${GREEN}✓ No reference to: $pattern${NC}"
    fi
done

# Check for required Play Store components
echo ""
echo -e "${YELLOW}Checking for required components...${NC}"

REQUIRED=(
    "SkyFiPlayStorePlugin"
    "com.optisense.skyfi.atak"
    "DropDownMapComponent"
)

for pattern in "${REQUIRED[@]}"; do
    if find "$TEMP_DIR" -type f \( -name "*.xml" -o -name "*.dex" \) -exec grep -l "$pattern" {} \; 2>/dev/null | head -1 > /dev/null; then
        echo -e "${GREEN}✓ Found required: $pattern${NC}"
    else
        echo -e "${RED}✗ Missing required: $pattern${NC}"
        FOUND_ISSUES=$((FOUND_ISSUES + 1))
    fi
done

# Check AndroidManifest
echo ""
echo -e "${YELLOW}Checking AndroidManifest.xml...${NC}"

if [ -f "$TEMP_DIR/AndroidManifest.xml" ]; then
    # Use aapt to dump manifest
    aapt dump badging "$APK_PATH" > "$TEMP_DIR/manifest_dump.txt" 2>/dev/null || true
    
    if grep -q "com.optisense.skyfi.atak" "$TEMP_DIR/manifest_dump.txt"; then
        echo -e "${GREEN}✓ Package name correct${NC}"
    else
        echo -e "${RED}✗ Package name incorrect${NC}"
        FOUND_ISSUES=$((FOUND_ISSUES + 1))
    fi
    
    if grep -q "com.atakmap.app.component" "$TEMP_DIR/manifest_dump.txt"; then
        echo -e "${GREEN}✓ ATAK component activity present${NC}"
    else
        echo -e "${RED}✗ ATAK component activity missing${NC}"
        FOUND_ISSUES=$((FOUND_ISSUES + 1))
    fi
fi

# Check plugin.xml
echo ""
echo -e "${YELLOW}Checking plugin.xml...${NC}"

if [ -f "$TEMP_DIR/assets/plugin.xml" ]; then
    if grep -q "IPlugin" "$TEMP_DIR/assets/plugin.xml"; then
        echo -e "${RED}✗ plugin.xml contains IPlugin reference${NC}"
        FOUND_ISSUES=$((FOUND_ISSUES + 1))
    else
        echo -e "${GREEN}✓ plugin.xml has no IPlugin reference${NC}"
    fi
    
    if grep -q "SkyFiPlayStorePlugin" "$TEMP_DIR/assets/plugin.xml"; then
        echo -e "${GREEN}✓ plugin.xml references Play Store component${NC}"
    else
        echo -e "${YELLOW}⚠ plugin.xml doesn't reference SkyFiPlayStorePlugin${NC}"
    fi
fi

# Summary
echo ""
echo "=================================================="
if [ $FOUND_ISSUES -eq 0 ]; then
    echo -e "${GREEN}✓ All checks passed!${NC}"
    echo "The APK appears to be correctly configured for Play Store ATAK"
else
    echo -e "${RED}✗ Found $FOUND_ISSUES issues${NC}"
    echo "Please review the issues above before deploying to Play Store"
fi
echo "=================================================="

# Optional: Test installation if device is connected
if command -v adb &> /dev/null; then
    echo ""
    echo -e "${YELLOW}ADB is available. Check for connected devices...${NC}"
    
    if adb devices | grep -q "device$"; then
        echo -e "${GREEN}Device connected${NC}"
        read -p "Would you like to install and test the APK? (y/n) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            echo "Installing APK..."
            adb install -r "$APK_PATH"
            
            echo ""
            echo "Installation complete!"
            echo "Next steps:"
            echo "1. Open ATAK on your device"
            echo "2. Look for 'SkyFi' in the toolbar"
            echo "3. Tap to open the plugin"
        fi
    else
        echo "No device connected. Connect a device to test installation."
    fi
fi

exit $FOUND_ISSUES