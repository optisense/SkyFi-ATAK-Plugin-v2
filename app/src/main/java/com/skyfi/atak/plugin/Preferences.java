package com.skyfi.atak.plugin;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.atakmap.android.maps.MapView;

public class Preferences {
    public static final String PREF_API_KEY = "pref_api_key";
    private String apiKey;

    public Preferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MapView.getMapView().getContext());
        apiKey = prefs.getString(PREF_API_KEY, "");
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
