# SkyFi ATAK Plugin

[![Build ATAK Plugin](https://github.com/jfuginay/skyfi-atak-plugin/actions/workflows/build.yml/badge.svg)](https://github.com/jfuginay/skyfi-atak-plugin/actions/workflows/build.yml)

This is the SkyFi plugin for ATAK (Android Team Awareness Kit) version 5.5.0. The plugin enables satellite imagery integration directly within ATAK for enhanced situational awareness.

## 🚀 Quick Start

### Download Pre-built APKs
If you don't want to build from source, you can download the latest APKs from:
- [GitHub Actions Artifacts](https://github.com/jfuginay/skyfi-atak-plugin/actions) (requires GitHub login)
- Check the latest successful build and download the artifact

### Build Status
The project uses GitHub Actions for continuous integration. Every push to main automatically builds both debug and release APKs on Ubuntu Linux, avoiding macOS compatibility issues.

## 📋 Requirements

- ATAK 5.5.0 CIV installed on Android device
- Java 11 (for local builds - Gradle 6.9.1 compatibility)
- Android SDK with API level 30
- Android Build Tools 30.0.3

## 🔨 Build Instructions

### Prerequisites

1. **Install Java 11:**
   ```bash
   # macOS with Homebrew
   brew install openjdk@11
   export JAVA_HOME=/opt/homebrew/opt/openjdk@11/libexec/openjdk.jdk/Contents/Home
   
   # Linux
   sudo apt-get install openjdk-11-jdk
   export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
   ```

2. **Install Android SDK:**
   ```bash
   # macOS
   brew install --cask android-commandlinetools
   
   # Linux
   wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
   unzip commandlinetools-linux-9477386_latest.zip
   
   # Set up SDK (all platforms)
   export ANDROID_SDK_ROOT=~/Library/Android/sdk  # or your preferred location
   sdkmanager "platforms;android-30" "build-tools;30.0.3" "platform-tools"
   ```

3. **Clone and Configure:**
   ```bash
   git clone https://github.com/jfuginay/skyfi-atak-plugin.git
   cd skyfi-atak-plugin
   
   # Create local.properties
   echo "sdk.dir=$ANDROID_SDK_ROOT" > local.properties
   
   # Copy gradle properties
   cp gradle.properties.example gradle.properties
   ```

### Building the Plugin

```bash
# Clean previous builds
./gradlew clean

# Build unsigned release APK
./gradlew assembleCivRelease

# Build debug APK (for testing)
./gradlew assembleCivDebug
```

The APKs will be generated at:
- **Release**: `app/build/outputs/apk/civ/release/ATAK-Plugin-skyfi-atak-plugin-clean-2.0-debug-5.5.0-civ-release-unsigned.apk`
- **Debug**: `app/build/outputs/apk/civ/debug/ATAK-Plugin-skyfi-atak-plugin-clean-2.0-debug-5.5.0-civ-debug.apk`

## 📱 Installation on Device

1. **Enable Developer Options:**
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
   - Enable "Install from Unknown Sources"

2. **Install via ADB:**
   ```bash
   # For release build
   adb install app/build/outputs/apk/civ/release/*-release-unsigned.apk
   
   # For debug build
   adb install app/build/outputs/apk/civ/debug/*-debug.apk
   ```

3. **Manual Installation:**
   - Copy APK to device
   - Use file manager to install
   - Accept security prompts

## 🐛 Known Issues

### Build Issues
- **AAPT2 Compatibility**: Android Gradle Plugin 4.2.2 has AAPT2 compatibility issues on macOS (especially ARM64/M1/M2)
  - ✅ **Solution**: Use GitHub Actions (automated builds on Linux)
  - ✅ **Alternative**: Build on Linux or Windows
  - ✅ **Docker**: Use Android build container

### Runtime Issues
- Ensure ATAK 5.5.0 is installed before plugin
- Plugin requires minimum Android 7.0 (API 24)

## 🏗️ Project Structure

```
skyfi-atak-plugin/
├── app/                          # Main plugin source code
│   ├── src/main/java/           # Java source files
│   ├── src/main/res/            # Resources (layouts, drawables, etc.)
│   ├── src/test/                # Unit tests
│   └── build.gradle             # App-level build configuration
├── gradle/                       # Gradle wrapper
├── .github/workflows/           # GitHub Actions CI/CD
├── atak-gradle-takdev.jar       # ATAK development plugin
├── local.properties             # Local SDK path (not in git)
├── gradle.properties            # Gradle settings
└── README.md                    # This file
```

## ⚙️ Configuration

| Setting | Value | Description |
|---------|-------|-------------|
| Plugin Version | 2.0 | SkyFi plugin version |
| ATAK Version | 5.5.0 | Compatible ATAK version |
| Min SDK | 24 | Android 7.0 (Nougat) |
| Target SDK | 30 | Android 11 |
| Compile SDK | 30 | Build tools target |
| Gradle | 6.9.1 | Required for ATAK compatibility |
| AGP | 4.2.2 | Android Gradle Plugin version |

## 🔧 Development Notes

### Build System
- Uses Gradle 6.9.1 for ATAK compatibility
- Android Gradle Plugin 4.2.2 (newer versions incompatible)
- R8 disabled for ATAK plugin compatibility
- ProGuard configured for code optimization

### ATAK Integration
- Implements `IPlugin` interface
- Extends `DropDownMapComponent`
- Integrates with ATAK's map and toolbar systems
- Uses ATAK's coordinate systems and map overlays

### Key Features
- Satellite imagery ordering integration
- Area of Interest (AOI) polygon drawing
- Archive browsing and ordering
- User profile management
- Real-time order status tracking

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is part of the SkyFi ATAK integration suite. For licensing information, please contact the SkyFi team.

## 🆘 Troubleshooting

### Build Failures
1. **Java Version**: Ensure Java 11 is being used
   ```bash
   java -version  # Should show 11.x.x
   ```

2. **Android SDK**: Verify SDK installation
   ```bash
   ls $ANDROID_SDK_ROOT/platforms/android-30
   ```

3. **Clean Build**: Try a clean build
   ```bash
   ./gradlew clean
   rm -rf ~/.gradle/caches/
   ./gradlew assembleCivDebug --refresh-dependencies
   ```

### Plugin Not Loading in ATAK
1. Verify ATAK version is 5.5.0
2. Check plugin permissions in ATAK settings
3. Review ATAK logs for loading errors
4. Ensure device has sufficient storage

## 📞 Support

- **GitHub Issues**: [Report bugs or request features](https://github.com/jfuginay/skyfi-atak-plugin/issues)
- **Documentation**: Check the `/docs` folder for additional guides
- **ATAK Forums**: For ATAK-specific questions

---

**Note**: This plugin requires a valid SkyFi account for satellite imagery access. Contact SkyFi for account setup and API credentials.