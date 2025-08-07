#!/bin/bash

# SkyFi ATAK Plugin Test Runner
# Comprehensive testing script for local development and CI/CD

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPORTS_DIR="$PROJECT_DIR/test-reports"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Create reports directory
mkdir -p "$REPORTS_DIR"

echo -e "${BLUE}🚀 SkyFi ATAK Plugin Test Runner${NC}"
echo -e "${BLUE}=================================${NC}"
echo "Project Directory: $PROJECT_DIR"
echo "Reports Directory: $REPORTS_DIR"
echo "Timestamp: $TIMESTAMP"
echo ""

# Function to print section headers
print_section() {
    echo -e "\n${BLUE}📋 $1${NC}"
    echo -e "${BLUE}$(printf '=%.0s' {1..50})${NC}"
}

# Function to print success messages
print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

# Function to print warning messages
print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

# Function to print error messages
print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Function to run tests with error handling
run_test_suite() {
    local test_name="$1"
    local gradle_command="$2"
    local description="$3"
    
    print_section "$test_name"
    echo "Description: $description"
    echo "Command: $gradle_command"
    echo ""
    
    if eval "$gradle_command"; then
        print_success "$test_name completed successfully"
        return 0
    else
        print_error "$test_name failed"
        return 1
    fi
}

# Function to check prerequisites
check_prerequisites() {
    print_section "Prerequisites Check"
    
    # Check Java
    if command -v java &> /dev/null; then
        java_version=$(java -version 2>&1 | head -n 1)
        print_success "Java found: $java_version"
    else
        print_error "Java not found. Please install Java 11 or higher."
        exit 1
    fi
    
    # Check Gradle wrapper
    if [ -f "./gradlew" ]; then
        print_success "Gradle wrapper found"
        chmod +x ./gradlew
    else
        print_error "Gradle wrapper not found"
        exit 1
    fi
    
    # Check Android SDK (optional)
    if [ -n "$ANDROID_HOME" ]; then
        print_success "Android SDK found: $ANDROID_HOME"
    else
        print_warning "ANDROID_HOME not set. Integration tests may not work."
    fi
}

# Function to clean previous builds
clean_build() {
    print_section "Clean Build"
    if ./gradlew clean; then
        print_success "Build cleaned successfully"
    else
        print_error "Failed to clean build"
        exit 1
    fi
}

# Function to run unit tests
run_unit_tests() {
    local failed_tests=0
    
    # Core plugin tests
    if ! run_test_suite "Core Plugin Tests" \
        "./gradlew testCivDebugUnitTest --tests 'com.skyfi.atak.plugin.SkyFiPluginTest' --continue" \
        "Tests core plugin functionality and lifecycle"; then
        ((failed_tests++))
    fi
    
    # API client tests
    if ! run_test_suite "API Client Tests" \
        "./gradlew testCivDebugUnitTest --tests 'com.skyfi.atak.plugin.APIClientTest' --continue" \
        "Tests API client functionality and configuration"; then
        ((failed_tests++))
    fi
    
    # Lifecycle tests
    if ! run_test_suite "Lifecycle Tests" \
        "./gradlew testCivDebugUnitTest --tests 'com.skyfi.atak.plugin.PluginLifecycleTest' --continue" \
        "Tests plugin lifecycle management"; then
        ((failed_tests++))
    fi
    
    # Error handling tests
    if ! run_test_suite "Error Handling Tests" \
        "./gradlew testCivDebugUnitTest --tests 'com.skyfi.atak.plugin.ErrorHandlingTest' --continue" \
        "Tests error handling and edge cases"; then
        ((failed_tests++))
    fi
    
    # Performance tests
    if ! run_test_suite "Performance Tests" \
        "./gradlew testCivDebugUnitTest --tests 'com.skyfi.atak.plugin.PerformanceTest' --continue" \
        "Tests performance and resource usage"; then
        ((failed_tests++))
    fi
    
    return $failed_tests
}

# Function to run regression tests
run_regression_tests() {
    local failed_tests=0
    
    # API regression tests
    if ! run_test_suite "API Regression Tests" \
        "./gradlew testCivDebugUnitTest --tests 'com.skyfi.atak.plugin.APIClientRegressionTest' --continue" \
        "Tests for API compatibility regressions"; then
        ((failed_tests++))
    fi
    
    # All regression tests
    if ! run_test_suite "All Regression Tests" \
        "./gradlew testCivDebugUnitTest --tests '*RegressionTest' --continue" \
        "Comprehensive regression test suite"; then
        ((failed_tests++))
    fi
    
    return $failed_tests
}

# Function to run stability tests
run_stability_tests() {
    if ! run_test_suite "Stability Test Suite" \
        "./gradlew testCivDebugUnitTest --tests 'com.skyfi.atak.plugin.StabilityTestSuite' --continue" \
        "Complete stability and regression test suite"; then
        return 1
    fi
    
    return 0
}

