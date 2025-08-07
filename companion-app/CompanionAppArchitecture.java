package com.skyfi.atak.companion;

import android.content.Intent;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

/**
 * SkyFi ATAK Companion App Architecture
 * 
 * This companion app provides SkyFi functionality for Play Store ATAK users
 * when the traditional plugin approach fails due to signature mismatches.
 * 
 * Architecture Components:
 * 1. Companion App (standalone Android app)
 * 2. ATAK Communication Bridge (AIDL/Intent-based)
 * 3. SkyFi Service Layer (shared business logic)
 * 4. Data Exchange Protocol (CoT messages and intents)
 * 
 * Key Features:
 * - Works with any ATAK version (SDK or Play Store)
 * - Provides full SkyFi functionality outside ATAK
 * - Seamless integration through Android's inter-app communication
 * - Maintains ATAK context awareness through CoT messaging
 */
public class CompanionAppArchitecture {
    private static final String TAG = "SkyFiCompanion";
    
    // ATAK package names for different versions
    public static final String ATAK_CIV_PACKAGE = "com.atakmap.app.civ";
    public static final String ATAK_MIL_PACKAGE = "com.atakmap.app";
    public static final String ATAK_PLAYSTORE_PACKAGE = "com.atakmap.app.civ";
    
    // CoT messaging constants
    public static final String COT_TYPE_SKYFI_REQUEST = "a-f-G-U-C-skyfi-request";
    public static final String COT_TYPE_SKYFI_RESPONSE = "a-f-G-U-C-skyfi-response";
    public static final String COT_TYPE_SKYFI_ORDER = "a-f-G-U-C-skyfi-order";
    
    // Intent actions for ATAK communication
    public static final String ACTION_ATAK_COT_EVENT = "com.atakmap.app.EXTERNAL_COT_EVENT";
    public static final String ACTION_SKYFI_ORDER_REQUEST = "com.skyfi.atak.companion.ORDER_REQUEST";
    public static final String ACTION_SKYFI_MAP_DATA = "com.skyfi.atak.companion.MAP_DATA";
    
    private Context context;
    private ATAKCommunicationBridge atakBridge;
    private SkyFiServiceManager serviceManager;
    
    public CompanionAppArchitecture(Context context) {
        this.context = context;
        this.atakBridge = new ATAKCommunicationBridge(context);
        this.serviceManager = new SkyFiServiceManager(context);
    }
    
    /**
     * Initialize the companion app and establish ATAK communication
     */
    public void initialize() {
        Log.i(TAG, "Initializing SkyFi Companion App");
        
        // Check for ATAK installation
        String atakPackage = detectATAKVersion();
        if (atakPackage == null) {
            Log.w(TAG, "No ATAK installation detected");
            // Show user guidance to install ATAK
            return;
        }
        
        Log.i(TAG, "Detected ATAK package: " + atakPackage);
        
        // Initialize communication bridge
        atakBridge.initialize(atakPackage);
        
        // Start SkyFi services
        serviceManager.startServices();
        
        // Register for ATAK events
        registerATAKListeners();
    }
    
    /**
     * Detect installed ATAK version
     */
    private String detectATAKVersion() {
        PackageManager pm = context.getPackageManager();
        
        // Check for Play Store ATAK first
        if (isPackageInstalled(pm, ATAK_PLAYSTORE_PACKAGE)) {
            return ATAK_PLAYSTORE_PACKAGE;
        }
        
        // Check for military ATAK
        if (isPackageInstalled(pm, ATAK_MIL_PACKAGE)) {
            return ATAK_MIL_PACKAGE;
        }
        
        // Check for civilian SDK ATAK
        if (isPackageInstalled(pm, ATAK_CIV_PACKAGE)) {
            return ATAK_CIV_PACKAGE;
        }
        
        return null;
    }
    
