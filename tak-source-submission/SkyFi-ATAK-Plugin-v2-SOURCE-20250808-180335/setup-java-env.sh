#!/bin/bash

# SkyFi ATAK Plugin Java Environment Setup Script
# This script ensures the correct Java version is available for building the ATAK plugin

set -e  # Exit on any error

echo "=== SkyFi ATAK Plugin Java Environment Setup ==="

# Define Java paths
JAVA11_PATH="/opt/homebrew/Cellar/openjdk@11/11.0.28/libexec/openjdk.jdk/Contents/Home"
JAVA17_PATH="/opt/homebrew/Cellar/openjdk@17/17.0.16/libexec/openjdk.jdk/Contents/Home"

# Function to check if Java installation exists
check_java_installation() {
    local java_path=$1
    local version_name=$2
    
    if [ -d "$java_path" ] && [ -x "$java_path/bin/java" ]; then
        echo "✓ Found $version_name at: $java_path"
        return 0
    else
        echo "✗ $version_name not found at: $java_path"
        return 1
    fi
}

# Check for Java installations
echo ""
echo "Checking Java installations..."

# TAK.gov uses JDK 17, so we prioritize that
if check_java_installation "$JAVA17_PATH" "OpenJDK 17"; then
    JAVA_HOME_TO_USE="$JAVA17_PATH"
    echo "✓ Using OpenJDK 17 (TAK.gov requirement)"
elif check_java_installation "$JAVA11_PATH" "OpenJDK 11"; then
    JAVA_HOME_TO_USE="$JAVA11_PATH"
    echo "⚠️  Using OpenJDK 11 (TAK.gov uses JDK 17)"
else
    echo ""
    echo "❌ ERROR: No compatible Java installation found!"
    echo ""
    echo "Please install OpenJDK 17 using Homebrew (TAK.gov requirement):"
    echo "  brew install openjdk@17"
    echo ""
    echo "Then create a symlink (if needed):"
    echo "  sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk"
    echo ""
    exit 1
fi

# Set JAVA_HOME
export JAVA_HOME="$JAVA_HOME_TO_USE"
echo ""
echo "Setting JAVA_HOME to: $JAVA_HOME"

# Verify Java version
echo ""
echo "Java version details:"
"$JAVA_HOME/bin/java" -version

# Test Gradle with the Java setup
echo ""
echo "Testing Gradle with Java setup..."
if ./gradlew --version > /dev/null 2>&1; then
    echo "✓ Gradle successfully configured with Java"
else
    echo "❌ ERROR: Gradle failed to run with current Java setup"
    exit 1
fi

echo ""
echo "✅ Java environment setup complete!"
echo ""
echo "To use this environment in your shell, run:"
echo "  export JAVA_HOME=\"$JAVA_HOME\""
echo ""
echo "Or source this script:"
echo "  source ./setup-java-env.sh"
echo ""
echo "To build the APK for TAK.gov submission, run:"
echo "  export JAVA_HOME=\"$JAVA_HOME\" && ./gradlew assembleGovUnsigned"