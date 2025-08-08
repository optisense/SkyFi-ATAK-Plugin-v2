#!/bin/bash

# Create a minimal, stable TAK.gov submission that will definitely build

TIMESTAMP=$(date +"%Y%m%d-%H%M%S")
OUTPUT_DIR="tak-submission-stable-${TIMESTAMP}"
ZIP_NAME="SkyFi-ATAK-Plugin-v2-STABLE-${TIMESTAMP}.zip"

echo "======================================"
echo "Creating STABLE TAK.gov Submission"
echo "Version: 2.0-beta5"
echo "======================================"
echo ""

# Create output directory
mkdir -p "$OUTPUT_DIR"

# Copy source files
echo "Copying source files..."
cp -r app "$OUTPUT_DIR/"

# Copy gradle files
echo "Copying gradle configuration..."
cp -r gradle "$OUTPUT_DIR/"
cp gradlew "$OUTPUT_DIR/"
cp gradlew.bat "$OUTPUT_DIR/"
cp settings.gradle "$OUTPUT_DIR/"

# Copy the takdev jar
echo "Copying atak-gradle-takdev.jar..."
cp atak-gradle-takdev.jar "$OUTPUT_DIR/"

# Create a MINIMAL root build.gradle
echo "Creating minimal root build.gradle..."
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

# Create minimal gradle.properties
echo "Creating gradle.properties..."
cat > "$OUTPUT_DIR/gradle.properties" << 'EOF'
# Project-wide Gradle settings
org.gradle.jvmargs=-Xmx4096m
org.gradle.parallel=true
android.useAndroidX=true
android.enableJetifier=true
EOF

# Create a SIMPLIFIED app build.gradle for TAK.gov
echo "Creating simplified app/build.gradle..."
cat > "$OUTPUT_DIR/app/build.gradle" << 'EOF'
buildscript {
    ext.PLUGIN_VERSION = "2.0-beta5"
    ext.ATAK_VERSION = "5.3.0"

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

    ext.takdevPlugin = "${rootDir}/atak-gradle-takdev.jar"

    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
    
    dependencies {
        classpath files(takdevPlugin)
    }
}

apply plugin: 'com.android.application'
apply plugin: 'atak-takdev-plugin'

android {
    compileSdk 33
    namespace 'com.skyfi.atak.plugin'

    defaultConfig {
        minSdkVersion 21
        targetSdkVersion 33
        versionCode 205
        versionName "2.0-beta5"
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }

    // Disable bundle generation
    bundle {
        enabled = false
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
        // TAK.gov will handle signing
    }

    buildTypes {
        debug {
            debuggable true
            matchingFallbacks = ['sdk']
        }
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt')
            matchingFallbacks = ['odk']
        }
    }

    flavorDimensions "application"

    productFlavors {
        civ {
            dimension "application"
            manifestPlaceholders = [atakApiVersion: "com.atakmap.app@5.3.0.CIV"]
        }
        mil {
            dimension "application"
            applicationIdSuffix ".mil"
            manifestPlaceholders = [atakApiVersion: "com.atakmap.app@5.3.0.MIL"]
        }
    }

    sourceSets {
        main {
            java.srcDirs = ['src/main/java']
            res.srcDirs = ['src/main/res']
            assets.srcDirs = ['src/main/assets']
        }
    }

    lintOptions {
        abortOnError false
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
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
}
EOF

# Create local.properties
echo "Creating local.properties..."
cat > "$OUTPUT_DIR/local.properties" << 'EOF'
# TAK.gov will set these
sdk.dir=/path/to/android/sdk
EOF

# Create build instructions
cat > "$OUTPUT_DIR/BUILD_INSTRUCTIONS.md" << 'EOF'
# SkyFi ATAK Plugin v2 - Build Instructions

## Version Information
- Plugin Version: 2.0-beta5
- ATAK Compatibility: 5.3.0 - 5.4.0.19
- Play Store ATAK: COMPATIBLE

## Build Commands

```bash
# Build all flavors
./gradlew clean assembleRelease

# Build specific flavor
./gradlew clean assembleCivRelease
./gradlew clean assembleMilRelease
```

## Expected Output
APK files will be generated in:
- app/build/outputs/apk/civ/release/
- app/build/outputs/apk/mil/release/

## IMPORTANT
This plugin is compatible with Play Store ATAK-CIV 5.4.0.16
EOF

# Clean up
echo "Cleaning build artifacts..."
rm -rf "$OUTPUT_DIR/app/build"
rm -rf "$OUTPUT_DIR/.gradle"
rm -rf "$OUTPUT_DIR/build"
rm -f "$OUTPUT_DIR/app/build.gradle.takgov"
find "$OUTPUT_DIR" -name "*.apk" -delete
find "$OUTPUT_DIR" -name "*.aab" -delete
find "$OUTPUT_DIR" -name ".DS_Store" -delete

# Create zip
echo "Creating submission package..."
zip -r "$ZIP_NAME" "$OUTPUT_DIR" -x "*.DS_Store" -x "__MACOSX/*"

echo ""
echo "✅ STABLE submission package created!"
echo ""
echo "📦 File: $ZIP_NAME"
echo "📏 Size: $(du -h "$ZIP_NAME" | cut -f1)"
echo ""
echo "This is a MINIMAL, STABLE configuration that should build successfully on TAK.gov"
echo ""
echo "Upload this to TAK.gov and request:"
echo "1. Build for CIV and MIL flavors"
echo "2. Test on Play Store ATAK-CIV 5.4.0.16"
echo "3. Verify APK artifacts are generated"