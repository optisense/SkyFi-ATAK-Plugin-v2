#!/bin/bash

# Create GitHub release with artifacts

set -e

# Check if gh is installed
if ! command -v gh &> /dev/null; then
    echo "GitHub CLI (gh) is not installed. Please install it first:"
    echo "brew install gh"
    exit 1
fi

# Variables
VERSION="2.0"
TAG="v${VERSION}-stable"
TIMESTAMP=$(date +%Y%m%d-%H%M%S)

echo "=== Creating GitHub Release for SkyFi ATAK Plugin v${VERSION} ==="

# Find the latest debug APK
DEBUG_APK=$(ls -t releases/debug/*.apk | head -1)
TAKGOV_SUBMISSION=$(ls -t releases/takgov/*.zip | head -1)

if [ ! -f "$DEBUG_APK" ]; then
    echo "Debug APK not found in releases/debug/"
    exit 1
fi

if [ ! -f "$TAKGOV_SUBMISSION" ]; then
    echo "TAK.gov submission not found in releases/takgov/"
    exit 1
fi

# Create release notes
cat > RELEASE_NOTES.md << EOF
## 🚀 SkyFi ATAK Plugin v${VERSION} - Stable Release

### ✨ What's New
- **Enhanced UI/UX**: Improved dashboard and navigation
- **Stability First**: Removed experimental AI features for rock-solid performance
- **Memory Management**: Fixed all known memory leaks
- **Plugin Initialization**: Fixed icon visibility issues

### 🐛 Bug Fixes
- Fixed OkHttp/Retrofit library conflicts causing crashes
- Fixed plugin initialization preventing icon from appearing
- Fixed memory leaks from broadcast receivers
- Fixed ImageCacheManager resource cleanup
- Fixed null pointer exceptions in Preferences

### 📦 Downloads
- **Debug APK**: \`$(basename "$DEBUG_APK")\` - For testing with debug ATAK
- **TAK.gov Submission**: \`$(basename "$TAKGOV_SUBMISSION")\` - Upload to https://tak.gov/user_builds

### 🔧 Installation

#### Debug Testing:
\`\`\`bash
adb install $(basename "$DEBUG_APK")
\`\`\`

#### Production:
1. Upload the TAK.gov submission ZIP to https://tak.gov/user_builds
2. Wait for the build to complete (~5-10 minutes)
3. Download the signed APK
4. Install on production ATAK

### 📍 Finding the SkyFi Button
1. Check the main ATAK toolbar (bottom/side)
2. Look in the overflow menu (⋮) if toolbar is crowded
3. When selecting shapes on the map, check the radial menu

### 🔍 Verified With
- ATAK 5.4.0 CIV
- Android 10+ devices

### ⚠️ Important Notes
- This version focuses on stability over features
- All AI enhancements have been temporarily removed
- Requires ATAK 5.4.0 or higher

### 📝 Changelog
See [commit history](https://github.com/optisense/SkyFi-ATAK-Plugin-v2/commits/main) for detailed changes.
EOF

echo "Creating release..."

# Create the release
gh release create "$TAG" \
    "$DEBUG_APK" \
    "$TAKGOV_SUBMISSION" \
    --title "SkyFi ATAK Plugin v${VERSION} - Stable Release" \
    --notes-file RELEASE_NOTES.md \
    --draft

echo
echo "✅ Draft release created!"
echo "Visit https://github.com/optisense/SkyFi-ATAK-Plugin-v2/releases to publish it"
echo

# Clean up
rm RELEASE_NOTES.md