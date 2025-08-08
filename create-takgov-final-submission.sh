#!/bin/bash

# Create TAK.gov submission following official plugin template structure

TIMESTAMP=$(date +"%Y%m%d-%H%M%S")
OUTPUT_DIR="SkyFi-ATAK-Plugin-v2"  # Fixed name as required by TAK.gov
ZIP_NAME="SkyFi-ATAK-Plugin-v2-FINAL-${TIMESTAMP}.zip"

echo "======================================"
echo "Creating TAK.gov Final Submission"
echo "Following Official Plugin Template"
echo "======================================"
echo ""

# Clean previous submission
rm -rf "$OUTPUT_DIR"

# Create directory structure
echo "Creating submission directory..."
mkdir -p "$OUTPUT_DIR"

# Copy essential files matching template structure
echo "Copying source files..."
cp -r app "$OUTPUT_DIR/"
cp -r gradle "$OUTPUT_DIR/"
cp gradlew "$OUTPUT_DIR/"
cp gradlew.bat "$OUTPUT_DIR/"
cp settings.gradle "$OUTPUT_DIR/"

# Copy the takdev jar (REQUIRED)
echo "Copying atak-gradle-takdev.jar..."
cp atak-gradle-takdev.jar "$OUTPUT_DIR/"

# Create root build.gradle matching template
echo "Creating root build.gradle..."
cat > "$OUTPUT_DIR/build.gradle" << 'EOF'
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:7.4.2'
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
EOF

# Create gradle.properties
echo "Creating gradle.properties..."
cat > "$OUTPUT_DIR/gradle.properties" << 'EOF'
org.gradle.jvmargs=-Xmx4096m
org.gradle.parallel=true
android.useAndroidX=true
android.enableJetifier=true
EOF

# Create app/build.gradle following template exactly
echo "Creating app/build.gradle..."
cat > "$OUTPUT_DIR/app/build.gradle" << 'EOF'
////////////////////////////////////////////////////////////////////////////////
//
// PLUGIN_VERSION is the common version name when describing the plugin.
// ATAK_VERSION   is for the version of ATAK this plugin should be compatible
//                with some examples include 3.11.0, 3.11.0.civ 3.11.1.fvey
//
////////////////////////////////////////////////////////////////////////////////

