# ATAK Plugin Keystore Configuration - FIXED

## Problem Resolved
The build was failing because it was looking for the keystore in the wrong location (`app/build/android_keystore`). For local ATAK plugin builds, you must use the SDK's keystore.

## Solution Applied

### 1. Keystore Location
The ATAK SDK provides a keystore for local development at:
```
sdk/ATAK-CIV-5.4.0.18-SDK/android_keystore
```

### 2. Configuration Changes Made

#### app/build.gradle
Updated the signing configurations to use the SDK keystore:
```gradle
signingConfigs {
    debug {
        // Use the SDK keystore for local debug builds
        storeFile file("${rootDir}/sdk/ATAK-CIV-5.4.0.18-SDK/android_keystore")
        storePassword "tnttnt"
        keyAlias "wintec_mapping"
        keyPassword "tnttnt"
    }
    release {
        // Use the SDK keystore for local release builds
        storeFile file("${rootDir}/sdk/ATAK-CIV-5.4.0.18-SDK/android_keystore")
        storePassword "tnttnt"
        keyAlias "wintec_mapping"
        keyPassword "tnttnt"
    }
    unsigned {
        // No signing config - for TAK.gov submission
    }
}
```

#### local.properties
Updated to point to the SDK keystore:
```properties
takReleaseKeyFile=/Users/jfuginay/Documents/dev/optisense-projects/SkyFi-ATAK-Plugin-v2/sdk/ATAK-CIV-5.4.0.18-SDK/android_keystore
takDebugKeyFile=/Users/jfuginay/Documents/dev/optisense-projects/SkyFi-ATAK-Plugin-v2/sdk/ATAK-CIV-5.4.0.18-SDK/android_keystore
```

## Build Commands

### Clean Build
```bash
./gradlew clean
```

### Build Debug APK
```bash
./gradlew assembleCivDebug
```

### Build Release APK
```bash
./gradlew assembleCivRelease
```

### Build All Variants
```bash
./gradlew assemble
```

### Build with Offline Mode (faster)
```bash
./gradlew --offline --no-daemon assembleCivDebug
./gradlew --offline --no-daemon assembleCivRelease
```

## Generated APK Locations
- **Debug APK**: `app/build/outputs/apk/civ/debug/ATAK-Plugin-SkyFi-ATAK-Plugin-v2-2.0-d71ce552-5.4.0-civ-debug.apk`
- **Release APK**: `app/build/outputs/apk/civ/release/ATAK-Plugin-SkyFi-ATAK-Plugin-v2-2.0-d71ce552-5.4.0-civ-release.apk`

## Important Notes

1. **SDK Keystore**: Always use the SDK-provided keystore for local builds. This ensures compatibility with ATAK's development environment.

2. **Production Signing**: For production deployment to TAK.gov, you'll need to submit unsigned APKs or source code. TAK.gov handles the official signing process.

3. **Keystore Details**:
   - Alias: `wintec_mapping`
   - Password: `tnttnt`
   - Valid until: April 12, 2161

4. **Build Warnings**: The warnings about SHA1withRSA and 1024-bit RSA keys are expected with the SDK keystore. These are acceptable for local development but TAK.gov will use proper production certificates for official builds.

## Verification
To verify an APK is properly signed:
```bash
jarsigner -verify -verbose -certs <path-to-apk>
```

## Troubleshooting

If builds are hanging:
1. Stop all Gradle daemons: `./gradlew --stop`
2. Use offline mode: `./gradlew --offline`
3. Disable daemon: `./gradlew --no-daemon`

## Success Confirmation
Both debug and release APKs have been successfully built and signed with the SDK keystore at commit d71ce55.