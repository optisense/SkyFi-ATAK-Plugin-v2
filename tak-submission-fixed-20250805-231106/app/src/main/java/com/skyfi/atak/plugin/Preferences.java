package com.skyfi.atak.plugin;

import com.atakmap.android.maps.MapView;
import com.atakmap.android.preference.AtakPreferences;

public class Preferences {
    public static final String PREF_API_KEY = "pref_api_key";
    public static final String PREF_LAYER_OPACITY_PREFIX = "pref_layer_opacity_";
    public static final int DEFAULT_OPACITY = 80;
    
    private String apiKey;
    private AtakPreferences prefs;

    public Preferences() {
        MapView mapView = MapView.getMapView();
        if (mapView != null && mapView.getContext() != null) {
            prefs = AtakPreferences.getInstance(mapView.getContext());
            apiKey = prefs.get(PREF_API_KEY, "");
        } else {
            // Fallback - use empty API key if MapView not ready
            apiKey = "";
        }
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
        if (prefs != null) {
            prefs.set(PREF_API_KEY, apiKey);
        }
    }
    
    public int getLayerOpacity(String layerName) {
        if (prefs != null) {
            return prefs.get(PREF_LAYER_OPACITY_PREFIX + layerName, DEFAULT_OPACITY);
        }
        return DEFAULT_OPACITY;
    }
    
    public void setLayerOpacity(String layerName, int opacity) {
        if (prefs != null) {
            prefs.set(PREF_LAYER_OPACITY_PREFIX + layerName, opacity);
        }
    }
}
