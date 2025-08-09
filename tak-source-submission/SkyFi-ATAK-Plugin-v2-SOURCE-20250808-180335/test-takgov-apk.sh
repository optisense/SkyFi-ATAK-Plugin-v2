#!/bin/bash

###############################################################################
# TAK.gov APK Testing Script
# Tests the TAK.gov-built APK on connected ATAK device
###############################################################################

set -e

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

APK_PATH="./releases/takgov-built-20250807-153747.apk"
PACKAGE_NAME="com.skyfi.atak.plugin"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}TAK.gov APK Testing Script${NC}"
echo -e "${BLUE}========================================${NC}"

# Check if APK exists
if [ ! -f "$APK_PATH" ]; then
    echo -e "${RED}✗ APK not found at: $APK_PATH${NC}"
    exit 1
fi

echo -e "${GREEN}✓ APK found: $APK_PATH${NC}"
echo -e "  Size: $(ls -lh $APK_PATH | awk '{print $5}')"

# Check for connected device
echo -e "\n${YELLOW}Checking for connected devices...${NC}"
if ! adb devices | grep -q "device$"; then
    echo -e "${RED}✗ No Android device connected${NC}"
    echo -e "${YELLOW}Please connect your device and enable USB debugging${NC}"
    exit 1
fi

DEVICE=$(adb devices | grep "device$" | head -1 | awk '{print $1}')
echo -e "${GREEN}✓ Device connected: $DEVICE${NC}"

# Check if ATAK is installed
echo -e "\n${YELLOW}Checking ATAK installation...${NC}"
ATAK_PACKAGES=$(adb shell pm list packages | grep -E "com.atakmap.app" || true)
if [ -z "$ATAK_PACKAGES" ]; then
    echo -e "${RED}✗ ATAK not installed on device${NC}"
    echo -e "${YELLOW}Please install ATAK first${NC}"
    exit 1
fi
echo -e "${GREEN}✓ ATAK found:${NC}"
echo "$ATAK_PACKAGES" | sed 's/^/  /'

# Uninstall previous version if exists
echo -e "\n${YELLOW}Checking for existing plugin installation...${NC}"
if adb shell pm list packages | grep -q "$PACKAGE_NAME"; then
    echo -e "${YELLOW}Found existing plugin, uninstalling...${NC}"
    adb uninstall "$PACKAGE_NAME"
    echo -e "${GREEN}✓ Previous version uninstalled${NC}"
else
    echo -e "${GREEN}✓ No previous installation found${NC}"
fi

# Install the new APK
echo -e "\n${YELLOW}Installing TAK.gov-built APK...${NC}"
if adb install "$APK_PATH"; then
    echo -e "${GREEN}✓ APK installed successfully${NC}"
else
    echo -e "${RED}✗ Failed to install APK${NC}"
    echo -e "${YELLOW}This might be due to signature mismatch with ATAK${NC}"
    exit 1
fi

# Start monitoring logcat
echo -e "\n${YELLOW}Starting logcat monitoring...${NC}"
echo -e "${BLUE}Watch for these key messages:${NC}"
echo -e "  - 'SkyFiPlugin: Plugin onStart() called'"
echo -e "  - 'SkyFiPlugin: Added toolbar item'"
echo -e "  - Any error messages containing 'SkyFi' or 'skyfi'"
echo -e "\n${YELLOW}Press Ctrl+C to stop monitoring${NC}\n"

# Clear logcat and start monitoring
adb logcat -c
echo -e "${GREEN}========== LOGCAT OUTPUT ==========${NC}"

# Start ATAK if not running
echo -e "${YELLOW}Starting ATAK...${NC}"
adb shell am start -n com.atakmap.app.civ/com.atakmap.app.ATAKActivity 2>/dev/null || \
    adb shell am start -n com.atakmap.app/com.atakmap.app.ATAKActivity 2>/dev/null || \
    echo -e "${YELLOW}Could not auto-start ATAK, please start it manually${NC}"

# Monitor logcat for plugin loading
adb logcat -v time | grep -E --line-buffered "(SkyFi|skyfi|PluginLoader|plugin\.load|Exception.*skyfi)" | while IFS= read -r line; do
    if echo "$line" | grep -q "Exception\|Error\|FATAL\|failed"; then
        echo -e "${RED}$line${NC}"
    elif echo "$line" | grep -q "onStart\|Added toolbar\|SUCCESS\|loaded"; then
        echo -e "${GREEN}$line${NC}"
    else
        echo -e "${YELLOW}$line${NC}"
    fi
done