package com.optisense.skyfi.atak.skyfiapi;

/**
 * Model class to store satellite feasibility information for tasking orders
 * Based on sensor type, location, and date range
 */
public class FeasibilityInfo {
    public enum FeasibilityLevel {
        EXCELLENT("Excellent", 0xFF4CAF50), // Green
        GOOD("Good", 0xFF8BC34A),           // Light Green
        FAIR("Fair", 0xFFFF9800),           // Orange
        POOR("Poor", 0xFFF44336);           // Red
        
        private final String displayName;
        private final int color;
        
        FeasibilityLevel(String displayName, int color) {
            this.displayName = displayName;
            this.color = color;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getColor() {
            return color;
        }
    }
    
    private int expectedPasses;
    private FeasibilityLevel feasibilityLevel;
    private String sensorType;
    private String explanation;
    private double latitude;
    private double longitude;
    private String dateRange;
    
    public FeasibilityInfo() {}
    
    public FeasibilityInfo(int expectedPasses, FeasibilityLevel feasibilityLevel, 
                          String sensorType, String explanation, double latitude, 
                          double longitude, String dateRange) {
        this.expectedPasses = expectedPasses;
        this.feasibilityLevel = feasibilityLevel;
        this.sensorType = sensorType;
        this.explanation = explanation;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dateRange = dateRange;
    }
    
    public int getExpectedPasses() {
        return expectedPasses;
    }
    
    public void setExpectedPasses(int expectedPasses) {
        this.expectedPasses = expectedPasses;
    }
    
    public FeasibilityLevel getFeasibilityLevel() {
        return feasibilityLevel;
    }
    
    public void setFeasibilityLevel(FeasibilityLevel feasibilityLevel) {
        this.feasibilityLevel = feasibilityLevel;
    }
    
    public String getSensorType() {
        return sensorType;
    }
    
    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }
    
    public String getExplanation() {
        return explanation;
    }
    
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public String getDateRange() {
        return dateRange;
    }
    
    public void setDateRange(String dateRange) {
        this.dateRange = dateRange;
    }
    
    @Override
    public String toString() {
        return "FeasibilityInfo{" +
                "expectedPasses=" + expectedPasses +
                ", feasibilityLevel=" + feasibilityLevel +
                ", sensorType='" + sensorType + '\'' +
                ", explanation='" + explanation + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", dateRange='" + dateRange + '\'' +
                '}';
    }
}