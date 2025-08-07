# SkyFi ATAK Plugin Installation Guide

## Overview

This guide provides step-by-step instructions for installing the SkyFi ATAK plugin on different ATAK versions and deployment scenarios. The SkyFi plugin brings satellite imagery ordering capabilities directly into your ATAK environment.

## Installation Methods Summary

| ATAK Version | Recommended Method | Alternative Method |
|-------------|------------------|-------------------|
| **ATAK SDK** (Development/TAK.gov) | Standard Plugin APK | N/A |
| **Play Store ATAK** | Companion App | Play Store Compatible APK |
| **Military ATAK** | Standard Plugin APK | TAK.gov Submission |

## Prerequisites

### General Requirements
- Android 5.0 (API 21) or higher
- ATAK installed and functional
- Internet connection for satellite imagery
- Storage permissions for image downloads
- Location permissions for mapping

### Developer Options (For APK Installation)
1. Enable **Developer Options** in Android Settings
2. Enable **USB Debugging** (if installing via ADB)
3. Enable **Install from Unknown Sources** (Android < 8.0)
4. Enable **Install Unknown Apps** for your file manager (Android 8.0+)

---

## Method 1: Standard Plugin Installation (SDK ATAK)

### Supported ATAK Versions
- ATAK SDK builds
- TAK.gov distributed builds
- Development/testing installations

### Installation Steps

#### Option A: ADB Installation (Recommended)
1. **Download the SDK-compatible APK**:
   - File: `sdk-compatible-ATAK-Plugin-SkyFi-ATAK-Plugin-v2-*.apk`
   - Location: From SkyFi releases or build output

2. **Connect your device**:
   ```bash
   adb devices
   # Verify your device is listed
   ```

3. **Install the plugin**:
   ```bash
   adb install -r sdk-compatible-ATAK-Plugin-*.apk
   ```

4. **Verify installation**:
   - Launch ATAK
   - Check Plugin Manager (Tools → Plugin Manager)
   - Look for "SkyFi" in the plugin list
   - Plugin should show as "Loaded" or "Active"

#### Option B: Manual APK Installation
1. **Transfer APK to device**:
   - Copy APK file to device storage
   - Use file manager app to locate the APK

2. **Install the APK**:
   - Tap the APK file in file manager
   - Follow Android installation prompts
   - Grant required permissions

3. **Launch ATAK and verify**:
   - Open ATAK application
   - Check Tools → Plugin Manager
   - Confirm SkyFi plugin is loaded

### Troubleshooting SDK Installation
- **Plugin not loading**: Check ATAK logs for signature errors
- **Permission denied**: Verify "Install from Unknown Sources" is enabled
- **Installation failed**: Try uninstalling previous versions first

---

## Method 2: Companion App Installation (Play Store ATAK)

### Why Use the Companion App?
Play Store ATAK has strict signature validation that may prevent traditional plugins from loading. The companion app provides full SkyFi functionality with seamless ATAK integration.

### Installation Steps

#### Step 1: Install SkyFi Companion App
1. **From Google Play Store** (Recommended):
   - Search for "SkyFi ATAK Companion"
   - Install the official app

2. **Manual Installation**:
   - Download: `skyfi-atak-companion-playstore-release.apk`
   - Install following standard APK installation process

#### Step 2: Initial Setup
1. **Launch the companion app**
2. **Grant permissions** when prompted:
   - Location access
   - Storage access
   - Phone access (for device ID)

3. **Sign in to your SkyFi account**:
   - Enter your SkyFi credentials
   - Or create a new account if needed

#### Step 3: Configure ATAK Integration
1. **In companion app settings**:
   - Enable "ATAK Integration"
   - Select your ATAK version
   - Configure communication preferences

2. **Test ATAK detection**:
   - Launch ATAK
   - Return to companion app
   - Verify ATAK is detected

#### Step 4: Using the Integration
1. **From ATAK**:
   - Long-press on map location
   - Select "SkyFi Imagery" from context menu
   - This launches the companion app with location

2. **From Companion App**:
   - Browse satellite imagery independently
   - Send imagery to ATAK via CoT messages
   - Manage orders and downloads

### Companion App Features
- **Full SkyFi functionality** without ATAK plugin limitations
- **Bidirectional communication** with ATAK via CoT messages
- **Location synchronization** between apps
- **Offline mode** for areas with limited connectivity
- **Order management** and history
- **Account management** and preferences

---

## Method 3: Play Store Compatible Plugin (Advanced)

### Experimental Approach
This method attempts to use a specially-signed APK that may be compatible with Play Store ATAK. Success is not guaranteed due to signature validation.

### Installation Steps
1. **Download Play Store compatible APK**:
   - File: `playstore-compatible-ATAK-Plugin-*.apk`

2. **Install using ADB**:
   ```bash
   adb install -r playstore-compatible-ATAK-Plugin-*.apk
   ```

3. **Test plugin loading**:
   - Launch ATAK
   - Check Plugin Manager
   - Monitor for signature validation errors

### If This Method Fails
- **Signature errors**: Use companion app instead
- **Plugin not recognized**: Try clearing ATAK cache
- **Installation issues**: Verify APK integrity

---

## Method 4: Enterprise/Military Deployment

### For System Administrators
Enterprise and military deployments may have additional requirements and capabilities.

### TAK.gov Submission Process
1. **Official plugin signing**:
   - Submit source code to TAK.gov
   - TAK.gov builds and signs plugin
   - Distributed through official channels

