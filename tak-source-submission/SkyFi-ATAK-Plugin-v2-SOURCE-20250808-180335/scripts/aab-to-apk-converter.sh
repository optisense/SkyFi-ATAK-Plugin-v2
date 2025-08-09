#!/bin/bash

# AAB to APK Conversion and Testing Script for ATAK Plugin Compatibility
# This script converts TAK.gov built AAB files to APK and tests different signing approaches
# for compatibility with Play Store ATAK-CIV

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
OUTPUT_DIR="$PROJECT_ROOT/atak-compatibility-tests"
BUNDLETOOL_VERSION="1.15.6"
BUNDLETOOL_PATH="$OUTPUT_DIR/bundletool-all-$BUNDLETOOL_VERSION.jar"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Create output directory
setup_environment() {
    log_info "Setting up environment..."
    mkdir -p "$OUTPUT_DIR"
    
    # Download bundletool if not exists
    if [ ! -f "$BUNDLETOOL_PATH" ]; then
        log_info "Downloading bundletool v$BUNDLETOOL_VERSION..."
        curl -L "https://github.com/google/bundletool/releases/download/$BUNDLETOOL_VERSION/bundletool-all-$BUNDLETOOL_VERSION.jar" \
            -o "$BUNDLETOOL_PATH"
        log_success "Bundletool downloaded"
    fi
    
    # Create test keystores directory
    mkdir -p "$OUTPUT_DIR/keystores"
    
    # Create device specs directory
    mkdir -p "$OUTPUT_DIR/device-specs"
}

# Generate different device specifications for APK extraction
generate_device_specs() {
    log_info "Generating device specifications..."
    
    # Generic Android device spec
    cat > "$OUTPUT_DIR/device-specs/generic-android.json" << 'EOF'
{
  "supportedAbis": ["armeabi-v7a", "arm64-v8a", "x86", "x86_64"],
  "supportedLocales": ["en-US"],
  "screenDensity": 480,
  "sdkVersion": 33
}
EOF

    # ATAK-specific device spec (common ATAK deployment targets)
    cat > "$OUTPUT_DIR/device-specs/atak-deployment.json" << 'EOF'
{
  "supportedAbis": ["armeabi-v7a", "arm64-v8a"],
  "supportedLocales": ["en-US"],
  "screenDensity": 320,
  "sdkVersion": 21
}
EOF

    # High-end military device spec
    cat > "$OUTPUT_DIR/device-specs/military-device.json" << 'EOF'
{
  "supportedAbis": ["arm64-v8a"],
  "supportedLocales": ["en-US"],
  "screenDensity": 560,
  "sdkVersion": 33
}
EOF
    
    log_success "Device specifications generated"
}

# Create test keystores with different signing approaches
create_test_keystores() {
    log_info "Creating test keystores..."
    
    # Standard debug keystore (matches ATAK SDK)
    if [ ! -f "$OUTPUT_DIR/keystores/debug.keystore" ]; then
        keytool -genkeypair \
            -alias debug \
            -keyalg RSA \
            -keysize 2048 \
            -validity 10000 \
            -keystore "$OUTPUT_DIR/keystores/debug.keystore" \
            -storepass android \
            -keypass android \
            -dname "CN=Debug,OU=Debug,O=Debug,L=Debug,S=Debug,C=US"
        log_success "Debug keystore created"
    fi
    
    # Play Store compatible keystore
    if [ ! -f "$OUTPUT_DIR/keystores/playstore-compat.keystore" ]; then
        keytool -genkeypair \
            -alias playstore \
            -keyalg RSA \
            -keysize 2048 \
            -validity 10000 \
            -keystore "$OUTPUT_DIR/keystores/playstore-compat.keystore" \
            -storepass skyfi2024 \
            -keypass skyfi2024 \
            -dname "CN=SkyFi Plugin,OU=ATAK Development,O=SkyFi Inc,L=San Francisco,S=California,C=US"
        log_success "Play Store compatible keystore created"
    fi
    
    # Test unsigned APK approach
    log_info "Unsigned APK approach will be tested without keystore"
}

