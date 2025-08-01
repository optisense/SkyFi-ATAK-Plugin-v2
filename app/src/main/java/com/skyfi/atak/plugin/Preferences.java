package com.skyfi.atak.plugin;

import com.atakmap.android.maps.MapView;
import com.atakmap.android.preference.AtakPreferences;

public class Preferences {
    public static final String PREF_API_KEY = "pref_api_key";
    public static final String PREF_DEFAULT_OPACITY = "pref_default_opacity";
    
    private String apiKey;
    private int defaultOpacity;
    private AtakPreferences prefs;

    public Preferences() {
        prefs = AtakPreferences.getInstance(MapView.getMapView().getContext());
        apiKey = prefs.get(PREF_API_KEY, "");
        defaultOpacity = prefs.get(PREF_DEFAULT_OPACITY, 100);
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
        prefs.set(PREF_API_KEY, apiKey);
    }
    
    public int getDefaultOpacity() {
        return defaultOpacity;
    }
    
    public void setDefaultOpacity(int opacity) {
        this.defaultOpacity = Math.max(20, Math.min(100, opacity));
        prefs.set(PREF_DEFAULT_OPACITY, this.defaultOpacity);
    }
    
    public float getDefaultOpacityAsAlpha() {
        return defaultOpacity / 100.0f;
    }
}
