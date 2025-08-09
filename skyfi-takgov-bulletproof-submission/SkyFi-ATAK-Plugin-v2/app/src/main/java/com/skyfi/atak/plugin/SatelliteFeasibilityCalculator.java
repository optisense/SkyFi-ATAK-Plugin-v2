package com.skyfi.atak.plugin;

import com.skyfi.atak.plugin.skyfiapi.FeasibilityInfo;
import com.skyfi.atak.plugin.skyfiapi.FeasibilityInfo.FeasibilityLevel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for calculating satellite pass feasibility based on sensor type,
 * geographic location, and date range.
 * 
 * This provides mock calculations based on realistic satellite constellation characteristics.
 */
public class SatelliteFeasibilityCalculator {
    
    /**
     * Calculate feasibility for a tasking order based on sensor type, location, and date range
     * 
     * @param sensorType The sensor type (ASAP, EO, SAR, ADS-B)
     * @param latitude Target latitude
     * @param longitude Target longitude
     * @param windowStart Start date in ISO format (YYYY-MM-DD)
     * @param windowEnd End date in ISO format (YYYY-MM-DD)
     * @return FeasibilityInfo containing pass count and feasibility assessment
     */
    public static FeasibilityInfo calculateFeasibility(String sensorType, double latitude, 
                                                      double longitude, String windowStart, 
                                                      String windowEnd) {
        if (sensorType == null || windowStart == null || windowEnd == null) {
            return createDefaultFeasibility();
        }
        
        try {
            // Parse dates
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startDate = LocalDate.parse(windowStart.substring(0, 10), formatter);
            LocalDate endDate = LocalDate.parse(windowEnd.substring(0, 10), formatter);
            
            long daysDuration = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            if (daysDuration <= 0) {
                return createDefaultFeasibility();
            }
            
            // Calculate expected passes based on sensor type and location
            int expectedPasses = calculateSensorPasses(sensorType, latitude, longitude, daysDuration);
            
            // Determine feasibility level
            FeasibilityLevel level = determineFeasibilityLevel(sensorType, expectedPasses, daysDuration);
            
            // Generate explanation
            String explanation = generateExplanation(sensorType, expectedPasses, daysDuration, latitude);
            
            String dateRange = startDate.format(DateTimeFormatter.ofPattern("MMM dd")) + 
                             " - " + endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
            
            return new FeasibilityInfo(expectedPasses, level, sensorType, explanation, 
                                     latitude, longitude, dateRange);
            
        } catch (Exception e) {
            return createDefaultFeasibility();
        }
    }
    
    /**
     * Calculate expected satellite passes based on sensor type and location
     */
    private static int calculateSensorPasses(String sensorType, double latitude, 
                                           double longitude, long daysDuration) {
        double latitudeFactor = calculateLatitudeFactor(latitude);
        
        switch (sensorType.toUpperCase()) {
            case "ASAP":
                // ASAP typically uses multiple constellation types for fastest response
                // Higher pass frequency due to multiple satellite types
                return (int) Math.round(daysDuration * 4.5 * latitudeFactor);
                
            case "EO":
                // Electro-optical satellites (Landsat, Sentinel, commercial constellations)
                // Moderate pass frequency
                return (int) Math.round(daysDuration * 3.2 * latitudeFactor);
                
            case "SAR":
                // Synthetic Aperture Radar satellites (Sentinel-1, TerraSAR-X, etc.)
                // Lower pass frequency but weather-independent
                return (int) Math.round(daysDuration * 2.1 * latitudeFactor);
                
            case "ADS-B":
                // Aircraft tracking satellites, specialized constellation
                // Variable frequency based on air traffic density
                double airTrafficFactor = calculateAirTrafficFactor(latitude, longitude);
                return (int) Math.round(daysDuration * 2.8 * latitudeFactor * airTrafficFactor);
                
            default:
                // Default to EO characteristics
                return (int) Math.round(daysDuration * 3.0 * latitudeFactor);
        }
    }
    
