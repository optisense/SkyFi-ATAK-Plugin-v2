package com.skyfi.atak.plugin;

import com.atakmap.android.maps.MapView;
import com.atakmap.android.preference.AtakPreferences;

public class Preferences {
    public static final String PREF_API_KEY = "pref_api_key";
    private String apiKey;
    private AtakPreferences prefs;

    public Preferences() {
        prefs = AtakPreferences.getInstance(MapView.getMapView().getContext());
        apiKey = prefs.get(PREF_API_KEY, "");
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
        prefs.set(PREF_API_KEY, apiKey);
    }
}
