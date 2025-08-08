# Play Store ATAK-CIV Compatibility Fix

## Problem Summary
The SkyFi ATAK plugin was failing to load on Play Store ATAK-CIV (version 5.4.0.16) with a `NoClassDefFoundError` for `IServiceController` interface, which is only available in newer SDK versions.

## Solution Implemented

### 1. Removed Incompatible APIs
- **IServiceController**: Removed all references to this interface
- **Pane API**: Removed all Pane, PaneBuilder, and IHostUIService references
- **Plugin Interface**: Removed gov.tak.api.plugin.IPlugin from plugin.xml

### 2. Switched to Compatible APIs
- **MapComponent**: Using DropDownMapComponent for plugin initialization
- **Dropdown Receivers**: Using dropdown receivers for all UI elements
- **Singleton Pattern**: Plugin uses singleton pattern for state management

### 3. Build Configuration Changes
- Compiling against ATAK SDK 5.3.0 for maximum compatibility
- Version range: 5.3.0 to 5.4.0.19
- Java 8 compatibility for TAK.gov builds

## Files Modified

### Core Plugin Files
- `/app/src/main/java/com/skyfi/atak/plugin/SkyFiPlugin.java`
  - Removed IServiceController dependency
  - Removed Pane API usage
  - Added singleton pattern
  - Fixed all UI references

- `/app/src/main/assets/plugin.xml`
  - Removed IPlugin interface declaration
  - Plugin now loads via MapComponent only

- `/app/build.gradle`
  - Changed ATAK_VERSION to 5.3.0
  - Added version range support
  - Updated to version 2.0-beta5

## TAK.gov Submission

A new submission package has been created:
- **File**: `SkyFi-ATAK-Plugin-v2-TAKGOV-V5-20250807-163639.zip`
- **Version**: 2.0-beta5
- **Compatibility**: ATAK 5.3.0 - 5.4.0.19

### Key Points for TAK.gov
1. This build MUST be tested on Play Store ATAK-CIV 5.4.0.16
2. The plugin no longer uses any APIs unavailable in older ATAK versions
3. All UI is handled through dropdown receivers for compatibility

## Testing Requirements

### Play Store ATAK-CIV (5.4.0.16)
- [ ] Plugin loads without crashes
- [ ] Toolbar icon appears
- [ ] Menu items work
- [ ] Drawing tools function
- [ ] Orders can be created
- [ ] No ClassNotFoundError exceptions

### SDK Versions (5.3.0 - 5.4.0.19)
- [ ] Plugin works on all SDK versions
- [ ] Features degrade gracefully on older versions

## Next Steps

1. **Upload to TAK.gov**: Submit the new package
2. **Request Play Store Testing**: Emphasize testing on ATAK-CIV 5.4.0.16
3. **Monitor Build Logs**: Ensure no cached source is used
4. **Test Signed APK**: Once built, test on Play Store ATAK immediately

## Technical Details

### API Compatibility Matrix

| API | SDK 5.4.0.18+ | Play Store 5.4.0.16 | Our Plugin |
|-----|---------------|---------------------|------------|
| IServiceController | ✅ | ❌ | Not Used |
| Pane API | ✅ | ❌ | Not Used |
| DropDownMapComponent | ✅ | ✅ | Used |
| MapComponent | ✅ | ✅ | Used |
| Dropdown Receivers | ✅ | ✅ | Used |

### Error Prevention
- No runtime class loading that could fail
- All APIs checked for availability before use
- Graceful degradation for missing features
- Extensive null checks and error handling

## Contact
For issues or questions: skyfi-dev@optisense.com