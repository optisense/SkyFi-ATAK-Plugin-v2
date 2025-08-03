# SkyFi ATAK Plugin v3 Enhanced - Release Notes

**Build Date:** August 3, 2025  
**File:** `SkyFi-ATAK-Plugin-v3-Enhanced-20250803-001838.apk`  
**ATAK Compatibility:** 5.5.0+ CIV  

## 🆕 New Features

### Enhanced Shape-to-AOI Conversion
- **Smart Polygon Detection** - Automatically detects drawn polygons and offers AOI conversion
- **Area Calculation** - Real-time area calculation in km² using shoelace formula
- **Interactive Dialog** - Three-option dialog for user control:
  - "Save & Use for Order" - Saves shape as named AOI and launches order
  - "Use for Order Only" - Uses shape for order without saving
  - "Cancel" - Cancels operation
- **Automatic Naming** - Auto-generates AOI names with timestamp

### Improved UI Components
- **Fixed Profile Button** - My Profile dropdown now displays correctly with proper error handling
- **Enhanced Menu Integration** - SkyFi menu appears in radial menu for all supported shapes
- **Better Error Handling** - Comprehensive logging and null checks for stability

## 🔧 Technical Improvements

### Shape Support
- **Rectangle Shapes** - SimpleRectangle, Rectangle, DrawingRectangle
- **Polygon Shapes** - DrawingShape with full geometry support
- **Association Objects** - Rectangle line associations
- **Marker Types** - Shape markers and waypoint markers

### Integration Features
- **WKT Conversion** - Automatic conversion to Well-Known Text format
- **AOI Management** - Full integration with existing AOI system
- **Menu Registration** - Automatic menu attachment to new shapes

### Stability Enhancements
- **Apple Silicon Build** - Fixed Android Gradle Plugin compatibility for M4 Macs
- **Profile Dropdown** - Added setRetain(true) and proper error handling
- **CardView Fixes** - Resolved ClassCastException errors in TaskingOrderFragment

## 📱 Installation Instructions

### For ATAK CIV (Sideloaded)
1. Enable "Unknown Sources" in Android settings
2. Install APK via ADB or file manager
3. Launch ATAK and activate SkyFi plugin

### For Play Store ATAK
- Wait for TAK.gov signed version (submitted, pending approval)
- Will be available through official TAK.gov plugin repository

## 🧪 Testing Instructions

### Shape-to-AOI Feature
1. **Draw a Polygon** - Use ATAK drawing tools to create any polygon shape
2. **Tap the Shape** - Long press or tap to open radial menu
3. **Select SkyFi** - Choose SkyFi option from radial menu
4. **Choose Action** - Select from dialog options based on your needs
5. **Verify AOI** - Check AOI list to confirm saved areas

### UI Testing
1. **Profile Menu** - Tap "My Profile" button and verify dropdown appears
2. **Menu Items** - Test View Orders, New Order From My Location, Set API Key
3. **Shape Menus** - Draw various shapes and verify SkyFi appears in radial menu

## 🐛 Known Issues
- Kotlin version warnings during build (does not affect functionality)
- Play Store ATAK compatibility requires TAK.gov signed version

## 🔄 Previous Versions
- **v2** - Basic profile menu and order functionality
- **v1** - Initial SkyFi integration with ATAK

---

**Team Testing:** This unsigned APK is ready for internal team testing on ATAK CIV installations. For production deployment to Play Store ATAK users, wait for the TAK.gov signed version.