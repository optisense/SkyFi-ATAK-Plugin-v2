# TAK.gov Build Fixes for SkyFi ATAK Plugin v2

## Summary of Issues and Solutions

### 1. **Thread Initialization Issue**
**Problem:** Plugin constructor attempts to prepare a Looper, which causes initialization failures.
**Solution:** Removed `Looper.prepare()` call - ATAK plugins run on main thread with existing Looper.

### 2. **Premature Service Access**
**Problem:** Plugin tries to add toolbar items in constructor before fully initialized.
**Solution:** Moved all initialization logic to `onStart()` lifecycle method.

### 3. **ProGuard/R8 Configuration**
**Problem:** Missing classes cause verification failures at runtime.
**Solution:** Added comprehensive `-dontwarn` rules for:
- Groovy classes (from Gradle plugin)
- AWT classes (from JTS geometry library) 
- Gradle API classes
- Apache Commons classes

### 4. **Kotlin Version Mismatch**
**Problem:** Dependencies use Kotlin 1.9.x while TAK.gov expects 1.7.1.
**Solution:** Downgraded OkHttp and Retrofit to versions compatible with Kotlin 1.7.x and excluded Kotlin transitive dependencies.

### 5. **JTS Geometry Library AWT Dependencies**
**Problem:** JTS includes AWT classes not available on Android.
**Solution:** Excluded AWT modules from JTS dependency.

## Build Instructions for TAK.gov

1. **Clean Build**
```bash
./gradlew clean
```

2. **Build Unsigned APK for TAK.gov**
```bash
./gradlew assembleCivRelease
```

3. **Verify ProGuard Rules Applied**
Check that build output shows:
- No R8 warnings about missing classes
- No Kotlin version mismatch errors

## Testing Instructions

1. **Local Debug Testing**
```bash
# Build debug version with local signing
./gradlew assembleCivDebug

# Install on test device
adb install -r app/build/outputs/apk/civ/debug/*.apk

# Monitor logs
./debug-plugin-loading.sh
```

2. **Expected Log Output (Success)**
```
SkyFiPlugin: Plugin onStart() called
SkyFiPlugin: Added toolbar item in onStart()
SkyFiPlugin: Registered dropdown receivers
```

3. **Common Failure Indicators**
- `ClassNotFoundException` - ProGuard rules incomplete
- `VerifyError` - Dependency version mismatch
- `UnsatisfiedLinkError` - Native library issues
- No "onStart() called" message - Constructor failure

## Files Modified

1. **SkyFiPlugin.java**
   - Removed Looper.prepare() from constructor
   - Moved API client initialization to onStart()
   - Added comprehensive logging

2. **proguard-gradle.txt**
   - Added -dontwarn rules for missing classes
   - Added -keep rules for plugin classes
   - Added -keep rules for AndroidX components

3. **build.gradle**
   - Downgraded OkHttp to 4.10.0
   - Downgraded Retrofit to 2.9.0
   - Excluded Kotlin transitive dependencies
   - Excluded AWT modules from JTS

## Verification Checklist

Before submitting to TAK.gov:

- [ ] Plugin builds without R8 warnings
- [ ] No Kotlin version mismatch errors in build log
- [ ] Plugin loads successfully on local debug device
- [ ] onStart() method is called (check logs)
- [ ] Toolbar item appears in ATAK
- [ ] No ClassNotFoundException in device logs
- [ ] No VerifyError in device logs

## Additional Notes

- TAK.gov uses Java 17 for building
- Target SDK must be 33 or lower
- MinSDK must be 21 or higher
- Plugin must implement `gov.tak.api.plugin.IPlugin`
- Do not include signing configuration for TAK.gov builds

## Support

If issues persist after applying these fixes:

1. Run the debug script: `./debug-plugin-loading.sh`
2. Check for additional missing classes in ProGuard output
3. Verify all dependencies are Android-compatible
4. Ensure no desktop Java APIs are used