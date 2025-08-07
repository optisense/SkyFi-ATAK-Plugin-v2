#!/bin/bash

# SkyFi ATAK Plugin APK Build Script
# This script builds the APK with proper Java environment setup

set -e  # Exit on any error

echo "=== SkyFi ATAK Plugin APK Build ==="

# Set up Java environment
JAVA_HOME="/opt/homebrew/Cellar/openjdk@11/11.0.28/libexec/openjdk.jdk/Contents/Home"

if [ ! -d "$JAVA_HOME" ]; then
    echo "❌ ERROR: Java 11 not found at $JAVA_HOME"
    echo "Please run ./setup-java-env.sh first"
    exit 1
fi

export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"

echo "Using Java Home: $JAVA_HOME"
java -version

echo ""
echo "Starting build process..."

# Build variants for different use cases
case "${1:-gov-unsigned}" in
    "debug")
        echo "Building debug APK..."
        ./gradlew assembleCivDebug
        echo ""
        echo "✅ Debug APK built successfully!"
        echo "Location: app/build/outputs/apk/civ/debug/"
        ;;
    "release")
        echo "Building release APK (signed)..."
        ./gradlew assembleCivRelease
        echo ""
        echo "✅ Release APK built successfully!"
        echo "Location: app/build/outputs/apk/civ/release/"
        ;;
    "gov-unsigned")
        echo "Building unsigned APK for TAK.gov submission..."
        ./gradlew assembleGovUnsigned
        echo ""
        echo "✅ Unsigned APK for TAK.gov built successfully!"
        echo "Location: app/build/outputs/apk/gov/unsigned/"
        ;;
    "clean")
        echo "Cleaning build artifacts..."
        ./gradlew clean
        echo "✅ Build artifacts cleaned!"
        ;;
    *)
        echo "Usage: $0 [debug|release|gov-unsigned|clean]"
        echo ""
        echo "  debug       - Build debug APK for testing"
        echo "  release     - Build signed release APK"
        echo "  gov-unsigned- Build unsigned APK for TAK.gov submission (default)"
        echo "  clean       - Clean build artifacts"
        echo ""
        exit 1
        ;;
esac

echo ""
echo "Build completed successfully! 🚀"