package com.skyfi.atak.plugin;

import android.content.Context;
import android.content.Intent;

import com.atak.plugins.impl.PluginContextProvider;
import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.log.Log;

import gov.tak.api.plugin.IPlugin;

/**
 * SkyFi Plugin Compatibility Wrapper - Works with ATAK 5.4.0.16
 * This wrapper provides compatibility without requiring IServiceController
 * which is not available in ATAK-CIV 5.4.0.16
 */
public class SkyFiPluginCompatWrapper implements IPlugin {
    
    private static final String TAG = "SkyFi.PluginCompat";
    private Context pluginContext;
    private SkyFiMapComponent mapComponent;
    private MapView mapView;
    
    /**
     * Default constructor for ATAK 5.4.0.16 compatibility
     */
    public SkyFiPluginCompatWrapper() {
        Log.d(TAG, "SkyFiPluginCompatWrapper constructor (no args) called");
    }
    
    /**
     * Constructor with context for direct initialization
     */
    public SkyFiPluginCompatWrapper(Context context) {
        Log.d(TAG, "SkyFiPluginCompatWrapper constructor (with context) called");
        this.pluginContext = context;
        initializeMapView();
    }
    
    /**
     * Initialize the plugin with a context
     */
    public void initialize(Context context) {
        Log.d(TAG, "initialize called with context");
        this.pluginContext = context;
        initializeMapView();
    }
    
    private void initializeMapView() {
        // Try to get MapView from the static instance
        try {
            mapView = MapView.getMapView();
            if (mapView != null) {
                Log.d(TAG, "MapView obtained from static instance");
            } else {
                Log.w(TAG, "MapView.getMapView() returned null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get MapView from static instance", e);
        }
    }
    
    @Override
    public void onStart() {
        Log.d(TAG, "onStart called");
        
        // If we don't have a context yet, try to get it from the MapView
        if (pluginContext == null && mapView != null) {
            pluginContext = mapView.getContext();
            Log.d(TAG, "Got context from MapView");
        }
        
        if (pluginContext == null) {
            Log.e(TAG, "Plugin context is null, cannot start");
            return;
        }
        
        // Try to get MapView again if we don't have it
        if (mapView == null) {
            initializeMapView();
        }
        
        // Create and initialize the MapComponent
        if (mapView != null) {
            mapComponent = new SkyFiMapComponent();
            Intent intent = new Intent();
            intent.setAction("com.skyfi.atak.plugin.PLUGIN_STARTED");
            
            try {
                mapComponent.onCreate(pluginContext, intent, mapView);
                Log.d(TAG, "SkyFiMapComponent created successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error creating SkyFiMapComponent", e);
            }
        } else {
            Log.e(TAG, "Cannot create MapComponent without MapView");
        }
    }
    
    @Override
    public void onStop() {
        Log.d(TAG, "onStop called");
        
        // Destroy the MapComponent
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
}