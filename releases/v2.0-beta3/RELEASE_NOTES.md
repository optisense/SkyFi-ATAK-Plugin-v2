# SkyFi ATAK Plugin v2.0-beta3 Release Notes

**Release Date:** August 7, 2025  
**Build:** 44f121e9  
**ATAK Compatibility:** 5.4.0+

## What's New in Beta 3 - Complete UI Overhaul & Advanced Features

This release introduces a comprehensive dark theme UI redesign, radial menu integration, enhanced drawing tools, and advanced shape selection capabilities.

### Major Updates

#### **Dark Theme Implementation**
- Complete UI overhaul with black/dark backgrounds
- SkyFi brand colors integration (black, white, accent blue #4A90E2)
- Enhanced visual hierarchy with proper surface colors
- Improved text contrast for better readability

#### **Radial Menu Integration**
- **SkyFi Icon in Shape Menus** - Tap any shape → See SkyFi option in radial menu
- **Direct AOI Conversion** - Convert shapes to AOIs with one tap
- **Context-Aware Options** - Save AOI, create tasking order, view archives, calculate feasibility
- **Cross-Platform Support** - Works with squares, rectangles, polygons, and more

#### **Enhanced Drawing Tools**
- **Custom Polygon Drawing** - Tap-to-draw interface with visual feedback
- **Smart Area Validation** - Real-time area calculation and minimum size checking
- **AOI Naming Dialog** - Name AOIs immediately after drawing
- **Seamless Workflow** - Direct integration with tasking order creation

#### **Advanced Shape Selection - "From Existing Shape"**
- **Universal Shape Support** - Select rectangles, circles, polygons, freehand drawings
- **Visual Feedback** - Shape highlighting and selection confirmation
- **Smart Geometry Extraction** - Handles all ATAK shape types with proper coordinate conversion
- **Error Handling** - Comprehensive validation and user-friendly error messages
- **Background Processing** - Thread-safe operations with progress indicators

#### **Custom UI Components**
- **GradientTextView** - Animated gradient text effects
- **NeumorphicButton** - Soft shadow button design
- **CircularProgressButton** - Loading state animations
- **GlowingCardView** - Accent color glow effects

#### **Smooth Animations**
- 12+ animation files (fade, slide, bounce, scale)
- Material Design ripple effects (Android 5.0+)
- Button elevation animations
- Card lift interactions
- Dialog entrance/exit animations

#### **Enhanced Styles**
- Modern button styles (primary, secondary, accent)
- Improved input field styling
- Card backgrounds with subtle shadows
- Floating Action Button (FAB) support
- Progress bar with gradient effects

### Technical Improvements
- **ATAK 5.4.0 Full Compatibility** - Fixed all ATAK SDK compilation errors
- **Android 15 Support** - Proper broadcast receiver registration for newest Android
- **Thread-Safe Operations** - Background processing for heavy geometry extraction
- **Memory Management** - Optimized resource cleanup and OutOfMemoryError handling
- **Build Optimization** - M4 Max native builds with improved Gradle configuration
- **Defensive Programming** - Comprehensive error handling for all MapItem operations
- **Tool Lifecycle Management** - Proper ATAK Tool interface implementation

### Bug Fixes
- **ATAK API Compliance** - Fixed Icon API usage and deprecated method calls
- **Threading Issues** - Resolved static context issues in PredictionEngine
- **Animation Compatibility** - Fixed AnimatorSet array conversion for Android compatibility
- **Broadcast Receivers** - Added Android 15 RECEIVER_NOT_EXPORTED flag requirement
- **Shape Processing** - Enhanced coordinate validation and conversion accuracy
- **Build Configuration** - Removed deprecated Gradle properties and warnings

### New Workflows

#### **Complete AOI Creation Options:**
1. **Draw on Map** → Custom tap-to-draw polygon tool
2. **From GPS Location** → Auto-generate AOI around current position  
3. **Enter Coordinates** → Manual coordinate entry
4. **From Existing Shape** → Select any shape already drawn on map

#### **Radial Menu Integration:**
1. **Right-click any shape** → Select "SkyFi AOI" from radial menu
2. **Choose action** → Save AOI, Create Order, View Archives, Calculate Feasibility
3. **Seamless processing** → Direct integration with existing workflows

## Installation

### Debug Version (for testing)
```bash
adb install -r skyfi-atak-plugin-v2.0-beta3-civ-debug.apk
```

### Release Version (for production)
```bash
adb install -r skyfi-atak-plugin-v2.0-beta3-civ-release.apk
```

## File Checksums

```
Debug APK:   22MB - skyfi-atak-plugin-v2.0-beta3-civ-debug.apk
Release APK: 21MB - skyfi-atak-plugin-v2.0-beta3-civ-release.apk
```

## Known Issues
- Plugin requires ATAK-CIV 5.4.0 or higher
- Dark theme is always enabled (no light mode toggle yet)
- Radial menu for circles not yet implemented (squares/polygons work perfectly)
- Some complex freehand shapes may require geometry simplification

## Next Release (v2.0-beta4)
- Circle support in radial menu integration
- Theme toggle switch (dark/light mode)  
- Advanced shape analysis and geometry validation
- Performance optimizations for older devices
- Extended context menu options for shapes

## Support
For issues or feedback, please contact the development team or create an issue in the project repository.