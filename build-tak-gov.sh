#!/bin/bash
# TAK.gov Optimized Build Script for SkyFi ATAK Plugin
# Addresses memory constraints and pod scheduling issues

set -e

echo "========================================="
echo "TAK.gov Build Script for SkyFi ATAK Plugin"
echo "========================================="

# Environment setup
export JAVA_HOME=${JAVA_HOME:-/usr/lib/jvm/java-17-openjdk-amd64}
export ANDROID_HOME=${ANDROID_HOME:-${PWD}/android-sdk}
export ANDROID_SDK_ROOT=${ANDROID_SDK_ROOT:-${ANDROID_HOME}}
export PATH=${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${PATH}

# Gradle memory optimization for constrained environments
export GRADLE_OPTS="-Xmx2g -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError -Dorg.gradle.daemon=false -Dorg.gradle.parallel=false -Dorg.gradle.caching=false"

echo "Java Version:"
java -version

echo ""
echo "Setting up Android SDK..."
if [ ! -d "${ANDROID_HOME}/cmdline-tools/latest" ]; then
    echo "Downloading Android command-line tools..."
    mkdir -p ${ANDROID_HOME}
    cd ${ANDROID_HOME}
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
    unzip -q commandlinetools-linux-9477386_latest.zip
    mkdir -p cmdline-tools/latest
    mv cmdline-tools/* cmdline-tools/latest/ 2>/dev/null || true
    rm commandlinetools-linux-9477386_latest.zip
    cd -
fi

echo "Accepting Android SDK licenses..."
yes | sdkmanager --licenses > /dev/null 2>&1 || true

echo "Installing required Android SDK components..."
sdkmanager "platform-tools" "platforms;android-33" "build-tools;33.0.0" > /dev/null

echo ""
echo "Creating local.properties..."
cat > local.properties << EOF
sdk.dir=${ANDROID_HOME}
takrepo.url=https://artifacts.tak.gov:443/artifactory/maven-release
takrepo.user=${TAKREPO_USER:-jfuginay}
takrepo.password=${TAKREPO_PASSWORD}
EOF

echo ""
echo "Checking Gradle wrapper..."
if [ ! -f "./gradlew" ]; then
    echo "ERROR: gradlew not found!"
    exit 1
fi
chmod +x ./gradlew

echo ""
echo "Gradle version:"
./gradlew --version | grep "Gradle" || true

echo ""
echo "========================================="
echo "Building SkyFi ATAK Plugin"
echo "========================================="

# Clean previous builds
echo "Cleaning previous builds..."
./gradlew clean --no-daemon --max-workers=2

# Build based on argument
BUILD_TYPE=${1:-debug}

case $BUILD_TYPE in
    debug)
        echo "Building DEBUG APK..."
        ./gradlew assembleCivDebug --stacktrace --no-daemon --max-workers=2
        echo ""
        echo "Build complete! Debug APK location:"
        ls -la app/build/outputs/apk/civ/debug/*.apk 2>/dev/null || echo "No debug APK found"
        ;;
    release)
        echo "Building RELEASE APK (unsigned)..."
        ./gradlew assembleCivRelease --stacktrace --no-daemon --max-workers=2
        echo ""
        echo "Build complete! Release APK location:"
        ls -la app/build/outputs/apk/civ/release/*.apk 2>/dev/null || echo "No release APK found"
        ;;
    all)
        echo "Building ALL variants..."
        ./gradlew assemble --stacktrace --no-daemon --max-workers=2
        echo ""
        echo "Build complete! All APKs:"
        find app/build/outputs/apk -name "*.apk" -type f
        ;;
    source-package)
        echo "Creating TAK.gov source submission package..."
        TIMESTAMP=$(date +%Y%m%d-%H%M%S)
        SUBMISSION_NAME="SkyFi-ATAK-Plugin-v2-TAKGOV-${TIMESTAMP}"
        mkdir -p build/submission/${SUBMISSION_NAME}
        
        # Copy source files, excluding build artifacts
        rsync -av \
            --exclude='.git*' \
            --exclude='build/' \
            --exclude='app/build/' \
            --exclude='*.apk' \
            --exclude='releases/' \
            --exclude='.gradle/' \
            --exclude='.idea/' \
            --exclude='android-sdk/' \
            --exclude='local.properties' \
            ./ build/submission/${SUBMISSION_NAME}/
        
        # Create README for TAK.gov
        cat > build/submission/${SUBMISSION_NAME}/TAK_GOV_BUILD_README.md << 'README'
# SkyFi ATAK Plugin v2 - TAK.gov Build Instructions

## Build Requirements
- Java JDK 17
- Gradle 8.10
- Android SDK (API 33)
- ATAK SDK 5.5.0

## Build Commands

### Setup
```bash
export JAVA_HOME=/path/to/jdk17
chmod +x gradlew
```

### Build Debug APK
```bash
./gradlew assembleCivDebug
```

### Build Release APK (for signing)
```bash
./gradlew assembleCivRelease
```

## Important Notes
- Plugin uses SkyFiPluginCompatWrapper for compatibility
- No IServiceController dependency
- Requires TAK.gov signing for production deployment
- Tested with ATAK-CIV 5.5.0

## TAK.gov Repository Access
Add to local.properties:
```
takrepo.url=https://artifacts.tak.gov:443/artifactory/maven-release
takrepo.user=YOUR_USERNAME
takrepo.password=YOUR_TOKEN
```
README
        
        cd build/submission
        zip -r ${SUBMISSION_NAME}.zip ${SUBMISSION_NAME}/
        echo ""
        echo "Source package created:"
        ls -lh ${SUBMISSION_NAME}.zip
        echo ""
        echo "This package is ready for TAK.gov submission for official building and signing."
        ;;
    *)
        echo "Usage: $0 [debug|release|all|source-package]"
        exit 1
        ;;
esac

echo ""
echo "========================================="
echo "Build Script Complete"
echo "========================================="