buildscript {

    ext.PLUGIN_VERSION = "2.0-beta5"
    ext.ATAK_VERSION = "5.3.0"

    def takdevVersion = '2.+'  // For ATAK 4.2+

    def getValueFromPropertiesFile = { propFile, key ->
        if(!propFile.isFile() || !propFile.canRead())
            return null
        def prop = new Properties()
        def reader = propFile.newReader()
        try {
            prop.load(reader)
        } finally {
            reader.close()
        }
        return prop.get(key)
    }

    def getProperty = { name, defValue ->
        def prop = project.properties[name] ?:
                getValueFromPropertiesFile(project.rootProject.file('local.properties'), name)
        return (null == prop) ? defValue : prop
    }

    def urlKey = 'takrepo.url'

    ext.isDevKitEnabled = { ->
        return getProperty(urlKey, null) != null
    }

    ext.takrepoUrl = getProperty(urlKey, 'https://localhost/')
    ext.takrepoUser = getProperty('takrepo.user', 'invalid')
    ext.takrepoPassword = getProperty('takrepo.password', 'invalid')
    ext.takdevPlugin = getProperty('takdev.plugin', "${rootDir}/atak-gradle-takdev.jar")

    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven {
            url = takrepoUrl
            credentials {
                username = takrepoUser
                password = takrepoPassword
            }
        }
    }
    dependencies {
        if(isDevKitEnabled()) {
            classpath "com.atakmap.gradle:atak-gradle-takdev:${takdevVersion}"
        } else {
            classpath files(takdevPlugin)
        }
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

apply plugin: 'com.android.application'
apply plugin: 'atak-takdev-plugin'

def supportedFlavors = [
    [ name : 'civ', default: true ],
    [ name : 'mil' ],
    [ name : 'gov' ],
]

android {
    compileSdk 33
    namespace 'com.skyfi.atak.plugin'

    signingConfigs {
        debug {
            def kf = getValueFromPropertiesFile(project.rootProject.file('local.properties'), 'takDebugKeyFile')
            if (kf != null) {
                def kfp = getValueFromPropertiesFile(project.rootProject.file('local.properties'), 'takDebugKeyFilePassword')
                def ka = getValueFromPropertiesFile(project.rootProject.file('local.properties'), 'takDebugKeyAlias')
                def kp = getValueFromPropertiesFile(project.rootProject.file('local.properties'), 'takDebugKeyPassword')
                if (kfp != null && ka != null && kp != null) {
                    storeFile file(kf)
                    storePassword kfp
                    keyAlias ka
                    keyPassword kp
                }
            }
        }
        release {
            def kf = getValueFromPropertiesFile(project.rootProject.file('local.properties'), 'takReleaseKeyFile')
            if (kf != null) {
                def kfp = getValueFromPropertiesFile(project.rootProject.file('local.properties'), 'takReleaseKeyFilePassword')
                def ka = getValueFromPropertiesFile(project.rootProject.file('local.properties'), 'takReleaseKeyAlias')
                def kp = getValueFromPropertiesFile(project.rootProject.file('local.properties'), 'takReleaseKeyPassword')
                if (kfp != null && ka != null && kp != null) {
                    storeFile file(kf)
                    storePassword kfp
                    keyAlias ka
                    keyPassword kp
                }
            }
        }
    }

    buildTypes {
        debug {
            debuggable true
            matchingFallbacks = ['sdk']
        }
        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.txt'
            signingConfig signingConfigs.release
            matchingFallbacks = ['odk']
        }
    }

    flavorDimensions "application"
    
    productFlavors {
        supportedFlavors.each { flav ->
            "${flav.name}" {
                getIsDefault().set(flav.default)
                dimension "application"
                
                if (!flav.name.equals("civ") && !flav.name.equals("mil")) {
                    applicationIdSuffix = ".${flav.name}"
                }
                matchingFallbacks = ['civ']
                
                def pluginApiFlavor = flav.name.equals('gov') ? 'CIV' : "${flav.name.toUpperCase()}"
                manifestPlaceholders = [atakApiVersion: "com.atakmap.app@" + ATAK_VERSION + ".${pluginApiFlavor}"]
            }
        }
        applicationVariants.all { variant ->
            variant.resValue "string", "versionName", variant.versionName
        }
    }

    packagingOptions {
        resources {
            excludes += ['META-INF/INDEX.LIST']
        }
        jniLibs {
            useLegacyPackaging true
        }
    }

    sourceSets {
        main {
            setProperty("archivesBaseName", "ATAK-Plugin-" + rootProject.name + "-" + PLUGIN_VERSION + "-" + getVersionName() + "-" + ATAK_VERSION)
            defaultConfig.versionCode = getVersionCode()
            defaultConfig.versionName = PLUGIN_VERSION + " (" + getVersionName() + ") - [" + ATAK_VERSION + "]"
        }

        debug {
            java.srcDirs 'src/debug/java'
            res.srcDirs 'src/debug/res'
        }
        release {
            java.srcDirs 'src/release/java'
            res.srcDirs 'src/release/res'
        }

        if (file("${projectDir}/src/gov").exists()) {
            gov {
                res.srcDirs = ['src/gov/res']
                java.srcDirs = ['src/gov/java']
            }
        }

        if (file("${projectDir}/src/mil").exists()) {
            mil {
                res.srcDirs = ['src/mil/res']
                java.srcDirs = ['src/mil/java']
            }
        }

        if (file("${projectDir}/src/civ").exists()) {
            civ {
                res.srcDirs = ['src/civ/res']
                java.srcDirs = ['src/civ/java']
            }
        }
    }

    defaultConfig {
        minSdkVersion 21
        targetSdkVersion 33
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    lintOptions {
        abortOnError false
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation fileTree(dir: 'libs', include: '*.jar')
    
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.recyclerview:recyclerview:1.3.1'
    implementation 'androidx.cardview:cardview:1.0.0'
    implementation 'com.google.android.material:material:1.9.0'
    
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.11.0'
    implementation 'com.google.code.gson:gson:2.10.1'
    
    implementation 'org.locationtech.jts:jts-core:1.18.2'
    implementation 'com.github.bumptech.glide:glide:4.15.1'
    
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'androidx.test:core:1.5.0'
    testImplementation 'org.robolectric:robolectric:4.10.3'
    testImplementation 'org.mockito:mockito-core:5.4.0'
    
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
}
EOF

# Create local.properties for TAK.gov
echo "Creating local.properties..."
cat > "$OUTPUT_DIR/local.properties" << 'EOF'
# SDK path will be set by TAK.gov
sdk.dir=/path/to/android/sdk
EOF

# Update ProGuard files
echo "Updating ProGuard configuration..."
cat > "$OUTPUT_DIR/app/proguard-rules.txt" << 'EOF'
-dontskipnonpubliclibraryclasses
-dontshrink
-dontoptimize

# ATAK Plugin Rules
-keepattributes *Annotation*
-keepattributes Signature
-keep class * implements gov.tak.api.plugin.IPlugin
-keep class * extends com.atakmap.android.maps.MapComponent
-keep class com.skyfi.atak.plugin.** { *; }
-keep class com.skyfi.atak.plugin.skyfiapi.** { *; }

# Retrofit
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-repackageclasses atakplugin.SkyFiATAKPlugin
EOF

# Ensure correct ProGuard repackage file
echo "-repackageclasses atakplugin.SkyFiATAKPlugin" > "$OUTPUT_DIR/app/proguard-gradle-repackage.txt"

# Clean up build artifacts
echo "Cleaning build artifacts..."
rm -rf "$OUTPUT_DIR/app/build"
rm -rf "$OUTPUT_DIR/.gradle"
rm -rf "$OUTPUT_DIR/build"
find "$OUTPUT_DIR" -name "*.apk" -delete
find "$OUTPUT_DIR" -name "*.aab" -delete
find "$OUTPUT_DIR" -name ".DS_Store" -delete
find "$OUTPUT_DIR" -name "*.iml" -delete

# Create the submission zip
echo "Creating submission package..."
zip -r "$ZIP_NAME" "$OUTPUT_DIR" -x "*.DS_Store" -x "__MACOSX/*" -x "*/build/*" -x "*/.gradle/*"

# Cleanup temp directory
rm -rf "$OUTPUT_DIR"

echo ""
echo "✅ TAK.gov Final Submission Package Created!"
echo ""
echo "📦 File: $ZIP_NAME"
echo "📏 Size: $(du -h "$ZIP_NAME" | cut -f1)"
echo "🔒 MD5: $(md5 -q "$ZIP_NAME" 2>/dev/null || md5sum "$ZIP_NAME" | cut -d' ' -f1)"
echo ""
echo "This package follows the official TAK plugin template structure and should build successfully."
echo ""
echo "IMPORTANT NOTES FOR TAK.gov SUBMISSION:"
echo "1. The root folder name will be used for APK naming"
echo "2. The gradle target 'assembleCivRelease' is defined"
echo "3. Uses atak-gradle-takdev version 2.+ for ATAK 4.2+"
echo "4. ProGuard repackage is set to 'atakplugin.SkyFiATAKPlugin'"
echo "5. AndroidManifest contains required com.atakmap.app.component activity"
echo ""
echo "Upload this to TAK.gov and request:"
echo "- Build for CIV and MIL flavors"
echo "- Test on Play Store ATAK-CIV 5.4.0.16"
echo "- Verify plugin loads without NoClassDefFoundError"