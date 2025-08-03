# SkyFi ATAK Plugin v2

[![Build and Release](https://github.com/optisense/SkyFi-ATAK-Plugin-v2/actions/workflows/build-release.yml/badge.svg)](https://github.com/optisense/SkyFi-ATAK-Plugin-v2/actions/workflows/build-release.yml)
[![PR Validation](https://github.com/optisense/SkyFi-ATAK-Plugin-v2/actions/workflows/pr-validation.yml/badge.svg)](https://github.com/optisense/SkyFi-ATAK-Plugin-v2/actions/workflows/pr-validation.yml)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/optisense/SkyFi-ATAK-Plugin-v2)](https://github.com/optisense/SkyFi-ATAK-Plugin-v2/releases)

_________________________________________________________________
## PURPOSE AND CAPABILITIES

SkyFi ATAK Plugin v2 integrates satellite imagery and geospatial intelligence capabilities directly into the Android Team Awareness Kit (ATAK). This plugin enables Space Force and military personnel to access real-time satellite data, perform mission planning, and enhance situational awareness through advanced geospatial tools.

### Key Features
- Real-time satellite imagery integration
- Mission planning and analysis tools
- Geospatial intelligence overlays
- Integration with ATAK's native mapping capabilities
- Priority tasking support (WHEN_AVAILABLE and PRIORITY)

_________________________________________________________________
## STATUS

**Current Status**: In Development - Beta Release  
**Target Release**: Space Force deployment  
**Version**: 2.0  
**ATAK Compatibility**: 5.4.0 (CIV and MIL variants)

### Recent Updates
- Removed NATSEC priority functionality (not yet available)
- Updated tasking order UI with radio button priority selection
- GitHub Actions CI/CD pipeline implemented
- Automated build and release process
- Multi-flavor support (CIV/MIL)
- Comprehensive testing framework

_________________________________________________________________
## 🚀 BETA TESTING ON ANDROID PHONES

### Quick Start for Beta Testers

#### Option 1: Download Pre-built APK (Recommended)
1. On your Android phone, download the latest beta APK from:
   - [GitHub Releases Page](https://github.com/optisense/SkyFi-ATAK-Plugin-v2/releases)
   - Download the appropriate APK file (e.g., `skyfi-atak-plugin-v2.0-beta-civ-debug.apk`)

#### Option 2: Install via ADB
If you have ADB (Android Debug Bridge) set up:
```bash
# Connect your phone via USB with debugging enabled
adb install -r skyfi-atak-plugin-v2.0-beta-civ-debug.apk
```

### Prerequisites for Beta Testing
1. **ATAK 5.4.0 CIV** must be installed on your Android device
   - Download from [tak.gov](https://tak.gov) (requires registration)
2. **Android Version**: Minimum Android 5.0 (API 21)
3. **Enable Installation from Unknown Sources**:
   - Go to Settings → Security
   - Enable "Unknown sources" or "Install unknown apps"
   - For Android 8.0+: Settings → Apps & notifications → Advanced → Special app access → Install unknown apps

### Installation Steps
1. **Download the APK** to your Android phone
2. **Open the APK file** using your file manager
3. **Tap "Install"** when prompted
4. **Accept any security prompts**
5. **Open ATAK** after installation
6. The SkyFi plugin will load automatically

### Testing the Plugin
1. **Access the Plugin**: In ATAK, tap the menu → Tools → SkyFi
2. **Login**: Use your SkyFi credentials
3. **Test Features**:
   - Draw an Area of Interest (AOI) on the map
   - Create a tasking order
   - Test priority selection (WHEN_AVAILABLE vs PRIORITY)
   - Browse satellite imagery archives
   - Check order status

### Reporting Beta Issues
Please report any issues or feedback:
- [GitHub Issues](https://github.com/optisense/SkyFi-ATAK-Plugin-v2/issues)
- Include:
  - Android version
  - ATAK version
  - Steps to reproduce
  - Screenshots if applicable

_________________________________________________________________
## POINT OF CONTACTS

**Development Team**: Space Force Development Team  
**Repository**: https://github.com/optisense/SkyFi-ATAK-Plugin-v2  
**Issues**: Please use GitHub Issues for bug reports and feature requests

_________________________________________________________________
## PORTS REQUIRED

The following network ports and protocols are required for plugin operation:

- **HTTPS (443)**: Secure communication with SkyFi services
- **HTTP (80)**: Fallback communication (if configured)
- **Custom APIs**: As configured in plugin settings

**Security Note**: All communications use encrypted channels and follow Space Force security protocols.

_________________________________________________________________
## EQUIPMENT REQUIRED

### Development Environment
- **Android Studio**: Latest stable version
- **Java**: OpenJDK 11
- **Android SDK**: API level 33 (with backwards compatibility to API 21)
- **ATAK SDK**: 5.4.0.18-SDK (included in repository)

### Target Devices
- Android devices running ATAK 5.4.0+
- Minimum Android API level 21
- Recommended: 4GB+ RAM, GPS capability

_________________________________________________________________
## EQUIPMENT SUPPORTED

### ATAK Versions
- **ATAK-CIV**: Civilian release builds
- **ATAK-MIL**: Military release builds
- **ATAK-GOV**: Government release builds (future support)

### Android Architectures
- ARM 32-bit (armeabi-v7a)
- ARM 64-bit (arm64-v8a)
- x86 (for development/testing)

_________________________________________________________________
## COMPILATION

### Quick Build (Local Development)
```bash
# Fast local build with debug signing
./build-plugin-quick.sh
```

### Manual Build Process
```bash
# Set up environment
export JAVA_HOME=/path/to/java-11
export ANDROID_HOME=/path/to/android-sdk

# Build specific variants
./gradlew assembleCivDebug      # CIV debug build
./gradlew assembleCivRelease    # CIV release build
./gradlew assembleMilDebug      # MIL debug build
./gradlew assembleMilRelease    # MIL release build

# Build all variants
./gradlew assembleDebug         # All debug builds
./gradlew assembleRelease       # All release builds
```

### CI/CD Pipeline
The project uses GitHub Actions for automated builds:

- **Pull Request Validation**: Triggered on PRs to main/develop
- **Release Builds**: Triggered on version tags (v*)
- **Manual Builds**: Available via workflow dispatch

### Build Artifacts
Built APKs are available in:
- Local builds: `app/build/outputs/apk/`
- CI builds: GitHub Actions artifacts
- Releases: GitHub Releases page

### Installation
```bash
# Install via ADB
adb install -r path/to/skyfi-plugin.apk

# Or use the quick install script
./quick-install.sh
```

_________________________________________________________________
## DEVELOPER NOTES

### Project Structure
- `/app/src/main/`: Main application source code
- `/app/src/gov/`: Government-specific source sets
- `/sdk/`: ATAK SDK dependencies
- `/.github/workflows/`: CI/CD pipeline definitions

### Key Configuration Files
- `build.gradle`: Main build configuration
- `gradle.properties`: Build properties and settings
- `local.properties`: Local development settings (not committed)

### Development Guidelines
1. Follow Android development best practices
2. Maintain compatibility with specified ATAK versions
3. Test on both CIV and MIL build variants
4. Use the provided CI/CD pipeline for testing
5. Create feature branches and use pull requests

### Security Considerations
- Debug builds use development certificates only
- Production builds require proper signing certificates
- All network communications use HTTPS
- Follow Space Force security protocols

### Testing
```bash
# Run unit tests
./gradlew test

# Run lint checks
./gradlew lint

# Generate test reports
./gradlew connectedAndroidTest
```

### Troubleshooting
- Check GitHub Actions logs for build failures
- Verify ATAK SDK dependencies are properly configured
- Ensure proper Java version (11) is being used
- Review ProGuard configuration for release builds

For additional support, please check the [ATAK Plugin Development Guide](ATAK_PLUGIN_DEVELOPMENT_GUIDE.md) or create an issue on GitHub.
