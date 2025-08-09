#!/bin/bash

# Create TAK.gov submission package for Play Store compatible ATAK
# This script prepares source code that TAK.gov will build and sign

set -e

echo "Creating TAK.gov submission package for Play Store ATAK..."

# Set variables
TIMESTAMP=$(date +"%Y%m%d-%H%M%S")
SUBMISSION_DIR="tak-submission-playstore-${TIMESTAMP}"
PLUGIN_NAME="SkyFi-ATAK-Plugin-v2"
PACKAGE_NAME="com.optisense.skyfi.atak"

# Create submission directory
mkdir -p "$SUBMISSION_DIR/$PLUGIN_NAME"

echo "Copying source files..."

# Copy essential project files
cp -r app "$SUBMISSION_DIR/$PLUGIN_NAME/"
cp build.gradle "$SUBMISSION_DIR/$PLUGIN_NAME/"
cp settings.gradle "$SUBMISSION_DIR/$PLUGIN_NAME/"
cp gradle.properties "$SUBMISSION_DIR/$PLUGIN_NAME/"
cp -r gradle "$SUBMISSION_DIR/$PLUGIN_NAME/"
cp gradlew "$SUBMISSION_DIR/$PLUGIN_NAME/"
cp gradlew.bat "$SUBMISSION_DIR/$PLUGIN_NAME/"

# Copy takdev jar if exists
if [ -f "atak-gradle-takdev.jar" ]; then
    cp atak-gradle-takdev.jar "$SUBMISSION_DIR/$PLUGIN_NAME/"
fi

# Clean up the submission directory
echo "Cleaning up build artifacts..."
cd "$SUBMISSION_DIR/$PLUGIN_NAME"

# Remove build directories
rm -rf app/build
rm -rf build
rm -rf .gradle

# Remove unnecessary build.gradle variants
rm -f app/build.gradle.backup
rm -f app/build.gradle.playstore
rm -f app/build.gradle.takgov
rm -f app/build.gradle.takgov-compat
rm -f app/build.gradle.final

# Remove class files (these will be rebuilt by TAK.gov)
find . -name "*.class" -delete

# Remove any keystore files (TAK.gov will use their own)
find . -name "*.keystore" -delete
find . -name "*.jks" -delete
rm -f android_keystore

# Clean up AndroidManifest backups
rm -f app/src/main/AndroidManifest.xml.backup
rm -f app/src/main/AndroidManifest_playstore.xml

# Clean up asset backups
rm -f app/src/main/assets/plugin.xml.backup
rm -f app/src/main/assets/plugin_playstore.xml
rm -f app/src/main/assets/menu_playstore.xml

# Remove OpacityControlDialog backup
rm -f app/src/main/java/com/optisense/skyfi/atak/OpacityControlDialog.java.bak

# Create local.properties for TAK.gov
cat > local.properties << 'EOF'
# TAK.gov build configuration
# This file will be replaced by TAK.gov build system
sdk.dir=/opt/android-sdk
ndk.dir=/opt/android-ndk
takdev.plugin=./atak-gradle-takdev.jar
EOF

# Update app/build.gradle for TAK.gov compatibility
echo "Configuring build.gradle for TAK.gov..."
cat > app/build.gradle << 'EOF'
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
    ext.MIN_ATAK_VERSION = "5.3.0"
    ext.MAX_ATAK_VERSION = "5.4.0.19"

    def takdevVersion = '2.+'

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
    ext.takdevPlugin = getProperty('takdev.plugin', rootDir.absolutePath + "/atak-gradle-takdev.jar")

    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven {
            url "https://jitpack.io"
        }
        maven {
            url = takrepoUrl
            credentials {
                username = takrepoUser
                password = takrepoPassword
            }
        }
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:7.3.1'
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
        maven {
            url "https://jitpack.io"
        }
    }
}

apply plugin: 'com.android.application'
apply plugin: 'atak-takdev-plugin'

