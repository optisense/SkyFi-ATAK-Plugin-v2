# SkyFi ATAK Plugin - Quick Setup Guide

## Quick Start (Choose Your Path)

### Path A: SDK ATAK Users (Developers/TAK.gov)
```bash
# 1. Install plugin APK
adb install -r sdk-compatible-ATAK-Plugin-*.apk

# 2. Launch ATAK and verify plugin loads
# 3. Sign in to SkyFi account
# 4. Start ordering satellite imagery!
```

### Path B: Play Store ATAK Users
```bash
# 1. Install companion app
# Option 1: Google Play Store → "SkyFi ATAK Companion" 
# Option 2: adb install -r skyfi-atak-companion-*.apk

# 2. Launch companion app and sign in
# 3. Enable ATAK integration in settings  
# 4. Use from ATAK context menu or standalone
```

## Installation Commands

### Build All Variants
```bash
cd SkyFi-ATAK-Plugin-v2
./gradlew buildAllVariants
```

### Test Compatibility
```bash
./scripts/atak-compatibility-tester.sh
```

### Convert TAK.gov AAB to APK
```bash
./scripts/aab-to-apk-converter.sh /path/to/plugin.aab
```

### Create Play Store Keystore
```bash
./scripts/create-playstore-keystore.sh
```

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Plugin won't load | Try companion app |
| Signature error | Use matching APK variant |
| No ATAK detected | Check companion app settings |
| Permission denied | Enable "Unknown sources" |

## Need Help?

- **Full Guide**: [ATAK_INSTALLATION_GUIDE.md](./ATAK_INSTALLATION_GUIDE.md)
- **Support**: support@skyfi.com
- **Logs**: `adb logcat -d > skyfi-logs.txt`

---
**Choose Path A for SDK ATAK, Path B for Play Store ATAK**