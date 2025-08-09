package com.skyfi.atak.plugin.ai.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Request for object detection in satellite imagery
 */
public class ObjectDetectionRequest extends AIRequest {
    @SerializedName("imagery_data")
    private ImageryData imageryData;
    
    @SerializedName("detection_types")
    private List<ObjectType> detectionTypes;
    
    @SerializedName("sensitivity_threshold")
    private double sensitivityThreshold = 0.7;
    
    @SerializedName("area_of_interest")
    private AreaOfInterest areaOfInterest;
    
    public enum ObjectType {
        VEHICLES, AIRCRAFT, BUILDINGS, INFRASTRUCTURE, PERSONNEL, SHIPS, 
        MILITARY_VEHICLES, CIVILIAN_VEHICLES, DAMAGED_STRUCTURES, ALL
    }
    
    public static class ImageryData {
        @SerializedName("image_url")
        private String imageUrl;
        
        @SerializedName("image_base64")
        private String imageBase64;
        
        @SerializedName("image_format")
        private String imageFormat;
        
        @SerializedName("resolution_meters_per_pixel")
        private double resolutionMetersPerPixel;
        
        @SerializedName("capture_date")
        private String captureDate;
        
        @SerializedName("satellite_name")
        private String satelliteName;
        
        // Getters and setters
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        
        public String getImageBase64() { return imageBase64; }
        public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
        
        public String getImageFormat() { return imageFormat; }
        public void setImageFormat(String imageFormat) { this.imageFormat = imageFormat; }
        
        public double getResolutionMetersPerPixel() { return resolutionMetersPerPixel; }
        public void setResolutionMetersPerPixel(double resolutionMetersPerPixel) { 
            this.resolutionMetersPerPixel = resolutionMetersPerPixel; 
        }
        
        public String getCaptureDate() { return captureDate; }
        public void setCaptureDate(String captureDate) { this.captureDate = captureDate; }
        
        public String getSatelliteName() { return satelliteName; }
        public void setSatelliteName(String satelliteName) { this.satelliteName = satelliteName; }
    }
    
    public static class AreaOfInterest {
        @SerializedName("wkt")
        private String wkt;
        
        @SerializedName("center_lat")
        private double centerLat;
        
        @SerializedName("center_lon")
        private double centerLon;
        
        @SerializedName("radius_meters")
        private double radiusMeters;
        
        // Getters and setters
        public String getWkt() { return wkt; }
        public void setWkt(String wkt) { this.wkt = wkt; }
        
        public double getCenterLat() { return centerLat; }
        public void setCenterLat(double centerLat) { this.centerLat = centerLat; }
        
        public double getCenterLon() { return centerLon; }
        public void setCenterLon(double centerLon) { this.centerLon = centerLon; }
        
        public double getRadiusMeters() { return radiusMeters; }
        public void setRadiusMeters(double radiusMeters) { this.radiusMeters = radiusMeters; }
    }
    
    // Getters and setters
    public ImageryData getImageryData() { return imageryData; }
    public void setImageryData(ImageryData imageryData) { this.imageryData = imageryData; }
    
    public List<ObjectType> getDetectionTypes() { return detectionTypes; }
    public void setDetectionTypes(List<ObjectType> detectionTypes) { this.detectionTypes = detectionTypes; }
    
    public double getSensitivityThreshold() { return sensitivityThreshold; }
    public void setSensitivityThreshold(double sensitivityThreshold) { this.sensitivityThreshold = sensitivityThreshold; }
    
    public AreaOfInterest getAreaOfInterest() { return areaOfInterest; }
    public void setAreaOfInterest(AreaOfInterest areaOfInterest) { this.areaOfInterest = areaOfInterest; }
}