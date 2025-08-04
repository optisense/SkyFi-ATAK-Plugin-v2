package com.skyfi.atak.plugin.ai.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response for object detection in satellite imagery
 */
public class ObjectDetectionResponse extends AIResponse {
    @SerializedName("detected_objects")
    private List<DetectedObject> detectedObjects;
    
    @SerializedName("summary")
    private DetectionSummary summary;
    
    public static class DetectedObject {
        @SerializedName("object_id")
        private String objectId;
        
        @SerializedName("object_type")
        private ObjectDetectionRequest.ObjectType objectType;
        
        @SerializedName("confidence_score")
        private double confidenceScore;
        
        @SerializedName("bounding_box")
        private BoundingBox boundingBox;
        
        @SerializedName("center_coordinates")
        private Coordinates centerCoordinates;
        
        @SerializedName("attributes")
        private ObjectAttributes attributes;
        
        @SerializedName("description")
        private String description;
        
        public static class BoundingBox {
            @SerializedName("x")
            private int x;
            
            @SerializedName("y")
            private int y;
            
            @SerializedName("width")
            private int width;
            
            @SerializedName("height")
            private int height;
            
            // Getters and setters
            public int getX() { return x; }
            public void setX(int x) { this.x = x; }
            
            public int getY() { return y; }
            public void setY(int y) { this.y = y; }
            
            public int getWidth() { return width; }
            public void setWidth(int width) { this.width = width; }
            
            public int getHeight() { return height; }
            public void setHeight(int height) { this.height = height; }
        }
        
        public static class Coordinates {
            @SerializedName("latitude")
            private double latitude;
            
            @SerializedName("longitude")
            private double longitude;
            
            @SerializedName("altitude")
            private Double altitude;
            
            // Getters and setters
            public double getLatitude() { return latitude; }
            public void setLatitude(double latitude) { this.latitude = latitude; }
            
            public double getLongitude() { return longitude; }
            public void setLongitude(double longitude) { this.longitude = longitude; }
            
            public Double getAltitude() { return altitude; }
            public void setAltitude(Double altitude) { this.altitude = altitude; }
        }
        
        public static class ObjectAttributes {
            @SerializedName("size_category")
            private String sizeCategory; // small, medium, large
            
            @SerializedName("movement_status")
            private String movementStatus; // stationary, moving, unknown
            
            @SerializedName("condition")
            private String condition; // intact, damaged, destroyed
            
            @SerializedName("military_classification")
            private String militaryClassification;
            
            // Getters and setters
            public String getSizeCategory() { return sizeCategory; }
            public void setSizeCategory(String sizeCategory) { this.sizeCategory = sizeCategory; }
            
            public String getMovementStatus() { return movementStatus; }
            public void setMovementStatus(String movementStatus) { this.movementStatus = movementStatus; }
            
            public String getCondition() { return condition; }
            public void setCondition(String condition) { this.condition = condition; }
            
            public String getMilitaryClassification() { return militaryClassification; }
            public void setMilitaryClassification(String militaryClassification) { 
                this.militaryClassification = militaryClassification; 
            }
        }
        
        // Getters and setters
        public String getObjectId() { return objectId; }
        public void setObjectId(String objectId) { this.objectId = objectId; }
        
        public ObjectDetectionRequest.ObjectType getObjectType() { return objectType; }
        public void setObjectType(ObjectDetectionRequest.ObjectType objectType) { this.objectType = objectType; }
        
        public double getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
        
        public BoundingBox getBoundingBox() { return boundingBox; }
        public void setBoundingBox(BoundingBox boundingBox) { this.boundingBox = boundingBox; }
        
        public Coordinates getCenterCoordinates() { return centerCoordinates; }
        public void setCenterCoordinates(Coordinates centerCoordinates) { this.centerCoordinates = centerCoordinates; }
        
        public ObjectAttributes getAttributes() { return attributes; }
        public void setAttributes(ObjectAttributes attributes) { this.attributes = attributes; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
    
    public static class DetectionSummary {
        @SerializedName("total_objects")
        private int totalObjects;
        
        @SerializedName("objects_by_type")
        private java.util.Map<ObjectDetectionRequest.ObjectType, Integer> objectsByType;
        
        @SerializedName("average_confidence")
        private double averageConfidence;
        
        @SerializedName("high_confidence_objects")
        private int highConfidenceObjects;
        
        // Getters and setters
        public int getTotalObjects() { return totalObjects; }
        public void setTotalObjects(int totalObjects) { this.totalObjects = totalObjects; }
        
        public java.util.Map<ObjectDetectionRequest.ObjectType, Integer> getObjectsByType() { return objectsByType; }
        public void setObjectsByType(java.util.Map<ObjectDetectionRequest.ObjectType, Integer> objectsByType) { 
            this.objectsByType = objectsByType; 
        }
        
        public double getAverageConfidence() { return averageConfidence; }
        public void setAverageConfidence(double averageConfidence) { this.averageConfidence = averageConfidence; }
        
        public int getHighConfidenceObjects() { return highConfidenceObjects; }
        public void setHighConfidenceObjects(int highConfidenceObjects) { this.highConfidenceObjects = highConfidenceObjects; }
    }
    
    // Getters and setters
    public List<DetectedObject> getDetectedObjects() { return detectedObjects; }
    public void setDetectedObjects(List<DetectedObject> detectedObjects) { this.detectedObjects = detectedObjects; }
    
    public DetectionSummary getSummary() { return summary; }
    public void setSummary(DetectionSummary summary) { this.summary = summary; }
}