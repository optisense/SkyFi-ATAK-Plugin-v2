# SkyFi ATAK Plugin

This is the SkyFi plugin for ATAK (Android Team Awareness Kit) version 5.5.0.

## Requirements

- ATAK 5.5.0 CIV installed on Android device
- Java 11 (for Gradle 6.9.1 compatibility)
- Android SDK with API level 30
- Android Build Tools 30.0.3

## Build Instructions

### Prerequisites

1. Install Java 11:
   ```bash
   # macOS with Homebrew
   brew install openjdk@11
   export JAVA_HOME=/opt/homebrew/opt/openjdk@11/libexec/openjdk.jdk/Contents/Home
   ```

2. Install Android SDK:
   ```bash
   # Install Android command line tools
   brew install --cask android-commandlinetools
   
   # Set up SDK
   export ANDROID_SDK_ROOT=~/Library/Android/sdk
   sdkmanager "platforms;android-30" "build-tools;30.0.3" "platform-tools"
   ```

3. Create local.properties (if not exists):
   ```bash
   echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties
   ```

### Building the Plugin

1. Clean build:
   ```bash
   ./gradlew clean
   ```

2. Build unsigned release APK:
   ```bash
   ./gradlew assembleCivRelease
   ```

3. Build debug APK (for testing):
   ```bash
   ./gradlew assembleCivDebug
   ```

The APK will be generated at:
- Release: `app/build/outputs/apk/civ/release/`
- Debug: `app/build/outputs/apk/civ/debug/`

## Known Issues

- **AAPT2 Compatibility**: The project uses Android Gradle Plugin 4.2.2 which may have AAPT2 compatibility issues on newer macOS systems (especially ARM64). Consider using:
  - Linux or Windows for building
  - Docker container with Android build tools
  - GitHub Actions for CI/CD builds

## Installation on Device

1. Enable "Unknown Sources" in Android settings
2. Copy APK to device
3. Install using file manager or ADB:
   ```bash
   adb install app/build/outputs/apk/civ/release/ATAK-Plugin-skyfi-atak-plugin-clean-2.0-debug-5.5.0-civ-release-unsigned.apk
   ```

## Project Structure

- `app/` - Main plugin source code
- `gradle/` - Gradle wrapper
- `atak-gradle-takdev.jar` - ATAK development plugin
- `local.properties` - Local SDK configuration (not in version control)

## Configuration

- Plugin Version: 2.0
- ATAK Version: 5.5.0
- Min SDK: 24 (Android 7.0)
- Target SDK: 30 (Android 11)
- Compile SDK: 30

## Development Notes

- The project uses Gradle 6.9.1 for ATAK compatibility
- Android Gradle Plugin 4.2.2 is required (newer versions are not compatible)
- R8 is disabled for ATAK plugin compatibility
- The release build is unsigned for TAK pipeline compatibility