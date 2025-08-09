package com.skyfi.atak.plugin.ai.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Request for predictive analytics and forecasting
 */
public class PredictionRequest extends AIRequest {
    @SerializedName("prediction_type")
    private PredictionType predictionType;
    
    @SerializedName("historical_data")
    private List<DataPoint> historicalData;
    
    @SerializedName("prediction_horizon_hours")
    private int predictionHorizonHours = 24;
    
    @SerializedName("area_of_interest")
    private String areaOfInterestWkt;
    
    @SerializedName("parameters")
    private PredictionParameters parameters;
    
    public enum PredictionType {
        POPULATION_MOVEMENT, WEATHER_IMPACT, THREAT_ASSESSMENT, 
        ROUTE_OPTIMIZATION, RESOURCE_REQUIREMENTS, ACTIVITY_PATTERNS
    }
    
    public static class DataPoint {
        @SerializedName("timestamp")
        private long timestamp;
        
        @SerializedName("latitude")
        private double latitude;
        
        @SerializedName("longitude")
        private double longitude;
        
        @SerializedName("value")
        private double value;
        
        @SerializedName("metadata")
        private java.util.Map<String, Object> metadata;
        
        // Getters and setters
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
        
        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }
        
        public java.util.Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(java.util.Map<String, Object> metadata) { this.metadata = metadata; }
    }
    
    public static class PredictionParameters {
        @SerializedName("confidence_level")
        private double confidenceLevel = 0.95;
        
        @SerializedName("resolution_meters")
        private double resolutionMeters = 100.0;
        
        @SerializedName("include_uncertainty")
        private boolean includeUncertainty = true;
        
        @SerializedName("external_factors")
        private List<String> externalFactors;
        
        // Getters and setters
        public double getConfidenceLevel() { return confidenceLevel; }
        public void setConfidenceLevel(double confidenceLevel) { this.confidenceLevel = confidenceLevel; }
        
        public double getResolutionMeters() { return resolutionMeters; }
        public void setResolutionMeters(double resolutionMeters) { this.resolutionMeters = resolutionMeters; }
        
        public boolean isIncludeUncertainty() { return includeUncertainty; }
        public void setIncludeUncertainty(boolean includeUncertainty) { this.includeUncertainty = includeUncertainty; }
        
        public List<String> getExternalFactors() { return externalFactors; }
        public void setExternalFactors(List<String> externalFactors) { this.externalFactors = externalFactors; }
    }
    
    // Getters and setters
    public PredictionType getPredictionType() { return predictionType; }
    public void setPredictionType(PredictionType predictionType) { this.predictionType = predictionType; }
    
    public List<DataPoint> getHistoricalData() { return historicalData; }
    public void setHistoricalData(List<DataPoint> historicalData) { this.historicalData = historicalData; }
    
    public int getPredictionHorizonHours() { return predictionHorizonHours; }
    public void setPredictionHorizonHours(int predictionHorizonHours) { this.predictionHorizonHours = predictionHorizonHours; }
    
    public String getAreaOfInterestWkt() { return areaOfInterestWkt; }
    public void setAreaOfInterestWkt(String areaOfInterestWkt) { this.areaOfInterestWkt = areaOfInterestWkt; }
    
    public PredictionParameters getParameters() { return parameters; }
    public void setParameters(PredictionParameters parameters) { this.parameters = parameters; }
}