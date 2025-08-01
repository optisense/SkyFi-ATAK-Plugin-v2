package com.skyfi.atak.plugin;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.atakmap.coremap.maps.coords.GeoPoint;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AOIManager {
    private static final String TAG = "SkyFi.AOIManager";
    private static final String PREF_KEY_AOIS = "skyfi_saved_aois";
    
    // Minimum AOI sizes per sensor type (in square kilometers)
    private static final Map<String, Double> SENSOR_MIN_AREAS = new HashMap<>();
    static {
        SENSOR_MIN_AREAS.put("optical", 0.25);
        SENSOR_MIN_AREAS.put("sar", 1.0);
        SENSOR_MIN_AREAS.put("hyperspectral", 4.0);
        SENSOR_MIN_AREAS.put("default", 0.25);
    }
    
    private final Context context;
    private final SharedPreferences prefs;
    private final Gson gson;
    private Map<String, AOI> savedAOIs;
    
    public static class AOI {
        public String id;
        public String name;
        public List<GeoPoint> points;
        public double areaSqKm;
        public String sensorType;
        public long createdTimestamp;
        public long modifiedTimestamp;
        
        public AOI() {
            this.id = UUID.randomUUID().toString();
            this.createdTimestamp = System.currentTimeMillis();
            this.modifiedTimestamp = this.createdTimestamp;
        }
        
        public AOI(String name, List<GeoPoint> points, double areaSqKm) {
            this();
            this.name = name;
            this.points = points;
            this.areaSqKm = areaSqKm;
            this.sensorType = "default";
        }
    }
    
    public AOIManager(Context context) {
        this.context = context;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.gson = new Gson();
        loadAOIs();
    }
    
    private void loadAOIs() {
        String json = prefs.getString(PREF_KEY_AOIS, "{}");
        Type type = new TypeToken<Map<String, AOI>>(){}.getType();
        savedAOIs = gson.fromJson(json, type);
        if (savedAOIs == null) {
            savedAOIs = new HashMap<>();
        }
    }
    
    private void saveAOIs() {
        String json = gson.toJson(savedAOIs);
        prefs.edit().putString(PREF_KEY_AOIS, json).apply();
    }
    
    public AOI createAOI(String name, List<GeoPoint> points, double areaSqKm, String sensorType) {
        AOI aoi = new AOI(name, points, areaSqKm);
        aoi.sensorType = sensorType != null ? sensorType : "default";
        
        savedAOIs.put(aoi.id, aoi);
        saveAOIs();
        
        Log.d(TAG, "Created AOI: " + name + " with area: " + areaSqKm + " sq km");
        return aoi;
    }
    
    public boolean renameAOI(String aoiId, String newName) {
        AOI aoi = savedAOIs.get(aoiId);
        if (aoi != null) {
            aoi.name = newName;
            aoi.modifiedTimestamp = System.currentTimeMillis();
            saveAOIs();
            Log.d(TAG, "Renamed AOI " + aoiId + " to: " + newName);
            return true;
        }
        return false;
    }
    
    public boolean deleteAOI(String aoiId) {
        if (savedAOIs.remove(aoiId) != null) {
            saveAOIs();
            Log.d(TAG, "Deleted AOI: " + aoiId);
            return true;
        }
        return false;
    }
    
    public List<AOI> getAllAOIs() {
        return new ArrayList<>(savedAOIs.values());
    }
    
    public AOI getAOI(String aoiId) {
        return savedAOIs.get(aoiId);
    }
    
    public double getMinimumAreaForSensor(String sensorType) {
        return SENSOR_MIN_AREAS.getOrDefault(sensorType, SENSOR_MIN_AREAS.get("default"));
    }
    
    public static double getMinimumAreaForPoint(GeoPoint center, String sensorType) {
        // Get minimum area requirement
        double minArea = SENSOR_MIN_AREAS.getOrDefault(sensorType, SENSOR_MIN_AREAS.get("default"));
        
        // Calculate radius needed for minimum area (area = π * r²)
        double radiusKm = Math.sqrt(minArea / Math.PI);
        
        return radiusKm;
    }
    
    public static List<GeoPoint> createMinimumAOIAroundPoint(GeoPoint center, String sensorType) {
        double radiusKm = getMinimumAreaForPoint(center, sensorType);
        
        // Create a square AOI around the point
        List<GeoPoint> points = new ArrayList<>();
        
        // Convert radius to degrees (approximate)
        double latDelta = radiusKm / 111.0; // 1 degree latitude ≈ 111 km
        double lonDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(center.getLatitude())));
        
        // Create square corners
        points.add(new GeoPoint(center.getLatitude() + latDelta, center.getLongitude() - lonDelta));
        points.add(new GeoPoint(center.getLatitude() + latDelta, center.getLongitude() + lonDelta));
        points.add(new GeoPoint(center.getLatitude() - latDelta, center.getLongitude() + lonDelta));
        points.add(new GeoPoint(center.getLatitude() - latDelta, center.getLongitude() - lonDelta));
        
        return points;
    }
}