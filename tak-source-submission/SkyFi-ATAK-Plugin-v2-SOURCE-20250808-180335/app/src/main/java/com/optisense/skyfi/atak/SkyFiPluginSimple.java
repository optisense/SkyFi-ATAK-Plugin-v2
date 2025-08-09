package com.optisense.skyfi.atak;

import android.util.Log;

/**
 * Simplest possible plugin implementation for Play Store ATAK
 * This class has NO dependencies on ATAK SDK classes
 */
public class SkyFiPluginSimple {
    
    private static final String TAG = "SkyFi.Plugin";
    
    public SkyFiPluginSimple() {
        Log.d(TAG, "SkyFiPluginSimple constructor called");
    }
    
    // This will be called via reflection by ATAK
    public void onStart() {
        Log.d(TAG, "Plugin started - SkyFi ATAK Plugin v2.0-beta5");
        try {
            // Use reflection to create the MapComponent
            Class<?> componentClass = Class.forName("com.optisense.skyfi.atak.SkyFiMapComponent");
            Object component = componentClass.newInstance();
            Log.d(TAG, "MapComponent created via reflection");
        } catch (Exception e) {
            Log.e(TAG, "Failed to create MapComponent: " + e.getMessage());
        }
    }
    
    public void onStop() {
        Log.d(TAG, "Plugin stopped");
    }
}