package com.optisense.skyfi.atak;

import android.content.Context;
import android.graphics.Color;

import com.atakmap.android.maps.MapGroup;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Shape;
import com.atakmap.android.drawing.mapItems.DrawingShape;
import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.coremap.maps.coords.GeoPointMetaData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages visualization of AOIs on the ATAK map
 */
public class AOIVisualizationManager {
    
    private static final String TAG = "SkyFi.AOIViz";
    private static final String AOI_GROUP_NAME = "SkyFi AOIs";
    
    private final Context context;
    private final MapView mapView;
    private final AOIManager aoiManager;
    private MapGroup aoiGroup;
    private Map<String, MapItem> aoiMapItems;
    
    public AOIVisualizationManager(Context context, MapView mapView) {
        this.context = context;
        this.mapView = mapView;
        this.aoiManager = new AOIManager(context);
        this.aoiMapItems = new HashMap<>();
        
        initializeMapGroup();
        loadAndDisplayAllAOIs();
    }
    
    private void initializeMapGroup() {
        MapGroup rootGroup = mapView.getRootGroup();
        aoiGroup = rootGroup.findMapGroup(AOI_GROUP_NAME);
        
        if (aoiGroup == null) {
            aoiGroup = rootGroup.addGroup(AOI_GROUP_NAME);
            aoiGroup.setMetaString("iconUri", "android.resource://com.optisense.skyfi.atak/drawable/skyfi_logo");
            aoiGroup.setMetaBoolean("permaGroup", true);
        }
    }
    
    /**
     * Load all saved AOIs and display them on the map
     */
    public void loadAndDisplayAllAOIs() {
        List<AOIManager.AOI> aois = aoiManager.getAllAOIs();
        
        for (AOIManager.AOI aoi : aois) {
            displayAOI(aoi);
        }
        
        Log.d(TAG, "Loaded " + aois.size() + " AOIs");
    }
    
    /**
     * Display a single AOI on the map
     */
    public void displayAOI(AOIManager.AOI aoi) {
        try {
            // Remove existing if present
            removeAOI(aoi.id);
            
            // Create drawing shape from points
            DrawingShape shape = new DrawingShape(mapView, aoiGroup, aoi.id);
            shape.setTitle("AOI: " + aoi.name);
            
            // Convert points
            for (int i = 0; i < aoi.points.size(); i++) {
                GeoPoint pt = aoi.points.get(i);
                shape.addPoint(GeoPointMetaData.wrap(pt));
            }
            
            // Close the shape
            shape.setClosed(true);
            
            // Set styling
            shape.setStrokeColor(Color.parseColor("#2196F3")); // SkyFi blue
            shape.setFillColor(Color.parseColor("#402196F3")); // Semi-transparent
            shape.setStrokeWeight(3.0);
            shape.setClickable(true);
            shape.setMovable(false);
            
            // Add metadata
            shape.setMetaString("skyfi_aoi_id", aoi.id);
            shape.setMetaString("skyfi_aoi_name", aoi.name);
            shape.setMetaDouble("skyfi_aoi_area", aoi.areaSqKm);
            shape.setMetaString("skyfi_sensor_type", aoi.sensorType);
            shape.setMetaString("type", "u-d-f"); // User defined feature
            
            // Add to group
            aoiGroup.addItem(shape);
            aoiMapItems.put(aoi.id, shape);
            
            Log.d(TAG, "Displayed AOI: " + aoi.name);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to display AOI: " + aoi.name, e);
        }
    }
    
    /**
     * Remove an AOI from the map
     */
    public void removeAOI(String aoiId) {
        MapItem item = aoiMapItems.get(aoiId);
        if (item != null) {
            aoiGroup.removeItem(item);
            aoiMapItems.remove(aoiId);
        }
    }
    
    /**
     * Update an AOI on the map
     */
    public void updateAOI(AOIManager.AOI aoi) {
        displayAOI(aoi); // Remove and re-add
    }
    
    /**
     * Toggle visibility of all AOIs
     */
    public void setAOIsVisible(boolean visible) {
        if (aoiGroup != null) {
            aoiGroup.setVisible(visible);
        }
    }
    
    /**
     * Refresh all AOIs from storage
     */
    public void refresh() {
        // Clear existing
        for (MapItem item : new ArrayList<>(aoiMapItems.values())) {
            aoiGroup.removeItem(item);
        }
        aoiMapItems.clear();
        
        // Reload
        loadAndDisplayAllAOIs();
    }
    
    /**
     * Clean up resources
     */
    public void dispose() {
        if (aoiGroup != null) {
            mapView.getRootGroup().removeGroup(aoiGroup);
        }
        aoiMapItems.clear();
    }
}