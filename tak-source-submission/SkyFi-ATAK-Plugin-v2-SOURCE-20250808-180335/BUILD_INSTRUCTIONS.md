# Build Instructions for TAK.gov

## Prerequisites
- Java 17
- Android SDK
- ATAK SDK 5.4.0.16

## Build Steps

1. Set up your environment:
   ```bash
   export JAVA_HOME=/path/to/java17
   ```

2. Configure local.properties with TAK.gov credentials:
   ```
   takrepo.url=https://artifacts.tak.gov/artifactory/maven
   takrepo.user=YOUR_USERNAME
   takrepo.password=YOUR_PASSWORD
   ```

3. Build the unsigned APK:
   ```bash
   ./gradlew assembleCivUnsigned
   ```

4. Sign with TAK.gov certificates

## Important Notes

- The plugin uses `SkyFiPluginCompatWrapper` for ATAK 5.4.0.16 compatibility
- IServiceController dependency has been removed
- Target SDK version is set to 5.4.0.16 in build.gradle

