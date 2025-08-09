# TAK.gov Official Submission Package - SkyFi ATAK Plugin v2.0-beta5

## ✅ Submission Compliance Status

This package has been prepared according to official TAK.gov requirements for ATAK plugin submission.

### Package Information
- **Plugin Name**: SkyFi ATAK Plugin
- **Version**: 2.0-beta5
- **Package ID**: com.skyfi.atak.plugin
- **Target ATAK**: 5.3.0 - 5.4.0.19
- **Submission Date**: 2025-08-08

## 📋 TAK.gov Requirements Checklist

### ✅ Build System Requirements
- [x] **Gradle Build System**: Using Gradle 7.5 with Android Gradle Plugin 7.3.1
- [x] **assembleCivRelease Target**: Defined and functional
- [x] **atak-gradle-takdev Plugin**: Version 2.+ configured
- [x] **TAK Repository**: Configured to use `https://artifacts.tak.gov/artifactory/maven`

### ✅ Source Code Structure
- [x] **Single Root Folder**: Package contains `SkyFi-ATAK-Plugin-v2` as root
- [x] **Folder Name**: Will become APK name after TAK.gov build
- [x] **Clean Source**: No build artifacts, IDE files, or local dependencies

### ✅ Configuration Requirements
- [x] **ProGuard**: Repackaging uses `SkyFiATAKPlugin` (not "PluginTemplate")
- [x] **Discovery Activity**: AndroidManifest.xml contains required discovery activity
- [x] **No Local SDK**: All SDK dependencies fetched from TAK.gov repository
- [x] **No Hardcoded Credentials**: Uses command-line parameters for authentication

## 🚀 Build Instructions for TAK.gov

### Official Build Command
```bash
./gradlew -Ptakrepo.force=true \
         -Ptakrepo.url=https://artifacts.tak.gov/artifactory/maven \
         -Ptakrepo.user=<TAK_USERNAME> \
         -Ptakrepo.password=<TAK_PASSWORD> \
         assembleCivRelease
```

### Expected Output
- **APK Location**: `app/build/outputs/apk/civ/release/`
- **APK Name**: `ATAK-Plugin-SkyFi-ATAK-Plugin-v2-2.0-beta5--5.3.0-civ-release.apk`

## 📦 Submission Package Contents

```
SkyFi-ATAK-Plugin-v2/
├── app/
│   ├── build.gradle           # App-level build configuration
│   ├── proguard-gradle.txt    # ProGuard rules
│   ├── proguard-gradle-repackage.txt  # Repackaging configuration
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml  # Plugin manifest with discovery
│           ├── assets/              # Plugin resources
│           ├── java/               # Source code
│           └── res/               # Android resources
├── build.gradle               # Root build configuration
├── gradle.properties          # Gradle properties
├── gradle/                    # Gradle wrapper
├── gradlew                    # Unix build script
├── gradlew.bat               # Windows build script
├── settings.gradle           # Project settings
└── README_TAKGOV.txt         # TAK.gov specific instructions
```

## 🔒 Security & Compliance

### Code Security
- No hardcoded API keys or credentials
- All sensitive data handled through secure preferences
- Network communications use HTTPS exclusively
- ProGuard obfuscation enabled for release builds

### TAK.gov Signing
- Plugin will be signed by TAK.gov with official keystore
- Debug keystore included for local testing only
- Production signing handled entirely by TAK.gov

## 🧪 Testing & Validation

### Pre-submission Testing
1. **Local Build Test** (without TAK credentials):
   ```bash
   ./gradlew clean assembleDebug
   ```

2. **TAK.gov Repository Test** (with credentials):
   ```bash
   ./test-with-credentials.sh <username> <password>
   ```

3. **Validation Script**:
   ```bash
   ./validate-takgov-build.sh
   ```

### Post-submission Testing
After TAK.gov builds and signs the plugin:
1. Download signed APK from TAK.gov
2. Install on ATAK device: `adb install <signed-apk>`
3. Verify plugin loads and functions correctly

## 📝 Plugin Description

The SkyFi ATAK Plugin enables satellite imagery ordering and viewing directly within ATAK. 

### Key Features
- **Direct Satellite Tasking**: Order new satellite imagery from within ATAK
- **Archive Search**: Browse and order existing satellite imagery
- **COG Support**: View Cloud Optimized GeoTIFF files directly on map
- **Shape Selection**: Use existing ATAK shapes to define Areas of Interest
- **Real-time Updates**: Track order status and receive notifications
- **Offline Capability**: Cache imagery for offline use

### Technical Capabilities
- Integrates with SkyFi satellite constellation API
- Supports multiple imagery formats (GeoTIFF, COG, JPEG2000)
- Automatic georeferencing and map overlay
- Optimized for low-bandwidth environments
- Compatible with ATAK 5.3.0 through 5.4.0.19

## 🛠️ Troubleshooting

### Common Build Issues

1. **SDK Not Found**:
   - Ensure TAK.gov credentials are correct
   - Check network connectivity to artifacts.tak.gov
   - Verify repository URL is correct

2. **ProGuard Errors**:
   - Check proguard-gradle.txt for syntax errors
   - Ensure all keep rules are properly defined

3. **Signing Issues**:
   - For local testing, ensure android_keystore exists
   - Production signing handled by TAK.gov only

## 📞 Support Contact

**Developer**: Optisense Inc. (DBA SkyFi)
- **Email**: support@skyfi.com
- **Website**: https://skyfi.com
- **GitHub**: https://github.com/skyfi

## 🔄 Version History

### v2.0-beta5 (Current)
- Full TAK.gov compliance
- Enhanced COG support
- Improved shape selection
- Dark theme support
- Performance optimizations

### Previous Versions
- v2.0-beta4: Added AI capabilities
- v2.0-beta3: UI overhaul
- v2.0-beta2: Archive search feature
- v2.0-beta1: Initial release

## ⚖️ License

This plugin is proprietary software of Optisense Inc. Distribution is controlled through TAK.gov official channels only.

---

## ✅ Final Submission Checklist

Before submitting to TAK.gov, verify:

- [ ] All files in submission package
- [ ] No local SDK references
- [ ] ProGuard configured correctly
- [ ] Discovery activity present
- [ ] Build tested with TAK credentials
- [ ] No sensitive information in source
- [ ] README_TAKGOV.txt included
- [ ] Version numbers consistent
- [ ] Package structure correct
- [ ] Gradle wrapper functional

---

**Submission Ready**: This package is ready for upload to TAK.gov submission portal.