# Convert AAB to APK using different methods
convert_aab_to_apk() {
    local aab_file="$1"
    local output_prefix="$2"
    
    if [ ! -f "$aab_file" ]; then
        log_error "AAB file not found: $aab_file"
        return 1
    fi
    
    log_info "Converting AAB to APK: $(basename "$aab_file")"
    
    # Method 1: Universal APK (contains all ABIs and resources)
    log_info "Creating universal APK..."
    java -jar "$BUNDLETOOL_PATH" build-apks \
        --bundle="$aab_file" \
        --output="$OUTPUT_DIR/${output_prefix}-universal.apks" \
        --mode=universal
    
    # Extract the universal APK
    unzip -o "$OUTPUT_DIR/${output_prefix}-universal.apks" \
        -d "$OUTPUT_DIR/${output_prefix}-universal/" 2>/dev/null || true
    
    if [ -f "$OUTPUT_DIR/${output_prefix}-universal/universal.apk" ]; then
        mv "$OUTPUT_DIR/${output_prefix}-universal/universal.apk" \
           "$OUTPUT_DIR/${output_prefix}-universal.apk"
        rm -rf "$OUTPUT_DIR/${output_prefix}-universal/"
        log_success "Universal APK created: ${output_prefix}-universal.apk"
    fi
    
    # Method 2: Device-specific APKs
    for device_spec in "$OUTPUT_DIR/device-specs"/*.json; do
        device_name=$(basename "$device_spec" .json)
        log_info "Creating APK for device spec: $device_name"
        
        java -jar "$BUNDLETOOL_PATH" build-apks \
            --bundle="$aab_file" \
            --output="$OUTPUT_DIR/${output_prefix}-${device_name}.apks" \
            --device-spec="$device_spec"
        
        # Extract device-specific APK
        unzip -o "$OUTPUT_DIR/${output_prefix}-${device_name}.apks" \
            -d "$OUTPUT_DIR/${output_prefix}-${device_name}/" 2>/dev/null || true
        
        # Find the main APK (usually the largest one)
        main_apk=$(find "$OUTPUT_DIR/${output_prefix}-${device_name}/" -name "*.apk" -type f | head -1)
        if [ -n "$main_apk" ]; then
            mv "$main_apk" "$OUTPUT_DIR/${output_prefix}-${device_name}.apk"
            rm -rf "$OUTPUT_DIR/${output_prefix}-${device_name}/"
            log_success "Device-specific APK created: ${output_prefix}-${device_name}.apk"
        fi
    done
}

# Re-sign APKs with different keystores
resign_apks() {
    local apk_pattern="$1"
    
    log_info "Re-signing APKs with different keystores..."
    
    for apk_file in "$OUTPUT_DIR"/$apk_pattern.apk; do
        if [ ! -f "$apk_file" ]; then
            continue
        fi
        
        base_name=$(basename "$apk_file" .apk)
        
        # Sign with debug keystore
        log_info "Signing with debug keystore: $(basename "$apk_file")"
        jarsigner -verbose \
            -sigalg SHA1withRSA \
            -digestalg SHA1 \
            -keystore "$OUTPUT_DIR/keystores/debug.keystore" \
            -storepass android \
            -keypass android \
            "$apk_file" debug
        
        zipalign -f 4 "$apk_file" "$OUTPUT_DIR/${base_name}-debug-signed.apk"
        
        # Sign with Play Store compatible keystore
        log_info "Signing with Play Store compatible keystore: $(basename "$apk_file")"
        cp "$apk_file" "$OUTPUT_DIR/${base_name}-unsigned.apk"
        
        jarsigner -verbose \
            -sigalg SHA256withRSA \
            -digestalg SHA-256 \
            -keystore "$OUTPUT_DIR/keystores/playstore-compat.keystore" \
            -storepass skyfi2024 \
            -keypass skyfi2024 \
            "$OUTPUT_DIR/${base_name}-unsigned.apk" playstore
        
        zipalign -f 4 "$OUTPUT_DIR/${base_name}-unsigned.apk" \
                    "$OUTPUT_DIR/${base_name}-playstore-signed.apk"
        
        rm "$OUTPUT_DIR/${base_name}-unsigned.apk"
        
        log_success "APK signed with both keystores"
    done
}

# Analyze APK compatibility
analyze_apk_compatibility() {
    log_info "Analyzing APK compatibility..."
    
    for apk_file in "$OUTPUT_DIR"/*.apk; do
        if [ ! -f "$apk_file" ]; then
            continue
        fi
        
        apk_name=$(basename "$apk_file")
        log_info "Analyzing: $apk_name"
        
        # Get APK info
        aapt dump badging "$apk_file" > "$OUTPUT_DIR/${apk_name}-info.txt" 2>&1 || {
            log_warning "Could not analyze APK with aapt: $apk_name"
        }
        
        # Check package name and permissions
        if [ -f "$OUTPUT_DIR/${apk_name}-info.txt" ]; then
            echo "=== APK Analysis for $apk_name ===" >> "$OUTPUT_DIR/compatibility-report.txt"
            grep -E "(package:|uses-permission:|supports-screens:)" \
                "$OUTPUT_DIR/${apk_name}-info.txt" >> "$OUTPUT_DIR/compatibility-report.txt" 2>/dev/null || true
            echo "" >> "$OUTPUT_DIR/compatibility-report.txt"
        fi
    done
    
    log_success "Compatibility analysis complete"
}

# Test APK installation (simulation)
test_apk_installation() {
    log_info "Testing APK installation compatibility..."
    
    cat > "$OUTPUT_DIR/installation-test-results.txt" << 'EOF'
# APK Installation Test Results

## Test Scenarios:
1. SDK ATAK with debug-signed APK
2. Play Store ATAK with debug-signed APK
3. Play Store ATAK with Play Store-signed APK
4. Play Store ATAK with unsigned APK

## Expected Results:
- SDK ATAK should load debug-signed APKs
- Play Store ATAK will likely reject debug-signed APKs
- Play Store ATAK may accept Play Store-signed APKs if signatures are compatible
- Unsigned APKs will be rejected by both

## Actual Test Results:
EOF

    for apk_file in "$OUTPUT_DIR"/*.apk; do
        if [ ! -f "$apk_file" ]; then
            continue
        fi
        
        apk_name=$(basename "$apk_file")
        echo "Testing: $apk_name" >> "$OUTPUT_DIR/installation-test-results.txt"
        
        # Check if APK is signed
        jarsigner -verify "$apk_file" &>/dev/null
        if [ $? -eq 0 ]; then
            echo "  - Signature: VALID" >> "$OUTPUT_DIR/installation-test-results.txt"
        else
            echo "  - Signature: INVALID or UNSIGNED" >> "$OUTPUT_DIR/installation-test-results.txt"
        fi
        
        # Check APK structure
        unzip -l "$apk_file" | grep -E "(classes\.dex|AndroidManifest\.xml)" &>/dev/null
        if [ $? -eq 0 ]; then
            echo "  - Structure: VALID APK" >> "$OUTPUT_DIR/installation-test-results.txt"
        else
            echo "  - Structure: INVALID APK" >> "$OUTPUT_DIR/installation-test-results.txt"
        fi
        
        echo "" >> "$OUTPUT_DIR/installation-test-results.txt"
    done
    
    log_success "Installation test simulation complete"
}

# Generate compatibility report
generate_report() {
    log_info "Generating compatibility report..."
    
    cat > "$OUTPUT_DIR/README.md" << 'EOF'
# ATAK Plugin Compatibility Testing Results

This directory contains the results of AAB to APK conversion and compatibility testing for the SkyFi ATAK Plugin.

## Generated Files:

### APK Files:
- `*-universal.apk`: Universal APKs containing all architectures
- `*-generic-android.apk`: APKs for generic Android devices
- `*-atak-deployment.apk`: APKs optimized for ATAK deployment targets
- `*-military-device.apk`: APKs for high-end military devices
- `*-debug-signed.apk`: APKs signed with debug keystore
- `*-playstore-signed.apk`: APKs signed with Play Store compatible keystore

### Analysis Files:
- `compatibility-report.txt`: Detailed APK analysis
- `installation-test-results.txt`: Installation test simulation results
- `*-info.txt`: Individual APK analysis files

### Configuration Files:
- `device-specs/`: Device specification files for APK generation
- `keystores/`: Test keystores for different signing approaches

## Usage Instructions:

1. **For SDK ATAK Users**: Use `*-debug-signed.apk` files
2. **For Play Store ATAK Users**: Try `*-playstore-signed.apk` files first
3. **For Testing**: Use universal APKs for broader device compatibility

## Next Steps:

1. Test these APKs on actual devices with both SDK and Play Store ATAK
2. Monitor plugin loading behavior and error logs
3. Adjust signing strategy based on test results
4. Consider companion app approach if direct plugin loading fails
EOF
    
    log_success "Compatibility report generated: $OUTPUT_DIR/README.md"
}

# Main execution function
main() {
    local aab_file="$1"
    
    if [ $# -lt 1 ]; then
        echo "Usage: $0 <path-to-aab-file> [output-prefix]"
        echo "Example: $0 /path/to/plugin.aab skyfi-plugin"
        exit 1
    fi
    
    local output_prefix="${2:-skyfi-atak-plugin}"
    
    log_info "Starting AAB to APK conversion and testing..."
    log_info "AAB file: $aab_file"
    log_info "Output prefix: $output_prefix"
    log_info "Output directory: $OUTPUT_DIR"
    
    setup_environment
    generate_device_specs
    create_test_keystores
    convert_aab_to_apk "$aab_file" "$output_prefix"
    resign_apks "$output_prefix*"
    analyze_apk_compatibility
    test_apk_installation
    generate_report
    
    log_success "AAB to APK conversion and testing complete!"
    log_info "Results available in: $OUTPUT_DIR"
    log_info "Check README.md for detailed instructions"
}

# Execute main function with all arguments
main "$@"