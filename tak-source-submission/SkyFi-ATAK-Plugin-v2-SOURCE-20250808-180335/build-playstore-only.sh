#!/bin/bash

################################################################################
# Build script for Play Store ATAK Plugin
# This builds a version that ONLY works with Google Play Store ATAK
# No SDK dependencies, no gov.tak.api classes
################################################################################

set -e

echo "=================================================="
echo "Building SkyFi ATAK Plugin for Play Store ONLY"
echo "=================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if we're in the right directory
if [ ! -f "app/build.gradle.playstore" ]; then
    echo -e "${RED}Error: build.gradle.playstore not found${NC}"
    echo "Please run this script from the project root directory"
    exit 1
fi

# Clean previous builds
echo -e "${YELLOW}Cleaning previous builds...${NC}"
rm -rf app/build
rm -rf build
./gradlew clean 2>/dev/null || true

# Backup original files
echo -e "${YELLOW}Backing up original files...${NC}"
cp app/build.gradle app/build.gradle.backup 2>/dev/null || true
cp app/src/main/AndroidManifest.xml app/src/main/AndroidManifest.xml.backup 2>/dev/null || true
cp app/src/main/assets/plugin.xml app/src/main/assets/plugin.xml.backup 2>/dev/null || true

# Switch to Play Store configuration
echo -e "${YELLOW}Switching to Play Store configuration...${NC}"
cp app/build.gradle.playstore app/build.gradle
cp app/src/main/AndroidManifest_playstore.xml app/src/main/AndroidManifest.xml
cp app/src/main/assets/plugin_playstore.xml app/src/main/assets/plugin.xml

# Create ATAK stub classes if needed
echo -e "${YELLOW}Creating ATAK stub classes...${NC}"
mkdir -p app/libs
cat > app/src/main/java/com/atakmap/android/maps/MapView.java << 'EOF'
package com.atakmap.android.maps;

import android.content.Context;
import android.view.ViewGroup;
import com.atakmap.coremap.maps.coords.GeoPoint;

public class MapView extends ViewGroup {
    public MapView(Context context) { super(context); }
    public static MapView getMapView() { return null; }
    public Context getContext() { return null; }
    public MapItem getSelfMarker() { return null; }
    public MapController getMapController() { return null; }
    public MapEventDispatcher getMapEventDispatcher() { return null; }
    public GeoPoint inverse(float x, float y) { return null; }
    
    public static class MapController {
        public void panTo(GeoPoint point, boolean animate) {}
    }
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {}
}
EOF

cat > app/src/main/java/com/atakmap/android/maps/MapItem.java << 'EOF'
package com.atakmap.android.maps;

import com.atakmap.coremap.maps.coords.GeoPoint;

public class MapItem {
    public GeoPoint getPoint() { return null; }
    public String getUID() { return ""; }
    public String getType() { return ""; }
    public String getTitle() { return ""; }
    public void setMetaString(String key, String value) {}
}
EOF

# Build the APK
echo -e "${YELLOW}Building Play Store APK...${NC}"

# Build debug version first for testing
echo "Building debug version..."
./gradlew :app:assembleDebug

if [ $? -eq 0 ]; then
    echo -e "${GREEN}Debug build successful!${NC}"
    
    # Build release version
    echo "Building release version..."
    ./gradlew :app:assembleRelease
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}Release build successful!${NC}"
    else
        echo -e "${RED}Release build failed${NC}"
    fi
else
    echo -e "${RED}Debug build failed${NC}"
    exit 1
fi

# Copy built APKs to output directory
echo -e "${YELLOW}Copying APKs to output directory...${NC}"
mkdir -p playstore-builds
cp app/build/outputs/apk/debug/*.apk playstore-builds/ 2>/dev/null || true
cp app/build/outputs/apk/release/*.apk playstore-builds/ 2>/dev/null || true

# Generate AAB for Play Store upload
echo -e "${YELLOW}Building AAB for Play Store upload...${NC}"
./gradlew :app:bundleRelease

if [ $? -eq 0 ]; then
    cp app/build/outputs/bundle/release/*.aab playstore-builds/ 2>/dev/null || true
    echo -e "${GREEN}AAB bundle created successfully!${NC}"
fi

# Restore original files
echo -e "${YELLOW}Restoring original files...${NC}"
mv app/build.gradle.backup app/build.gradle 2>/dev/null || true
mv app/src/main/AndroidManifest.xml.backup app/src/main/AndroidManifest.xml 2>/dev/null || true
mv app/src/main/assets/plugin.xml.backup app/src/main/assets/plugin.xml 2>/dev/null || true

# Clean up stub classes
rm -f app/src/main/java/com/atakmap/android/maps/MapView.java
rm -f app/src/main/java/com/atakmap/android/maps/MapItem.java

echo "=================================================="
echo -e "${GREEN}Play Store build complete!${NC}"
echo "=================================================="
echo "Output files:"
ls -la playstore-builds/

echo ""
echo "Next steps:"
echo "1. Test the debug APK in Play Store ATAK"
echo "2. Sign the release AAB with your Play Store key"
echo "3. Upload the AAB to Google Play Console"
echo ""
echo "Important notes:"
echo "- This build ONLY works with Play Store ATAK"
echo "- It will NOT work with SDK ATAK or TAK.gov ATAK"
echo "- All SDK-specific features have been removed"