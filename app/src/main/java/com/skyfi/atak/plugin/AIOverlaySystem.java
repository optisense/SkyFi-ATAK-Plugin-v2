package com.skyfi.atak.plugin;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;

import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Marker;
import com.atakmap.android.maps.graphics.GLMapItem;
import com.atakmap.android.maps.heatmap.HeatMapLayer;
import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.map.layer.Layer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI Analysis Overlay System for displaying toggleable AI-powered layers
 * Manages multiple overlay layers including object detection, threat analysis, and predictions
 */
public class AIOverlaySystem {
    
    private static final String TAG = "AIOverlaySystem";
    
    // Layer Types
    public enum AILayerType {
        OBJECT_DETECTION("Object Detection", "Shows AI-detected objects", Color.CYAN),
        THREAT_ANALYSIS("Threat Analysis", "Displays threat probability heatmap", Color.RED),
        MOVEMENT_PREDICTION("Movement Prediction", "Shows predicted movement paths", Color.YELLOW),
        POPULATION_DENSITY("Population Density", "Visualizes population distribution", Color.GREEN),
        INFRASTRUCTURE_STATUS("Infrastructure Status", "Shows infrastructure health", Color.BLUE),
        WEATHER_OVERLAY("Weather Overlay", "AI-powered weather visualization", Color.WHITE),
        ROUTE_OPTIMIZATION("Route Optimization", "Optimized route suggestions", Color.MAGENTA);
        
        public final String displayName;
        public final String description;
        public final int defaultColor;
        
        AILayerType(String displayName, String description, int defaultColor) {
            this.displayName = displayName;
            this.description = description;
            this.defaultColor = defaultColor;
        }
    }
    
    // Core components
    private Context context;
    private MapView mapView;
    private Map<AILayerType, AIOverlayLayer> overlayLayers;
    private Map<AILayerType, Boolean> layerVisibility;
    private AIServiceClient aiServiceClient;
    private Handler mainHandler;
    
    // Listeners
    private OnLayerUpdateListener layerUpdateListener;
    private OnLayerToggleListener layerToggleListener;
    
    public interface OnLayerUpdateListener {
        void onLayerUpdated(AILayerType layerType, int objectCount);
        void onLayerError(AILayerType layerType, String error);
    }
    
    public interface OnLayerToggleListener {
        void onLayerToggled(AILayerType layerType, boolean visible);
    }
    
    public AIOverlaySystem(Context context, MapView mapView) {
        this.context = context;
        this.mapView = mapView;
        this.overlayLayers = new ConcurrentHashMap<>();
        this.layerVisibility = new ConcurrentHashMap<>();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.aiServiceClient = AIServiceClient.getInstance(context);
        
        initializeLayers();
    }
    
    private void initializeLayers() {
        // Initialize all AI overlay layers
        for (AILayerType layerType : AILayerType.values()) {
            AIOverlayLayer layer = new AIOverlayLayer(layerType, mapView);
            overlayLayers.put(layerType, layer);
            layerVisibility.put(layerType, false); // All layers start hidden
        }
        
        Log.d(TAG, "Initialized " + overlayLayers.size() + " AI overlay layers");
    }
    
    /**
     * Toggle visibility of an AI layer
     */
    public void toggleLayer(AILayerType layerType) {
        setLayerVisible(layerType, !isLayerVisible(layerType));
    }
    
    /**
     * Set visibility of an AI layer
     */
    public void setLayerVisible(AILayerType layerType, boolean visible) {
        layerVisibility.put(layerType, visible);
        
        AIOverlayLayer layer = overlayLayers.get(layerType);
        if (layer != null) {
            layer.setVisible(visible);
            
            if (visible) {
                // Refresh layer data when made visible
                refreshLayer(layerType);
            }
            
            if (layerToggleListener != null) {
                layerToggleListener.onLayerToggled(layerType, visible);
            }
        }
    }
    
