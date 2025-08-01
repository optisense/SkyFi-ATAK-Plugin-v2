package com.skyfi.atak.plugin;

import android.content.Context;
import android.content.SharedPreferences;
import com.atakmap.coremap.log.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages Areas of Interest (AOIs) including naming, storage, and retrieval
 */
public class AOIManager {
    private static final String LOGTAG = "AOIManager";
    private static final String PREFS_NAME = "skyfi_aoi_prefs";
    private static final String AOI_KEY = "saved_aois";
    
    private final Context context;
    private final SharedPreferences prefs;
    private Map<String, AOI> aoiMap;
    
    public static class AOI {
        public String id;
        public String name;
        public String wkt;
        public long timestamp;
        public double areaKm2;
        public String sensorType;
        
        public AOI(String id, String name, String wkt) {
            this.id = id;
            this.name = name;
            this.wkt = wkt;
            this.timestamp = System.currentTimeMillis();
        }
        
        public JSONObject toJSON() throws Exception {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("name", name);
            json.put("wkt", wkt);
            json.put("timestamp", timestamp);
            json.put("areaKm2", areaKm2);
            json.put("sensorType", sensorType);
            return json;
        }
        
        public static AOI fromJSON(JSONObject json) throws Exception {
            AOI aoi = new AOI(
                json.getString("id"),
                json.getString("name"),
                json.getString("wkt")
            );
            aoi.timestamp = json.getLong("timestamp");
            aoi.areaKm2 = json.optDouble("areaKm2", 0);
            aoi.sensorType = json.optString("sensorType", "");
            return aoi;
        }
    }
    
    public AOIManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.aoiMap = new HashMap<>();
        loadAOIs();
    }
    
    /**
     * Save a new AOI or update existing one
     */
    public void saveAOI(AOI aoi) {
        aoiMap.put(aoi.id, aoi);
        persistAOIs();
    }
    
    /**
     * Get AOI by ID
     */
    public AOI getAOI(String id) {
        return aoiMap.get(id);
    }
    
    /**
     * Get all saved AOIs
     */
    public ArrayList<AOI> getAllAOIs() {
        return new ArrayList<>(aoiMap.values());
    }
    
    /**
     * Delete an AOI
     */
    public void deleteAOI(String id) {
        aoiMap.remove(id);
        persistAOIs();
    }
    
    /**
     * Rename an AOI
     */
    public void renameAOI(String id, String newName) {
        AOI aoi = aoiMap.get(id);
        if (aoi != null) {
            aoi.name = newName;
            persistAOIs();
        }
    }
    
    /**
     * Generate a unique ID for a new AOI
     */
    public String generateAOIId() {
        return "aoi_" + System.currentTimeMillis();
    }
    
    /**
     * Calculate minimum AOI size based on sensor type
     */
    public static double getMinimumAOISize(String sensorType) {
        // Minimum sizes in km²
        switch (sensorType.toLowerCase()) {
            case "siwei":
                return 25.0; // 5km x 5km
            case "satellogic":
                return 100.0; // 10km x 10km
            case "umbra":
                return 16.0; // 4km x 4km
            case "geosat":
                return 25.0; // 5km x 5km
            case "planet":
                return 100.0; // 10km x 10km
            case "impro":
                return 64.0; // 8km x 8km
            default:
                return 25.0; // Default 5km x 5km
        }
    }
    
    /**
     * Get sensor requirements text
     */
    public static String getSensorRequirements(String sensorType) {
        double minSize = getMinimumAOISize(sensorType);
        double sideLength = Math.sqrt(minSize);
        return String.format("Minimum AOI: %.1f km x %.1f km (%.0f km²)", 
            sideLength, sideLength, minSize);
    }
    
    private void loadAOIs() {
        try {
            String jsonString = prefs.getString(AOI_KEY, "[]");
            JSONArray jsonArray = new JSONArray(jsonString);
            
            aoiMap.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                AOI aoi = AOI.fromJSON(jsonArray.getJSONObject(i));
                aoiMap.put(aoi.id, aoi);
            }
        } catch (Exception e) {
            Log.e(LOGTAG, "Failed to load AOIs", e);
        }
    }
    
    private void persistAOIs() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (AOI aoi : aoiMap.values()) {
                jsonArray.put(aoi.toJSON());
            }
            
            prefs.edit()
                .putString(AOI_KEY, jsonArray.toString())
                .apply();
        } catch (Exception e) {
            Log.e(LOGTAG, "Failed to persist AOIs", e);
        }
    }
}