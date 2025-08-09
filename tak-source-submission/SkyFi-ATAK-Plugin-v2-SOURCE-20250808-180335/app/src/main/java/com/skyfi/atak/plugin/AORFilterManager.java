package com.skyfi.atak.plugin;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.atakmap.coremap.maps.coords.GeoPoint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.skyfi.atak.plugin.skyfiapi.Archive;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTReader;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AORFilterManager {
    private static final String TAG = "SkyFi.AORFilter";
    private static final String PREF_KEY_AORS = "skyfi_aor_definitions";
    
    private final Context context;
    private final SharedPreferences prefs;
    private final Gson gson;
    private Map<String, AORDefinition> aorDefinitions;
    private String selectedAOR = "all";
    
    public static class AORDefinition {
        public String name;
        public String description;
        public List<GeoPoint> boundaryPoints;
        public String wktPolygon;
        
        public AORDefinition(String name, String description, List<GeoPoint> boundaryPoints) {
            this.name = name;
            this.description = description;
            this.boundaryPoints = boundaryPoints;
            this.wktPolygon = convertToWKT(boundaryPoints);
        }
        
        private String convertToWKT(List<GeoPoint> points) {
            if (points == null || points.size() < 3) return null;
            
            StringBuilder wkt = new StringBuilder("POLYGON((");
            for (int i = 0; i < points.size(); i++) {
                if (i > 0) wkt.append(", ");
                GeoPoint point = points.get(i);
                wkt.append(point.getLongitude()).append(" ").append(point.getLatitude());
            }
            // Close the polygon
            GeoPoint firstPoint = points.get(0);
            wkt.append(", ").append(firstPoint.getLongitude()).append(" ").append(firstPoint.getLatitude());
            wkt.append("))");
            return wkt.toString();
        }
    }
    
    public AORFilterManager(Context context) {
        this.context = context;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
        // Configure Gson to handle NaN and special floating point values
        this.gson = new GsonBuilder()
                .serializeSpecialFloatingPointValues()
                .create();
        loadAORDefinitions();
        initializeDefaultAORs();
    }
    
    private void loadAORDefinitions() {
        String json = prefs.getString(PREF_KEY_AORS, "{}");
        Type type = new TypeToken<Map<String, AORDefinition>>(){}.getType();
        aorDefinitions = gson.fromJson(json, type);
        if (aorDefinitions == null) {
            aorDefinitions = new HashMap<>();
        }
    }
    
    private void saveAORDefinitions() {
        String json = gson.toJson(aorDefinitions);
        prefs.edit().putString(PREF_KEY_AORS, json).apply();
    }
    
    private void initializeDefaultAORs() {
        if (aorDefinitions.isEmpty()) {
            // Add some example AORs
            List<GeoPoint> middleEast = new ArrayList<>();
            middleEast.add(new GeoPoint(30.0, 25.0));
            middleEast.add(new GeoPoint(40.0, 25.0));
            middleEast.add(new GeoPoint(40.0, 50.0));
            middleEast.add(new GeoPoint(30.0, 50.0));
            addAOR("Middle East", "Middle East region", middleEast);
            
            List<GeoPoint> europe = new ArrayList<>();
            europe.add(new GeoPoint(35.0, -10.0));
            europe.add(new GeoPoint(70.0, -10.0));
            europe.add(new GeoPoint(70.0, 40.0));
            europe.add(new GeoPoint(35.0, 40.0));
            addAOR("Europe", "European region", europe);
            
            List<GeoPoint> asiaPacific = new ArrayList<>();
            asiaPacific.add(new GeoPoint(-10.0, 90.0));
            asiaPacific.add(new GeoPoint(50.0, 90.0));
            asiaPacific.add(new GeoPoint(50.0, 180.0));
            asiaPacific.add(new GeoPoint(-10.0, 180.0));
            addAOR("Asia Pacific", "Asia Pacific region", asiaPacific);
        }
    }
    
    public void addAOR(String name, String description, List<GeoPoint> boundaryPoints) {
        AORDefinition aor = new AORDefinition(name, description, boundaryPoints);
        aorDefinitions.put(name, aor);
        saveAORDefinitions();
        Log.d(TAG, "Added AOR: " + name);
    }
    
    public List<String> getAvailableAORs() {
        List<String> aorNames = new ArrayList<>();
        aorNames.add("all"); // Option to show all
        aorNames.addAll(aorDefinitions.keySet());
        return aorNames;
    }
    
    public void setSelectedAOR(String aorName) {
        this.selectedAOR = aorName;
    }
    
    public String getSelectedAOR() {
        return selectedAOR;
    }
    
    public List<Archive> filterArchivesByAOR(List<Archive> archives) {
        if (selectedAOR == null || selectedAOR.equals("all") || archives.isEmpty()) {
            return archives;
        }
        
        AORDefinition aor = aorDefinitions.get(selectedAOR);
        if (aor == null || aor.wktPolygon == null) {
            return archives;
        }
        
        List<Archive> filteredArchives = new ArrayList<>();
        
        try {
            WKTReader reader = new WKTReader();
            Polygon aorPolygon = (Polygon) reader.read(aor.wktPolygon);
            GeometryFactory geometryFactory = new GeometryFactory();
            
            for (Archive archive : archives) {
                if (isArchiveInAOR(archive, aorPolygon, geometryFactory)) {
                    filteredArchives.add(archive);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error filtering archives by AOR", e);
            return archives; // Return original list if filtering fails
        }
        
        Log.d(TAG, "Filtered " + archives.size() + " archives to " + filteredArchives.size() + " for AOR: " + selectedAOR);
        return filteredArchives;
    }
    
    private boolean isArchiveInAOR(Archive archive, Polygon aorPolygon, GeometryFactory geometryFactory) {
        if (archive.getFootprint() == null) {
            return false; // Can't determine location without footprint
        }
        
        try {
            // Parse the archive footprint
            WKTReader reader = new WKTReader();
            org.locationtech.jts.geom.Geometry archiveGeometry = reader.read(archive.getFootprint());
            
            // Check if the archive geometry intersects with the AOR
            return aorPolygon.intersects(archiveGeometry);
            
        } catch (Exception e) {
            Log.w(TAG, "Could not parse archive footprint: " + archive.getFootprint(), e);
            
            // Fallback: try to extract center point from footprint string
            try {
                GeoPoint centerPoint = extractCenterFromFootprint(archive.getFootprint());
                if (centerPoint != null) {
                    Point point = geometryFactory.createPoint(new Coordinate(centerPoint.getLongitude(), centerPoint.getLatitude()));
                    return aorPolygon.contains(point);
                }
            } catch (Exception e2) {
                Log.w(TAG, "Could not extract center point from footprint", e2);
            }
        }
        
        return false;
    }
    
    private GeoPoint extractCenterFromFootprint(String footprint) {
        // This is a simple implementation - you might need to enhance this based on your footprint format
        if (footprint == null) return null;
        
        try {
            // Look for coordinate patterns in the footprint string
            // This is a basic implementation that might need refinement
            String[] parts = footprint.split("[,\\s]+");
            if (parts.length >= 2) {
                double lat = Double.parseDouble(parts[0]);
                double lon = Double.parseDouble(parts[1]);
                return new GeoPoint(lat, lon);
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not parse coordinates from footprint: " + footprint, e);
        }
        
        return null;
    }
}