android {
    compileSdk 33
    namespace 'com.optisense.skyfi.atak'

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }

    bundle {
        storeArchive {
            enable = false
        }
    }

    packagingOptions {
        jniLibs {
            useLegacyPackaging = true
        }
        resources {
            excludes += ['META-INF/INDEX.LIST']
        }
    }

    signingConfigs {
        // TAK.gov will configure signing
        release {
            // Configured by TAK.gov build system
        }
    }

    buildTypes {
        debug {
            debuggable true
            matchingFallbacks = ['sdk']
        }
        release {
            minifyEnabled true
            proguardFiles 'proguard-gradle.txt', 'proguard-gradle-repackage.txt'
            matchingFallbacks = ['odk']
        }
    }

    flavorDimensions "application"

    productFlavors {
        civ {
            dimension "application"
            getIsDefault().set(true)
            manifestPlaceholders = [atakApiVersion: "com.atakmap.app@" + ATAK_VERSION + ".CIV"]
            matchingFallbacks = ['civ']
        }
    }

    sourceSets {
        main {
            setProperty("archivesBaseName", "ATAK-Plugin-" + rootProject.name + "-" + PLUGIN_VERSION + "-" + getVersionName() + "-" + ATAK_VERSION)
            defaultConfig.versionCode = getVersionCode()
            defaultConfig.versionName = PLUGIN_VERSION + " (" + getVersionName() + ") - [" + ATAK_VERSION + "]"
        }

        debug.setRoot('build-types/debug')
        release.setRoot('build-types/release')
    }

    defaultConfig {
        applicationId "com.optisense.skyfi.atak"
        minSdkVersion 21
        targetSdkVersion 33

        ndk {
            abiFilters "armeabi-v7a", "arm64-v8a", "x86"
        }
    }

    lint {
        abortOnError true
        checkReleaseBuilds true
    }
}

afterEvaluate {
    project.file('proguard-gradle-repackage.txt').text = "-repackageclasses atakplugin.SkyFiATAKPlugin"
}

dependencies {
    implementation fileTree(dir: 'libs', include: '*.jar')
    
    // Core dependencies
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation('com.squareup.retrofit2:retrofit:2.11.0')
    implementation('com.squareup.retrofit2:converter-gson:2.11.0')
    implementation('androidx.recyclerview:recyclerview:1.3.2')
    implementation('org.locationtech.jts:jts-core:1.16.1')
    implementation('androidx.swiperefreshlayout:swiperefreshlayout:1.1.0')
    implementation('androidx.cardview:cardview:1.0.0')
    implementation('com.google.code.gson:gson:2.10.1')
    
    // Test dependencies
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'org.mockito:mockito-core:5.11.0'
    testImplementation 'org.robolectric:robolectric:4.11.1'
    testImplementation 'com.squareup.okhttp3:mockwebserver:4.12.0'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
}

configurations.all {
    resolutionStrategy.eachDependency { details ->
        if (details.requested.group == 'androidx.core') {
            details.useVersion "1.15.0"
        }
        if (details.requested.group == 'androidx.lifecycle') {
            details.useVersion "2.8.7"
        }
        if (details.requested.group == 'androidx.fragment') {
            details.useVersion "1.8.5"
        }
    }
}

configurations.implementation {
    exclude group: 'androidx.core', module: 'core-ktx'
    exclude group: 'androidx.core', module: 'core'
    exclude group: 'androidx.fragment', module: 'fragment'
    exclude group: 'androidx.lifecycle', module: 'lifecycle'
    exclude group: 'androidx.lifecycle', module: 'lifecycle-process'
    exclude group: 'androidx.tracing', module: 'tracing'
    exclude group: 'androidx.tracing', module: 'tracing-ktx'
    exclude group: 'androidx.annotation', module: 'annotation'
}
EOF

# Create simplified plugin.xml for Play Store
echo "Creating Play Store compatible plugin.xml..."
cat > app/src/main/assets/plugin.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<!-- Play Store ATAK Plugin Configuration -->
<plugin>
    <!-- Direct MapComponent registration for Play Store ATAK -->
    <component>com.optisense.skyfi.atak.playstore.SkyFiPlayStorePlugin</component>
</plugin>
EOF

# Update AndroidManifest.xml to ensure Play Store compatibility
echo "Updating AndroidManifest.xml..."
cat > app/src/main/AndroidManifest.xml << 'EOF'
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    package="com.optisense.skyfi.atak"
    tools:ignore="GoogleAppIndexingWarning">

    <application
        android:allowBackup="false"
        android:icon="@drawable/ic_launcher"
        android:label="@string/app_name"
        android:description="@string/app_desc"
        android:theme="@style/AppTheme" >
        
        <meta-data android:name="plugin-api" android:value="${atakApiVersion}"/>
        <meta-data android:name="app_desc" android:value="@string/app_desc"/>
        
        <!-- Play Store compatible component -->
        <meta-data android:name="component" android:value="com.optisense.skyfi.atak.playstore.SkyFiPlayStorePlugin"/>

        <!-- Required for ATAK plugin discovery -->
        <activity android:name="com.atakmap.app.component"
            android:exported="true"
            tools:ignore="MissingClass">
            <intent-filter android:label="@string/app_name">
                <action android:name="com.atakmap.app.component" />
            </intent-filter>
        </activity>

    </application>

