#!/bin/bash

# SkyFi ATAK Plugin - Play Store Upload Preparation Script
# This script prepares the unsigned APK from TAK.gov for Google Play Store upload

set -e

echo "========================================="
echo "SkyFi ATAK Plugin Play Store Preparation"
echo "========================================="

# Configuration
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APK_NAME="SkyFi-ATAK-Plugin-v2.0-beta5-PlayStore-Unsigned.apk"
AAB_NAME="SkyFi-ATAK-Plugin-v2.0-beta5-PlayStore.aab"
UPLOAD_DIR="${PROJECT_ROOT}/playstore-upload"
TIMESTAMP=$(date +"%Y%m%d-%H%M%S")

echo "Project root: ${PROJECT_ROOT}"
echo "Timestamp: ${TIMESTAMP}"

# Create upload directory
echo ""
echo "Creating upload directory..."
mkdir -p "${UPLOAD_DIR}"
mkdir -p "${UPLOAD_DIR}/metadata"
mkdir -p "${UPLOAD_DIR}/assets"

# Check if unsigned APK exists
if [ ! -f "${PROJECT_ROOT}/${APK_NAME}" ]; then
    echo "ERROR: Unsigned APK not found: ${APK_NAME}"
    echo "Please ensure the APK has been copied from TAK.gov download"
    exit 1
fi

echo "✓ Found unsigned APK: ${APK_NAME}"

# Copy APK to upload directory
echo ""
echo "Preparing APK for upload..."
cp "${PROJECT_ROOT}/${APK_NAME}" "${UPLOAD_DIR}/SkyFi-ATAK-Plugin-v2.0-beta5-${TIMESTAMP}.apk"

# Copy AAB if available (preferred for Play Store)
if [ -f "${PROJECT_ROOT}/${AAB_NAME}" ]; then
    echo "✓ Found AAB file, copying for upload..."
    cp "${PROJECT_ROOT}/${AAB_NAME}" "${UPLOAD_DIR}/SkyFi-ATAK-Plugin-v2.0-beta5-${TIMESTAMP}.aab"
fi

# Extract version information
echo ""
echo "Extracting version information..."
if [ -f "${PROJECT_ROOT}/VERSION.txt" ]; then
    VERSION_NUMBER=$(cat "${PROJECT_ROOT}/VERSION.txt" | head -1 | tr -d '\n\r')
    echo "Version: ${VERSION_NUMBER}"
else
    VERSION_NUMBER="2.0.0"
    echo "Using default version: ${VERSION_NUMBER}"
fi

# Copy app icon and screenshots if they exist
echo ""
echo "Preparing store assets..."
if [ -f "${PROJECT_ROOT}/app/src/main/res/drawable/ic_launcher.png" ]; then
    cp "${PROJECT_ROOT}/app/src/main/res/drawable/ic_launcher.png" "${UPLOAD_DIR}/assets/app-icon.png"
    echo "✓ Copied app icon"
fi

if [ -f "${PROJECT_ROOT}/app/src/main/assets/skyfi_logo.png" ]; then
    cp "${PROJECT_ROOT}/app/src/main/assets/skyfi_logo.png" "${UPLOAD_DIR}/assets/feature-graphic.png"
    echo "✓ Copied feature graphic"
fi

# Create release notes
echo ""
echo "Generating release notes..."
cat > "${UPLOAD_DIR}/metadata/release-notes.txt" << EOF
SkyFi ATAK Plugin v${VERSION_NUMBER} - Beta 5

NEW IN THIS VERSION:
• Enhanced UI with modern dark theme design
• Improved satellite imagery ordering workflow
• Better integration with ATAK map interface
• Optimized performance and stability
• Advanced area of interest (AOI) selection tools
• Real-time satellite pass predictions
• Enhanced order tracking and management

FEATURES:
• Order high-resolution satellite imagery directly from ATAK
• Draw custom areas of interest on the map
• Browse and preview available satellite data
• Track order status and delivery
• Seamless integration with ATAK's tactical interface
• Support for multiple satellite providers

COMPATIBILITY:
• Requires ATAK-CIV version 5.3.0 or higher
• Compatible with Android API 21+ devices
• Optimized for tablets and smartphones

