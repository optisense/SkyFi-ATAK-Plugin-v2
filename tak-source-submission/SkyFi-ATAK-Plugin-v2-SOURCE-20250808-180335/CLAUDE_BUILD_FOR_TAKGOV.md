# Claude Build Instructions for TAK.gov Submission

This file is for Claude or any other LLM/automation system to understand how to build and package the SkyFi ATAK Plugin for TAK.gov third-party signing.

---

## Build and Submission Steps

1. **Unzip the source package** so that all files are in a single root folder (e.g., `SkyFi-ATAK-Plugin-v2`).
2. **Ensure Java 1.8 and Android SDK** (compileSdkVersion 31, buildToolsVersion 30.0.3) are installed.
3. **Build the plugin APK** using:

   ```sh
   ./gradlew assembleCivRelease
   ```

   - The APK will be in `app/build/outputs/apk/civ/release/`.

4. **All dependencies** are declared in the Gradle files. No local file system dependencies are required.
5. The build will use the included `atak-gradle-takdev.jar` for offline compatibility.
6. **Troubleshooting:**
   - If you see errors about missing flavors or build types, check `app/build.gradle` for correct configuration.
   - For ProGuard/R8 issues, see `app/proguard-gradle.txt` and `app/proguard-gradle-repackage.txt`.

7. **Documentation:**
   - See `BUILD_INSTRUCTIONS.md` and `TAKGOV_SUBMISSION_README.md` for more details.
   - Contact: support@skyfi.com

---

## Packaging for TAK.gov

- Use the provided `create-takgov-submission.sh` script to generate a compliant zip archive for upload to https://tak.gov/user_builds.
- The archive must include:
  - All source code, Gradle scripts, and required assets/resources
  - `atak-gradle-takdev.jar`
  - `BUILD_INSTRUCTIONS.md`, `TAKGOV_SUBMISSION_README.md`, and `VERSION.txt`
- The root folder in the zip must be named `SkyFi-ATAK-Plugin-v2`.

---

## Example Submission Command

```sh
./create-takgov-submission.sh
```

This will produce a zip file like `SkyFi-ATAK-Plugin-v2-takgov-submission-YYYYMMDD-HHMMSS.zip` ready for upload to TAK.gov.

---

## Notes
- Do not generate AAB files; only APKs are accepted by TAK.gov.
- Ensure all ProGuard and manifest requirements are met (see `TAKGOV_SUBMISSION_README.md`).
- For any questions, contact support@skyfi.com.
