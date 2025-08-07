# TAK.gov Submission Package - v2.0-beta4 (FIXED)
## SkyFi ATAK Plugin v2

**Date**: August 7, 2025  
**Latest Package**: `SkyFi-ATAK-Plugin-v2-takgov-submission-20250807-151922.zip`  
**Previous (has build error)**: `SkyFi-ATAK-Plugin-v2-takgov-submission-20250807-150903.zip`

## Critical Fixes Applied

### 1. Fixed ProductFlavor/BuildType Name Collision
- **Issue**: TAK.gov build failed with "ProductFlavor names cannot collide with BuildType names"
- **Fix**: Removed conflicting 'playstore' buildType and productFlavor
- **File**: `app/build.gradle` line 203

### 2. Fixed Looper.prepare() Crash
- **Issue**: Plugin was calling `Looper.prepare()` in constructor causing IllegalStateException
- **Fix**: Removed Looper initialization, moved API client init to `onStart()` method
- **File**: `SkyFiPlugin.java`

### 2. Java Compatibility
- **Issue**: Java 11 causing compatibility issues
- **Fix**: Changed to Java 1.8 for better ATAK compatibility
- **File**: `app/build.gradle`

### 3. AAB Generation Prevention
- **Issue**: TAK.gov was generating AAB files instead of APK
- **Fix**: Added `bundle { enabled = false }` configuration
- **File**: `app/build.gradle`

### 4. ProGuard Configuration
- **Issue**: Critical classes being stripped by R8/ProGuard
- **Fix**: Added comprehensive keep rules for all plugin classes
- **File**: `app/proguard-gradle.txt`

## Build Configuration

```gradle
android {
    compileSdkVersion 31
    buildToolsVersion '30.0.3'
    namespace 'com.skyfi.atak.plugin'
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    
    bundle {
        enabled = false  // Prevents AAB generation
    }
}
```

## Submission Contents

The package includes:
- Complete source code with all fixes
- ATAK SDK 5.4.0.18
- Gradle build configuration
- ProGuard rules
- All required assets and resources

## Testing Recommendations

1. Build the plugin with: `./gradlew assembleCivRelease`
2. Test on ATAK 5.4.0+ CIV version
3. Verify plugin loads without crashes
4. Check logcat for "SkyFiPlugin: Plugin onStart() called"

## Expected Build Output

TAK.gov should produce:
- **APK File** (not AAB): `ATAK-Plugin-SkyFi-ATAK-Plugin-v2-*.apk`
- **Size**: ~22MB
- **Target**: ATAK 5.4.0+

## Contact Information

For any issues or questions regarding this submission:
- **Organization**: Optisense (DBA SkyFi)
- **Technical Contact**: Development Team
- **Repository**: https://github.com/optisense/SkyFi-ATAK-Plugin-v2

## Notes for TAK.gov Build Team

- Plugin has been thoroughly tested with debug builds
- All crash issues from previous submission have been resolved
- ProGuard configuration has been optimized to prevent class stripping
- Java compatibility has been aligned with ATAK requirements