    /**
     * Check if a layer is currently visible
     */
    public boolean isLayerVisible(AILayerType layerType) {
        return layerVisibility.getOrDefault(layerType, false);
    }
    
    /**
     * Refresh data for a specific layer
     */
    public void refreshLayer(AILayerType layerType) {
        if (!isLayerVisible(layerType)) {
            return;
        }
        
        // Get current map view bounds
        GeoPoint[] bounds = getCurrentViewBounds();
        
        switch (layerType) {
            case OBJECT_DETECTION:
                refreshObjectDetectionLayer(bounds);
                break;
            case THREAT_ANALYSIS:
                refreshThreatAnalysisLayer(bounds);
                break;
            case MOVEMENT_PREDICTION:
                refreshMovementPredictionLayer(bounds);
                break;
            case POPULATION_DENSITY:
                refreshPopulationDensityLayer(bounds);
                break;
            case INFRASTRUCTURE_STATUS:
                refreshInfrastructureStatusLayer(bounds);
                break;
            case WEATHER_OVERLAY:
                refreshWeatherOverlayLayer(bounds);
                break;
            case ROUTE_OPTIMIZATION:
                refreshRouteOptimizationLayer(bounds);
                break;
        }
    }
    
    /**
     * Refresh all visible layers
     */
    public void refreshAllLayers() {
        for (AILayerType layerType : AILayerType.values()) {
            if (isLayerVisible(layerType)) {
                refreshLayer(layerType);
            }
        }
    }
    
