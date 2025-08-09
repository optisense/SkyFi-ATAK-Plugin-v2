#!/bin/bash

# TAK.gov Build Validation Script
# Tests the plugin build with official TAK.gov repository
# Ensures compliance before submission

set -e

echo "============================================"
echo "TAK.gov Build Validation Script"
echo "============================================"
echo ""

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# Check for required files
echo "1. Checking required files..."
echo "------------------------------"

required_files=(
    "app/build.gradle"
    "build.gradle"
    "settings.gradle"
    "gradle.properties"
    "gradlew"
    "app/src/main/AndroidManifest.xml"
    "app/proguard-gradle.txt"
)

all_files_present=true
for file in "${required_files[@]}"; do
    if [ -f "$file" ]; then
        print_success "$file exists"
    else
        print_error "$file missing"
        all_files_present=false
    fi
done

if [ "$all_files_present" = false ]; then
    echo ""
    print_error "Missing required files. Please ensure all files are present."
    exit 1
fi

echo ""
echo "2. Checking build.gradle configuration..."
echo "------------------------------"

# Check for atak-gradle-takdev version 2.+
if grep -q "atak-gradle-takdev.*2\.\+" app/build.gradle; then
    print_success "Using atak-gradle-takdev version 2.+"
else
    print_error "Not using atak-gradle-takdev version 2.+"
fi

# Check for TAK.gov repository URL
if grep -q "artifacts.tak.gov/artifactory/maven" app/build.gradle; then
    print_success "TAK.gov repository configured"
else
    print_warning "TAK.gov repository not explicitly configured (will use command line params)"
fi

# Check for assembleCivRelease task
if grep -q "assembleCivRelease" app/build.gradle || grep -q "productFlavors" app/build.gradle; then
    print_success "assembleCivRelease target available"
else
    print_error "assembleCivRelease target not found"
fi

echo ""
echo "3. Checking AndroidManifest.xml..."
echo "------------------------------"

# Check for discovery activity
if grep -q "com.atakmap.app.component" app/src/main/AndroidManifest.xml; then
    print_success "Discovery activity present"
else
    print_error "Discovery activity missing"
fi

# Check for plugin metadata
if grep -q "plugin-api" app/src/main/AndroidManifest.xml; then
    print_success "Plugin API metadata present"
else
    print_error "Plugin API metadata missing"
fi

echo ""
echo "4. Checking ProGuard configuration..."
echo "------------------------------"

# Check ProGuard repackage file
if [ -f "app/proguard-gradle-repackage.txt" ]; then
    if grep -q "SkyFiATAKPlugin" app/proguard-gradle-repackage.txt; then
        print_success "ProGuard repackaging configured correctly"
    else
        print_error "ProGuard still using PluginTemplate"
    fi
else
    print_warning "ProGuard repackage file will be created at build time"
fi

echo ""
echo "5. Checking for problematic configurations..."
echo "------------------------------"

# Check for local SDK references
if grep -q "flatDir" app/build.gradle; then
    print_error "Local SDK references (flatDir) found - should use TAK.gov repository"
else
    print_success "No local SDK references"
fi

# Check for hardcoded credentials
if grep -q "password.*=.*['\"].*[^'\"]" app/build.gradle gradle.properties; then
    print_warning "Possible hardcoded credentials found"
else
    print_success "No hardcoded credentials detected"
fi

echo ""
echo "6. Testing build (dry run without credentials)..."
echo "------------------------------"

# Try a clean to ensure Gradle wrapper works
if ./gradlew clean 2>/dev/null; then
    print_success "Gradle wrapper functional"
else
    print_error "Gradle wrapper not working"
fi

echo ""
echo "============================================"
echo "VALIDATION SUMMARY"
echo "============================================"
echo ""

echo "To perform a full build test with TAK.gov credentials:"
echo ""
echo "./gradlew -Ptakrepo.force=true \\"
echo "         -Ptakrepo.url=https://artifacts.tak.gov/artifactory/maven \\"
echo "         -Ptakrepo.user=<USERNAME> \\"
echo "         -Ptakrepo.password=<PASSWORD> \\"
echo "         assembleCivRelease"
echo ""

echo "Expected output location:"
echo "app/build/outputs/apk/civ/release/*.apk"
echo ""

# Create a test script for TAK.gov credentials
cat > test-with-credentials.sh << 'EOF'
#!/bin/bash
# Test build with TAK.gov credentials
# Usage: ./test-with-credentials.sh <username> <password>

if [ $# -ne 2 ]; then
    echo "Usage: $0 <tak_username> <tak_password>"
    exit 1
fi

TAK_USER="$1"
TAK_PASS="$2"

echo "Testing build with TAK.gov repository..."
./gradlew clean
./gradlew -Ptakrepo.force=true \
         -Ptakrepo.url=https://artifacts.tak.gov/artifactory/maven \
         -Ptakrepo.user="$TAK_USER" \
         -Ptakrepo.password="$TAK_PASS" \
         assembleCivRelease

if [ $? -eq 0 ]; then
    echo "✓ Build successful!"
    ls -la app/build/outputs/apk/civ/release/
else
    echo "✗ Build failed"
    exit 1
fi
EOF

chmod +x test-with-credentials.sh

print_success "Created test-with-credentials.sh for testing with TAK.gov credentials"
echo ""
echo "============================================"
echo "Validation complete!"
echo "============================================"