# Function to run integration tests
run_integration_tests() {
    print_section "Integration Tests"
    
    if [ -z "$ANDROID_HOME" ]; then
        print_warning "Skipping integration tests - ANDROID_HOME not set"
        return 0
    fi
    
    if ! run_test_suite "Android Integration Tests" \
        "./gradlew connectedCivDebugAndroidTest --continue" \
        "Tests plugin integration with Android environment"; then
        return 1
    fi
    
    return 0
}

# Function to run build verification
run_build_verification() {
    local failed_builds=0
    
    # Debug build
    if ! run_test_suite "Debug Build" \
        "./gradlew assembleCivDebug" \
        "Builds debug APK"; then
        ((failed_builds++))
    fi
    
    # Release build
    if ! run_test_suite "Release Build" \
        "./gradlew assembleCivRelease" \
        "Builds release APK"; then
        ((failed_builds++))
    fi
    
    # Lint checks
    if ! run_test_suite "Lint Checks" \
        "./gradlew lintCivDebug" \
        "Runs Android lint checks"; then
        ((failed_builds++))
    fi
    
    return $failed_builds
}

# Function to generate reports
generate_reports() {
    print_section "Generating Reports"
    
    # Generate test reports
    if ./gradlew testCivDebugUnitTest jacocoTestReport; then
        print_success "Test reports generated"
    else
        print_warning "Failed to generate some test reports"
    fi
    
    # Copy reports to central location
    if [ -d "app/build/reports" ]; then
        cp -r app/build/reports "$REPORTS_DIR/reports_$TIMESTAMP"
        print_success "Reports copied to $REPORTS_DIR/reports_$TIMESTAMP"
    fi
    
    # Generate summary
    cat > "$REPORTS_DIR/test_summary_$TIMESTAMP.txt" << EOF
SkyFi ATAK Plugin Test Summary
==============================
Timestamp: $TIMESTAMP
Project: SkyFi ATAK Plugin v2

Test Results:
- Unit Tests: $unit_test_result
- Regression Tests: $regression_test_result  
- Stability Tests: $stability_test_result
- Integration Tests: $integration_test_result
- Build Verification: $build_verification_result

Reports Location: $REPORTS_DIR/reports_$TIMESTAMP
EOF
    
    print_success "Test summary generated: $REPORTS_DIR/test_summary_$TIMESTAMP.txt"
}

# Function to print final summary
print_summary() {
    print_section "Test Summary"
    
    echo "Unit Tests: $unit_test_result"
    echo "Regression Tests: $regression_test_result"
    echo "Stability Tests: $stability_test_result"
    echo "Integration Tests: $integration_test_result"
    echo "Build Verification: $build_verification_result"
    echo ""
    
    if [ "$overall_result" = "PASSED" ]; then
        print_success "🎉 All tests passed! Plugin is stable and ready."
    else
        print_error "❌ Some tests failed. Please review the results."
        echo ""
        echo "Failed components:"
        [ "$unit_test_result" = "FAILED" ] && echo "- Unit Tests"
        [ "$regression_test_result" = "FAILED" ] && echo "- Regression Tests"
        [ "$stability_test_result" = "FAILED" ] && echo "- Stability Tests"
        [ "$integration_test_result" = "FAILED" ] && echo "- Integration Tests"
        [ "$build_verification_result" = "FAILED" ] && echo "- Build Verification"
    fi
}

# Main execution
main() {
    cd "$PROJECT_DIR"
    
    # Check prerequisites
    check_prerequisites
    
    # Clean build
    clean_build
    
    # Initialize result variables
    unit_test_result="PASSED"
    regression_test_result="PASSED"
    stability_test_result="PASSED"
    integration_test_result="PASSED"
    build_verification_result="PASSED"
    overall_result="PASSED"
    
    # Run test suites
    if ! run_unit_tests; then
        unit_test_result="FAILED"
        overall_result="FAILED"
    fi
    
    if ! run_regression_tests; then
        regression_test_result="FAILED"
        overall_result="FAILED"
    fi
    
    if ! run_stability_tests; then
        stability_test_result="FAILED"
        overall_result="FAILED"
    fi
    
    if ! run_integration_tests; then
        integration_test_result="FAILED"
        overall_result="FAILED"
    fi
    
    if ! run_build_verification; then
        build_verification_result="FAILED"
        overall_result="FAILED"
    fi
    
    # Generate reports
    generate_reports
    
    # Print summary
    print_summary
    
    # Exit with appropriate code
    if [ "$overall_result" = "PASSED" ]; then
        exit 0
    else
        exit 1
    fi
}

# Parse command line arguments
case "${1:-all}" in
    "unit")
        check_prerequisites
        clean_build
        run_unit_tests
        ;;
    "regression")
        check_prerequisites
        clean_build
        run_regression_tests
        ;;
    "stability")
        check_prerequisites
        clean_build
        run_stability_tests
        ;;
    "integration")
        check_prerequisites
        clean_build
        run_integration_tests
        ;;
    "build")
        check_prerequisites
        clean_build
        run_build_verification
        ;;
    "all"|*)
        main
        ;;
esac