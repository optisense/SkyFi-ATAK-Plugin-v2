package com.skyfi.atak.plugin;

import android.content.Context;
import android.content.Intent;

import com.atak.plugins.impl.PluginContextProvider;
import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.log.Log;

import gov.tak.api.plugin.IPlugin;
import gov.tak.api.plugin.IServiceController;

/**
 * SkyFi Plugin Wrapper - IPlugin implementation for ATAK 5.4+
 * This wrapper provides compatibility with the newer IPlugin interface
 * while maintaining backward compatibility through SkyFiMapComponent
 */
public class SkyFiPluginWrapper implements IPlugin {
    
    private static final String TAG = "SkyFi.PluginWrapper";
    private IServiceController serviceController;
    private Context pluginContext;
    private SkyFiMapComponent mapComponent;
    private MapView mapView;
    
    public SkyFiPluginWrapper(IServiceController serviceController) {
        Log.d(TAG, "SkyFiPluginWrapper constructor called");
        this.serviceController = serviceController;
        
        // Get plugin context
        final PluginContextProvider ctxProvider = serviceController
                .getService(PluginContextProvider.class);
        if (ctxProvider != null) {
            pluginContext = ctxProvider.getPluginContext();
            Log.d(TAG, "Plugin context obtained");
        } else {
            Log.e(TAG, "Failed to get PluginContextProvider");
        }
        
        // Get MapView from service controller
        try {
            // Try to get MapView through reflection or service
            Object mapService = serviceController.getService("com.atakmap.android.maps.MapView");
            if (mapService instanceof MapView) {
                mapView = (MapView) mapService;
                Log.d(TAG, "MapView obtained from service controller");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get MapView from service controller", e);
        }
        
        // If we couldn't get MapView from service, try to get it from the static instance
        if (mapView == null) {
            try {
                mapView = MapView.getMapView();
                Log.d(TAG, "MapView obtained from static instance");
            } catch (Exception e) {
                Log.e(TAG, "Failed to get MapView from static instance", e);
            }
        }
    }
    
    @Override
    public void onStart() {
        Log.d(TAG, "onStart called");
        
        if (pluginContext == null) {
            Log.e(TAG, "Plugin context is null, cannot start");
            return;
        }
        
        if (mapView == null) {
            Log.w(TAG, "MapView is null, attempting to get it again");
            try {
                mapView = MapView.getMapView();
            } catch (Exception e) {
                Log.e(TAG, "Still cannot get MapView", e);
            }
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