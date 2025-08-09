package com.optisense.skyfi.atak;

import com.atakmap.android.maps.MapView;
import com.atakmap.android.preference.AtakPreferences;

public class Preferences {
    public static final String PREF_API_KEY = "pref_api_key";
    public static final String PREF_LAYER_OPACITY_PREFIX = "pref_layer_opacity_";
    public static final int DEFAULT_OPACITY = 80;
    
    // AI Configuration preferences
    public static final String PREF_GOOGLE_PROJECT_ID = "pref_google_project_id";
    public static final String PREF_GOOGLE_ACCESS_TOKEN = "pref_google_access_token";
    public static final String PREF_TAK_SERVER_URL = "pref_tak_server_url";
    public static final String PREF_CLIENT_CERT_PATH = "pref_client_cert_path";
    public static final String PREF_CLIENT_KEY_PATH = "pref_client_key_path";
    public static final String PREF_AI_ENABLED = "pref_ai_enabled";
    public static final String PREF_OFFLINE_AI_ENABLED = "pref_offline_ai_enabled";
    public static final String PREF_AI_CONFIDENCE_THRESHOLD = "pref_ai_confidence_threshold";
    public static final String PREF_AI_CACHE_SIZE_MB = "pref_ai_cache_size_mb";
    
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
    
    public int getLayerOpacity(String layerName) {
        return prefs.get(PREF_LAYER_OPACITY_PREFIX + layerName, DEFAULT_OPACITY);
    }
    
    public void setLayerOpacity(String layerName, int opacity) {
        prefs.set(PREF_LAYER_OPACITY_PREFIX + layerName, opacity);
    }
    
    // AI Configuration methods
    
    public String getGoogleProjectId() {
        return prefs.get(PREF_GOOGLE_PROJECT_ID, "");
    }
    
    public void setGoogleProjectId(String projectId) {
        prefs.set(PREF_GOOGLE_PROJECT_ID, projectId);
    }
    
    public String getGoogleAccessToken() {
        return prefs.get(PREF_GOOGLE_ACCESS_TOKEN, "");
    }
    
    public void setGoogleAccessToken(String accessToken) {
        prefs.set(PREF_GOOGLE_ACCESS_TOKEN, accessToken);
    }
    
    public String getTakServerUrl() {
        return prefs.get(PREF_TAK_SERVER_URL, "wss://your-tak-server:8089");
    }
    
    public void setTakServerUrl(String serverUrl) {
        prefs.set(PREF_TAK_SERVER_URL, serverUrl);
    }
    
    public String getClientCertPath() {
        return prefs.get(PREF_CLIENT_CERT_PATH, "");
    }
    
    public void setClientCertPath(String certPath) {
        prefs.set(PREF_CLIENT_CERT_PATH, certPath);
    }
    
    public String getClientKeyPath() {
        return prefs.get(PREF_CLIENT_KEY_PATH, "");
    }
    
    public void setClientKeyPath(String keyPath) {
        prefs.set(PREF_CLIENT_KEY_PATH, keyPath);
    }
    
    public boolean isAIEnabled() {
        return prefs.get(PREF_AI_ENABLED, true);
    }
    
    public void setAIEnabled(boolean enabled) {
        prefs.set(PREF_AI_ENABLED, enabled);
    }
    
    public boolean isOfflineAIEnabled() {
        return prefs.get(PREF_OFFLINE_AI_ENABLED, true);
    }
    
    public void setOfflineAIEnabled(boolean enabled) {
        prefs.set(PREF_OFFLINE_AI_ENABLED, enabled);
    }
    
    public double getAIConfidenceThreshold() {
        return prefs.get(PREF_AI_CONFIDENCE_THRESHOLD, 0.7);
    }
    
    public void setAIConfidenceThreshold(double threshold) {
        prefs.set(PREF_AI_CONFIDENCE_THRESHOLD, threshold);
    }
    
    public long getAICacheSizeMB() {
        return prefs.get(PREF_AI_CACHE_SIZE_MB, 50L);
    }
    
    public void setAICacheSizeMB(long sizeMB) {
        prefs.set(PREF_AI_CACHE_SIZE_MB, sizeMB);
    }
}