IMPORTANT NOTES:
• This app requires ATAK (Android Team Awareness Kit) to be installed
• Plugin integrates with ATAK as an add-on component
• Internet connection required for satellite data ordering
• Account registration with SkyFi required for ordering

For support and documentation, visit: https://docs.skyfi.com/atak
EOF

# Create metadata file with app details
cat > "${UPLOAD_DIR}/metadata/app-metadata.json" << EOF
{
  "packageName": "com.skyfi.atak.plugin",
  "title": "SkyFi ATAK Plugin",
  "shortDescription": "Satellite imagery ordering plugin for ATAK",
  "fullDescription": "The SkyFi ATAK Plugin enables users to order high-resolution satellite imagery directly from within the Android Team Awareness Kit (ATAK). This powerful plugin seamlessly integrates with ATAK's interface, allowing tactical users to draw areas of interest on the map and request satellite captures from multiple providers. Features include real-time satellite pass predictions, order tracking, and direct delivery of imagery to ATAK for immediate tactical use.",
  "version": "${VERSION_NUMBER}",
  "versionCode": "$(date +%s)",
  "category": "TOOLS",
  "contentRating": "Everyone",
  "website": "https://skyfi.com",
  "email": "support@skyfi.com",
  "privacyPolicy": "https://skyfi.com/privacy",
  "screenshots": [],
  "features": [
    "Order satellite imagery from within ATAK",
    "Draw custom areas of interest",
    "Real-time satellite predictions",
    "Multi-provider support",
    "Order tracking and management",
    "Seamless ATAK integration"
  ],
  "requirements": [
    "ATAK-CIV 5.3.0 or higher",
    "Android 5.0 (API 21) or higher",
    "Internet connection",
    "SkyFi account for ordering"
  ]
}
EOF

# Create Play Console preparation checklist
cat > "${UPLOAD_DIR}/PLAY_CONSOLE_CHECKLIST.md" << EOF
# Google Play Console Upload Checklist

## Pre-Upload Verification
- [ ] APK/AAB file prepared and tested
- [ ] Version code is higher than previous release
- [ ] App signing key uploaded to Play Console
- [ ] All required permissions documented
- [ ] Privacy Policy updated and accessible

## Required Store Listing Assets
- [ ] App icon (512x512 PNG)
- [ ] Feature graphic (1024x500 PNG)
- [ ] Screenshots (minimum 2, recommended 8)
  - [ ] Phone screenshots (at least 2)
  - [ ] Tablet screenshots (recommended)
- [ ] Video trailer (optional but recommended)

## App Information
- [ ] App title: "SkyFi ATAK Plugin"
- [ ] Short description (80 characters max)
- [ ] Full description (4000 characters max)
- [ ] Category: Tools
- [ ] Content rating: Everyone
- [ ] Contact email: support@skyfi.com
- [ ] Privacy Policy URL: https://skyfi.com/privacy
- [ ] Website URL: https://skyfi.com

## Technical Requirements
- [ ] Target SDK version 33 (Android 13)
- [ ] Minimum SDK version 21 (Android 5.0)
- [ ] 64-bit compliance (arm64-v8a support)
- [ ] App Bundle uploaded (preferred) or APK
- [ ] ProGuard/R8 code obfuscation enabled for release

## Special Considerations for ATAK Plugin
- [ ] Note in description that ATAK is required
- [ ] Explain plugin functionality clearly
- [ ] Include screenshots showing ATAK integration
- [ ] Mention tactical/professional use case
- [ ] Link to ATAK on Play Store in description

## Testing Requirements
- [ ] Internal testing with team members
- [ ] Closed testing with limited users
- [ ] All core features tested
- [ ] Installation and uninstallation tested
- [ ] Permissions and privacy features verified

## Compliance & Policies
- [ ] Data Safety form completed in Play Console
- [ ] Sensitive permissions justified
- [ ] Privacy Policy covers all data collection
- [ ] Restricted permissions compliance
- [ ] No prohibited content or features

