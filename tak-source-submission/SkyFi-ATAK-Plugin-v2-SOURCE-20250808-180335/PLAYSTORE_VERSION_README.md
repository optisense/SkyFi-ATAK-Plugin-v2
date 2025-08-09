# SkyFi ATAK Plugin - Play Store Edition

This is a special version of the SkyFi ATAK Plugin designed **EXCLUSIVELY** for Google Play Store ATAK.

## ⚠️ IMPORTANT COMPATIBILITY NOTICE

**This version ONLY works with:**
- ✅ Google Play Store ATAK (com.atakmap.app.civ)

**This version does NOT work with:**
- ❌ ATAK SDK versions
- ❌ TAK.gov distributed ATAK
- ❌ ATAK-Mil versions
- ❌ Any SDK-based ATAK builds

## Key Differences from SDK Version

### Removed Dependencies
- No `gov.tak.api.*` classes (don't exist in Play Store ATAK)
- No `IPlugin` interface implementation
- No SDK-specific plugin loading mechanisms
- No `IServiceController` usage
- No native library loading

### Direct Integration
- Uses `DropDownMapComponent` directly
- Simplified plugin.xml without SDK references
- Direct component registration
- Play Store compatible package structure

## Build Instructions

### Prerequisites
1. Android Studio or command line with Gradle
2. Play Store signing key (for release builds)
3. Java 11 or higher

### Building for Play Store

```bash
# Use the dedicated Play Store build script
./build-playstore-only.sh
```

This script will:
1. Switch to Play Store-specific configuration
2. Build both debug and release APKs
3. Generate AAB bundle for Play Store upload
4. Restore original configuration

### Manual Build

```bash
# Clean previous builds
./gradlew clean

# Use Play Store build configuration
cp app/build.gradle.playstore app/build.gradle
cp app/src/main/AndroidManifest_playstore.xml app/src/main/AndroidManifest.xml
cp app/src/main/assets/plugin_playstore.xml app/src/main/assets/plugin.xml

# Build debug APK for testing
./gradlew :app:assembleDebug

# Build release AAB for Play Store
./gradlew :app:bundleRelease
```

## Installation & Testing

### Testing in Play Store ATAK

1. Install Play Store ATAK from Google Play
2. Install the debug APK:
   ```bash
   adb install playstore-builds/app-debug.apk
   ```
3. Launch ATAK
4. The plugin should auto-load
5. Look for "SkyFi" in the toolbar

### Verifying Installation

You can verify the plugin is installed by:
1. Opening the installed plugin app directly (it has an info activity)
2. Checking ATAK's plugin manager
3. Looking for the SkyFi toolbar button in ATAK

## Features

All core SkyFi features are available:
- ✅ Satellite tasking with custom AOIs
- ✅ Order management
- ✅ Archive imagery search
- ✅ AOI drawing tools
- ✅ Coordinate input (Lat/Lon, MGRS)
- ✅ GPS-based AOI creation
- ✅ Preview mode for imagery
- ✅ API key management

## Architecture

### Main Components

1. **SkyFiPlayStorePlugin** - Main plugin component extending DropDownMapComponent
2. **Simplified Receivers** - Direct broadcast receivers without SDK dependencies
3. **Play Store Manifest** - Configured for Play Store discovery
4. **Minimal Dependencies** - Only uses classes available in Play Store ATAK

### Package Structure

```
com.optisense.skyfi.atak/
├── playstore/
│   └── SkyFiPlayStorePlugin.java    # Main Play Store plugin
├── PlayStoreInfoActivity.java       # Info activity for verification
└── [existing components]             # Reused where compatible
```

## Play Store Deployment

### Preparing for Upload

1. **Sign the AAB:**
   ```bash
   jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
     -keystore playstore-skyfi.keystore \
     app-release.aab skyfi
   ```

2. **Verify signing:**
   ```bash
   jarsigner -verify -verbose app-release.aab
   ```

3. **Upload to Play Console:**
   - Use the generated AAB file
   - Set appropriate content rating
   - Add required screenshots
   - Configure pricing (free/paid)

### Play Store Listing

Recommended description:
```
SkyFi for ATAK - Satellite Tasking Plugin

Easily task satellites and view imagery directly within ATAK.

This plugin requires ATAK (Android Team Awareness Kit) to be installed.

Features:
• Task satellites with custom areas of interest
• Manage and track tasking orders
• Search historical satellite imagery
• Draw AOIs directly on the map
• Support for multiple coordinate formats
• Real-time satellite feasibility analysis

Note: This version is specifically for Play Store ATAK.
```

## Troubleshooting

### Plugin Not Loading

1. **Check ATAK version:** Must be Play Store ATAK
2. **Verify installation:** Check if plugin app is installed
3. **Clear ATAK cache:** Settings → Apps → ATAK → Clear Cache
4. **Reinstall:** Uninstall plugin, restart ATAK, reinstall plugin

### ClassNotFoundException Errors

If you see errors about missing classes:
- You're likely using SDK ATAK instead of Play Store ATAK
- Use the SDK version of the plugin instead

### API Connection Issues

1. Set API key in plugin settings
2. Ensure internet connectivity
3. Check firewall/VPN settings

## Development Notes

### Key Design Decisions

1. **No Reflection:** Unlike the SDK version, this doesn't use reflection to find interfaces
2. **Direct Component:** Registers as a MapComponent directly
3. **Simplified Loading:** No complex plugin loading mechanism
4. **Minimal Stubs:** Only essential ATAK classes are referenced

### Testing Checklist

- [ ] Plugin loads in Play Store ATAK
- [ ] Toolbar button appears
- [ ] Can create AOIs
- [ ] Can submit tasking orders
- [ ] Archive search works
- [ ] Settings persist
- [ ] No crashes on rotation
- [ ] Memory usage acceptable

## Support

For issues specific to the Play Store version:
1. Verify you're using Play Store ATAK
2. Check the info activity for status
3. Review logcat for errors
4. Contact support with version info

## License

© 2024 OptiSense - All Rights Reserved

This Play Store edition is distributed under commercial license.
Contact OptiSense for licensing information.