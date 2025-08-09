package com.optisense.skyfi.atak;

import android.content.Context;
import android.content.Intent;

import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.log.Log;

import java.lang.reflect.Method;

/**
 * SkyFi Plugin Wrapper - Simple wrapper for Play Store ATAK compatibility
 * This wrapper uses reflection to work with any available plugin interface
 * without directly depending on gov.tak APIs that aren't in Play Store ATAK
 */
public class SkyFiPluginWrapper {
    
    private static final String TAG = "SkyFi.PluginWrapper";
    private Context pluginContext;
    private SkyFiMapComponent mapComponent;
    private MapView mapView;
    private Object serviceController;
    
    /**
     * Default constructor for reflection-based instantiation
     */
    public SkyFiPluginWrapper() {
        Log.d(TAG, "SkyFiPluginWrapper default constructor called");
    }
    
    /**
     * Constructor that accepts a service controller object
     * Uses reflection to avoid direct dependency on IServiceController
     */
    public SkyFiPluginWrapper(Object serviceController) {
        Log.d(TAG, "SkyFiPluginWrapper constructor with service controller called");
        this.serviceController = serviceController;
        
        // Try to get plugin context via reflection
        try {
            // Try to get PluginContextProvider through reflection
            Method getServiceMethod = serviceController.getClass().getMethod("getService", Class.class);
            
            // Try to get the PluginContextProvider class
            Class<?> providerClass = Class.forName("com.atak.plugins.impl.PluginContextProvider");
            Object ctxProvider = getServiceMethod.invoke(serviceController, providerClass);
            
            if (ctxProvider != null) {
                Method getContextMethod = ctxProvider.getClass().getMethod("getPluginContext");
                pluginContext = (Context) getContextMethod.invoke(ctxProvider);
                Log.d(TAG, "Plugin context obtained via reflection");
            }
            
            // Try to get MapView
            Object mapViewObj = getServiceMethod.invoke(serviceController, MapView.class);
            if (mapViewObj instanceof MapView) {
                mapView = (MapView) mapViewObj;
                Log.d(TAG, "MapView obtained from service controller");
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not get context/mapview via reflection: " + e.getMessage());
        }
        
        // Fallback to static MapView if needed
        if (mapView == null) {
            try {
                mapView = MapView.getMapView();
                Log.d(TAG, "MapView obtained from static instance");
            } catch (Exception e) {
                Log.e(TAG, "Failed to get MapView from static instance", e);
            }
        }
    }
    
    /**
     * Called when plugin should start - compatible with IPlugin.onStart()
     */
    public void onStart() {
        Log.d(TAG, "onStart called");
        
        // If we don't have a context, try to get it from MapView
        if (pluginContext == null && mapView != null) {
            pluginContext = mapView.getContext();
            Log.d(TAG, "Using MapView context as plugin context");
        }
        
        // Last resort - try to get MapView statically
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
        
        // Create and initialize the MapComponent
        if (mapView != null && pluginContext != null) {
            mapComponent = new SkyFiMapComponent();
            Intent intent = new Intent();
            intent.setAction("com.optisense.skyfi.atak.PLUGIN_STARTED");
            
            try {
                mapComponent.onCreate(pluginContext, intent, mapView);
                Log.d(TAG, "SkyFiMapComponent created successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error creating SkyFiMapComponent", e);
            }
        } else {
            Log.e(TAG, "Cannot create MapComponent - mapView: " + (mapView != null) + 
                      ", context: " + (pluginContext != null));
        }
    }
    
    /**
     * Called when plugin should stop - compatible with IPlugin.onStop()
     */
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
    
    /**
     * Alternative entry point for direct MapComponent creation
     * Used when the plugin is loaded as a simple MapComponent
     */
    public static SkyFiMapComponent createMapComponent() {
        Log.d(TAG, "Creating MapComponent directly");
        return new SkyFiMapComponent();
    }
}