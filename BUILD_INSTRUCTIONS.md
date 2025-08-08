# Build Instructions for TAK.gov Submission

## SkyFi ATAK Plugin v2

**Date:** August 7, 2025

---

## How to Build

1. Unzip the source package so that all files are in a single root folder (e.g., `SkyFi-ATAK-Plugin-v2`).
2. Ensure you have Java 1.8 and the Android SDK installed (compileSdkVersion 31, buildToolsVersion 30.0.3).
3. Run the following command to build the plugin APK:

```sh
./gradlew assembleCivRelease
```

- This will produce an APK in `app/build/outputs/apk/civ/release/`.

## Requirements

- All dependencies are declared in the Gradle files.
- No local file system dependencies.
- The build will use the included `atak-gradle-takdev.jar` for offline compatibility.

## Troubleshooting

- If you see errors about missing flavors or build types, check `app/build.gradle` for correct configuration.
- For ProGuard/R8 issues, see `app/proguard-gradle.txt` and `app/proguard-gradle-repackage.txt`.

## Contact

- Organization: Optisense (DBA SkyFi)
- Support: support@skyfi.com

---

For more details, see `TAKGOV_SUBMISSION_README.md`.
