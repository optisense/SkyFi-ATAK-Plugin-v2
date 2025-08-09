#!/bin/bash

# TAK.gov Signing Submission Packaging Script
# Purpose: Package unsigned APK for TAK.gov signing (NOT building)

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}TAK.gov Signing Submission Packager${NC}"
echo -e "${GREEN}========================================${NC}"

# Find the most recent submission directory
SUBMISSION_DIR=$(ls -dt tak-signing-submission-* 2>/dev/null | head -1)

if [ -z "$SUBMISSION_DIR" ]; then
    echo -e "${RED}Error: No signing submission directory found${NC}"
    echo "Please create a submission directory first using the preparation steps"
    exit 1
fi

echo -e "${YELLOW}Found submission directory: $SUBMISSION_DIR${NC}"

# Verify required files
echo -e "\n${YELLOW}Verifying required files...${NC}"

REQUIRED_FILES=(
    "ATAK-Plugin-SkyFi-ATAK-Plugin-v2-2.0-beta5--5.3.0-civ-release-unsigned.apk"
    "README_SIGNING_REQUEST.md"
    "plugin.xml"
    "signing-metadata.json"
    "SIGNING_CHECKLIST.txt"
)

MISSING_FILES=()
for file in "${REQUIRED_FILES[@]}"; do
    if [ -f "$SUBMISSION_DIR/$file" ]; then
        echo -e "  ${GREEN}✓${NC} $file"
    else
        echo -e "  ${RED}✗${NC} $file"
        MISSING_FILES+=("$file")
    fi
done

if [ ${#MISSING_FILES[@]} -gt 0 ]; then
    echo -e "\n${RED}Error: Missing required files:${NC}"
    for file in "${MISSING_FILES[@]}"; do
        echo "  - $file"
    done
    exit 1
fi

# Display APK information
echo -e "\n${YELLOW}APK Information:${NC}"
APK_FILE="$SUBMISSION_DIR/ATAK-Plugin-SkyFi-ATAK-Plugin-v2-2.0-beta5--5.3.0-civ-release-unsigned.apk"
APK_SIZE=$(ls -lh "$APK_FILE" | awk '{print $5}')
APK_HASH=$(shasum -a 256 "$APK_FILE" | cut -d' ' -f1)

echo "  File: $(basename "$APK_FILE")"
echo "  Size: $APK_SIZE"
echo "  SHA256: $APK_HASH"

# Create the ZIP package
OUTPUT_ZIP="SkyFi-ATAK-Plugin-v2-SIGNING-REQUEST-$(date +%Y%m%d-%H%M%S).zip"

echo -e "\n${YELLOW}Creating ZIP package: $OUTPUT_ZIP${NC}"

# Change to submission directory to create clean paths in ZIP
cd "$SUBMISSION_DIR"

# Create ZIP with all files
zip -r "../$OUTPUT_ZIP" . -x "*.DS_Store" "*/.*"

cd ..

# Verify ZIP creation
if [ -f "$OUTPUT_ZIP" ]; then
    ZIP_SIZE=$(ls -lh "$OUTPUT_ZIP" | awk '{print $5}')
    echo -e "\n${GREEN}✓ Package created successfully!${NC}"
    echo -e "  File: $OUTPUT_ZIP"
    echo -e "  Size: $ZIP_SIZE"
    
    # Show ZIP contents
    echo -e "\n${YELLOW}Package contents:${NC}"
    unzip -l "$OUTPUT_ZIP" | grep -v "Archive:" | grep -v "Length" | grep -v "^---" | tail -n +2 | head -n -2
    
    echo -e "\n${GREEN}========================================${NC}"
    echo -e "${GREEN}SIGNING SUBMISSION PACKAGE READY${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Upload $OUTPUT_ZIP to TAK.gov portal"
    echo "2. Select 'Signing Only' as submission type"
    echo "3. Reference build ID: j-skyfi-com-20250808-020619"
    echo "4. Wait for TAK.gov to sign and return the signed APK"
    echo ""
    echo -e "${YELLOW}Important: This is a SIGNING-ONLY submission.${NC}"
    echo -e "${YELLOW}The APK has already been built by TAK.gov.${NC}"
else
    echo -e "${RED}Error: Failed to create ZIP package${NC}"
    exit 1
fi