2. **Enterprise management**:
   - Deploy via MDM solutions
   - Configure enterprise settings
   - Manage user accounts centrally

### Custom Keystore Deployment
For organizations with custom signing certificates:
1. **Build with organization keystore**
2. **Deploy via enterprise app store**
3. **Configure ATAK to accept organization certificates**

---

## Post-Installation Configuration

### First Launch Setup
1. **SkyFi Account**:
   - Sign in with existing account
   - Or create new account at https://skyfi.com

2. **API Configuration**:
   - Default API endpoint should work
   - Custom endpoints available for enterprise users

3. **Preferences**:
   - Image quality settings
   - Download preferences
   - Offline mode configuration

### ATAK Integration Settings
1. **Plugin Settings** (Traditional Plugin):
   - Access via ATAK Plugin Manager
   - Configure imagery sources
   - Set download locations

2. **Companion App Settings**:
   - ATAK communication preferences
   - CoT message configuration
   - Location sharing settings

---

## Using the SkyFi Plugin

### Basic Workflow
1. **Select area of interest** in ATAK
2. **Access SkyFi functionality**:
   - Plugin: Via menu or toolbar
   - Companion: Via context menu or app
3. **Browse available imagery**
4. **Place satellite imagery orders**
5. **Download and view imagery** in ATAK

### Advanced Features
- **Area-based ordering** using ATAK shapes
- **Multi-spectral imagery** options
- **Historical imagery** browsing
- **Order management** and tracking
- **Collaborative sharing** with team members

---

## Troubleshooting

### Common Issues

#### Plugin Not Loading
**Symptoms**: Plugin doesn't appear in ATAK Plugin Manager

**Solutions**:
1. Check ATAK version compatibility
2. Verify APK signature matches ATAK requirements
3. Clear ATAK cache and restart
4. Try companion app approach

#### Signature Verification Errors
**Symptoms**: "Package not signed correctly" or similar errors

**Solutions**:
1. Use companion app instead of plugin
2. Try different APK variant
3. Check if ATAK accepts third-party plugins

#### Connection Issues
**Symptoms**: Cannot connect to SkyFi services

**Solutions**:
1. Verify internet connection
2. Check firewall/proxy settings
3. Validate SkyFi account credentials
4. Try different network environment

#### Permission Problems
**Symptoms**: App crashes or features don't work

**Solutions**:
1. Grant all requested permissions in Android Settings
2. Check location services are enabled
3. Verify storage permissions
4. Restart both ATAK and SkyFi app

### Getting Help

#### Diagnostic Information
When reporting issues, please include:
- ATAK version and source (SDK/Play Store/etc.)
- Android version and device model
- SkyFi plugin/app version
- Error messages from ATAK logs
- Steps to reproduce the issue

#### Support Channels
- **Documentation**: https://docs.skyfi.com/atak
- **Email Support**: support@skyfi.com
- **GitHub Issues**: https://github.com/skyfi/atak-plugin/issues
- **Community Forum**: https://community.skyfi.com

#### Log Collection
For technical support, collect logs using:
```bash
adb logcat -d > atak-skyfi-logs.txt
```
Send logs with your support request.

---

## Frequently Asked Questions

### Q: Which installation method should I use?
**A**: Use the standard plugin for SDK ATAK, and the companion app for Play Store ATAK.

### Q: Can I use both the plugin and companion app?
**A**: Yes, but it's not necessary. Choose the method that works best for your ATAK version.

### Q: Do I need a SkyFi account?
**A**: Yes, a SkyFi account is required to order and download satellite imagery.

### Q: Is the plugin free?
**A**: The plugin itself is free, but satellite imagery orders are charged based on usage.

### Q: Can I use this offline?
**A**: Limited offline functionality is available for previously downloaded imagery.

### Q: Is my data secure?
**A**: Yes, all data transmission is encrypted and follows industry security standards.

---

## Version Compatibility Matrix

| ATAK Version | Plugin Support | Companion App | Notes |
|-------------|---------------|---------------|-------|
| 4.10.x SDK | ✅ Full | ✅ Yes | Recommended: Plugin |
| 4.9.x SDK | ✅ Full | ✅ Yes | Recommended: Plugin |
| 5.x Play Store | ⚠️ Limited | ✅ Yes | Recommended: Companion |
| 4.x Play Store | ❌ No | ✅ Yes | Use: Companion only |
| Military builds | ✅ Full* | ✅ Yes | *Subject to approval |

### Legend
- ✅ Full support and testing
- ⚠️ Limited or experimental support
- ❌ Not supported or not tested

---

## Additional Resources

### Documentation
- [SkyFi API Documentation](https://docs.skyfi.com/api)
- [ATAK Plugin Development Guide](https://docs.skyfi.com/atak/development)
- [Companion App User Guide](https://docs.skyfi.com/atak/companion)

### Video Tutorials
- [Plugin Installation Walkthrough](https://videos.skyfi.com/atak-install)
- [Using SkyFi with ATAK](https://videos.skyfi.com/atak-usage)
- [Troubleshooting Common Issues](https://videos.skyfi.com/atak-troubleshoot)

### Community
- [SkyFi Community Forum](https://community.skyfi.com)
- [ATAK User Groups](https://community.skyfi.com/atak)
- [Feature Requests](https://feedback.skyfi.com)

---

**Last Updated**: August 2025  
**Version**: 2.0-beta3  
**Contact**: support@skyfi.com