    /**
     * Calculate latitude factor - higher latitudes get more frequent passes
     * due to orbital mechanics (satellites converge at poles)
     */
    private static double calculateLatitudeFactor(double latitude) {
        double absLat = Math.abs(latitude);
        
        if (absLat < 30) {
            return 0.8; // Equatorial regions - fewer passes
        } else if (absLat < 45) {
            return 1.0; // Mid-latitudes - baseline
        } else if (absLat < 60) {
            return 1.3; // Higher latitudes - more passes
        } else {
            return 1.6; // Polar regions - most passes
        }
    }
    
    /**
     * Calculate air traffic factor for ADS-B sensor type
     */
    private static double calculateAirTrafficFactor(double latitude, double longitude) {
        double absLat = Math.abs(latitude);
        
        // North America, Europe, East Asia have higher air traffic
        boolean isHighTrafficRegion = (latitude > 25 && latitude < 75 && 
                                     ((longitude > -140 && longitude < -60) ||  // North America
                                      (longitude > -15 && longitude < 50) ||    // Europe
                                      (longitude > 100 && longitude < 150)));   // East Asia
        
        if (isHighTrafficRegion) {
            return 1.4;
        } else if (absLat < 60) {
            return 1.0; // Moderate traffic
        } else {
            return 0.6; // Low traffic (polar regions)
        }
    }
    
    /**
     * Determine feasibility level based on expected passes and duration
     */
    private static FeasibilityLevel determineFeasibilityLevel(String sensorType, 
                                                            int expectedPasses, 
                                                            long daysDuration) {
        double passesPerDay = (double) expectedPasses / daysDuration;
        
        // Adjust thresholds based on sensor type
        double excellentThreshold, goodThreshold, fairThreshold;
        
        switch (sensorType.toUpperCase()) {
            case "ASAP":
                excellentThreshold = 4.0;
                goodThreshold = 2.5;
                fairThreshold = 1.5;
                break;
            case "SAR":
                excellentThreshold = 2.5;
                goodThreshold = 1.5;
                fairThreshold = 1.0;
                break;
            case "ADS-B":
                excellentThreshold = 3.0;
                goodThreshold = 2.0;
                fairThreshold = 1.2;
                break;
            default: // EO and others
                excellentThreshold = 3.5;
                goodThreshold = 2.0;
                fairThreshold = 1.2;
                break;
        }
        
        if (passesPerDay >= excellentThreshold) {
            return FeasibilityLevel.EXCELLENT;
        } else if (passesPerDay >= goodThreshold) {
            return FeasibilityLevel.GOOD;
        } else if (passesPerDay >= fairThreshold) {
            return FeasibilityLevel.FAIR;
        } else {
            return FeasibilityLevel.POOR;
        }
    }
    
    /**
     * Generate human-readable explanation of feasibility
     */
    private static String generateExplanation(String sensorType, int expectedPasses, 
                                            long daysDuration, double latitude) {
        double passesPerDay = (double) expectedPasses / daysDuration;
        String latitudeDesc = getLatitudeDescription(latitude);
        
        StringBuilder explanation = new StringBuilder();
        explanation.append(String.format("%.1f avg passes/day for %s sensors. ", 
                                        passesPerDay, sensorType));
        explanation.append(latitudeDesc);
        
        if (passesPerDay >= 3.0) {
            explanation.append(" High probability of successful tasking.");
        } else if (passesPerDay >= 2.0) {
            explanation.append(" Good probability of successful tasking.");
        } else if (passesPerDay >= 1.0) {
            explanation.append(" Consider extending date range for better results.");
        } else {
            explanation.append(" Consider longer date range or different sensor type.");
        }
        
        return explanation.toString();
    }
    
    /**
     * Get latitude-based description
     */
    private static String getLatitudeDescription(double latitude) {
        double absLat = Math.abs(latitude);
        
        if (absLat < 30) {
            return "Equatorial location has moderate pass frequency.";
        } else if (absLat < 45) {
            return "Mid-latitude location provides good coverage.";
        } else if (absLat < 60) {
            return "Higher latitude increases pass frequency.";
        } else {
            return "Polar location offers excellent pass frequency.";
        }
    }
    
    /**
     * Create a default feasibility info when calculation fails
     */
    private static FeasibilityInfo createDefaultFeasibility() {
        return new FeasibilityInfo(0, FeasibilityLevel.POOR, "UNKNOWN", 
                                 "Unable to calculate feasibility. Please check inputs.", 
                                 0.0, 0.0, "Unknown");
    }
}