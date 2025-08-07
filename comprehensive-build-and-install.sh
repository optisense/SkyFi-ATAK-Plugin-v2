#!/bin/bash

# Comprehensive build script for SkyFi ATAK Plugin v2
# This script handles both local debug builds and TAK.gov submission preparation

set -e

echo "================================================"
echo "SkyFi ATAK Plugin v2 - Comprehensive Build Tool"
echo "================================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
TIMESTAMP=$(date +"%Y%m%d-%H%M%S")
PROJECT_NAME="SkyFi-ATAK-Plugin-v2"

# Function to check Java version
check_java() {
    echo -e "${YELLOW}Checking Java environment...${NC}"
    
    if [ -n "$JAVA_HOME" ]; then
        echo "Current JAVA_HOME: $JAVA_HOME"
        "$JAVA_HOME/bin/java" -version 2>&1 | head -n 1
    else
        echo "JAVA_HOME not set, using system Java"
        java -version 2>&1 | head -n 1
    fi
}

# Function to set Java 17 if available
setup_java17() {
    echo -e "${YELLOW}Setting up Java 17 environment...${NC}"
    
    # Check for Java 17 on macOS
    if [ -d "/opt/homebrew/opt/openjdk@17" ]; then
        export JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
        echo "Using Homebrew Java 17"
    elif [ -d "/Library/Java/JavaVirtualMachines/openjdk-17.jdk" ]; then
        export JAVA_HOME="/Library/Java/JavaVirtualMachines/openjdk-17.jdk/Contents/Home"
        echo "Using system Java 17"
    else
        echo -e "${YELLOW}Java 17 not found, using current Java version${NC}"
    fi
    
    if [ -n "$JAVA_HOME" ]; then
        export PATH="$JAVA_HOME/bin:$PATH"
    fi
}

# Function to copy keystore
setup_keystore() {
    echo -e "${YELLOW}Setting up keystore...${NC}"
    
    if [ ! -d "app/build" ]; then
        mkdir -p app/build
    fi
    
    if [ -f "android_keystore" ]; then
        cp android_keystore app/build/
        echo "Keystore copied to app/build/"
    else
        echo -e "${RED}Warning: android_keystore not found in root directory${NC}"
        echo "Creating a temporary debug keystore..."
        keytool -genkey -v -keystore app/build/android_keystore \
            -alias wintec_mapping -keyalg RSA -keysize 2048 -validity 10000 \
            -storepass tnttnt -keypass tnttnt \
            -dname "CN=Debug, OU=Debug, O=Debug, L=Debug, S=Debug, C=US"
    fi
}

# Function to clean build
clean_build() {
    echo -e "${YELLOW}Cleaning previous builds...${NC}"
    ./gradlew clean || true
    rm -rf app/build || true
    rm -rf .gradle || true
    echo "Clean complete"
}

# Function to build debug APK
build_debug() {
    echo -e "${GREEN}Building Debug APK for local testing...${NC}"
    
    setup_keystore
    
    # Build with explicit Java home if available
    if [ -n "$JAVA_HOME" ]; then
        JAVA_HOME="$JAVA_HOME" ./gradlew assembleCivDebug --stacktrace
    else
        ./gradlew assembleCivDebug --stacktrace
    fi
    
    # Find and display the APK
    APK_PATH=$(find app/build/outputs/apk -name "*.apk" -type f | head -n 1)
    if [ -n "$APK_PATH" ]; then
        echo -e "${GREEN}Debug APK built successfully:${NC}"
        echo "$APK_PATH"
        
        # Copy to root for easy access
        cp "$APK_PATH" "${PROJECT_NAME}-DEBUG-${TIMESTAMP}.apk"
        echo -e "${GREEN}Copied to: ${PROJECT_NAME}-DEBUG-${TIMESTAMP}.apk${NC}"
        
        return 0
    else
        echo -e "${RED}Failed to build APK${NC}"
        return 1
    fi
}

# Function to build release APK (unsigned for TAK.gov)
build_release() {
    echo -e "${GREEN}Building Release APK (unsigned for TAK.gov)...${NC}"
    
    # Build release without signing
    if [ -n "$JAVA_HOME" ]; then
        JAVA_HOME="$JAVA_HOME" ./gradlew assembleCivRelease --stacktrace
    else
        ./gradlew assembleCivRelease --stacktrace
    fi
    
    # Find and display the APK
    APK_PATH=$(find app/build/outputs/apk -name "*release*.apk" -type f | head -n 1)
    if [ -n "$APK_PATH" ]; then
        echo -e "${GREEN}Release APK built successfully:${NC}"
        echo "$APK_PATH"
        return 0
    else
        echo -e "${RED}Failed to build release APK${NC}"
        return 1
    fi
}

