#!/bin/bash

# Create Play Store Compatible Keystore for ATAK Plugin
# This script generates a keystore that may be more compatible with Play Store ATAK

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
KEYSTORE_DIR="$PROJECT_ROOT/keystores"
KEYSTORE_FILE="$KEYSTORE_DIR/playstore-compatible.keystore"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Create keystores directory
mkdir -p "$KEYSTORE_DIR"

# Check if keystore already exists
if [ -f "$KEYSTORE_FILE" ]; then
    log_warning "Play Store compatible keystore already exists: $KEYSTORE_FILE"
    read -p "Do you want to recreate it? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_info "Keeping existing keystore"
        exit 0
    fi
    rm "$KEYSTORE_FILE"
fi

log_info "Creating Play Store compatible keystore..."

# Generate the keystore
keytool -genkeypair \
    -alias skyfi-playstore \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -keystore "$KEYSTORE_FILE" \
    -storepass skyfi2024 \
    -keypass skyfi2024 \
    -dname "CN=SkyFi ATAK Plugin,OU=Mobile Development,O=SkyFi Inc,L=San Francisco,S=California,C=US" \
    -storetype JKS

log_success "Play Store compatible keystore created: $KEYSTORE_FILE"

# Display keystore information
log_info "Keystore details:"
keytool -list -keystore "$KEYSTORE_FILE" -storepass skyfi2024 -v

# Create a properties file with keystore information
cat > "$KEYSTORE_DIR/playstore-keystore.properties" << EOF
# Play Store Compatible Keystore Properties
# Generated on $(date)

keystore.file=$KEYSTORE_FILE
keystore.password=skyfi2024
key.alias=skyfi-playstore
key.password=skyfi2024

# Usage in build.gradle:
# signingConfigs {
#     playstore {
#         storeFile file("$KEYSTORE_FILE")
#         storePassword "skyfi2024"
#         keyAlias "skyfi-playstore"
#         keyPassword "skyfi2024"
#     }
# }
EOF

log_success "Keystore properties file created: $KEYSTORE_DIR/playstore-keystore.properties"

# Create security note
cat > "$KEYSTORE_DIR/SECURITY_NOTE.md" << 'EOF'
# Keystore Security Notes

## Important Security Considerations

### Production Use:
1. **Never commit keystores to version control**
2. **Use secure passwords in production** 
3. **Store keystores in secure locations**
4. **Backup keystores securely**

### Current Keystore:
- This is a test/development keystore
- Uses default passwords for demonstration
- Should be replaced with production keystore for actual deployment

### Play Store Compatibility:
- This keystore uses standard Java KeyStore (JKS) format
- Compatible with Android signing requirements
- May not resolve Play Store ATAK signature validation issues
- Consider companion app approach if plugin loading fails

### Alternative Approaches:
1. **Companion App**: Standalone app with ATAK integration
2. **Intent-based Communication**: Use Android intents for ATAK interaction  
3. **TAK.gov Submission**: Official signing through TAK.gov process

## Usage Guidelines

### For Testing:
- Use this keystore for local testing only
- Test with both SDK and Play Store ATAK versions
- Monitor ATAK logs for signature validation errors

### For Production:
- Generate new production keystore with secure passwords
- Store keystore credentials in secure environment variables
- Use Android App Bundle (AAB) format where possible
- Consider code signing certificates for enhanced security
EOF

log_success "Security documentation created: $KEYSTORE_DIR/SECURITY_NOTE.md"

log_info "Setup complete! Next steps:"
echo "1. Build Play Store compatible APK: ./gradlew assemblePlaystorePlaystore"
echo "2. Test with Play Store ATAK installation"
echo "3. Monitor ATAK logs for plugin loading behavior"
echo "4. Consider companion app if plugin doesn't load"