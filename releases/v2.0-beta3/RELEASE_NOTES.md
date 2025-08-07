# SkyFi ATAK Plugin v2.0-beta3 Release Notes

**Release Date:** August 6, 2025  
**Build:** 06ab9f2b  
**ATAK Compatibility:** 5.4.0+

## 🎨 What's New in Beta 3 - Dark Theme UI Makeover

This release introduces a comprehensive dark theme UI redesign to match the SkyFi.com aesthetic.

### Major Updates

#### **Dark Theme Implementation**
- Complete UI overhaul with black/dark backgrounds
- SkyFi brand colors integration (black, white, accent blue #4A90E2)
- Enhanced visual hierarchy with proper surface colors
- Improved text contrast for better readability

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
- Fixed all ATAK SDK compilation errors
- Optimized for M4 Max build performance
- Reduced APK size through R8 optimization
- Improved Gradle build configuration
- Better memory management

### Bug Fixes
- Fixed Icon API usage for marker icons
- Resolved static context issues in PredictionEngine
- Fixed AnimatorSet array conversion
- Removed deprecated Gradle properties
- Corrected ATAK API method calls

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
- Some legacy UI elements may not fully support the new theme

## Next Release (v2.0-beta4)
- Theme toggle switch (dark/light mode)
- Additional UI customization options
- Performance optimizations for older devices
- Extended AI overlay features

## Support
For issues or feedback, please contact the development team or create an issue in the project repository.