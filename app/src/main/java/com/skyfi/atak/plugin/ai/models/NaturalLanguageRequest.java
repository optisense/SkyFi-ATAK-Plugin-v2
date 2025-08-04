package com.skyfi.atak.plugin.ai.models;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

/**
 * Request for natural language processing and queries
 */
public class NaturalLanguageRequest extends AIRequest {
    @SerializedName("query_text")
    private String queryText;
    
    @SerializedName("query_type")
    private QueryType queryType;
    
    @SerializedName("language")
    private String language = "en";
    
    @SerializedName("geospatial_context")
    private GeospatialContext geospatialContext;
    
    @SerializedName("conversation_history")
    private java.util.List<ConversationMessage> conversationHistory;
    
    public enum QueryType {
        SEARCH, ANALYSIS, COMMAND, QUESTION, REPORT_GENERATION
    }
    
    public static class GeospatialContext {
        @SerializedName("current_location")
        private Location currentLocation;
        
        @SerializedName("area_of_interest")
        private String areaOfInterestWkt;
        
        @SerializedName("map_bounds")
        private MapBounds mapBounds;
        
        @SerializedName("zoom_level")
        private int zoomLevel;
        
        public static class Location {
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
        
        public static class MapBounds {
            @SerializedName("north")
            private double north;
            
            @SerializedName("south")
            private double south;
            
            @SerializedName("east")
            private double east;
            
            @SerializedName("west")
            private double west;
            
            // Getters and setters
            public double getNorth() { return north; }
            public void setNorth(double north) { this.north = north; }
            
            public double getSouth() { return south; }
            public void setSouth(double south) { this.south = south; }
            
            public double getEast() { return east; }
            public void setEast(double east) { this.east = east; }
            
            public double getWest() { return west; }
            public void setWest(double west) { this.west = west; }
        }
        
        // Getters and setters
        public Location getCurrentLocation() { return currentLocation; }
        public void setCurrentLocation(Location currentLocation) { this.currentLocation = currentLocation; }
        
        public String getAreaOfInterestWkt() { return areaOfInterestWkt; }
        public void setAreaOfInterestWkt(String areaOfInterestWkt) { this.areaOfInterestWkt = areaOfInterestWkt; }
        
        public MapBounds getMapBounds() { return mapBounds; }
        public void setMapBounds(MapBounds mapBounds) { this.mapBounds = mapBounds; }
        
        public int getZoomLevel() { return zoomLevel; }
        public void setZoomLevel(int zoomLevel) { this.zoomLevel = zoomLevel; }
    }
    
    public static class ConversationMessage {
        @SerializedName("role")
        private String role; // user, assistant, system
        
        @SerializedName("content")
        private String content;
        
        @SerializedName("timestamp")
        private long timestamp;
        
        // Getters and setters
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
    
    // Getters and setters
    public String getQueryText() { return queryText; }
    public void setQueryText(String queryText) { this.queryText = queryText; }
    
    public QueryType getQueryType() { return queryType; }
    public void setQueryType(QueryType queryType) { this.queryType = queryType; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public GeospatialContext getGeospatialContext() { return geospatialContext; }
    public void setGeospatialContext(GeospatialContext geospatialContext) { this.geospatialContext = geospatialContext; }
    
    public java.util.List<ConversationMessage> getConversationHistory() { return conversationHistory; }
    public void setConversationHistory(java.util.List<ConversationMessage> conversationHistory) { 
        this.conversationHistory = conversationHistory; 
    }
}