## Release Configuration
- [ ] Release notes written (what's new)
- [ ] Staged rollout percentage set (recommend 5% initially)
- [ ] Target release date scheduled
- [ ] Pre-registration enabled (if applicable)

## Post-Upload Steps
- [ ] Monitor for policy violations
- [ ] Respond to user reviews
- [ ] Track crash reports and ANRs
- [ ] Monitor download and usage metrics
- [ ] Prepare next version based on feedback
EOF

# Create signing verification script
cat > "${UPLOAD_DIR}/verify-signing.sh" << 'EOF'
#!/bin/bash

# Verify APK signing status
APK_FILE="$1"

if [ -z "$APK_FILE" ]; then
    echo "Usage: $0 <path-to-apk>"
    exit 1
fi

echo "Verifying APK signing status..."
echo "APK: $APK_FILE"

# Check if APK exists
if [ ! -f "$APK_FILE" ]; then
    echo "ERROR: APK file not found: $APK_FILE"
    exit 1
fi

# Try to extract signing info using unzip and openssl
echo ""
echo "Checking for signing certificates..."

# Extract META-INF directory
TEMP_DIR=$(mktemp -d)
unzip -q "$APK_FILE" "META-INF/*" -d "$TEMP_DIR" 2>/dev/null || true

if [ -d "$TEMP_DIR/META-INF" ]; then
    echo "META-INF directory found:"
    ls -la "$TEMP_DIR/META-INF/"
    
    # Look for certificate files
    CERT_FILES=$(find "$TEMP_DIR/META-INF" -name "*.RSA" -o -name "*.DSA" -o -name "*.EC" 2>/dev/null)
    
    if [ -n "$CERT_FILES" ]; then
        echo ""
        echo "Certificate files found:"
        echo "$CERT_FILES"
        echo ""
        echo "This APK appears to be SIGNED"
        echo "WARNING: For Play Store upload with app signing by Google,"
        echo "you should upload the UNSIGNED APK and let Google handle signing."
    else
        echo ""
        echo "No certificate files found in META-INF"
        echo "This APK appears to be UNSIGNED"
        echo "✓ Ready for Play Store upload with Google Play App Signing"
    fi
else
    echo "No META-INF directory found"
    echo "This APK appears to be UNSIGNED"
    echo "✓ Ready for Play Store upload with Google Play App Signing"
fi

# Cleanup
rm -rf "$TEMP_DIR"

echo ""
echo "For Google Play Store:"
echo "1. Upload the UNSIGNED APK/AAB to Play Console"
echo "2. Google will sign it automatically with your upload key"
echo "3. Users will receive the Google-signed version"
EOF

chmod +x "${UPLOAD_DIR}/verify-signing.sh"

# Verify the APK is unsigned
echo ""
echo "Verifying APK signing status..."
"${UPLOAD_DIR}/verify-signing.sh" "${PROJECT_ROOT}/${APK_NAME}"

# Generate final summary
echo ""
echo "========================================="
echo "PREPARATION COMPLETE"
echo "========================================="
echo ""
echo "Upload directory: ${UPLOAD_DIR}"
echo ""
echo "Files prepared for Play Store upload:"
if [ -f "${UPLOAD_DIR}/SkyFi-ATAK-Plugin-v2.0-beta5-${TIMESTAMP}.aab" ]; then
    echo "  ✓ App Bundle (AAB): SkyFi-ATAK-Plugin-v2.0-beta5-${TIMESTAMP}.aab [PREFERRED]"
fi
echo "  ✓ APK: SkyFi-ATAK-Plugin-v2.0-beta5-${TIMESTAMP}.apk"
echo "  ✓ Release notes: metadata/release-notes.txt"
echo "  ✓ App metadata: metadata/app-metadata.json"
echo "  ✓ Upload checklist: PLAY_CONSOLE_CHECKLIST.md"
echo "  ✓ Signing verification script: verify-signing.sh"
echo ""
echo "NEXT STEPS:"
echo "1. Review the PLAY_CONSOLE_CHECKLIST.md"
echo "2. Gather required screenshots and store assets"
echo "3. Log in to Google Play Console"
echo "4. Create a new release in your app"
echo "5. Upload the AAB file (preferred) or APK file"
echo "6. Fill in store listing with prepared metadata"
echo "7. Complete the release rollout"
echo ""
echo "IMPORTANT:"
echo "- Google Play App Signing is enabled, so upload the UNSIGNED APK/AAB"
echo "- Google will handle the signing with your upload certificate"
echo "- Review the Data Safety section requirements carefully"
echo "- Test thoroughly before full rollout (use staged rollout)"
echo ""
echo "For questions, contact: support@skyfi.com"
echo "Documentation: https://docs.skyfi.com/atak"