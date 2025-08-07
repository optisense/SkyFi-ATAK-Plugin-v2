SkyFi ATAK Plugin v2.0 - TAK.gov Build Instructions
====================================================

This source package has been prepared for the TAK.gov build pipeline.

Build Requirements:
- Java 17 (compatible with TAK.gov environment)
- Android SDK
- Gradle 7.5
- atak-gradle-takdev plugin version 2.+ (latest)

Build Command:
./gradlew -Ptakrepo.force=true \
          -Ptakrepo.url=https://artifacts.tak.gov/artifactory/maven \
          -Ptakrepo.user=<user> \
          -Ptakrepo.password=<pass> \
          assembleCivRelease

Where <user> and <pass> are your artifacts.tak.gov credentials.

The source structure complies with TAK.gov requirements:
- Single root folder containing all source
- Gradle build system
- assembleCivRelease target defined
- Uses atak-gradle-takdev plugin for SDK resolution
- AndroidManifest.xml contains required intent-filter for plugin discovery
- Proguard repackaging configured as "atakplugin.SkyFiATAKPlugin"

Note: This package does not include signing configuration.
TAK.gov will sign the APK using their official certificates.