    private boolean isPackageInstalled(PackageManager pm, String packageName) {
        try {
            pm.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Register listeners for ATAK events and user interactions
     */
    private void registerATAKListeners() {
        // Register for CoT events from ATAK
        atakBridge.registerCoTListener(new ATAKCommunicationBridge.CoTEventListener() {
            @Override
            public void onCoTEvent(String cotXml) {
                handleATAKCoTEvent(cotXml);
            }
        });
        
        // Register for map interaction events
        atakBridge.registerMapEventListener(new ATAKCommunicationBridge.MapEventListener() {
            @Override
            public void onMapLocationSelected(double lat, double lon) {
                handleMapLocationSelection(lat, lon);
            }
        });
    }
    
    /**
     * Handle CoT events received from ATAK
     */
    private void handleATAKCoTEvent(String cotXml) {
        Log.d(TAG, "Received CoT event from ATAK: " + cotXml);
        
        // Parse CoT XML to extract relevant information
        CoTEvent event = CoTParser.parse(cotXml);
        
        if (event.getType().startsWith("a-f-G-U-C-skyfi")) {
            // Handle SkyFi-specific CoT events
            processSkyFiCoTEvent(event);
        } else if (event.getType().equals("a-f-G-E-V-A")) {
            // Handle shape/area selection events
            processAreaSelectionEvent(event);
        }
    }
    
    /**
     * Process SkyFi-specific CoT events
     */
    private void processSkyFiCoTEvent(CoTEvent event) {
        switch (event.getType()) {
            case COT_TYPE_SKYFI_REQUEST:
                handleSkyFiRequest(event);
                break;
            case COT_TYPE_SKYFI_ORDER:
                handleSkyFiOrder(event);
                break;
            default:
                Log.w(TAG, "Unknown SkyFi CoT event type: " + event.getType());
        }
    }
    
    /**
     * Handle map location selection from ATAK
     */
    private void handleMapLocationSelection(double lat, double lon) {
        Log.d(TAG, String.format("Map location selected: %.6f, %.6f", lat, lon));
        
        // Launch SkyFi order interface with the selected location
        Intent intent = new Intent(context, SkyFiOrderActivity.class);
        intent.putExtra("latitude", lat);
        intent.putExtra("longitude", lon);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    
    /**
     * Send satellite imagery order back to ATAK as CoT
     */
    public void sendOrderResultToATAK(SatelliteOrder order) {
        // Create CoT message for the order result
        String cotXml = createOrderResultCoT(order);
        
        // Send to ATAK via communication bridge
        atakBridge.sendCoTMessage(cotXml);
        
        // Also send via intent for broader compatibility
        Intent intent = new Intent(ACTION_ATAK_COT_EVENT);
        intent.putExtra("cot", cotXml);
        context.sendBroadcast(intent);
    }
    
    /**
     * Create CoT XML for order result
     */
    private String createOrderResultCoT(SatelliteOrder order) {
        return String.format(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<event version=\"2.0\" uid=\"%s\" type=\"%s\" time=\"%s\" start=\"%s\" stale=\"%s\">" +
                "<point lat=\"%.6f\" lon=\"%.6f\" hae=\"0.0\" ce=\"9999999.0\" le=\"9999999.0\" />" +
                "<detail>" +
                    "<skyfi>" +
                        "<order id=\"%s\" status=\"%s\" />" +
                        "<imagery url=\"%s\" />" +
                        "<metadata resolution=\"%.2f\" cloud_cover=\"%.1f\" />" +
                    "</skyfi>" +
                "</detail>" +
            "</event>",
            order.getId(),
            COT_TYPE_SKYFI_RESPONSE,
            getCurrentTimeStamp(),
            getCurrentTimeStamp(),
            getStaleTimeStamp(),
            order.getLatitude(),
            order.getLongitude(),
            order.getId(),
            order.getStatus(),
            order.getImageryUrl(),
            order.getResolution(),
            order.getCloudCover()
        );
    }
    
    /**
     * Launch SkyFi companion app from ATAK context menu
     */
    public static void launchFromATAK(Context atakContext, double lat, double lon) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(
            "com.skyfi.atak.companion",
            "com.skyfi.atak.companion.MainActivity"
        ));
        intent.putExtra("latitude", lat);
        intent.putExtra("longitude", lon);
        intent.putExtra("source", "atak");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        try {
            atakContext.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch SkyFi Companion App", e);
            
            // Fallback: Open Play Store to install companion app
            Intent playStoreIntent = new Intent(Intent.ACTION_VIEW);
            playStoreIntent.setData(Uri.parse("market://details?id=com.skyfi.atak.companion"));
            playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            atakContext.startActivity(playStoreIntent);
        }
    }
    
    // Utility methods
    private String getCurrentTimeStamp() {
        return java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
    }
    
    private String getStaleTimeStamp() {
        // Set stale time to 24 hours from now
        long staleTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000);
        return java.text.DateFormat.getDateTimeInstance().format(new java.util.Date(staleTime));
    }
    
