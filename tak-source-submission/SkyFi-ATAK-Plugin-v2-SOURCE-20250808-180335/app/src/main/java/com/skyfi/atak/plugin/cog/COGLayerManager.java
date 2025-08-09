package com.skyfi.atak.plugin.cog;

import android.content.Context;

import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.filesystem.FileSystemUtils;
import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.coords.GeoBounds;
import com.atakmap.coremap.maps.coords.GeoPoint;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages Cloud Optimized GeoTIFF layers in ATAK
 * Handles registration, display, and lifecycle of COG tile layers
 */
public class COGLayerManager {
    
    private static final String TAG = "SkyFi.COGLayerManager";
    private static COGLayerManager instance;
    
    private final Context context;
    private final MapView mapView;
    private final COGTileServer tileServer;
    private final Map<String, COGLayer> activeLayers;
    private final File cacheDir;
    
    /**
     * Represents a single COG layer
     */
    public static class COGLayer {
        public final String id;
        public final String name;
        public final String cogUrl;
        public final GeoBounds bounds;
        public final String tileUrl;
        public boolean isVisible;
        
        public COGLayer(String id, String name, String cogUrl, GeoBounds bounds, String tileUrl) {
            this.id = id;
            this.name = name;
            this.cogUrl = cogUrl;
            this.bounds = bounds;
            this.tileUrl = tileUrl;
            this.isVisible = false;
        }
    }
    
    private COGLayerManager(Context context, MapView mapView) {
        this.context = context;
        this.mapView = mapView;
        this.activeLayers = new HashMap<>();
        
        // Initialize cache directory
        this.cacheDir = new File(FileSystemUtils.getItem("Databases"), "skyfi_cog_cache");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        
        // Start tile server
        this.tileServer = new COGTileServer();
        this.tileServer.startServer();
        
        Log.d(TAG, "COG Layer Manager initialized");
    }
    
    public static synchronized COGLayerManager getInstance(Context context, MapView mapView) {
        if (instance == null) {
            instance = new COGLayerManager(context, mapView);
        }
        return instance;
    }
    
    /**
     * Add a COG as a layer in ATAK
     * @param name Display name for the layer
     * @param cogUrl URL to the Cloud Optimized GeoTIFF
     * @param bounds Geographic bounds of the image (can be null for auto-detection)
     * @return Layer ID if successful, null otherwise
     */
    public String addCOGLayer(String name, String cogUrl, GeoBounds bounds) {
        try {
            // Generate unique layer ID
            String layerId = "cog_" + UUID.randomUUID().toString().substring(0, 8);
            
            // Register with tile server
            if (!tileServer.registerCOG(layerId, cogUrl)) {
                Log.e(TAG, "Failed to register COG with tile server");
                return null;
            }
            
            // Get tile URL pattern
            String tileUrl = tileServer.getTileUrlPattern(layerId);
            
            // If bounds not provided, use a default or try to extract from COG
            if (bounds == null) {
                // Default to world bounds, should ideally read from COG metadata
                bounds = new GeoBounds(-90, -180, 90, 180);
            }
            
            // Create layer object
            COGLayer layer = new COGLayer(layerId, name, cogUrl, bounds, tileUrl);
            
            // Store in active layers
            activeLayers.put(layerId, layer);
            
            // Note: Integration with ATAK's tile system would happen here
            // For now, we just register the layer internally
            Log.d(TAG, "Added COG layer: " + name + " (ID: " + layerId + ")");
            Log.d(TAG, "Tile URL pattern: " + tileUrl);
            
            return layerId;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to add COG layer", e);
            return null;
        }
    }
    
    /**
     * Remove a COG layer
     */
    public void removeCOGLayer(String layerId) {
        COGLayer layer = activeLayers.remove(layerId);
        if (layer != null) {
            // Remove from tile server
            tileServer.unregisterCOG(layerId);
            
            // Clear cache
            File layerCache = new File(cacheDir, layerId);
            FileSystemUtils.deleteDirectory(layerCache, false);
            
            Log.d(TAG, "Removed COG layer: " + layer.name);
        }
    }
    
    /**
     * Toggle layer visibility
     */
    public void setLayerVisibility(String layerId, boolean visible) {
        COGLayer layer = activeLayers.get(layerId);
        if (layer != null) {
            layer.isVisible = visible;
            // Note: Actual visibility control would integrate with ATAK's layer system
            Log.d(TAG, "Set layer visibility: " + layer.name + " = " + visible);
        }
    }
    
    /**
     * Get all active COG layers
     */
    public Map<String, COGLayer> getActiveLayers() {
        return new HashMap<>(activeLayers);
    }
    
    /**
     * Clean up resources
     */
    public void dispose() {
        // Remove all layers
        for (String layerId : activeLayers.keySet()) {
            removeCOGLayer(layerId);
        }
        
        // Stop tile server
        if (tileServer != null) {
            tileServer.stopServer();
        }
        
        Log.d(TAG, "COG Layer Manager disposed");
    }
    
    /**
     * Helper method to create bounds from corner coordinates
     */
    public static GeoBounds createBounds(double minLat, double minLon, double maxLat, double maxLon) {
        return new GeoBounds(minLat, minLon, maxLat, maxLon);
    }
    
    /**
     * Add a COG from a SkyFi order
     */
    public String addCOGFromOrder(String orderId, String orderName, String cogUrl, 
                                  double minLat, double minLon, double maxLat, double maxLon) {
        GeoBounds bounds = createBounds(minLat, minLon, maxLat, maxLon);
        String displayName = "SkyFi Order: " + orderName;
        
        String layerId = addCOGLayer(displayName, cogUrl, bounds);
        if (layerId != null) {
            // Auto-show the layer
            setLayerVisibility(layerId, true);
            
            // Pan map to the layer
            panToLayer(layerId);
        }
        
        return layerId;
    }
    
    /**
     * Pan the map to center on a layer
     */
    public void panToLayer(String layerId) {
        COGLayer layer = activeLayers.get(layerId);
        if (layer != null && layer.bounds != null) {
            GeoPoint center = new GeoPoint(
                (layer.bounds.getNorth() + layer.bounds.getSouth()) / 2,
                (layer.bounds.getEast() + layer.bounds.getWest()) / 2
            );
            
            mapView.getMapController().panTo(center, true);
            
            // Note: Zoom to fit would be implemented here
            // mapView.getMapController().zoomToFit(layer.bounds);
        }
    }
}