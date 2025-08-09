# SkyFi ATAK Plugin v2.0-beta5 - Play Store Deployment Guide

## Deployment Package Contents

This directory contains the following Play Store-ready artifacts:

### 1. Android App Bundle (Primary Upload)
- **File**: `ATAK-Plugin-SkyFi-ATAK-Plugin-v2-2.0-beta5--5.3.0-civ-release-SIGNED.aab`
- **Purpose**: Primary upload for Google Play Console
- **Signed with**: Play Store keystore (skyfi-playstore)
- **Size**: 5.7MB

### 2. APK (Testing/Backup)
- **File**: `ATAK-Plugin-SkyFi-ATAK-Plugin-v2-2.0-beta5--5.3.0-civ-release-SIGNED.apk`
- **Purpose**: Local testing and backup deployment
- **Signed with**: Play Store keystore (skyfi-playstore)
- **Size**: 6.3MB

## Google Play Console Upload Steps

### Step 1: Access Play Console
1. Navigate to [Google Play Console](https://play.google.com/console)
2. Sign in with SkyFi Google Play account
3. Select the SkyFi ATAK Plugin application

### Step 2: Create New Release
1. Go to **Release** → **Testing** or **Production**
2. Click **Create new release**
3. Choose **Android App Bundle** as upload method

### Step 3: Upload AAB File
1. Upload: `ATAK-Plugin-SkyFi-ATAK-Plugin-v2-2.0-beta5--5.3.0-civ-release-SIGNED.aab`
2. Google Play will automatically:
   - Validate the upload
   - Generate optimized APKs for different device configurations
   - Sign with Google Play App Signing key

### Step 4: Release Information
- **Version Name**: 2.0-beta5
- **Version Code**: 1
- **Release Notes**: See below

## Release Notes Template

```
SkyFi ATAK Plugin v2.0-beta5 Release

🎉 NEW FEATURES:
• Enhanced dark theme UI with modern Material Design
• Improved satellite imagery preview system
• Advanced ordering workflow with better error handling
• Real-time status updates for satellite pass predictions

🔧 IMPROVEMENTS:
• Optimized performance and memory usage
• Better compatibility with ATAK-CIV 5.3.0+
• Enhanced error messages and user feedback
• Improved network request handling

🐛 BUG FIXES:
• Fixed UI responsiveness issues
• Resolved crashes during image loading
• Fixed coordinate input validation
• Improved plugin loading stability

📋 COMPATIBILITY:
• ATAK-CIV 5.3.0 and higher
• Android API 21+ (Android 5.0+)
• Target API 33 (Android 13)
```

## Technical Specifications

### Package Information
- **Package Name**: com.skyfi.atak.plugin
- **Application ID**: com.skyfi.atak.plugin
- **Min SDK**: API 21 (Android 5.0)
- **Target SDK**: API 33 (Android 13)
- **Compile SDK**: API 33

### Signing Configuration
- **Keystore**: playstore-skyfi.keystore
- **Key Alias**: skyfi-playstore
- **Signature Scheme**: v1, v2, v3

### Permissions
- Network access for satellite data retrieval
- Storage access for image caching
- Location access for GPS coordinates
- Camera access for map overlays

## Verification Steps

### Pre-Upload Verification
✅ APK/AAB files are properly signed with Play Store key
✅ Package name matches existing Play Console app
✅ Version code increments from previous release
✅ All required permissions are declared
✅ App bundle size is within Play Store limits

### Post-Upload Verification
1. Check Google Play Console for upload success
2. Review generated APK variants
3. Test on internal track first
4. Verify crash reporting integration
5. Monitor Early Access feedback

## Rollback Plan

If issues are discovered after release:

1. **Immediate**: Use Play Console to halt rollout
2. **Short-term**: Rollback to previous stable version
3. **Long-term**: Address issues in next patch release

## Support Information

### Development Team
- **Primary Contact**: SkyFi Development Team
- **Email**: dev@skyfi.com
- **GitHub**: https://github.com/skyfi/atak-plugin

### User Support
- **Email**: support@skyfi.com
- **Documentation**: https://docs.skyfi.com/atak
- **Issues**: Report via Play Console or GitHub

## Security Notes

⚠️ **Important Security Information**:
- All APKs signed with production keystore
- No debug symbols included in release builds
- ProGuard/R8 code obfuscation enabled
- Dependencies scanned for vulnerabilities
- Compliant with Google Play security requirements

## Next Steps

1. Upload AAB to Google Play Console
2. Test on internal track
3. Promote to beta track for wider testing
4. Monitor crash reports and user feedback
5. Plan next iteration based on feedback

---

**Generated**: August 8, 2025
**Build Source**: TAK.gov official build system
**Signing**: Local signing with Play Store key