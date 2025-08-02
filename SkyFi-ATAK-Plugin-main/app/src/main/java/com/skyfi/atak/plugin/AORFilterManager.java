package com.skyfi.atak.plugin;

import android.content.Context;
import android.util.Log;

import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.coremap.maps.coords.GeoPointMetaData;
import com.skyfi.atak.plugin.skyfiapi.Archive;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTReader;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages Area of Responsibility (AOR) filtering for images and archives.
 * Provides functionality to filter results based on current map view region.
 */
public class AORFilterManager {
    private static final String TAG = "SkyFi.AORFilter";
    private static AORFilterManager instance;
    
    public enum FilterMode {
        WORLD,      // Show all images worldwide
        REGION      // Show only images in current region
    }
    
    private final Context context;
    private final Preferences preferences;
    private FilterMode currentMode = FilterMode.WORLD;
    private Geometry currentRegion = null;
    private final GeometryFactory geometryFactory;
    private final WKTReader wktReader;
    
    // Preference keys
    private static final String PREF_AOR_FILTER_MODE = "pref_aor_filter_mode";
    private static final String PREF_SHOW_REGION_PROMPT = "pref_show_region_prompt";
    
    private AORFilterManager(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = new Preferences();
        this.geometryFactory = new GeometryFactory();
        this.wktReader = new WKTReader();
        
        // Load saved filter mode
        String savedMode = preferences.getPreference(PREF_AOR_FILTER_MODE, FilterMode.WORLD.name());
        try {
            this.currentMode = FilterMode.valueOf(savedMode);
        } catch (IllegalArgumentException e) {
            this.currentMode = FilterMode.WORLD;
        }
    }
    
    public static synchronized AORFilterManager getInstance(Context context) {
        if (instance == null) {
            instance = new AORFilterManager(context);
        }
        return instance;
    }
    
    /**
     * Set the current filter mode
     */
    public void setFilterMode(FilterMode mode) {
        this.currentMode = mode;
        preferences.setPreference(PREF_AOR_FILTER_MODE, mode.name());
        Log.d(TAG, "Filter mode set to: " + mode);
    }
    
    /**
     * Get the current filter mode
     */
    public FilterMode getFilterMode() {
        return currentMode;
    }
    
    /**
     * Update the current region based on map view bounds
     */
    public void updateCurrentRegion(MapView mapView) {
        if (mapView == null) return;
        
        try {
            // Get current map bounds
            GeoPoint upperLeft = mapView.getUpperLeft();
            GeoPoint lowerRight = mapView.getLowerRight();
            
            // Create bounding box coordinates
            Coordinate[] coords = new Coordinate[5];
            coords[0] = new Coordinate(upperLeft.getLongitude(), upperLeft.getLatitude());
            coords[1] = new Coordinate(lowerRight.getLongitude(), upperLeft.getLatitude());
            coords[2] = new Coordinate(lowerRight.getLongitude(), lowerRight.getLatitude());
            coords[3] = new Coordinate(upperLeft.getLongitude(), lowerRight.getLatitude());
            coords[4] = coords[0]; // Close the polygon
            
            currentRegion = geometryFactory.createPolygon(coords);
            Log.d(TAG, "Updated current region bounds");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to update current region", e);
            currentRegion = null;
        }
    }
    
    /**
     * Check if an archive intersects with the current region
     */
    public boolean isArchiveInRegion(Archive archive) {
        if (currentMode == FilterMode.WORLD || currentRegion == null || archive == null) {
            return true; // Show all if in world mode or no region set
        }
        
        try {
            // Parse the archive's AOI
            Geometry archiveGeometry = wktReader.read(archive.getAoi());
            
            // Check if the archive intersects with current region
            return currentRegion.intersects(archiveGeometry);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to check archive region intersection", e);
            return true; // Show if unable to determine
        }
    }
    
    /**
     * Filter a list of archives based on current region
     */
    public List<Archive> filterArchives(List<Archive> archives) {
        if (currentMode == FilterMode.WORLD || archives == null) {
            return archives;
        }
        
        List<Archive> filteredArchives = new ArrayList<>();
        for (Archive archive : archives) {
            if (isArchiveInRegion(archive)) {
                filteredArchives.add(archive);
            }
        }
        
        Log.d(TAG, "Filtered " + archives.size() + " archives to " + filteredArchives.size() + " in region");
        return filteredArchives;
    }
    
    /**
     * Check if a point is within the current region
     */
    public boolean isPointInRegion(double latitude, double longitude) {
        if (currentMode == FilterMode.WORLD || currentRegion == null) {
            return true;
        }
        
        try {
            Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
            return currentRegion.contains(point);
        } catch (Exception e) {
            Log.e(TAG, "Failed to check point region intersection", e);
            return true;
        }
    }
    
    /**
     * Check if we should show the "Search in this region?" prompt
     */
    public boolean shouldShowRegionPrompt() {
        return preferences.getPreferenceBoolean(PREF_SHOW_REGION_PROMPT, true);
    }
    
    /**
     * Set whether to show the region prompt
     */
    public void setShowRegionPrompt(boolean show) {
        preferences.setPreference(PREF_SHOW_REGION_PROMPT, show);
    }
    
    /**
     * Get a human-readable description of the current filter mode
     */
    public String getFilterModeDescription() {
        switch (currentMode) {
            case REGION:
                return "Showing images in current region only";
            case WORLD:
            default:
                return "Showing images worldwide";
        }
    }
    
    /**
     * Get the current region as a WKT string (for debugging or API calls)
     */
    public String getCurrentRegionWKT() {
        if (currentRegion == null) {
            return null;
        }
        return currentRegion.toText();
    }
    
    /**
     * Check if the current map view has significantly changed to warrant asking about region filtering
     */
    public boolean hasRegionChangedSignificantly(MapView mapView) {
        if (mapView == null || currentRegion == null) {
            return true;
        }
        
        try {
            // Get current map bounds
            GeoPoint upperLeft = mapView.getUpperLeft();
            GeoPoint lowerRight = mapView.getLowerRight();
            
            // Calculate current view area
            double currentLatSpan = upperLeft.getLatitude() - lowerRight.getLatitude();
            double currentLonSpan = lowerRight.getLongitude() - upperLeft.getLongitude();
            double currentArea = Math.abs(currentLatSpan * currentLonSpan);
            
            // Get stored region bounds
            Coordinate[] coords = currentRegion.getCoordinates();
            if (coords.length < 4) return true;
            
            double storedLatSpan = coords[0].y - coords[2].y;
            double storedLonSpan = coords[1].x - coords[0].x;
            double storedArea = Math.abs(storedLatSpan * storedLonSpan);
            
            // Consider significant if area changed by more than 50%
            double areaRatio = currentArea / storedArea;
            return areaRatio < 0.5 || areaRatio > 2.0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to check region change", e);
            return true;
        }
    }
}