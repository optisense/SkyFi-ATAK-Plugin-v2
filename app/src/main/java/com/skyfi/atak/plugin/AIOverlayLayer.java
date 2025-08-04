package com.skyfi.atak.plugin;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Marker;
import com.atakmap.android.maps.PointMapItem;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.map.layer.AbstractLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Individual AI overlay layer that manages rendering of AI analysis items
 */
public class AIOverlayLayer extends AbstractLayer {
    
    private final AIOverlaySystem.AILayerType layerType;
    private final MapView mapView;
    private final ConcurrentMap<String, AIOverlayItem> overlayItems;
    private final Paint layerPaint;
    private boolean isVisible;
    
    public AIOverlayLayer(AIOverlaySystem.AILayerType layerType, MapView mapView) {
        super(layerType.displayName);
        this.layerType = layerType;
        this.mapView = mapView;
        this.overlayItems = new ConcurrentHashMap<>();
        this.isVisible = false;
        
        // Initialize paint for this layer
        this.layerPaint = new Paint();
        this.layerPaint.setColor(layerType.defaultColor);
        this.layerPaint.setAntiAlias(true);
        this.layerPaint.setStyle(Paint.Style.FILL);
    }
    
    public void addItem(AIOverlayItem item) {
        overlayItems.put(item.getId(), item);
        
        // Create visual representation based on layer type
        createVisualRepresentation(item);
        
        // Request map refresh using proper ATAK method
        if (mapView != null) {
            mapView.post(() -> mapView.invalidate());
        }
    }
    
    public void removeItem(String itemId) {
        AIOverlayItem item = overlayItems.remove(itemId);
        if (item != null) {
            removeVisualRepresentation(item);
            
            // Request map refresh
            if (mapView != null) {
                mapView.post(() -> mapView.invalidate());
            }
        }
    }
    
    public void clearItems() {
        for (AIOverlayItem item : overlayItems.values()) {
            removeVisualRepresentation(item);
        }
        overlayItems.clear();
        
        // Request map refresh using proper ATAK method
        if (mapView != null) {
            mapView.post(() -> mapView.invalidate());
        }
    }
    
    public int getItemCount() {
        return overlayItems.size();
    }
    
    public void setVisible(boolean visible) {
        if (this.isVisible != visible) {
            this.isVisible = visible;
            
            // Toggle visibility of all visual representations
            for (AIOverlayItem item : overlayItems.values()) {
                toggleVisualRepresentation(item, visible);
            }
            
            // Request map refresh
            if (mapView != null) {
                mapView.post(() -> mapView.invalidate());
            }
        }
    }
    
    public boolean isVisible() {
        return isVisible;
    }
    
    private void createVisualRepresentation(AIOverlayItem item) {
        switch (layerType) {
            case OBJECT_DETECTION:
                createObjectDetectionMarker(item);
                break;
            case THREAT_ANALYSIS:
                createThreatHeatmapPoint(item);
                break;
            case MOVEMENT_PREDICTION:
                createMovementPredictionPath(item);
                break;
            case POPULATION_DENSITY:
                createPopulationDensityIndicator(item);
                break;
            case INFRASTRUCTURE_STATUS:
                createInfrastructureStatusMarker(item);
                break;
            case WEATHER_OVERLAY:
                createWeatherIndicator(item);
                break;
            case ROUTE_OPTIMIZATION:
                createRouteOptimizationMarker(item);
                break;
        }
    }
    
    private void createObjectDetectionMarker(AIOverlayItem item) {
        Marker marker = new Marker(item.getId());
        marker.setPoint(item.getLocation());
        marker.setTitle(item.getTitle());
        marker.setMetaString("remarks", "Confidence: " + String.format("%.1f%%", item.getConfidence() * 100));
        marker.setType("ai-object-detection");
        marker.setVisible(isVisible);
        
        // Add to map
        mapView.getRootGroup().addItem(marker);
        item.setVisualRepresentation(marker);
    }
    
    private void createThreatHeatmapPoint(AIOverlayItem item) {
        // Create a circular threat indicator with color based on threat level
        Marker marker = new Marker(item.getId());
        marker.setPoint(item.getLocation());
        marker.setTitle(item.getTitle());
        
        // Set color based on threat level
        int threatColor = interpolateColor(Color.GREEN, Color.RED, item.getConfidence());
        marker.setColor(threatColor);
        marker.setType("ai-threat-indicator");
        marker.setVisible(isVisible);
        
        mapView.getRootGroup().addItem(marker);
        item.setVisualRepresentation(marker);
    }
    
