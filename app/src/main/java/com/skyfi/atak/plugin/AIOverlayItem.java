package com.skyfi.atak.plugin;

import com.atakmap.coremap.maps.coords.GeoPoint;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an individual AI analysis item that can be displayed on the map
 */
public class AIOverlayItem {
    
    private final String id;
    private final GeoPoint location;
    private final String title;
    private final double confidence;
    private final AIOverlaySystem.AILayerType layerType;
    private final long timestamp;
    
    // Additional properties
    private Map<String, Object> properties;
    private Object visualRepresentation; // The actual map item (Marker, etc.)
    
    // Metadata
    private String description;
    private String source;
    private boolean isHighPriority;
    private double radius; // For area-based items
    
    public AIOverlayItem(String id, GeoPoint location, String title, 
                        double confidence, AIOverlaySystem.AILayerType layerType) {
        this.id = id;
        this.location = location;
        this.title = title;
        this.confidence = Math.max(0.0, Math.min(1.0, confidence)); // Clamp between 0-1
        this.layerType = layerType;
        this.timestamp = System.currentTimeMillis();
        this.properties = new HashMap<>();
        this.isHighPriority = confidence > 0.8; // High priority if confidence > 80%
        this.radius = 50.0; // Default radius in meters
    }
    
    public String getId() {
        return id;
    }
    
    public GeoPoint getLocation() {
        return location;
    }
    
    public String getTitle() {
        return title;
    }
    
    public double getConfidence() {
        return confidence;
    }
    
    public AIOverlaySystem.AILayerType getLayerType() {
        return layerType;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public boolean isHighPriority() {
        return isHighPriority;
    }
    
    public void setHighPriority(boolean highPriority) {
        this.isHighPriority = highPriority;
    }
    
    public double getRadius() {
        return radius;
    }
    
    public void setRadius(double radius) {
        this.radius = Math.max(0.0, radius);
    }
    
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }
    
    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    public <T> T getProperty(String key, Class<T> type) {
        Object value = properties.get(key);
        if (value != null && type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }
    
    public Map<String, Object> getAllProperties() {
        return new HashMap<>(properties);
    }
    
    public void setVisualRepresentation(Object visual) {
        this.visualRepresentation = visual;
    }
    
    public Object getVisualRepresentation() {
        return visualRepresentation;
    }
    
    /**
     * Get age of this item in milliseconds
     */
    public long getAge() {
        return System.currentTimeMillis() - timestamp;
    }
    
    /**
     * Check if this item is considered stale (older than specified time)
     */
    public boolean isStale(long maxAgeMs) {
        return getAge() > maxAgeMs;
    }
    
    /**
     * Get confidence level as a categorical string
     */
    public String getConfidenceLevel() {
        if (confidence >= 0.9) return "Very High";
        if (confidence >= 0.7) return "High";
        if (confidence >= 0.5) return "Medium";
        if (confidence >= 0.3) return "Low";
        return "Very Low";
    }
    
    /**
     * Get priority level based on confidence and layer type
     */
    public int getPriorityLevel() {
        // Base priority on layer type
        int basePriority;
        switch (layerType) {
            case THREAT_ANALYSIS:
                basePriority = 1; // Highest priority
                break;
            case OBJECT_DETECTION:
                basePriority = 2;
                break;
            case MOVEMENT_PREDICTION:
                basePriority = 3;
                break;
            case INFRASTRUCTURE_STATUS:
                basePriority = 4;
                break;
            case POPULATION_DENSITY:
                basePriority = 5;
                break;
            case WEATHER_OVERLAY:
                basePriority = 6;
                break;
            case ROUTE_OPTIMIZATION:
                basePriority = 7;
                break;
            default:
                basePriority = 8;
        }
        
        // Adjust based on confidence
        if (confidence >= 0.8) {
            basePriority = Math.max(1, basePriority - 1);
        } else if (confidence < 0.5) {
            basePriority = Math.min(10, basePriority + 1);
        }
        
        return basePriority;
    }
    
    /**
     * Create a formatted display string for this item
     */
    public String getDisplayString() {
        StringBuilder sb = new StringBuilder();
        sb.append(title);
        
        if (description != null && !description.isEmpty()) {
            sb.append("\n").append(description);
        }
        
        sb.append("\nConfidence: ").append(getConfidenceLevel());
        sb.append(" (").append(String.format("%.1f%%", confidence * 100)).append(")");
        
        if (source != null && !source.isEmpty()) {
            sb.append("\nSource: ").append(source);
        }
        
        // Add age information
        long ageSeconds = getAge() / 1000;
        if (ageSeconds < 60) {
            sb.append("\nAge: ").append(ageSeconds).append("s");
        } else if (ageSeconds < 3600) {
            sb.append("\nAge: ").append(ageSeconds / 60).append("m");
        } else {
            sb.append("\nAge: ").append(ageSeconds / 3600).append("h");
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return "AIOverlayItem{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", layerType=" + layerType +
                ", confidence=" + confidence +
                ", location=" + location +
                ", timestamp=" + timestamp +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        AIOverlayItem that = (AIOverlayItem) o;
        return id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}