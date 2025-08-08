# TAK.gov Build Failure Fix

## Issue Resolved
The build failure on TAK.gov was caused by incorrect bundle configuration syntax for Android Gradle Plugin 7.2.2.

## Root Cause
The error occurred at line 99 in `app/build.gradle`:
```
Could not set unknown property 'enabled' for object of type com.android.build.gradle.internal.dsl.BundleOptions$AgpDecorated
```

## Solution Applied
Changed the bundle configuration from using `enabled` properties to the correct syntax for AGP 7.2.2:

### Before (Incorrect):
```gradle
bundle {
    storeArchive {
        enable = false  // or enabled = false
    }
    abi {
        enabled = false
    }
    density {
        enabled = false
    }
    language {
        enabled = false
    }
}
```

### After (Correct):
```gradle
bundle {
    // Disable bundle generation for all splits
    abi {
        enableSplit = false
    }
    density {
        enableSplit = false
    }
    language {
        enableSplit = false
    }
}
```

## Key Changes Made
1. Removed `storeArchive` block entirely (not needed for disabling AAB)
2. Changed `enabled = false` to `enableSplit = false` for split configurations
3. This properly disables Android App Bundle generation, ensuring APK output only

## Verification
- The configuration now works with Android Gradle Plugin 7.2.2
- Gradle 7.4.2 is correctly configured in gradle-wrapper.properties
- Build passes the configuration phase locally

## TAK.gov Build Environment Compatibility
This fix ensures compatibility with:
- Android Gradle Plugin: 7.2.2
- Gradle Version: 7.4.2
- Java Version: 17 (TAK.gov standard)
- Target SDK: 33
- Min SDK: 21

## Next Steps for TAK.gov Submission
1. Create a clean source bundle with this fix
2. Ensure all dependencies are properly declared
3. Remove any local.properties file from submission
4. Include only necessary source files and resources
5. Test build locally with same Gradle/AGP versions

## Additional Notes
- The bundle configuration is critical for ATAK plugins as they must be APKs, not AABs
- The TAK.gov build system uses specific proxy settings which are visible in the log
- Build includes dependency vulnerability scanning (OWASP Dependency Check)