    private void refreshObjectDetectionLayer(GeoPoint[] bounds) {
        aiServiceClient.analyzeArea(bounds, "object_detection", new AIServiceClient.AIAnalysisCallback() {
            @Override
            public void onSuccess(AIServiceClient.AIAnalysisResult result) {
                mainHandler.post(() -> {
                    AIOverlayLayer layer = overlayLayers.get(AILayerType.OBJECT_DETECTION);
                    if (layer != null) {
                        layer.clearItems();
                        
                        // Add detected objects to layer
                        for (AIServiceClient.DetectedObject obj : result.detectedObjects) {
                            AIOverlayItem item = new AIOverlayItem(
                                obj.id,
                                obj.location,
                                obj.type,
                                obj.confidence,
                                AILayerType.OBJECT_DETECTION
                            );
                            layer.addItem(item);
                        }
                        
                        if (layerUpdateListener != null) {
                            layerUpdateListener.onLayerUpdated(AILayerType.OBJECT_DETECTION, 
                                result.detectedObjects.size());
                        }
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Object detection refresh failed: " + error);
                if (layerUpdateListener != null) {
                    layerUpdateListener.onLayerError(AILayerType.OBJECT_DETECTION, error);
                }
            }
        });
    }
    
    private void refreshThreatAnalysisLayer(GeoPoint[] bounds) {
        // Generate mock threat probability data
        mainHandler.post(() -> {
            AIOverlayLayer layer = overlayLayers.get(AILayerType.THREAT_ANALYSIS);
            if (layer != null) {
                layer.clearItems();
                
                // Create threat probability heatmap points
                List<AIOverlayItem> threatPoints = generateMockThreatPoints(bounds);
                for (AIOverlayItem item : threatPoints) {
                    layer.addItem(item);
                }
                
                if (layerUpdateListener != null) {
                    layerUpdateListener.onLayerUpdated(AILayerType.THREAT_ANALYSIS, threatPoints.size());
                }
            }
        });
    }
    
    private void refreshMovementPredictionLayer(GeoPoint[] bounds) {
        // Get movement predictions from AI service
        GeoPoint centerPoint = calculateCenter(bounds);
        
        aiServiceClient.getPredictiveInsights(centerPoint, "next_6_hours", 
            new AIServiceClient.AIPredictionCallback() {
                @Override
                public void onSuccess(AIServiceClient.AIPredictionResult result) {
                    mainHandler.post(() -> {
                        AIOverlayLayer layer = overlayLayers.get(AILayerType.MOVEMENT_PREDICTION);
                        if (layer != null) {
                            layer.clearItems();
                            
                            // Add movement prediction paths
                            List<AIOverlayItem> movementPaths = parsePredictionPaths(result);
                            for (AIOverlayItem item : movementPaths) {
                                layer.addItem(item);
                            }
                            
                            if (layerUpdateListener != null) {
                                layerUpdateListener.onLayerUpdated(AILayerType.MOVEMENT_PREDICTION, 
                                    movementPaths.size());
                            }
                        }
                    });
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Movement prediction refresh failed: " + error);
                    if (layerUpdateListener != null) {
                        layerUpdateListener.onLayerError(AILayerType.MOVEMENT_PREDICTION, error);
                    }
                }
            });
    }
    
    private void refreshPopulationDensityLayer(GeoPoint[] bounds) {
        // Mock population density data
        mainHandler.post(() -> {
            AIOverlayLayer layer = overlayLayers.get(AILayerType.POPULATION_DENSITY);
            if (layer != null) {
                layer.clearItems();
                
                List<AIOverlayItem> densityPoints = generateMockPopulationData(bounds);
                for (AIOverlayItem item : densityPoints) {
                    layer.addItem(item);
                }
                
                if (layerUpdateListener != null) {
                    layerUpdateListener.onLayerUpdated(AILayerType.POPULATION_DENSITY, densityPoints.size());
                }
            }
        });
    }
    
    private void refreshInfrastructureStatusLayer(GeoPoint[] bounds) {
        // Mock infrastructure status data
        mainHandler.post(() -> {
            AIOverlayLayer layer = overlayLayers.get(AILayerType.INFRASTRUCTURE_STATUS);
            if (layer != null) {
                layer.clearItems();
                
                List<AIOverlayItem> infrastructureItems = generateMockInfrastructureData(bounds);
                for (AIOverlayItem item : infrastructureItems) {
                    layer.addItem(item);
                }
                
                if (layerUpdateListener != null) {
                    layerUpdateListener.onLayerUpdated(AILayerType.INFRASTRUCTURE_STATUS, 
                        infrastructureItems.size());
                }
            }
        });
    }
    
    private void refreshWeatherOverlayLayer(GeoPoint[] bounds) {
        // Mock weather overlay data
        mainHandler.post(() -> {
            AIOverlayLayer layer = overlayLayers.get(AILayerType.WEATHER_OVERLAY);
            if (layer != null) {
                layer.clearItems();
                
                List<AIOverlayItem> weatherItems = generateMockWeatherData(bounds);
                for (AIOverlayItem item : weatherItems) {
                    layer.addItem(item);
                }
                
                if (layerUpdateListener != null) {
                    layerUpdateListener.onLayerUpdated(AILayerType.WEATHER_OVERLAY, weatherItems.size());
                }
            }
        });
    }
    
    private void refreshRouteOptimizationLayer(GeoPoint[] bounds) {
        // Mock route optimization data
        mainHandler.post(() -> {
            AIOverlayLayer layer = overlayLayers.get(AILayerType.ROUTE_OPTIMIZATION);
            if (layer != null) {
                layer.clearItems();
                
                List<AIOverlayItem> routeItems = generateMockRouteData(bounds);
                for (AIOverlayItem item : routeItems) {
                    layer.addItem(item);
                }
                
                if (layerUpdateListener != null) {
                    layerUpdateListener.onLayerUpdated(AILayerType.ROUTE_OPTIMIZATION, routeItems.size());
                }
            }
        });
    }
    
    private GeoPoint[] getCurrentViewBounds() {
        // Get current map view bounds
        GeoPoint center = mapView.getPoint();
        double scale = mapView.getMapScale();
        
        // Calculate approximate bounds based on view
        double latSpan = scale * 0.001; // Rough approximation
        double lonSpan = scale * 0.001;
        
        return new GeoPoint[] {
            new GeoPoint(center.getLatitude() - latSpan, center.getLongitude() - lonSpan),
            new GeoPoint(center.getLatitude() + latSpan, center.getLongitude() + lonSpan)
        };
    }
    
    private GeoPoint calculateCenter(GeoPoint[] bounds) {
        if (bounds.length < 2) return bounds[0];
        
        double centerLat = (bounds[0].getLatitude() + bounds[1].getLatitude()) / 2;
        double centerLon = (bounds[0].getLongitude() + bounds[1].getLongitude()) / 2;
        
        return new GeoPoint(centerLat, centerLon);
    }
    
    // Mock data generators for demonstration
    private List<AIOverlayItem> generateMockThreatPoints(GeoPoint[] bounds) {
        List<AIOverlayItem> items = new ArrayList<>();
        GeoPoint center = calculateCenter(bounds);
        
        // Generate random threat probability points
        for (int i = 0; i < 10; i++) {
            double offsetLat = (Math.random() - 0.5) * 0.01;
            double offsetLon = (Math.random() - 0.5) * 0.01;
            
            GeoPoint location = new GeoPoint(
                center.getLatitude() + offsetLat,
                center.getLongitude() + offsetLon
            );
            
            double threatLevel = Math.random();
            AIOverlayItem item = new AIOverlayItem(
                "threat_" + i,
                location,
                "Threat Probability: " + String.format("%.0f%%", threatLevel * 100),
                threatLevel,
                AILayerType.THREAT_ANALYSIS
            );
            
            items.add(item);
        }
        
        return items;
    }
    
    private List<AIOverlayItem> generateMockPopulationData(GeoPoint[] bounds) {
        List<AIOverlayItem> items = new ArrayList<>();
        GeoPoint center = calculateCenter(bounds);
        
        // Generate population density points
        for (int i = 0; i < 15; i++) {
            double offsetLat = (Math.random() - 0.5) * 0.02;
            double offsetLon = (Math.random() - 0.5) * 0.02;
            
            GeoPoint location = new GeoPoint(
                center.getLatitude() + offsetLat,
                center.getLongitude() + offsetLon
            );
            
            int population = (int) (Math.random() * 1000) + 50;
            AIOverlayItem item = new AIOverlayItem(
                "pop_" + i,
                location,
                "Population: " + population,
                Math.min(population / 1000.0, 1.0),
                AILayerType.POPULATION_DENSITY
            );
            
            items.add(item);
        }
        
        return items;
    }
    
    private List<AIOverlayItem> generateMockInfrastructureData(GeoPoint[] bounds) {
        List<AIOverlayItem> items = new ArrayList<>();
        GeoPoint center = calculateCenter(bounds);
        
        String[] infraTypes = {"Power Grid", "Water System", "Communications", "Transportation"};
        String[] statusTypes = {"Operational", "Degraded", "Offline"};
        
        for (int i = 0; i < 8; i++) {
            double offsetLat = (Math.random() - 0.5) * 0.015;
            double offsetLon = (Math.random() - 0.5) * 0.015;
            
            GeoPoint location = new GeoPoint(
                center.getLatitude() + offsetLat,
                center.getLongitude() + offsetLon
            );
            
            String infraType = infraTypes[(int) (Math.random() * infraTypes.length)];
            String status = statusTypes[(int) (Math.random() * statusTypes.length)];
            
            double confidence = Math.random();
            AIOverlayItem item = new AIOverlayItem(
                "infra_" + i,
                location,
                infraType + ": " + status,
                confidence,
                AILayerType.INFRASTRUCTURE_STATUS
            );
            
            items.add(item);
        }
        
        return items;
    }
    
    private List<AIOverlayItem> generateMockWeatherData(GeoPoint[] bounds) {
        List<AIOverlayItem> items = new ArrayList<>();
        GeoPoint center = calculateCenter(bounds);
        
        String[] weatherTypes = {"Clear", "Cloudy", "Rain", "Fog", "Storm"};
        
        for (int i = 0; i < 6; i++) {
            double offsetLat = (Math.random() - 0.5) * 0.02;
            double offsetLon = (Math.random() - 0.5) * 0.02;
            
            GeoPoint location = new GeoPoint(
                center.getLatitude() + offsetLat,
                center.getLongitude() + offsetLon
            );
            
            String weather = weatherTypes[(int) (Math.random() * weatherTypes.length)];
            int visibility = (int) (Math.random() * 10) + 1;
            
            AIOverlayItem item = new AIOverlayItem(
                "weather_" + i,
                location,
                weather + " - Vis: " + visibility + "km",
                visibility / 10.0,
                AILayerType.WEATHER_OVERLAY
            );
            
            items.add(item);
        }
        
        return items;
    }
    
    private List<AIOverlayItem> generateMockRouteData(GeoPoint[] bounds) {
        List<AIOverlayItem> items = new ArrayList<>();
        GeoPoint center = calculateCenter(bounds);
        
        // Generate optimized route waypoints
        for (int i = 0; i < 5; i++) {
            double offsetLat = (Math.random() - 0.5) * 0.01;
            double offsetLon = (Math.random() - 0.5) * 0.01;
            
            GeoPoint location = new GeoPoint(
                center.getLatitude() + offsetLat,
                center.getLongitude() + offsetLon
            );
            
            int efficiency = (int) (Math.random() * 30) + 70; // 70-100% efficiency
            
            AIOverlayItem item = new AIOverlayItem(
                "route_" + i,
                location,
                "Route Efficiency: " + efficiency + "%",
                efficiency / 100.0,
                AILayerType.ROUTE_OPTIMIZATION
            );
            
            items.add(item);
        }
        
        return items;
    }
    
    private List<AIOverlayItem> parsePredictionPaths(AIServiceClient.AIPredictionResult result) {
        // In a real implementation, this would parse the prediction result
        // For now, return mock movement paths
        return generateMockMovementPaths();
    }
    
    private List<AIOverlayItem> generateMockMovementPaths() {
        List<AIOverlayItem> items = new ArrayList<>();
        GeoPoint center = mapView.getPoint();
        
        for (int i = 0; i < 3; i++) {
            double offsetLat = (Math.random() - 0.5) * 0.005;
            double offsetLon = (Math.random() - 0.5) * 0.005;
            
            GeoPoint location = new GeoPoint(
                center.getLatitude() + offsetLat,
                center.getLongitude() + offsetLon
            );
            
            double probability = Math.random();
            
            AIOverlayItem item = new AIOverlayItem(
                "movement_" + i,
                location,
                "Movement Prediction: " + String.format("%.0f%%", probability * 100),
                probability,
                AILayerType.MOVEMENT_PREDICTION
            );
            
            items.add(item);
        }
        
        return items;
    }
    
    public List<AILayerType> getVisibleLayers() {
        List<AILayerType> visibleLayers = new ArrayList<>();
        for (Map.Entry<AILayerType, Boolean> entry : layerVisibility.entrySet()) {
            if (entry.getValue()) {
                visibleLayers.add(entry.getKey());
            }
        }
        return visibleLayers;
    }
    
    public int getLayerItemCount(AILayerType layerType) {
        AIOverlayLayer layer = overlayLayers.get(layerType);
        return layer != null ? layer.getItemCount() : 0;
    }
    
    public void setOnLayerUpdateListener(OnLayerUpdateListener listener) {
        this.layerUpdateListener = listener;
    }
    
    public void setOnLayerToggleListener(OnLayerToggleListener listener) {
        this.layerToggleListener = listener;
    }
    
    public void dispose() {
        // Clean up all layers
        for (AIOverlayLayer layer : overlayLayers.values()) {
            layer.dispose();
        }
        overlayLayers.clear();
        layerVisibility.clear();
    }
}