#!/bin/bash

# debug-plugin-loading.sh - Debug SkyFi ATAK Plugin Loading Issues
# This script helps diagnose common plugin loading problems

echo "=========================================="
echo "SkyFi ATAK Plugin Loading Debug Script"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if ADB is available
print_status "Checking ADB availability..."
if ! command -v adb &> /dev/null; then
    print_error "ADB is not installed or not in PATH"
    print_status "Please install Android SDK platform tools"
    exit 1
fi
print_success "ADB is available"

# Check if device is connected
print_status "Checking for connected devices..."
DEVICES=$(adb devices | grep -v "List of devices attached" | grep -v "^$")
if [ -z "$DEVICES" ]; then
    print_error "No Android devices connected"
    print_status "Please connect your device and enable USB debugging"
    exit 1
fi
print_success "Device connected: $DEVICES"

# Check if ATAK is installed
print_status "Checking if ATAK is installed..."
ATAK_PACKAGE="com.atakmap.app.civ"
if adb shell pm list packages | grep -q "$ATAK_PACKAGE"; then
    print_success "ATAK civilian package is installed"
    ATAK_VERSION=$(adb shell dumpsys package $ATAK_PACKAGE | grep "versionName" | head -1 | cut -d'=' -f2)
    print_status "ATAK version: $ATAK_VERSION"
else
    print_error "ATAK civilian package is not installed"
    print_status "Please install ATAK from Google Play Store or APK"
    exit 1
fi

# Function to monitor logs for plugin loading
monitor_plugin_logs() {
    print_status "Monitoring logcat for SkyFi plugin loading..."
    print_status "Start ATAK now to see plugin loading logs"
    print_status "Press Ctrl+C to stop monitoring"
    
    adb logcat -v time | grep -E "(SkyFiPlugin|PluginApi|PluginLoader|com.skyfi.atak.plugin)" --line-buffered | while read line; do
        if echo "$line" | grep -q "ERROR\|FATAL"; then
            print_error "$line"
        elif echo "$line" | grep -q "WARN"; then
            print_warning "$line"
        elif echo "$line" | grep -q "SkyFiPlugin.*onStart\|SkyFiPlugin.*initialization"; then
            print_success "$line"
        else
            print_status "$line"
        fi
    done
}

# Function to check plugin APK installation
check_plugin_installation() {
    print_status "Checking if SkyFi plugin is installed..."
    PLUGIN_PACKAGE="com.skyfi.atak.plugin"
    
    if adb shell pm list packages | grep -q "$PLUGIN_PACKAGE"; then
        print_success "SkyFi plugin package is installed"
        PLUGIN_VERSION=$(adb shell dumpsys package $PLUGIN_PACKAGE | grep "versionName" | head -1 | cut -d'=' -f2)
        print_status "Plugin version: $PLUGIN_VERSION"
        
        # Check plugin signature
        print_status "Checking plugin signature..."
        adb shell dumpsys package $PLUGIN_PACKAGE | grep -A 5 "signatures:"
        
        return 0
    else
        print_error "SkyFi plugin package is not installed"
        return 1
    fi
}

# Function to check common issues
check_common_issues() {
    print_status "Checking for common plugin loading issues..."
    
    # Check available storage
    STORAGE=$(adb shell df /data | tail -1 | awk '{print $4}')
    if [ "$STORAGE" -lt 100000 ]; then
        print_warning "Low storage space available: ${STORAGE}K"
    else
        print_success "Sufficient storage space: ${STORAGE}K"
    fi
    
    # Check if plugin directory exists
    if adb shell ls /data/data/com.atakmap.app.civ/plugins/ &>/dev/null; then
        print_success "ATAK plugins directory exists"
        PLUGIN_COUNT=$(adb shell ls /data/data/com.atakmap.app.civ/plugins/ | wc -l)
        print_status "Number of plugins in directory: $PLUGIN_COUNT"
    else
        print_warning "ATAK plugins directory may not exist"
    fi
    
    # Check ATAK permissions
    print_status "Checking ATAK permissions..."
    adb shell dumpsys package com.atakmap.app.civ | grep "permission" | head -10
}

# Function to install plugin
install_plugin() {
    APK_PATH=""
    
    # Look for APK files in common locations
    if [ -f "app/build/outputs/apk/civ/debug/app-civ-debug.apk" ]; then
        APK_PATH="app/build/outputs/apk/civ/debug/app-civ-debug.apk"
    elif [ -f "app/build/outputs/apk/civ/release/app-civ-release.apk" ]; then
        APK_PATH="app/build/outputs/apk/civ/release/app-civ-release.apk"
    else
        print_error "No plugin APK found. Build the plugin first."
        return 1
    fi
    
    print_status "Installing plugin from: $APK_PATH"
    if adb install -r "$APK_PATH"; then
        print_success "Plugin installed successfully"
        return 0
    else
        print_error "Failed to install plugin"
        return 1
    fi
}

# Function to uninstall plugin
uninstall_plugin() {
    print_status "Uninstalling SkyFi plugin..."
    if adb uninstall com.skyfi.atak.plugin; then
        print_success "Plugin uninstalled successfully"
    else
        print_error "Failed to uninstall plugin or plugin not installed"
    fi
}

# Function to clear ATAK data
clear_atak_data() {
    print_warning "This will clear ALL ATAK data including settings and preferences"
    read -p "Are you sure you want to continue? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_status "Clearing ATAK data..."
        adb shell pm clear com.atakmap.app.civ
        print_success "ATAK data cleared"
    else
        print_status "Operation cancelled"
    fi
}

# Function to extract logcat for debugging
extract_logs() {
    TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
    LOG_FILE="skyfi_plugin_debug_${TIMESTAMP}.log"
    
    print_status "Extracting relevant logs to: $LOG_FILE"
    adb logcat -d -v time | grep -E "(SkyFiPlugin|PluginApi|PluginLoader|com.skyfi.atak.plugin|AndroidRuntime)" > "$LOG_FILE"
    
    if [ -s "$LOG_FILE" ]; then
        print_success "Logs extracted to: $LOG_FILE"
        print_status "Log file size: $(wc -l < "$LOG_FILE") lines"
    else
        print_warning "No relevant logs found or log file is empty"
    fi
}

# Main menu
show_menu() {
    echo
    echo "What would you like to do?"
    echo "1. Monitor plugin loading logs (real-time)"
    echo "2. Check plugin installation status"
    echo "3. Install plugin APK"
    echo "4. Uninstall plugin"
    echo "5. Check common issues"
    echo "6. Extract debug logs to file"
    echo "7. Clear ATAK data (use with caution)"
    echo "8. Exit"
    echo
}

# Main execution loop
while true; do
    show_menu
    read -p "Choose an option (1-8): " choice
    
    case $choice in
        1) monitor_plugin_logs ;;
        2) check_plugin_installation ;;
        3) install_plugin ;;
        4) uninstall_plugin ;;
        5) check_common_issues ;;
        6) extract_logs ;;
        7) clear_atak_data ;;
        8) 
            print_status "Exiting debug script"
            exit 0
            ;;
        *) 
            print_error "Invalid option. Please choose 1-8."
            ;;
    esac
    
    echo
    read -p "Press Enter to continue..."
done