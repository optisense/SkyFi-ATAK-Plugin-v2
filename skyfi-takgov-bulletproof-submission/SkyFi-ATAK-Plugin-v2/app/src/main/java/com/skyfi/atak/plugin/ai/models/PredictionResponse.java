package com.skyfi.atak.plugin.ai.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response for predictive analytics and forecasting
 */
public class PredictionResponse extends AIResponse {
    @SerializedName("predictions")
    private List<Prediction> predictions;
    
    @SerializedName("summary")
    private PredictionSummary summary;
    
    @SerializedName("visualization_data")
    private VisualizationData visualizationData;
    
    public static class Prediction {
        @SerializedName("timestamp")
        private long timestamp;
        
        @SerializedName("latitude")
        private double latitude;
        
        @SerializedName("longitude")
        private double longitude;
        
        @SerializedName("predicted_value")
        private double predictedValue;
        
        @SerializedName("confidence_interval")
        private ConfidenceInterval confidenceInterval;
        
        @SerializedName("probability")
        private double probability;
        
        @SerializedName("risk_level")
        private RiskLevel riskLevel;
        
        @SerializedName("description")
        private String description;
        
        public enum RiskLevel {
            LOW, MEDIUM, HIGH, CRITICAL
        }
        
        public static class ConfidenceInterval {
            @SerializedName("lower_bound")
            private double lowerBound;
            
            @SerializedName("upper_bound")
            private double upperBound;
            
            @SerializedName("confidence_level")
            private double confidenceLevel;
            
            // Getters and setters
            public double getLowerBound() { return lowerBound; }
            public void setLowerBound(double lowerBound) { this.lowerBound = lowerBound; }
            
            public double getUpperBound() { return upperBound; }
            public void setUpperBound(double upperBound) { this.upperBound = upperBound; }
            
            public double getConfidenceLevel() { return confidenceLevel; }
            public void setConfidenceLevel(double confidenceLevel) { this.confidenceLevel = confidenceLevel; }
        }
        
        // Getters and setters
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
        
        public double getPredictedValue() { return predictedValue; }
        public void setPredictedValue(double predictedValue) { this.predictedValue = predictedValue; }
        
        public ConfidenceInterval getConfidenceInterval() { return confidenceInterval; }
        public void setConfidenceInterval(ConfidenceInterval confidenceInterval) { this.confidenceInterval = confidenceInterval; }
        
        public double getProbability() { return probability; }
        public void setProbability(double probability) { this.probability = probability; }
        
        public RiskLevel getRiskLevel() { return riskLevel; }
        public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
    
    public static class PredictionSummary {
        @SerializedName("total_predictions")
        private int totalPredictions;
        
        @SerializedName("high_risk_areas")
        private int highRiskAreas;
        
        @SerializedName("average_confidence")
        private double averageConfidence;
        
        @SerializedName("key_insights")
        private List<String> keyInsights;
        
        @SerializedName("recommended_actions")
        private List<String> recommendedActions;
        
        // Getters and setters
        public int getTotalPredictions() { return totalPredictions; }
        public void setTotalPredictions(int totalPredictions) { this.totalPredictions = totalPredictions; }
        
        public int getHighRiskAreas() { return highRiskAreas; }
        public void setHighRiskAreas(int highRiskAreas) { this.highRiskAreas = highRiskAreas; }
        
        public double getAverageConfidence() { return averageConfidence; }
        public void setAverageConfidence(double averageConfidence) { this.averageConfidence = averageConfidence; }
        
        public List<String> getKeyInsights() { return keyInsights; }
        public void setKeyInsights(List<String> keyInsights) { this.keyInsights = keyInsights; }
        
        public List<String> getRecommendedActions() { return recommendedActions; }
        public void setRecommendedActions(List<String> recommendedActions) { this.recommendedActions = recommendedActions; }
    }
    
    public static class VisualizationData {
        @SerializedName("heatmap_data")
        private List<HeatmapPoint> heatmapData;
        
        @SerializedName("movement_vectors")
        private List<MovementVector> movementVectors;
        
        @SerializedName("risk_zones")
        private List<RiskZone> riskZones;
        
        public static class HeatmapPoint {
            @SerializedName("latitude")
            private double latitude;
            
            @SerializedName("longitude")
            private double longitude;
            
            @SerializedName("intensity")
            private double intensity;
            
            // Getters and setters
            public double getLatitude() { return latitude; }
            public void setLatitude(double latitude) { this.latitude = latitude; }
            
            public double getLongitude() { return longitude; }
            public void setLongitude(double longitude) { this.longitude = longitude; }
            
            public double getIntensity() { return intensity; }
            public void setIntensity(double intensity) { this.intensity = intensity; }
        }
        
        public static class MovementVector {
            @SerializedName("start_lat")
            private double startLat;
            
            @SerializedName("start_lon")
            private double startLon;
            
            @SerializedName("end_lat")
            private double endLat;
            
            @SerializedName("end_lon")
            private double endLon;
            
            @SerializedName("magnitude")
            private double magnitude;
            
            // Getters and setters
            public double getStartLat() { return startLat; }
            public void setStartLat(double startLat) { this.startLat = startLat; }
            
            public double getStartLon() { return startLon; }
            public void setStartLon(double startLon) { this.startLon = startLon; }
            
            public double getEndLat() { return endLat; }
            public void setEndLat(double endLat) { this.endLat = endLat; }
            
            public double getEndLon() { return endLon; }
            public void setEndLon(double endLon) { this.endLon = endLon; }
            
            public double getMagnitude() { return magnitude; }
            public void setMagnitude(double magnitude) { this.magnitude = magnitude; }
        }
        
        public static class RiskZone {
            @SerializedName("area_wkt")
            private String areaWkt;
            
            @SerializedName("risk_level")
            private Prediction.RiskLevel riskLevel;
            
            @SerializedName("description")
            private String description;
            
            // Getters and setters
            public String getAreaWkt() { return areaWkt; }
            public void setAreaWkt(String areaWkt) { this.areaWkt = areaWkt; }
            
            public Prediction.RiskLevel getRiskLevel() { return riskLevel; }
            public void setRiskLevel(Prediction.RiskLevel riskLevel) { this.riskLevel = riskLevel; }
            
            public String getDescription() { return description; }
            public void setDescription(String description) { this.description = description; }
        }
        
        // Getters and setters
        public List<HeatmapPoint> getHeatmapData() { return heatmapData; }
        public void setHeatmapData(List<HeatmapPoint> heatmapData) { this.heatmapData = heatmapData; }
        
        public List<MovementVector> getMovementVectors() { return movementVectors; }
        public void setMovementVectors(List<MovementVector> movementVectors) { this.movementVectors = movementVectors; }
        
        public List<RiskZone> getRiskZones() { return riskZones; }
        public void setRiskZones(List<RiskZone> riskZones) { this.riskZones = riskZones; }
    }
    
    // Getters and setters
    public List<Prediction> getPredictions() { return predictions; }
    public void setPredictions(List<Prediction> predictions) { this.predictions = predictions; }
    
    public PredictionSummary getSummary() { return summary; }
    public void setSummary(PredictionSummary summary) { this.summary = summary; }
    
    public VisualizationData getVisualizationData() { return visualizationData; }
    public void setVisualizationData(VisualizationData visualizationData) { this.visualizationData = visualizationData; }
}