    // Inner classes and interfaces
    private void handleSkyFiRequest(CoTEvent event) { /* Implementation */ }
    private void handleSkyFiOrder(CoTEvent event) { /* Implementation */ }
    private void processAreaSelectionEvent(CoTEvent event) { /* Implementation */ }
}

/**
 * Communication bridge for ATAK integration
 */
class ATAKCommunicationBridge {
    private static final String TAG = "ATAKBridge";
    private Context context;
    private String atakPackage;
    
    public ATAKCommunicationBridge(Context context) {
        this.context = context;
    }
    
    public void initialize(String atakPackage) {
        this.atakPackage = atakPackage;
        Log.i(TAG, "Initializing ATAK communication bridge for: " + atakPackage);
    }
    
    public void sendCoTMessage(String cotXml) {
        Intent intent = new Intent(CompanionAppArchitecture.ACTION_ATAK_COT_EVENT);
        intent.setPackage(atakPackage);
        intent.putExtra("cot", cotXml);
        context.sendBroadcast(intent);
    }
    
    public void registerCoTListener(CoTEventListener listener) {
        // Register broadcast receiver for CoT events
        // Implementation would use BroadcastReceiver
    }
    
    public void registerMapEventListener(MapEventListener listener) {
        // Register for map interaction events
        // Implementation would use intent filters
    }
    
    interface CoTEventListener {
        void onCoTEvent(String cotXml);
    }
    
    interface MapEventListener {
        void onMapLocationSelected(double lat, double lon);
    }
}

/**
 * Service manager for SkyFi business logic
 */
class SkyFiServiceManager {
    private Context context;
    
    public SkyFiServiceManager(Context context) {
        this.context = context;
    }
    
    public void startServices() {
        // Start background services for SkyFi functionality
        // Order processing, API communication, etc.
    }
}

/**
 * CoT event data structure
 */
class CoTEvent {
    private String type;
    private String uid;
    private double latitude;
    private double longitude;
    private String xml;
    
    public String getType() { return type; }
    public String getUid() { return uid; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getXml() { return xml; }
    
    // Setters and additional methods...
}

/**
 * CoT XML parser
 */
class CoTParser {
    public static CoTEvent parse(String cotXml) {
        // Parse CoT XML and extract relevant information
        // Implementation would use XML parsing library
        return new CoTEvent();
    }
}

/**
 * Satellite order data structure
 */
class SatelliteOrder {
    private String id;
    private double latitude;
    private double longitude;
    private String status;
    private String imageryUrl;
    private double resolution;
    private double cloudCover;
    
    // Getters and setters
    public String getId() { return id; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getStatus() { return status; }
    public String getImageryUrl() { return imageryUrl; }
    public double getResolution() { return resolution; }
    public double getCloudCover() { return cloudCover; }
}