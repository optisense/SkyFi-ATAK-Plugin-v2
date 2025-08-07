# Java Environment Setup for SkyFi ATAK Plugin

## Problem Solved
Fixed "Unable to locate a Java Runtime" error when building the SkyFi ATAK Plugin on macOS.

## Solution Summary
The issue was that while Java was installed via Homebrew, the `JAVA_HOME` environment variable was not properly configured. The ATAK plugin requires Java 11 specifically.

## Java Installation Status
- ✅ **OpenJDK 11**: `/opt/homebrew/Cellar/openjdk@11/11.0.28/libexec/openjdk.jdk/Contents/Home`
- ✅ **OpenJDK 17**: `/opt/homebrew/Cellar/openjdk@17/17.0.16/libexec/openjdk.jdk/Contents/Home`
- ✅ **Selected**: OpenJDK 11 (required for ATAK compatibility)

## Files Created

### 1. `setup-java-env.sh` (Executable)
Comprehensive setup script that:
- Checks for Java installations
- Sets up JAVA_HOME properly
- Verifies Gradle compatibility
- Provides usage instructions

**Usage:**
```bash
./setup-java-env.sh
```

### 2. `build-apk.sh` (Executable)
Convenient build script for different APK variants:
- **Debug**: `./build-apk.sh debug`
- **Release**: `./build-apk.sh release`
- **TAK.gov Unsigned**: `./build-apk.sh gov-unsigned` (default)
- **Clean**: `./build-apk.sh clean`

### 3. `export-java-env.sh`
Lightweight script to source Java environment:
```bash
source ./export-java-env.sh
```

### 4. `.envrc`
direnv configuration for automatic environment setup when entering the project directory.

### 5. `~/.zshrc`
System-wide shell configuration with:
- Automatic JAVA_HOME setup
- Convenient aliases:
  - `atak-java` - Check Java configuration
  - `atak-build` - Quick access to build script
  - `atak-setup` - Quick access to setup script

## Quick Start

### Option 1: Use the build script (Recommended)
```bash
# Build unsigned APK for TAK.gov submission
./build-apk.sh gov-unsigned

# Build debug APK for testing
./build-apk.sh debug
```

### Option 2: Manual build
```bash
# Set up environment
source ./export-java-env.sh

# Build for TAK.gov submission
./gradlew assembleGovUnsigned
```

### Option 3: Using system-wide configuration
```bash
# Restart your terminal or run:
source ~/.zshrc

# Now you can use aliases from anywhere:
atak-build gov-unsigned
```

## Build Outputs

APK files will be generated in:
- **Debug**: `app/build/outputs/apk/civ/debug/`
- **Release**: `app/build/outputs/apk/civ/release/`
- **Gov Unsigned**: `app/build/outputs/apk/gov/unsigned/`

## Troubleshooting

### If Java is not found:
```bash
# Install Java 11 via Homebrew
brew install openjdk@11

# Run setup to verify
./setup-java-env.sh
```

### If Gradle still fails:
```bash
# Stop any running Gradle daemons
./gradlew --stop

# Try building again
./build-apk.sh gov-unsigned
```

### Build warnings are normal
The build process shows warnings about remote TAK repositories that may not be accessible. These warnings are expected and don't affect the build process.

## For TAK.gov Submission

The unsigned APK for TAK.gov submission will be built with:
```bash
./build-apk.sh gov-unsigned
```

The APK will be located at: `app/build/outputs/apk/gov/unsigned/`

---

**Status**: ✅ Java environment successfully configured and tested
**Next Step**: Ready to build APK for TAK.gov submission