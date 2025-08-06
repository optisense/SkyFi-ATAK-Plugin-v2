# Screenshots Guide for SkyFi ATAK Plugin v2

This document outlines the key screenshots needed to enhance the documentation and provide visual guidance for users.

## 📸 Priority Screenshots Needed

### 1. Installation & Setup (High Priority)

#### 1.1 ATAK Plugin Installation
- **File**: `screenshots/installation/atak-plugin-install.png`
- **Description**: Android APK installation dialog showing SkyFi plugin permissions
- **Context**: Used in ONBOARDING_GUIDE.md installation section

#### 1.2 SkyFi Toolbar Icon
- **File**: `screenshots/ui/skyfi-toolbar-icon.png`
- **Description**: ATAK toolbar showing the SkyFi satellite icon
- **Context**: First-time users need to locate the plugin

#### 1.3 API Key Setup
- **File**: `screenshots/setup/api-key-dialog.png`
- **Description**: SkyFi API key input dialog
- **Context**: Essential for initial configuration

### 2. Core Functionality (High Priority)

#### 2.1 Main Plugin Menu
- **File**: `screenshots/ui/main-menu.png`
- **Description**: SkyFi plugin main menu with all options
- **Context**: Overview of available features

#### 2.2 Satellite Tasking Dialog
- **File**: `screenshots/tasking/new-order-dialog.png`
- **Description**: New satellite order creation dialog
- **Context**: Core functionality demonstration

#### 2.3 Order Status List
- **File**: `screenshots/orders/order-list.png`
- **Description**: List of satellite orders with status indicators
- **Context**: Shows order management capabilities

### 3. Drawing & AOI Management (Medium Priority)

#### 3.1 ATAK Drawing Integration
- **File**: `screenshots/drawing/atak-polygon-draw.png`
- **Description**: Using ATAK's native drawing tools to create AOI
- **Context**: Shows seamless ATAK integration

#### 3.2 Context Menu Integration
- **File**: `screenshots/drawing/context-menu-skyfi.png`
- **Description**: Right-click context menu on drawn shape showing SkyFi options
- **Context**: Demonstrates workflow integration

#### 3.3 AOI Management Screen
- **File**: `screenshots/aoi/aoi-manager.png`
- **Description**: AOI management interface with saved areas
- **Context**: Shows area management capabilities

### 4. Advanced Features (Medium Priority)

#### 4.1 Coordinate Input Dialog
- **File**: `screenshots/input/coordinate-dialog.png`
- **Description**: Coordinate input dialog with Lat/Long and MGRS options
- **Context**: Precise coordinate entry demonstration

#### 4.2 Sensor Selection
- **File**: `screenshots/tasking/sensor-selection.png`
- **Description**: Sensor type selection (Optical, SAR, Hyperspectral)
- **Context**: Shows different imagery options

#### 4.3 Priority Selection
- **File**: `screenshots/tasking/priority-options.png`
- **Description**: Priority selection radio buttons (WHEN_AVAILABLE vs PRIORITY)
- **Context**: Demonstrates pricing options

### 5. Imagery & Results (Medium Priority)

#### 5.1 Archive Browser
- **File**: `screenshots/imagery/archive-browser.png`
- **Description**: Historical imagery browser with thumbnails
- **Context**: Shows imagery search capabilities

#### 5.2 Image Overlay on Map
- **File**: `screenshots/imagery/map-overlay.png`
- **Description**: Satellite imagery overlaid on ATAK map
- **Context**: End result of satellite tasking

#### 5.3 Opacity Controls
- **File**: `screenshots/imagery/opacity-slider.png`
- **Description**: Opacity control dialog for image overlays
- **Context**: Image management features

### 6. Error States & Troubleshooting (Low Priority)

#### 6.1 Connection Error
- **File**: `screenshots/errors/connection-error.png`
- **Description**: Network connection error dialog
- **Context**: Troubleshooting documentation

#### 6.2 Invalid Coordinates
- **File**: `screenshots/errors/invalid-coordinates.png`
- **Description**: Error message for invalid coordinate input
- **Context**: User input validation

#### 6.3 Insufficient Credits
- **File**: `screenshots/errors/insufficient-credits.png`
- **Description**: Error dialog when user has insufficient account credits
- **Context**: Account management

## 📱 Device & Platform Coverage

### Target Devices for Screenshots
1. **Android Tablet** (Primary)
   - Samsung Galaxy Tab or similar
   - Landscape orientation preferred
   - ATAK 5.4.0 CIV installed

2. **Android Phone** (Secondary)
   - Modern Android device (Android 10+)
   - Portrait orientation
   - Show mobile-optimized UI

### Screenshot Specifications
- **Resolution**: 1920x1080 minimum
- **Format**: PNG (for transparency support)
- **Quality**: High quality, no compression artifacts
- **Annotations**: Use red arrows/circles for callouts when needed

## 🎨 Visual Guidelines

### Consistency Standards
- Use consistent device frames
- Maintain consistent ATAK theme/appearance
- Ensure good contrast and readability
- Include relevant map data (but avoid sensitive locations)

### Annotation Style
- **Red arrows** for pointing to specific UI elements
- **Red circles** for highlighting important areas
- **Yellow highlights** for text emphasis
- **Consistent font** for any text overlays

## 📁 File Organization

```
screenshots/
├── installation/
│   ├── atak-plugin-install.png
│   └── skyfi-toolbar-icon.png
├── setup/
│   └── api-key-dialog.png
├── ui/
│   └── main-menu.png
├── tasking/
│   ├── new-order-dialog.png
│   ├── sensor-selection.png
│   └── priority-options.png
├── orders/
│   └── order-list.png
├── drawing/
│   ├── atak-polygon-draw.png
│   └── context-menu-skyfi.png
├── aoi/
│   └── aoi-manager.png
├── input/
│   └── coordinate-dialog.png
├── imagery/
│   ├── archive-browser.png
│   ├── map-overlay.png
│   └── opacity-slider.png
└── errors/
    ├── connection-error.png
    ├── invalid-coordinates.png
    └── insufficient-credits.png
```

## 🔄 Update Process

### When to Update Screenshots
- Major UI changes
- New features added
- ATAK version updates
- User feedback indicates confusion

### Maintenance Schedule
- **Quarterly review** of all screenshots
- **Immediate update** for breaking changes
- **Version tagging** to match plugin releases

## 📝 Usage in Documentation

### Integration Points
- **README.md**: Hero image and key feature screenshots
- **ONBOARDING_GUIDE.md**: Step-by-step visual walkthrough
- **BETA_TESTING_GUIDE.md**: Installation and testing screenshots
- **GitHub Issues**: Reference screenshots for bug reports

### Alt Text Requirements
All screenshots must include descriptive alt text for accessibility:
```markdown
![SkyFi plugin main menu showing satellite tasking options](screenshots/ui/main-menu.png)
```

---

**Note**: This is a planning document. Actual screenshots need to be captured using real devices with the SkyFi ATAK Plugin v2 installed and configured.