    private void createMovementPredictionPath(AIOverlayItem item) {
        // Create movement prediction arrow/path
        Marker marker = new Marker(item.getId());
        marker.setPoint(item.getLocation());
        marker.setTitle(item.getTitle());
        marker.setType("ai-movement-prediction");
        marker.setVisible(isVisible);
        
        // Set transparency based on confidence
        int alpha = (int) (item.getConfidence() * 255);
        marker.setColor(Color.argb(alpha, 255, 255, 0)); // Yellow with variable alpha
        
        mapView.getRootGroup().addItem(marker);
        item.setVisualRepresentation(marker);
    }
    
    private void createPopulationDensityIndicator(AIOverlayItem item) {
        Marker marker = new Marker(item.getId());
        marker.setPoint(item.getLocation());
        marker.setTitle(item.getTitle());
        marker.setType("ai-population-density");
        marker.setVisible(isVisible);
        
        // Scale size based on population density
        double scale = 0.5 + (item.getConfidence() * 1.5); // Scale from 0.5 to 2.0
        marker.setMetaDouble("scale", scale);
        
        mapView.getRootGroup().addItem(marker);
        item.setVisualRepresentation(marker);
    }
    
    private void createInfrastructureStatusMarker(AIOverlayItem item) {
        Marker marker = new Marker(item.getId());
        marker.setPoint(item.getLocation());
        marker.setTitle(item.getTitle());
        marker.setType("ai-infrastructure");
        marker.setVisible(isVisible);
        
        // Set color based on infrastructure status (confidence represents health)
        int statusColor = interpolateColor(Color.RED, Color.GREEN, item.getConfidence());
        marker.setColor(statusColor);
        
        mapView.getRootGroup().addItem(marker);
        item.setVisualRepresentation(marker);
    }
    
    private void createWeatherIndicator(AIOverlayItem item) {
        Marker marker = new Marker(item.getId());
        marker.setPoint(item.getLocation());
        marker.setTitle(item.getTitle());
        marker.setType("ai-weather");
        marker.setVisible(isVisible);
        
        // Set transparency based on weather intensity
        int alpha = (int) (item.getConfidence() * 255);
        marker.setColor(Color.argb(alpha, 255, 255, 255)); // White with variable alpha
        
        mapView.getRootGroup().addItem(marker);
        item.setVisualRepresentation(marker);
    }
    
    private void createRouteOptimizationMarker(AIOverlayItem item) {
        Marker marker = new Marker(item.getId());
        marker.setPoint(item.getLocation());
        marker.setTitle(item.getTitle());
        marker.setType("ai-route-optimization");
        marker.setVisible(isVisible);
        
        // Set color based on route efficiency
        int efficiencyColor = interpolateColor(Color.RED, Color.GREEN, item.getConfidence());
        marker.setColor(efficiencyColor);
        
        mapView.getRootGroup().addItem(marker);
        item.setVisualRepresentation(marker);
    }
    
    private void removeVisualRepresentation(AIOverlayItem item) {
        Object visual = item.getVisualRepresentation();
        if (visual instanceof PointMapItem) {
            mapView.getRootGroup().removeItem((PointMapItem) visual);
        }
    }
    
    private void toggleVisualRepresentation(AIOverlayItem item, boolean visible) {
        Object visual = item.getVisualRepresentation();
        if (visual instanceof PointMapItem) {
            ((PointMapItem) visual).setVisible(visible);
        }
    }
    
    private int interpolateColor(int colorStart, int colorEnd, double ratio) {
        ratio = Math.max(0.0, Math.min(1.0, ratio)); // Clamp ratio between 0 and 1
        
        int startA = Color.alpha(colorStart);
        int startR = Color.red(colorStart);
        int startG = Color.green(colorStart);
        int startB = Color.blue(colorStart);
        
        int endA = Color.alpha(colorEnd);
        int endR = Color.red(colorEnd);
        int endG = Color.green(colorEnd);
        int endB = Color.blue(colorEnd);
        
        int interpolatedA = (int) (startA + ratio * (endA - startA));
        int interpolatedR = (int) (startR + ratio * (endR - startR));
        int interpolatedG = (int) (startG + ratio * (endG - startG));
        int interpolatedB = (int) (startB + ratio * (endB - startB));
        
        return Color.argb(interpolatedA, interpolatedR, interpolatedG, interpolatedB);
    }
    
    public AIOverlaySystem.AILayerType getLayerType() {
        return layerType;
    }
    
    public List<AIOverlayItem> getItems() {
        return new ArrayList<>(overlayItems.values());
    }
    
    public AIOverlayItem getItem(String itemId) {
        return overlayItems.get(itemId);
    }
    
    public void dispose() {
        clearItems();
    }
}