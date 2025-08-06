# 🛰️ SkyFi ATAK Plugin v2 - Complete Onboarding Guide

```
 ____  _          ______ _       ___  _____ ___  _  __
/ ___|| | ___   _|  ____(_)     / _ \|_   _/ _ \| |/ /
\___ \| |/ / | | | |__  | |    / /_\ \ | |/ /_\ \ ' / 
 ___) |   <| |_| |  __| | |    |  _  | | ||  _  |  <  
|____/|_|\_\\__, |_|    |_|    |_| |_| |_||_| |_|_|\_\
            |___/                                     
```

Welcome to the SkyFi ATAK Plugin v2! This guide will get you up and running whether you're new to ATAK or a seasoned veteran.

## 🎯 Quick Start (5 minutes)

**Already know ATAK?** Jump to [Installation](#installation)  
**New to ATAK?** Start with [What is ATAK?](#what-is-atak)

---

## What is ATAK?

**ATAK (Android Team Awareness Kit)** is a tactical mapping and situational awareness app used by military and first responders. Think of it as "Google Maps for tactical operations" with features like:

- Real-time location sharing with your team
- Drawing tools for marking areas and routes  
- Messaging and coordination tools
- Plugin ecosystem for specialized capabilities

**SkyFi's plugin** adds satellite imagery superpowers to ATAK - letting you task satellites and access imagery directly within your tactical workflow.

---

## What SkyFi Adds to ATAK

### 🛰️ Satellite Tasking
- **Point and Task**: Tap anywhere on the map to order satellite imagery
- **Draw Areas**: Use ATAK's drawing tools to define areas of interest
- **Smart Scheduling**: Real-time feasibility analysis shows when satellites can capture your area

### 📡 Imagery Integration  
- **Offline Caching**: Download imagery for offline use in the field
- **Archive Access**: Browse and search historical satellite imagery
- **Overlay Management**: Control transparency and layering of satellite data

### 🎯 Mission Planning
- **Priority Tasking**: Rush orders for time-critical intelligence
- **Multi-Sensor Support**: Optical, SAR, and Hyperspectral imagery
- **Coordinate Flexibility**: Works with Lat/Long, MGRS, and current location

---

## Installation

### Prerequisites

1. **ATAK 5.4.0** installed on your Android device
   - Download from [tak.gov](https://tak.gov) (requires free registration)
   - Minimum Android 5.0 (API 21)

2. **SkyFi Account** 
   - Sign up at [skyfi.com](https://skyfi.com)
   - Get your API key from account settings

3. **Enable Unknown Sources**
   - Android Settings → Security → Unknown Sources
   - Or: Settings → Apps → Special Access → Install Unknown Apps

### Install the Plugin

#### Option 1: Direct Download (Recommended)
1. Download the latest APK: [GitHub Releases](https://github.com/optisense/SkyFi-ATAK-Plugin-v2/releases)
2. Open the APK file on your device
3. Tap "Install" and accept permissions
4. Open ATAK - the SkyFi icon should appear in the toolbar

#### Option 2: ADB Install (Developers)
```bash
adb install -r skyfi-atak-plugin-v2.0-beta-civ-debug.apk
```

### First-Time Setup

1. **Open ATAK** and look for the SkyFi icon in the toolbar
2. **Tap the SkyFi icon** to open the plugin
3. **Set API Key**: Go to Settings → Set API Key
4. **Enter your SkyFi API key** from your account
5. **Test connection** by viewing your profile

---

## Your First Satellite Task

### Method 1: Point and Click (Easiest)
1. **Tap the SkyFi icon** in ATAK's toolbar
2. **Select "New Order from My Location"**
3. **Choose your sensor type** (Optical recommended for beginners)
4. **Set priority** (WHEN_AVAILABLE is cheaper)
5. **Submit order** and wait for confirmation

### Method 2: Draw an Area (Most Flexible)
1. **Use ATAK's drawing tools** to draw a polygon on the map
2. **Right-click the shape** → SkyFi → "Task Satellite"
3. **Configure your order** (sensor, priority, etc.)
4. **Submit** and track progress in "View Orders"

### Method 3: Coordinate Input (Precise)
1. **SkyFi menu** → "Coordinate Input"
2. **Enter coordinates** (Lat/Long or MGRS)
3. **Set area size** (minimum varies by sensor)
4. **Configure and submit** your order

---

## Understanding Your Orders

### Order Status Colors
- 🟡 **PENDING**: Order submitted, waiting for satellite
- 🔵 **PROCESSING**: Satellite captured, processing imagery  
- 🟢 **COMPLETED**: Imagery ready for download
- 🔴 **FAILED**: Issue with capture or processing

### Priority Types
- **WHEN_AVAILABLE**: Standard pricing, flexible timing
- **PRIORITY**: Higher cost, faster scheduling

### Sensor Types
- **Optical (EO)**: Standard visible light imagery (0.25 sq km minimum)
- **SAR**: Radar imagery, works through clouds (1.0 sq km minimum)  
- **Hyperspectral**: Advanced spectral analysis (4.0 sq km minimum)

---

## Pro Tips for ATAK Veterans

### Integration Points
- **Native Drawing Tools**: All ATAK shapes can be tasked via context menu
- **Coordinate Systems**: Full MGRS support alongside Lat/Long
- **Offline Capability**: Cached imagery works without network
- **Map Layers**: Satellite imagery appears as standard ATAK overlays

### Performance Notes
- **Memory Usage**: ~50MB typical, 100MB max with heavy caching
- **Network**: HTTPS only, certificate pinned for security
- **Storage**: 100MB disk cache, configurable cleanup

### Advanced Features
- **Batch Operations**: Select multiple images for caching/archiving
- **AOR Filtering**: Filter imagery by area of responsibility
- **Metadata Search**: Sort by date, location, cloud coverage
- **Opacity Controls**: Adjust transparency for overlay analysis

---

## Troubleshooting

### Plugin Won't Load
- ✅ Verify ATAK version is 5.4.0+
- ✅ Check Android version (minimum 5.0)
- ✅ Restart ATAK after installation
- ✅ Look in Settings → Tools for "SkyFi Plugin"

### Can't Connect to SkyFi
- ✅ Verify API key is correct
- ✅ Check internet connection
- ✅ Try logging out/in from SkyFi website
- ✅ Contact support if account issues persist

### Orders Not Working
- ✅ Check area meets minimum size for sensor
- ✅ Verify coordinates are valid
- ✅ Ensure sufficient account credits
- ✅ Check order status in "View Orders"

### Images Won't Load
- ✅ Check network connection
- ✅ Clear image cache (Settings → Clear Cache)
- ✅ Try refreshing the order list
- ✅ Verify order status is COMPLETED

---

## Getting Help

### Documentation
- 📖 [Technical Guide](ATAK_PLUGIN_DEVELOPMENT_GUIDE.md) - For developers
- 📋 [Beta Testing Guide](BETA_TESTING_GUIDE.md) - For testers
- 📊 [Changelog](CHANGELOG_V1_TO_V2.md) - What's new in v2

### Support Channels
- 🐛 [GitHub Issues](https://github.com/optisense/SkyFi-ATAK-Plugin-v2/issues) - Bug reports
- 💬 [SkyFi Support](https://skyfi.com/support) - Account and billing
- 📧 Email: support@skyfi.com

---

## Quick Reference Card

| Action | Steps |
|--------|-------|
| **Quick Task** | SkyFi icon → New Order from My Location |
| **Draw & Task** | Draw shape → Right-click → SkyFi → Task Satellite |
| **View Orders** | SkyFi icon → View Orders |
| **Cache Image** | Select image → Cache button |
| **Set API Key** | SkyFi icon → Set API Key |
| **Clear Cache** | SkyFi icon → Settings → Clear Cache |

---

**Ready to get started?** Install the plugin and task your first satellite! 🛰️

*Having issues? Check our [troubleshooting section](#troubleshooting) or [create an issue](https://github.com/optisense/SkyFi-ATAK-Plugin-v2/issues).*