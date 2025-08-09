# Google Play Store Signing Configuration

## Key Information

### Upload Key (for submitting to Google Play)
- **Keystore File**: `keystores/playstore-skyfi.keystore`
- **Key Alias**: `skyfi-playstore`
- **Store Password**: `skyfi2024`
- **Key Password**: `skyfi2024`
- **Key Algorithm**: RSA 2048-bit
- **Validity**: 10,000 days (expires ~2052)

### Exported Upload Key for Google Play Console
- **File**: `skyfi-playstore-upload-key.zip`
- **Contents**:
  - `encryptedPrivateKey` - Encrypted with Google's public key
  - `certificate.pem` - Public certificate

## Setup Instructions

### 1. Upload to Google Play Console

1. Go to Google Play Console
2. Select your app
3. Navigate to Setup → App signing
4. Choose "Export and upload a key from Java keystore"
5. Upload the `skyfi-playstore-upload-key.zip` file
6. Google Play will verify and accept your upload key

### 2. Build Configuration

To build APKs/AABs signed with this key for upload to Google Play:

```gradle
android {
    signingConfigs {
        playstore {
            storeFile file("${rootDir}/keystores/playstore-skyfi.keystore")
            storePassword "skyfi2024"
            keyAlias "skyfi-playstore"
            keyPassword "skyfi2024"
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.playstore
            // ... other configurations
        }
    }
}
```

### 3. Building for Play Store

```bash
# Build signed AAB (App Bundle) for Play Store
./gradlew bundleRelease

# Build signed APK for testing
./gradlew assembleRelease
```

## Important Notes

1. **Keep the keystore secure** - Back up `keystores/playstore-skyfi.keystore` in a safe location
2. **Never commit passwords** - Use gradle.properties or environment variables in production
3. **Google Play App Signing** - Google will re-sign your app with their own key for distribution
4. **Upload Key** - You only need the upload key to submit updates to Google Play

## Re-exporting the Key (if needed)

If you need to re-export the key for any reason:

```bash
./export-playstore-key.sh
# Enter password: skyfi2024 (twice when prompted)
```

## Keystore Details

```bash
# View keystore contents
keytool -list -v -keystore keystores/playstore-skyfi.keystore -storepass skyfi2024

# Certificate fingerprints (for API key restrictions)
# SHA1: (run command above to see)
# SHA256: (run command above to see)
```

## Files Created

- `keystores/playstore-skyfi.keystore` - The actual keystore file (KEEP SAFE!)
- `skyfi-playstore-upload-key.zip` - Encrypted key for Google Play Console upload
- `export-playstore-key.sh` - Script to re-export the key if needed
- `export-key-automated.sh` - Automated version of the export script