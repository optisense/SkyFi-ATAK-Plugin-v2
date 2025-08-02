# ATAK Plugin Development Guide

## Table of Contents
1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Development Environment Setup](#development-environment-setup)
4. [Creating Your First Plugin](#creating-your-first-plugin)
5. [Plugin Architecture](#plugin-architecture)
6. [Key Components](#key-components)
7. [Building and Testing](#building-and-testing)
8. [Distribution](#distribution)
9. [Best Practices](#best-practices)
10. [Common Issues](#common-issues)

## Overview

ATAK (Android Team Awareness Kit) plugins extend the functionality of ATAK by adding custom tools, data sources, and capabilities. Plugins are Android applications that integrate with ATAK through a well-defined API.

## Prerequisites

### Required Software
- **Java Development Kit (JDK) 17**
- **Android Studio** (latest version)
- **Android SDK** (API Level 30 minimum, 35 recommended)
- **ATAK SDK** (5.4.0 or later)
- **Git** for version control

### Required Knowledge
- Java or Kotlin programming
- Android development basics
- Understanding of geospatial concepts (coordinates, projections)

## Development Environment Setup

### 1. Install Development Tools

```bash
# macOS (using Homebrew)
brew install openjdk@17
brew install --cask android-studio
brew install --cask android-platform-tools

# Set JAVA_HOME
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
```

### 2. Download ATAK SDK

1. Register at https://tak.gov
2. Download the ATAK CIV SDK
3. Extract to your project directory

### 3. Project Structure

```
your-plugin/
├── app/
│   ├── build.gradle
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── assets/
│   │   │   │   ├── plugin.xml
│   │   │   │   └── menu.xml
│   │   │   ├── java/
│   │   │   └── res/
│   │   └── test/
├── build.gradle
├── settings.gradle
├── gradle.properties
└── local.properties
```

## Creating Your First Plugin

### 1. Clone the Plugin Template

```bash
# From the ATAK SDK samples
cp -r sdk/ATAK-CIV-5.4.0.18-SDK/samples/plugintemplate my-plugin
cd my-plugin
```

### 2. Configure build.gradle

```gradle
buildscript {
    ext.PLUGIN_VERSION = "1.0"
    ext.ATAK_VERSION = "5.4.0"
    
    def takdevVersion = '2.+'  // For ATAK 4.2+
    
    dependencies {
        classpath 'com.android.tools.build:gradle:8.8.2'
        classpath "com.atakmap.gradle:atak-gradle-takdev:${takdevVersion}"
    }
}

android {
    compileSdkVersion 35
    
    defaultConfig {
        minSdkVersion 26
        targetSdkVersion 34
        versionCode 1
        versionName PLUGIN_VERSION
    }
    
    buildTypes {
        debug {
            debuggable true
            matchingFallbacks = ['sdk']
        }
        release {
            minifyEnabled true
            proguardFiles 'proguard-gradle.txt'
            matchingFallbacks = ['odk']
        }
    }
}
```

### 3. Create Plugin Manifest

`AndroidManifest.xml`:
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">
    
    <application
        android:allowBackup="false"
        android:icon="@drawable/ic_launcher"
        android:label="@string/app_name">
        
        <!-- Required for plugin discovery -->
        <activity android:name="com.atakmap.app.component"
            android:exported="true"
            tools:ignore="MissingClass">
            <intent-filter>
                <action android:name="com.atakmap.app.component" />
            </intent-filter>
        </activity>
        
        <!-- Plugin metadata -->
        <meta-data android:name="plugin-api" 
                   android:value="${atakApiVersion}"/>
    </application>
</manifest>
```

### 4. Create Plugin Descriptor

`assets/plugin.xml`:
```xml
<plugin 
    pluginApi="com.atakmap.app@5.4.0.CIV"
    name="My Plugin"
    description="My ATAK Plugin"
    version="1.0">
    
    <components>
        <component 
            class="com.example.myplugin.MyPluginLifecycle"/>
    </components>
</plugin>
```

## Plugin Architecture

### Main Components

1. **Plugin Lifecycle** - Manages plugin initialization and shutdown
2. **Map Component** - Integrates with ATAK's mapping system
3. **Drop Down Receiver** - Creates UI panels
4. **Tool** - Implements interactive map tools

### Plugin Lifecycle Class

```java
public class MyPluginLifecycle extends AbstractPlugin implements PluginLifecycle {
    
    private Context pluginContext;
    private MyMapComponent mapComponent;
    
    public MyPluginLifecycle(Context context) {
        super(context, "My Plugin", "com.example.myplugin", "plugin.xml");
        this.pluginContext = context;
    }
    
    @Override
    public void onCreate(Context atakContext, Intent intent) {
        // Initialize plugin
        mapComponent = new MyMapComponent();
        mapComponent.onCreate(atakContext, intent, pluginContext);
    }
    
    @Override
    public void onDestroy(Context atakContext) {
        // Cleanup
        if (mapComponent != null) {
            mapComponent.onDestroy(atakContext);
        }
    }
}
```

### Map Component

```java
public class MyMapComponent extends AbstractMapComponent {
    
    private MyDropDownReceiver dropDown;
    
    @Override
    public void onCreate(Context atakContext, Intent intent, Context pluginContext) {
        super.onCreate(atakContext, intent, pluginContext);
        
        // Initialize components
        dropDown = new MyDropDownReceiver(MapView.getMapView(), pluginContext);
        
        // Register receivers
        DocumentedIntentFilter filter = new DocumentedIntentFilter();
        filter.addAction("com.example.myplugin.SHOW_DROPDOWN");
        registerReceiver(dropDown, filter);
    }
    
    @Override
    protected void onDestroyImpl(Context context) {
        // Cleanup
        if (dropDown != null) {
            unregisterReceiver(dropDown);
        }
    }
}
```

## Key Components

### 1. Drop Down Receivers (UI Panels)

```java
public class MyDropDownReceiver extends DropDownReceiver {
    
    private View view;
    
    public MyDropDownReceiver(MapView mapView, Context pluginContext) {
        super(mapView);
        
        LayoutInflater inflater = LayoutInflater.from(pluginContext);
        view = inflater.inflate(R.layout.my_dropdown, null);
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("com.example.myplugin.SHOW_DROPDOWN".equals(intent.getAction())) {
            showDropDown(view, HALF_WIDTH, FULL_HEIGHT, FULL_WIDTH, 
                        HALF_HEIGHT, false, this);
        }
    }
    
    @Override
    public void onDropDownVisible(boolean visible) {
        // Handle visibility changes
    }
}
```

### 2. Map Items

```java
// Create a marker
Marker marker = new Marker(new GeoPoint(latitude, longitude));
marker.setType("a-u-G");  // MIL-STD-2525 type
marker.setTitle("My Marker");
marker.setMetaString("callsign", "MARKER-1");
marker.setColor(Color.RED);

// Add to map
MapView.getMapView().getRootGroup().addItem(marker);
```

### 3. Tools (Interactive Map Controls)

```java
public class MyDrawingTool extends Tool implements MapEventDispatcher.MapEventDispatchListener {
    
    public static final String TOOL_NAME = "com.example.myplugin.DRAW_TOOL";
    
    public MyDrawingTool(MapView mapView) {
        super(mapView, TOOL_NAME);
    }
    
    @Override
    public void onMapEvent(MapEvent event) {
        if (event.getType().equals(MapEvent.MAP_CLICK)) {
            GeoPoint point = event.getPoint();
            // Handle map click
        }
    }
    
    @Override
    protected void setActive(boolean active) {
        if (active) {
            _mapView.getMapEventDispatcher().addMapEventListener(this);
        } else {
            _mapView.getMapEventDispatcher().removeMapEventListener(this);
        }
    }
}
```

### 4. Preferences

```java
public class MyPreferenceFragment extends PluginPreferenceFragment {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
```

## Building and Testing

### Building the Plugin

```bash
# Debug build
./gradlew assembleCivDebug

# Release build (unsigned)
./gradlew assembleCivRelease

# Find APK in:
# app/build/outputs/apk/civ/debug/
```

### Testing with ATAK

1. **Install SDK ATAK** (debug-signed):
```bash
adb install sdk/atak.apk
```

2. **Install your plugin**:
```bash
adb install app/build/outputs/apk/civ/debug/*.apk
```

3. **View logs**:
```bash
adb logcat | grep -E "ATAK|MyPlugin"
```

## Distribution

### TAK.gov Submission

1. Create source archive:
```bash
./prepare-takgov-submission.sh
```

2. Upload to https://tak.gov/user_builds
3. Download signed APK when complete

### Direct Distribution

For development/testing only:
- Both ATAK and plugin must use same signature
- Use SDK's debug keystore for both

## Best Practices

### 1. Memory Management
- Dispose of map items when not needed
- Unregister receivers in onDestroy
- Avoid holding references to Context

### 2. Threading
- Use UI thread for map operations
- Background threads for network/heavy computation
- Consider using AtakBroadcast for IPC

### 3. Permissions
- Declare required permissions in manifest
- Handle runtime permissions appropriately

### 4. Error Handling
```java
try {
    // Plugin operations
} catch (Exception e) {
    Log.e(TAG, "Plugin error", e);
    // Graceful degradation
}
```

### 5. Logging
```java
import com.atakmap.coremap.log.Log;

public class MyPlugin {
    private static final String TAG = "MyPlugin";
    
    Log.d(TAG, "Debug message");
    Log.e(TAG, "Error message", exception);
}
```

## Common Issues

### 1. Plugin Not Loading
- Check AndroidManifest.xml has correct activity
- Verify plugin.xml is in assets/
- Ensure matching signatures

### 2. ClassNotFoundException
- Missing dependencies
- ProGuard stripping required classes
- Add to proguard-rules.txt:
```
-keep class com.example.myplugin.** { *; }
```

### 3. Build Failures
- Wrong SDK version
- Missing ATAK gradle plugin
- Check local.properties paths

### 4. Runtime Crashes
- Check logcat for stack traces
- Verify ATAK version compatibility
- Test on multiple devices

## Resources

- **Official Documentation**: SDK includes PDF guide
- **Sample Plugins**: In SDK samples/ directory
- **TAK.gov Forums**: Community support
- **GitHub Examples**: Search for "atak-plugin"

## Version Compatibility

| ATAK Version | SDK Version | Min Android API | Target API |
|--------------|-------------|-----------------|------------|
| 5.4.x        | 5.4.0       | 26 (Android 8)  | 34         |
| 5.3.x        | 5.3.0       | 24 (Android 7)  | 33         |
| 4.10.x       | 4.10.0      | 24 (Android 7)  | 31         |

---

Remember: Always test your plugin thoroughly before distribution!