</manifest>
EOF

# Create README for TAK.gov
cat > README_TAKGOV.txt << 'EOF'
SkyFi ATAK Plugin v2 - TAK.gov Submission
==========================================

Package: com.optisense.skyfi.atak
Version: 2.0-beta5
Target: ATAK-CIV Play Store Version

IMPORTANT NOTES FOR TAK.GOV BUILD TEAM:
----------------------------------------
1. This plugin is designed specifically for the Google Play Store version of ATAK-CIV
2. The package name has been changed from "com.skyfi.atak.plugin" to "com.optisense.skyfi.atak"
   to avoid conflicts with existing Play Store submissions
3. The plugin uses the Play Store compatible component: SkyFiPlayStorePlugin
4. No SDK-specific IPlugin interface dependencies are included
5. All external dependencies are from standard Maven repositories

BUILD REQUIREMENTS:
------------------
- Android Gradle Plugin: 7.3.1
- Compile SDK: 33
- Min SDK: 21
- Target SDK: 33
- Java Version: 1.8
- ATAK Version: 5.3.0 - 5.4.0.19

SIGNING REQUIREMENTS:
--------------------
This plugin requires signing with the official TAK.gov keystore for Play Store distribution.
Please use your standard Play Store signing configuration.

DEPENDENCIES:
------------
All dependencies are available from standard Maven repositories:
- OkHttp: 4.12.0
- Retrofit: 2.11.0
- Gson: 2.10.1
- AndroidX RecyclerView: 1.3.2
- JTS Core: 1.16.1

TESTING:
--------
The plugin has been tested with:
- ATAK-CIV 5.3.0 (Play Store version)
- ATAK-CIV 5.4.0 (Play Store version)

CONTACT:
--------
Technical Contact: engineering@optisense.com
Support: support@skyfi.com

Thank you for processing this submission.
EOF

# Create BUILD_INSTRUCTIONS.md
cat > BUILD_INSTRUCTIONS.md << 'EOF'
# Build Instructions for TAK.gov

## Overview
This plugin is designed for the Google Play Store version of ATAK-CIV.

## Quick Build
```bash
./gradlew assembleCivRelease
```

## Build Configuration
The plugin is pre-configured for TAK.gov infrastructure:
- Uses standard TAK.gov repository for dependencies
- Configured for CIV flavor only
- Ready for Play Store signing

## Package Structure
- **Package Name**: com.optisense.skyfi.atak
- **Application ID**: com.optisense.skyfi.atak
- **Main Component**: com.optisense.skyfi.atak.playstore.SkyFiPlayStorePlugin

## Verification
After building, verify:
1. APK is properly signed with TAK.gov keystore
2. Package name is com.optisense.skyfi.atak
3. Plugin loads in Play Store ATAK-CIV

## Support
For build issues, contact: engineering@optisense.com
EOF

# Create VERSION.txt
cat > VERSION.txt << 'EOF'
2.0-beta5
EOF

cd ../..

# Create the final ZIP package
echo "Creating submission ZIP..."
zip -r "SkyFi-ATAK-Plugin-v2-TAKGOV-PLAYSTORE-${TIMESTAMP}.zip" "$SUBMISSION_DIR"

echo ""
echo "================================================================"
echo "TAK.gov Play Store Submission Package Created Successfully!"
echo "================================================================"
echo ""
echo "Package: SkyFi-ATAK-Plugin-v2-TAKGOV-PLAYSTORE-${TIMESTAMP}.zip"
echo "Directory: $SUBMISSION_DIR"
echo ""
echo "Package Details:"
echo "- Target: Google Play Store ATAK-CIV"
echo "- Package Name: com.optisense.skyfi.atak"
echo "- Version: 2.0-beta5"
echo "- Component: SkyFiPlayStorePlugin"
echo ""
echo "Next Steps:"
echo "1. Submit the ZIP file to TAK.gov for building and signing"
echo "2. TAK.gov will build with their Play Store signing configuration"
echo "3. The signed APK will work with Play Store ATAK-CIV"
echo ""
echo "IMPORTANT:"
echo "- This submission is for Play Store ATAK only"
echo "- Package name is com.optisense.skyfi.atak (not com.skyfi.atak.plugin)"
echo "- Uses simplified plugin.xml for Play Store compatibility"
echo ""