package com.optisense.skyfi.atak;

import android.content.Context;
import android.content.Intent;

import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.log.Log;

/**
 * SkyFi Plugin Bridge - Universal entry point for all ATAK versions
 * This bridge provides maximum compatibility by not implementing any specific interfaces
 * and instead using reflection to work with whatever plugin system is available.
 */
public class SkyFiPluginBridge {
    
    private static final String TAG = "SkyFi.PluginBridge";
    private SkyFiMapComponent mapComponent;
    private Context pluginContext;
    private MapView mapView;
    
    /**
     * Static factory method for plugin loaders that need a specific class
     */
    public static Object createPlugin(Object... args) {
        Log.d(TAG, "createPlugin called with " + (args != null ? args.length : 0) + " arguments");
        
        // If we got a service controller, create wrapper
        if (args != null && args.length > 0 && args[0] != null) {
            try {
                // Check if we have IServiceController (without importing it)
                String className = args[0].getClass().getName();
                if (className.contains("ServiceController")) {
                    Log.d(TAG, "Detected ServiceController, creating wrapper");
                    return new SkyFiPluginWrapper(args[0]);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error checking service controller", e);
            }
        }
        
        // Otherwise create a simple bridge
        return new SkyFiPluginBridge();
    }
    
    /**
     * Initialize the plugin - called by various plugin systems
     */
    public void initialize(Context context, Intent intent, MapView view) {
        Log.d(TAG, "initialize called");
        this.pluginContext = context;
        this.mapView = view;
        
        if (mapComponent == null) {
            mapComponent = new SkyFiMapComponent();
            
            try {
                mapComponent.onCreate(context, intent, view);
                Log.d(TAG, "SkyFiMapComponent created successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error creating SkyFiMapComponent", e);
            }
        }
    }
    
    /**
     * Start the plugin - compatible with various plugin interfaces
     */
    public void onStart() {
        Log.d(TAG, "onStart called");
        
        // Try to get MapView if we don't have it
        if (mapView == null) {
            try {
                mapView = MapView.getMapView();
                if (mapView != null && pluginContext == null) {
                    pluginContext = mapView.getContext();
                }
                Log.d(TAG, "Got MapView and context from static instance");
            } catch (Exception e) {
                Log.e(TAG, "Failed to get MapView statically", e);
            }
        }
        
        // Initialize if we have what we need
        if (mapView != null && pluginContext != null && mapComponent == null) {
            Intent intent = new Intent();
            intent.setAction("com.optisense.skyfi.atak.PLUGIN_STARTED");
            initialize(pluginContext, intent, mapView);
        }
    }
    
    /**
     * Stop the plugin - compatible with various plugin interfaces
     */
    public void onStop() {
        Log.d(TAG, "onStop called");
        
        if (mapComponent != null && pluginContext != null && mapView != null) {
            try {
                mapComponent.onDestroyImpl(pluginContext, mapView);
                Log.d(TAG, "SkyFiMapComponent destroyed successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error destroying SkyFiMapComponent", e);
            }
            mapComponent = null;
        }
    }
    
    /**
     * Direct MapComponent accessor for legacy loaders
     */
    public SkyFiMapComponent getMapComponent() {
        if (mapComponent == null) {
            mapComponent = new SkyFiMapComponent();
        }
        return mapComponent;
    }
}