# Function to install on device
install_on_device() {
    echo -e "${YELLOW}Installing on connected Android device...${NC}"
    
    # Check if device is connected
    if ! adb devices | grep -q "device$"; then
        echo -e "${RED}No Android device connected${NC}"
        echo "Please connect your device and enable USB debugging"
        return 1
    fi
    
    # Find the debug APK
    APK_PATH=$(find . -name "${PROJECT_NAME}-DEBUG-*.apk" -type f | head -n 1)
    if [ -z "$APK_PATH" ]; then
        APK_PATH=$(find app/build/outputs/apk -name "*debug*.apk" -type f | head -n 1)
    fi
    
    if [ -z "$APK_PATH" ]; then
        echo -e "${RED}No debug APK found. Please build first.${NC}"
        return 1
    fi
    
    echo "Installing: $APK_PATH"
    
    # Uninstall previous version if exists
    echo "Uninstalling previous version..."
    adb uninstall com.skyfi.atak.plugin 2>/dev/null || true
    
    # Install new version
    adb install -r "$APK_PATH"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}Installation successful!${NC}"
        echo ""
        echo "To load the plugin in ATAK:"
        echo "1. Open ATAK on your device"
        echo "2. Go to Settings -> Tool Preferences"
        echo "3. Find 'SkyFi Plugin' and enable it"
        echo "4. Restart ATAK if required"
    else
        echo -e "${RED}Installation failed${NC}"
        return 1
    fi
}

# Function to create TAK.gov submission
create_takgov_submission() {
    echo -e "${GREEN}Creating TAK.gov submission package...${NC}"
    
    OUTPUT_DIR="tak-submission-${TIMESTAMP}"
    ZIP_NAME="${PROJECT_NAME}-TAKGOV-${TIMESTAMP}.zip"
    
    # Create clean directory
    rm -rf "${OUTPUT_DIR}"
    mkdir -p "${OUTPUT_DIR}/${PROJECT_NAME}"
    
    # Copy essential files
    echo "Copying source files..."
    cp -r gradle "${OUTPUT_DIR}/${PROJECT_NAME}/"
    cp gradlew "${OUTPUT_DIR}/${PROJECT_NAME}/"
    cp gradlew.bat "${OUTPUT_DIR}/${PROJECT_NAME}/"
    cp settings.gradle "${OUTPUT_DIR}/${PROJECT_NAME}/"
    cp build.gradle "${OUTPUT_DIR}/${PROJECT_NAME}/"
    
    # Copy app directory
    mkdir -p "${OUTPUT_DIR}/${PROJECT_NAME}/app"
    cp -r app/src "${OUTPUT_DIR}/${PROJECT_NAME}/app/"
    cp app/build.gradle "${OUTPUT_DIR}/${PROJECT_NAME}/app/"
    cp app/proguard-gradle.txt "${OUTPUT_DIR}/${PROJECT_NAME}/app/" 2>/dev/null || true
    
    # Create gradle.properties for TAK.gov
    cat > "${OUTPUT_DIR}/${PROJECT_NAME}/gradle.properties" << 'EOF'
org.gradle.daemon=true
org.gradle.configureondemand=true
org.gradle.parallel=true
org.gradle.jvmargs=-Xmx4096m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8
android.useAndroidX=true
android.enableJetifier=false
systemProp.takrepo.force=true
EOF
    
    # Ensure proguard repackage file exists
    echo "-repackageclasses atakplugin.SkyFiATAKPlugin" > "${OUTPUT_DIR}/${PROJECT_NAME}/app/proguard-gradle-repackage.txt"
    
    # Clean up
    find "${OUTPUT_DIR}/${PROJECT_NAME}" -name ".DS_Store" -delete 2>/dev/null || true
    find "${OUTPUT_DIR}/${PROJECT_NAME}" -name "*.iml" -delete 2>/dev/null || true
    find "${OUTPUT_DIR}/${PROJECT_NAME}" -name ".idea" -type d -exec rm -rf {} + 2>/dev/null || true
    find "${OUTPUT_DIR}/${PROJECT_NAME}" -name "build" -type d -exec rm -rf {} + 2>/dev/null || true
    
    # Create zip
    cd "${OUTPUT_DIR}"
    zip -r "../${ZIP_NAME}" "${PROJECT_NAME}"
    cd ..
    
    echo -e "${GREEN}TAK.gov submission package created: ${ZIP_NAME}${NC}"
    echo "Size: $(du -h ${ZIP_NAME} | cut -f1)"
}

# Main menu
show_menu() {
    echo ""
    echo "Select an option:"
    echo "1) Clean build directories"
    echo "2) Build Debug APK (for local testing)"
    echo "3) Install Debug APK on connected device"
    echo "4) Build and Install (combines 2 & 3)"
    echo "5) Create TAK.gov submission package"
    echo "6) Build everything (Debug + TAK.gov package)"
    echo "7) Exit"
    echo ""
    read -p "Enter choice [1-7]: " choice
}

# Main execution
check_java
setup_java17
check_java

while true; do
    show_menu
    case $choice in
        1)
            clean_build
            ;;
        2)
            build_debug
            ;;
        3)
            install_on_device
            ;;
        4)
            build_debug
            if [ $? -eq 0 ]; then
                install_on_device
            fi
            ;;
        5)
            create_takgov_submission
            ;;
        6)
            clean_build
            build_debug
            create_takgov_submission
            echo ""
            echo -e "${GREEN}All builds complete!${NC}"
            echo "Debug APK: ${PROJECT_NAME}-DEBUG-${TIMESTAMP}.apk"
            echo "TAK.gov package: ${PROJECT_NAME}-TAKGOV-${TIMESTAMP}.zip"
            ;;
        7)
            echo "Exiting..."
            exit 0
            ;;
        *)
            echo -e "${RED}Invalid option${NC}"
            ;;
    esac
done