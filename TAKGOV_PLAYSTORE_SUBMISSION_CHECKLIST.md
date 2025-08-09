# TAK.gov Play Store Submission Checklist

## Submission Package Information
- **Package Name**: `com.optisense.skyfi.atak`
- **Version**: 2.0-beta5
- **Target**: Google Play Store ATAK-CIV
- **Submission File**: `SkyFi-ATAK-Plugin-v2-TAKGOV-PLAYSTORE-20250808-111234.zip`

## Pre-Submission Verification

### Package Structure ✅
- [x] Package name is `com.optisense.skyfi.atak` throughout
- [x] No references to old package name `com.skyfi.atak.plugin`
- [x] Source code organized under correct package structure
- [x] All Java files use `package com.optisense.skyfi.atak`

### Build Configuration ✅
- [x] `app/build.gradle` configured for TAK.gov infrastructure
- [x] Namespace set to `com.optisense.skyfi.atak`
- [x] ApplicationId set to `com.optisense.skyfi.atak`
- [x] CIV flavor configured as default
- [x] No local SDK paths or dependencies
- [x] ProGuard rules included

### Play Store Compatibility ✅
- [x] Uses `SkyFiPlayStorePlugin` component (not IPlugin interface)
- [x] `plugin.xml` references Play Store compatible component
- [x] `AndroidManifest.xml` includes required activity for plugin discovery
- [x] No SDK-specific dependencies

### Clean Source ✅
- [x] No build artifacts (build/, .gradle/)
- [x] No keystore files
- [x] No backup files (.bak, .backup)
- [x] No class files
- [x] No IDE-specific files

### Documentation ✅
- [x] `README_TAKGOV.txt` with submission notes
- [x] `BUILD_INSTRUCTIONS.md` for TAK.gov build team
- [x] `VERSION.txt` with current version
- [x] Contact information provided

## Submission Instructions

### 1. Upload Package
Submit the ZIP file to TAK.gov through their official submission portal:
- File: `SkyFi-ATAK-Plugin-v2-TAKGOV-PLAYSTORE-20250808-111234.zip`
- Type: Source Code Submission
- Target: Google Play Store ATAK-CIV

### 2. Specify Requirements
In your submission notes, include:
```
Plugin Name: SkyFi ATAK Plugin v2
Package: com.optisense.skyfi.atak
Version: 2.0-beta5
Target Platform: Google Play Store ATAK-CIV
Signing Required: Yes - Play Store Compatible Signing
Build Flavor: CIV only
```

### 3. Important Notes for TAK.gov
Emphasize these points in your submission:
- This is for **Play Store distribution only**
- Package name changed to avoid conflicts: `com.optisense.skyfi.atak`
- Uses simplified plugin architecture without IPlugin interface
- All dependencies from standard Maven repositories
- Requires Play Store compatible signing

### 4. Expected Output
After TAK.gov processing, you should receive:
- Signed APK compatible with Play Store ATAK-CIV
- Build logs for verification
- Signature verification report

## Post-Submission Verification

Once you receive the signed APK from TAK.gov:

### 1. Verify Package Name
```bash
aapt dump badging signed-plugin.apk | grep package
# Should show: package: name='com.optisense.skyfi.atak'
```

### 2. Verify Signature
```bash
apksigner verify --verbose signed-plugin.apk
# Should show valid signature from TAK.gov
```

### 3. Test Installation
1. Install Play Store version of ATAK-CIV
2. Install the signed plugin APK
3. Launch ATAK and verify plugin loads
4. Check Tools > SkyFi menu appears

## Troubleshooting

### If Plugin Doesn't Load
1. Check ATAK logs for signature validation errors
2. Verify package name matches exactly
3. Ensure ATAK version compatibility (5.3.0 - 5.4.0.19)

### If Build Fails at TAK.gov
1. Check for missing dependencies
2. Verify gradle configuration
3. Ensure no hardcoded paths

### Common Issues
- **Package name mismatch**: Ensure all references use `com.optisense.skyfi.atak`
- **Signature validation**: Must be signed by TAK.gov for Play Store ATAK
- **Component not found**: Verify `SkyFiPlayStorePlugin` class exists

## Support Contacts

### TAK.gov Support
- Submission Portal: [TAK.gov submission system]
- Technical Issues: TAK.gov support team

### SkyFi/OptiSense Support
- Technical: engineering@optisense.com
- General: support@skyfi.com

## Revision History
- 2025-08-08: Initial Play Store submission package created
- Package name changed from `com.skyfi.atak.plugin` to `com.optisense.skyfi.atak`
- Simplified architecture for Play Store compatibility