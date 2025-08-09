# TAK.gov Submission Compliance Checklist

## ✅ Package Structure
- [x] Single root folder with plugin name
- [x] Folder name becomes APK name
- [x] Clean source code without build artifacts

## ✅ Build System
- [x] Gradle build system present
- [x] `assembleCivRelease` target defined
- [x] Uses `atak-gradle-takdev` plugin version 2.+
- [x] Configured for TAK.gov maven repository

## ✅ Configuration
- [x] ProGuard repackaging uses "SkyFiATAKPlugin" (not "PluginTemplate")
- [x] AndroidManifest.xml contains discovery activity
- [x] Package name: com.skyfi.atak.plugin
- [x] Plugin version: 2.0-beta5

## ✅ Dependencies
- [x] No local SDK references
- [x] No flatDir repositories
- [x] SDK fetched from TAK.gov repository
- [x] All dependencies resolved from maven

## ✅ Verification Command
```bash
./gradlew -Ptakrepo.force=true \
         -Ptakrepo.url=https://artifacts.tak.gov/artifactory/maven \
         -Ptakrepo.user=<USERNAME> \
         -Ptakrepo.password=<PASSWORD> \
         assembleCivRelease
```

## ✅ Files Included
- app/ (source code and resources)
- build.gradle (root)
- settings.gradle
- gradle.properties
- gradlew, gradlew.bat
- gradle/ (wrapper)
- README_TAKGOV.txt

## ✅ Files Excluded
- Local SDK copies
- Build directories
- IDE configuration files
- Local properties
- Custom keystores
- Test APKs

## 📋 Submission Process
1. Upload ZIP to TAK.gov submission portal
2. TAK.gov builds from source
3. TAK.gov signs with official keystore
4. Signed APK available for download

## 🔒 Security Notes
- No sensitive credentials in source
- No hardcoded API keys
- TAK.gov handles all signing
- Official keystore never distributed
