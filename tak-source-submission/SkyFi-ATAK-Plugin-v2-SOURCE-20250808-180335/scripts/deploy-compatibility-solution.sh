#!/bin/bash

# Master deployment script for SkyFi ATAK compatibility solution
# This script orchestrates the complete compatibility strategy implementation

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
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

log_step() {
    echo -e "${CYAN}[STEP]${NC} $1"
}

log_header() {
    echo -e "${PURPLE}[HEADER]${NC} $1"
}

# Display banner
display_banner() {
    echo -e "${CYAN}"
    cat << 'EOF'
╔═══════════════════════════════════════════════════════════════════════════════╗
║                        SkyFi ATAK Compatibility Solution                     ║
║                                                                               ║
║  This script implements a comprehensive strategy for ATAK plugin             ║
║  compatibility across SDK and Play Store ATAK installations.                 ║
╚═══════════════════════════════════════════════════════════════════════════════╝
EOF
    echo -e "${NC}"
}

# Check prerequisites
check_prerequisites() {
    log_header "🔍 Checking Prerequisites"
    
    local missing_tools=()
    
    # Check for required tools
    for tool in java keytool gradle adb; do
        if ! command -v $tool &> /dev/null; then
            missing_tools+=($tool)
        fi
    done
    
    if [ ${#missing_tools[@]} -ne 0 ]; then
        log_error "Missing required tools: ${missing_tools[*]}"
        log_info "Please install missing tools and run again"
        exit 1
    fi
    
    # Check Java version
    local java_version=$(java -version 2>&1 | grep -oP 'version "?(1\.)?\K\d+' | head -1)
    if [ "$java_version" -lt 8 ]; then
        log_warning "Java version $java_version detected. Java 8+ recommended for ATAK development"
    fi
    
    log_success "All prerequisites satisfied"
}

# Setup keystores
setup_keystores() {
    log_header "🔐 Setting Up Keystores"
    
    if [ ! -f "$PROJECT_ROOT/keystores/playstore-compatible.keystore" ]; then
        log_step "Creating Play Store compatible keystore..."
        "$SCRIPT_DIR/create-playstore-keystore.sh"
    else
        log_success "Play Store keystore already exists"
    fi
}

# Build all APK variants
build_all_variants() {
    log_header "🔨 Building All APK Variants"
    
    log_step "Building standard SDK-compatible APK..."
    cd "$PROJECT_ROOT"
    ./gradlew assembleCivRelease
    
    log_step "Building Play Store-compatible APK..."
    ./gradlew assemblePlaystorePlaystore || {
        log_warning "Play Store build failed - may need keystore setup"
        log_info "Creating keystore and retrying..."
        "$SCRIPT_DIR/create-playstore-keystore.sh"
        ./gradlew assemblePlaystorePlaystore
    }
    
    log_step "Creating compatibility package..."
    ./gradlew createCompatibilityPackage
    
    log_success "All APK variants built successfully"
}

# Build companion app
build_companion_app() {
    log_header "📱 Building Companion App"
    
    local companion_dir="$PROJECT_ROOT/companion-app"
    
    if [ -d "$companion_dir" ]; then
        log_step "Building companion app variants..."
        cd "$companion_dir"
        
        if [ -f "build.gradle" ]; then
            # Build all companion app flavors
            ./gradlew assemblePlaystoreRelease
            ./gradlew assembleStandaloneRelease
            ./gradlew createATAKIntegrationPackage
            
            log_success "Companion app built successfully"
        else
            log_warning "Companion app build configuration not found"
        fi
    else
        log_warning "Companion app source not found - skipping build"
    fi
}

# Run compatibility tests
run_compatibility_tests() {
    log_header "🧪 Running Compatibility Tests"
    
    if command -v adb &> /dev/null && [ "$(adb devices | grep -E 'device$' | wc -l)" -gt 0 ]; then
        log_step "Running comprehensive compatibility tests..."
        "$SCRIPT_DIR/atak-compatibility-tester.sh"
        log_success "Compatibility tests completed"
    else
        log_warning "No Android devices connected - skipping device tests"
        log_step "Running simulation mode tests..."
        "$SCRIPT_DIR/atak-compatibility-tester.sh" || true
    fi
}

# Test AAB conversion
test_aab_conversion() {
    log_header "📦 Testing AAB Conversion"
    
    # Look for existing AAB files
    local aab_files=$(find "$PROJECT_ROOT" -name "*.aab" -type f | head -1)
    
    if [ -n "$aab_files" ]; then
        log_step "Testing AAB to APK conversion with existing AAB..."
        "$SCRIPT_DIR/aab-to-apk-converter.sh" "$aab_files" "test-conversion"
        log_success "AAB conversion test completed"
    else
        log_info "No AAB files found - conversion test skipped"
        log_info "AAB converter is ready for use when TAK.gov AAB files are available"
    fi
}

# Generate deployment package
generate_deployment_package() {
    log_header "📦 Generating Deployment Package"
    
    local deploy_dir="$PROJECT_ROOT/deployment-package"
    local timestamp=$(date +%Y%m%d-%H%M%S)
    
    rm -rf "$deploy_dir"
    mkdir -p "$deploy_dir"
    
    log_step "Collecting APK files..."
    
    # Collect SDK-compatible APKs
    find "$PROJECT_ROOT/app/build/outputs/apk" -name "*.apk" -type f | while read -r apk; do
        local variant=$(basename "$(dirname "$apk")")
        local build_type=$(basename "$(dirname "$(dirname "$apk")")")
        cp "$apk" "$deploy_dir/sdk-${build_type}-${variant}-$(basename "$apk")"
    done
    
    # Collect companion app APKs
    find "$PROJECT_ROOT/companion-app/build/outputs/apk" -name "*.apk" -type f 2>/dev/null | while read -r apk; do
        local variant=$(basename "$(dirname "$apk")")
        local build_type=$(basename "$(dirname "$(dirname "$apk")")")
        cp "$apk" "$deploy_dir/companion-${build_type}-${variant}-$(basename "$apk")"
    done 2>/dev/null || true
    
    # Copy scripts
    log_step "Including deployment scripts..."
    cp -r "$SCRIPT_DIR" "$deploy_dir/"
    
    # Copy documentation
    log_step "Including documentation..."
    cp "$PROJECT_ROOT/ATAK_INSTALLATION_GUIDE.md" "$deploy_dir/"
    cp "$PROJECT_ROOT/QUICK_SETUP_GUIDE.md" "$deploy_dir/"
    
    # Create deployment manifest
    log_step "Creating deployment manifest..."
    cat > "$deploy_dir/DEPLOYMENT_MANIFEST.md" << EOF
# SkyFi ATAK Compatibility Deployment Package

Generated: $(date)
Version: 2.0-beta3

## Package Contents

### APK Files
$(find "$deploy_dir" -name "*.apk" -exec basename {} \; | sort)

### Scripts  
$(find "$deploy_dir/scripts" -name "*.sh" -exec basename {} \; | sort)

### Documentation
- ATAK_INSTALLATION_GUIDE.md - Comprehensive installation guide
- QUICK_SETUP_GUIDE.md - Quick reference for setup
- DEPLOYMENT_MANIFEST.md - This file

## Installation Methods

1. **SDK ATAK**: Use sdk-*.apk files
2. **Play Store ATAK**: Use companion-*.apk files  
3. **Enterprise**: Choose based on deployment requirements

## Quick Commands

\`\`\`bash
# Test compatibility
./scripts/atak-compatibility-tester.sh

# Convert TAK.gov AAB
./scripts/aab-to-apk-converter.sh your-file.aab

# Create keystores
./scripts/create-playstore-keystore.sh
\`\`\`

## Support

- Documentation: ATAK_INSTALLATION_GUIDE.md
- Email: support@skyfi.com
- GitHub: https://github.com/skyfi/atak-plugin
EOF
    
    log_success "Deployment package created: $deploy_dir"
}

# Create distribution archive
create_distribution() {
    log_header "📦 Creating Distribution Archive"
    
    local deploy_dir="$PROJECT_ROOT/deployment-package"
    local timestamp=$(date +%Y%m%d-%H%M%S)
    local archive_name="skyfi-atak-compatibility-solution-${timestamp}.zip"
    
    if [ -d "$deploy_dir" ]; then
        log_step "Creating distribution archive..."
        cd "$PROJECT_ROOT"
        zip -r "$archive_name" "deployment-package/" -x "*.git*" "*.DS_Store*"
        
        log_success "Distribution archive created: $archive_name"
        log_info "Archive size: $(du -h "$archive_name" | cut -f1)"
    else
        log_error "Deployment package not found - run generate_deployment_package first"
        return 1
    fi
}

# Display summary
display_summary() {
    log_header "📋 Deployment Summary"
    
    echo -e "${GREEN}✅ Compatibility solution deployment complete!${NC}"
    echo
    echo "Created components:"
    echo "  🔐 Play Store compatible keystores"
    echo "  📦 Multiple APK variants (SDK + Play Store)"
    echo "  📱 Companion app (multiple flavors)"
    echo "  🧪 Compatibility testing tools"
    echo "  📄 Comprehensive documentation"
    echo "  🚀 Ready-to-deploy package"
    echo
    echo "Key files:"
    echo "  • deployment-package/ - Complete deployment bundle"
    echo "  • ATAK_INSTALLATION_GUIDE.md - User documentation"
    echo "  • QUICK_SETUP_GUIDE.md - Quick reference"
    echo "  • scripts/ - All deployment tools"
    echo
    echo -e "${CYAN}Next steps:${NC}"
    echo "  1. Test the solution with your ATAK installations"
    echo "  2. Share deployment package with users"
    echo "  3. Gather feedback and iterate"
    echo "  4. Consider TAK.gov submission for official signing"
    echo
    echo -e "${YELLOW}Support resources:${NC}"
    echo "  • Full documentation: ATAK_INSTALLATION_GUIDE.md"
    echo "  • Quick setup: QUICK_SETUP_GUIDE.md"
    echo "  • Email support: support@skyfi.com"
}

# Main execution function
main() {
    display_banner
    
    log_info "Starting SkyFi ATAK compatibility solution deployment..."
    echo
    
    check_prerequisites
    setup_keystores
    build_all_variants
    build_companion_app
    test_aab_conversion
    run_compatibility_tests
    generate_deployment_package
    create_distribution
    
    echo
    display_summary
    
    log_success "Compatibility solution deployment completed successfully!"
}

# Handle script arguments
case "${1:-deploy}" in
    "deploy")
        main
        ;;
    "build")
        check_prerequisites
        setup_keystores  
        build_all_variants
        build_companion_app
        ;;
    "test")
        run_compatibility_tests
        ;;
    "package")
        generate_deployment_package
        create_distribution
        ;;
    "help")
        echo "Usage: $0 [deploy|build|test|package|help]"
        echo "  deploy  - Full deployment (default)"
        echo "  build   - Build all variants only"
        echo "  test    - Run compatibility tests only"
        echo "  package - Generate deployment package only"
        echo "  help    - Show this help message"
        ;;
    *)
        echo "Unknown command: $1"
        echo "Use '$0 help' for usage information"
        exit 1
        ;;
esac