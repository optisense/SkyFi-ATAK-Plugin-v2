#!/bin/bash

# TAK.gov Signing Package Verification Script
# Verifies the signing submission package is ready for upload

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}TAK.gov Signing Package Verification${NC}"
echo -e "${BLUE}========================================${NC}"

# Find the most recent signing request ZIP
SIGNING_ZIP=$(ls -t SkyFi-ATAK-Plugin-v2-SIGNING-REQUEST-*.zip 2>/dev/null | head -1)

if [ -z "$SIGNING_ZIP" ]; then
    echo -e "${RED}Error: No signing request ZIP found${NC}"
    exit 1
fi

echo -e "\n${YELLOW}Verifying package: $SIGNING_ZIP${NC}"

# Create temp directory for extraction
TEMP_DIR=$(mktemp -d)
trap "rm -rf $TEMP_DIR" EXIT

# Extract ZIP to temp directory
unzip -q "$SIGNING_ZIP" -d "$TEMP_DIR"

echo -e "\n${YELLOW}Package Contents:${NC}"
ls -la "$TEMP_DIR"

# Verify required files
echo -e "\n${YELLOW}File Verification:${NC}"

# Check unsigned APK
APK_FILE="$TEMP_DIR/ATAK-Plugin-SkyFi-ATAK-Plugin-v2-2.0-beta5--5.3.0-civ-release-unsigned.apk"
if [ -f "$APK_FILE" ]; then
    echo -e "  ${GREEN}✓${NC} Unsigned APK present"
    
    # Verify it's actually unsigned by checking for signature files
    if unzip -l "$APK_FILE" | grep -qE "META-INF/.*\.(RSA|DSA|EC|SF)"; then
        echo -e "  ${RED}✗${NC} WARNING: APK appears to be signed (should be unsigned)"
    else
        echo -e "  ${GREEN}✓${NC} APK is unsigned (as expected - no signature files found)"
    fi
    
    # Display APK info
    APK_SIZE=$(ls -lh "$APK_FILE" | awk '{print $5}')
    APK_HASH=$(shasum -a 256 "$APK_FILE" | cut -d' ' -f1)
    echo -e "    Size: $APK_SIZE"
    echo -e "    SHA256: $APK_HASH"
else
    echo -e "  ${RED}✗${NC} Unsigned APK missing"
fi

# Check other required files
if [ -f "$TEMP_DIR/README_SIGNING_REQUEST.md" ]; then
    echo -e "  ${GREEN}✓${NC} README present"
else
    echo -e "  ${RED}✗${NC} README missing"
fi

if [ -f "$TEMP_DIR/plugin.xml" ]; then
    echo -e "  ${GREEN}✓${NC} plugin.xml present"
else
    echo -e "  ${RED}✗${NC} plugin.xml missing"
fi

if [ -f "$TEMP_DIR/signing-metadata.json" ]; then
    echo -e "  ${GREEN}✓${NC} signing-metadata.json present"
    
    # Verify JSON is valid
    if command -v python3 &> /dev/null; then
        if python3 -m json.tool "$TEMP_DIR/signing-metadata.json" > /dev/null 2>&1; then
            echo -e "  ${GREEN}✓${NC} JSON metadata is valid"
        else
            echo -e "  ${RED}✗${NC} JSON metadata is invalid"
        fi
    fi
else
    echo -e "  ${RED}✗${NC} signing-metadata.json missing"
fi

if [ -f "$TEMP_DIR/SIGNING_CHECKLIST.txt" ]; then
    echo -e "  ${GREEN}✓${NC} Signing checklist present"
else
    echo -e "  ${RED}✗${NC} Signing checklist missing"
fi

# Check for source code (should NOT be present)
echo -e "\n${YELLOW}Source Code Check:${NC}"
if find "$TEMP_DIR" -name "*.java" -o -name "*.kt" -o -name "*.gradle" | grep -q .; then
    echo -e "  ${RED}✗${NC} WARNING: Source code files found (should be signing-only)"
else
    echo -e "  ${GREEN}✓${NC} No source code files (correct for signing-only)"
fi

# Display package info
echo -e "\n${YELLOW}Package Information:${NC}"
ZIP_SIZE=$(ls -lh "$SIGNING_ZIP" | awk '{print $5}')
FILE_COUNT=$(unzip -l "$SIGNING_ZIP" | tail -1 | awk '{print $2}')

echo "  Package: $SIGNING_ZIP"
echo "  Size: $ZIP_SIZE"
echo "  Files: $FILE_COUNT"
echo "  Type: SIGNING-ONLY REQUEST"
echo "  Build ID: j-skyfi-com-20250808-020619"

echo -e "\n${BLUE}========================================${NC}"
echo -e "${GREEN}VERIFICATION COMPLETE${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo "This package is ready for upload to TAK.gov for SIGNING ONLY."
echo "Remember to:"
echo "  1. Select 'Signing Only' as submission type"
echo "  2. Reference the original build ID"
echo "  3. Specify ATAK CIV distribution"