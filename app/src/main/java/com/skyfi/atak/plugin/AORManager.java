package com.skyfi.atak.plugin;

import android.content.Context;
import android.content.SharedPreferences;
import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.coords.GeoPoint;

import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages Areas of Responsibility (AOR) for filtering and organizing imagery
 */
public class AORManager {
    private static final String LOGTAG = "AORManager";
    private static final String PREFS_NAME = "skyfi_aor_prefs";
    private static final String AOR_KEY = "areas_of_responsibility";
    
    private final Context context;
    private final SharedPreferences prefs;
    private final Map<String, AOR> aorMap;
    private final GeometryFactory geometryFactory;
    private final WKTReader wktReader;
    private final WKTWriter wktWriter;
    
    public static class AOR {
        public String id;
        public String name;
        public String wkt;
        public int color;
        public boolean active;
        public long createdTime;
        public long modifiedTime;
        
        public AOR(String id, String name, String wkt) {
            this.id = id;
            this.name = name;
            this.wkt = wkt;
            this.color = 0xFF2196F3; // Default blue
            this.active = true;
            this.createdTime = System.currentTimeMillis();
            this.modifiedTime = this.createdTime;
        }
    }
    
    public AORManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.aorMap = new HashMap<>();
        this.geometryFactory = new GeometryFactory();
        this.wktReader = new WKTReader();
        this.wktWriter = new WKTWriter();
        loadAORs();
    }
    
    /**
     * Get all AORs
     */
    public ArrayList<AOR> getAllAORs() {
        return new ArrayList<>(aorMap.values());
    }
    
    /**
     * Get active AORs
     */
    public ArrayList<AOR> getActiveAORs() {
        ArrayList<AOR> activeAORs = new ArrayList<>();
        for (AOR aor : aorMap.values()) {
            if (aor.active) {
                activeAORs.add(aor);
            }
        }
        return activeAORs;
    }
    
    /**
     * Get AOR by ID
     */
    public AOR getAOR(String id) {
        return aorMap.get(id);
    }
    
    /**
     * Save or update an AOR
     */
    public void saveAOR(AOR aor) {
        aor.modifiedTime = System.currentTimeMillis();
        aorMap.put(aor.id, aor);
        saveAORs();
    }
    
    /**
     * Delete an AOR
     */
    public void deleteAOR(String id) {
        aorMap.remove(id);
        saveAORs();
    }
    
    /**
     * Toggle AOR active state
     */
    public void toggleAOR(String id) {
        AOR aor = aorMap.get(id);
        if (aor != null) {
            aor.active = !aor.active;
            saveAOR(aor);
        }
    }
    
    /**
     * Check if a point is within any active AOR
     */
    public boolean isPointInActiveAOR(GeoPoint point) {
        try {
            Point jtsPoint = geometryFactory.createPoint(
                new Coordinate(point.getLongitude(), point.getLatitude())
            );
            
            for (AOR aor : getActiveAORs()) {
                Geometry geometry = wktReader.read(aor.wkt);
                if (geometry.contains(jtsPoint)) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(LOGTAG, "Error checking point in AOR", e);
        }
        return false;
    }
    
    /**
     * Check if a polygon (WKT) intersects with any active AOR
     */
    public boolean doesPolygonIntersectActiveAOR(String polygonWkt) {
        try {
            Geometry polygon = wktReader.read(polygonWkt);
            
            for (AOR aor : getActiveAORs()) {
                Geometry aorGeometry = wktReader.read(aor.wkt);
                if (polygon.intersects(aorGeometry)) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(LOGTAG, "Error checking polygon intersection", e);
        }
        return false;
    }
    
    /**
     * Get AORs that contain a point
     */
    public ArrayList<AOR> getAORsContainingPoint(GeoPoint point) {
        ArrayList<AOR> containingAORs = new ArrayList<>();
        try {
            Point jtsPoint = geometryFactory.createPoint(
                new Coordinate(point.getLongitude(), point.getLatitude())
            );
            
            for (AOR aor : aorMap.values()) {
                Geometry geometry = wktReader.read(aor.wkt);
                if (geometry.contains(jtsPoint)) {
                    containingAORs.add(aor);
                }
            }
        } catch (Exception e) {
            Log.e(LOGTAG, "Error finding AORs containing point", e);
        }
        return containingAORs;
    }
    
    /**
     * Get AORs that intersect with a polygon
     */
    public ArrayList<AOR> getAORsIntersectingPolygon(String polygonWkt) {
        ArrayList<AOR> intersectingAORs = new ArrayList<>();
        try {
            Geometry polygon = wktReader.read(polygonWkt);
            
            for (AOR aor : aorMap.values()) {
                Geometry aorGeometry = wktReader.read(aor.wkt);
                if (polygon.intersects(aorGeometry)) {
                    intersectingAORs.add(aor);
                }
            }
        } catch (Exception e) {
            Log.e(LOGTAG, "Error finding AORs intersecting polygon", e);
        }
        return intersectingAORs;
    }
    
    /**
     * Create AOR from bounding box
     */
    public AOR createAORFromBounds(String name, double north, double south, double east, double west) {
        ArrayList<Coordinate> coordinates = new ArrayList<>();
        coordinates.add(new Coordinate(west, north));
        coordinates.add(new Coordinate(east, north));
        coordinates.add(new Coordinate(east, south));
        coordinates.add(new Coordinate(west, south));
        coordinates.add(new Coordinate(west, north)); // Close the polygon
        
        Polygon polygon = geometryFactory.createPolygon(
            coordinates.toArray(new Coordinate[0])
        );
        
        String wkt = wktWriter.write(polygon);
        String id = "aor_" + System.currentTimeMillis();
        
        return new AOR(id, name, wkt);
    }
    
    /**
     * Generate a unique AOR ID
     */
    public String generateAORId() {
        return "aor_" + System.currentTimeMillis();
    }
    
    private void loadAORs() {
        try {
            String jsonString = prefs.getString(AOR_KEY, "[]");
            JSONArray jsonArray = new JSONArray(jsonString);
            
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                AOR aor = new AOR(
                    jsonObject.getString("id"),
                    jsonObject.getString("name"),
                    jsonObject.getString("wkt")
                );
                aor.color = jsonObject.optInt("color", 0xFF2196F3);
                aor.active = jsonObject.optBoolean("active", true);
                aor.createdTime = jsonObject.optLong("createdTime", System.currentTimeMillis());
                aor.modifiedTime = jsonObject.optLong("modifiedTime", aor.createdTime);
                
                aorMap.put(aor.id, aor);
            }
        } catch (Exception e) {
            Log.e(LOGTAG, "Error loading AORs", e);
        }
    }
    
    private void saveAORs() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (AOR aor : aorMap.values()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", aor.id);
                jsonObject.put("name", aor.name);
                jsonObject.put("wkt", aor.wkt);
                jsonObject.put("color", aor.color);
                jsonObject.put("active", aor.active);
                jsonObject.put("createdTime", aor.createdTime);
                jsonObject.put("modifiedTime", aor.modifiedTime);
                jsonArray.put(jsonObject);
            }
            
            prefs.edit().putString(AOR_KEY, jsonArray.toString()).apply();
        } catch (Exception e) {
            Log.e(LOGTAG, "Error saving AORs", e);
        }
    }
}