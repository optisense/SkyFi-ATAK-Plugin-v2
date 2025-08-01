package com.skyfi.atak.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Validates AOI sizes against provider requirements and provides visual feedback
 */
public class AOISizeValidator {
    
    /**
     * Provider compatibility result
     */
    public static class ProviderCompatibility {
        public final String provider;
        public final boolean isCompatible;
        public final String reason;
        public final double minimumSizeKm2;
        public final double recommendedMinKm2;
        
        public ProviderCompatibility(String provider, boolean isCompatible, String reason, 
                                    double minimumSizeKm2, double recommendedMinKm2) {
            this.provider = provider;
            this.isCompatible = isCompatible;
            this.reason = reason;
            this.minimumSizeKm2 = minimumSizeKm2;
            this.recommendedMinKm2 = recommendedMinKm2;
        }
    }
    
    /**
     * Overall validation result
     */
    public static class ValidationResult {
        public final boolean hasCompatibleProviders;
        public final List<ProviderCompatibility> providerResults;
        public final String overallWarning;
        public final int warningLevel; // 0=OK, 1=Warning, 2=Error
        
        public ValidationResult(boolean hasCompatibleProviders, 
                              List<ProviderCompatibility> providerResults,
                              String overallWarning,
                              int warningLevel) {
            this.hasCompatibleProviders = hasCompatibleProviders;
            this.providerResults = providerResults;
            this.overallWarning = overallWarning;
            this.warningLevel = warningLevel;
        }
    }
    
    // Provider minimum sizes in km²
    private static final Map<String, Double> PROVIDER_MIN_SIZES = new HashMap<>();
    private static final Map<String, Double> PROVIDER_RECOMMENDED_SIZES = new HashMap<>();
    
    static {
        // Actual minimum sizes
        PROVIDER_MIN_SIZES.put("siwei", 25.0);      // 5km x 5km
        PROVIDER_MIN_SIZES.put("satellogic", 100.0); // 10km x 10km
        PROVIDER_MIN_SIZES.put("umbra", 16.0);      // 4km x 4km
        PROVIDER_MIN_SIZES.put("geosat", 25.0);     // 5km x 5km
        PROVIDER_MIN_SIZES.put("planet", 100.0);    // 10km x 10km
        PROVIDER_MIN_SIZES.put("impro", 64.0);      // 8km x 8km
        
        // Recommended sizes (slightly larger for better results)
        PROVIDER_RECOMMENDED_SIZES.put("siwei", 36.0);      // 6km x 6km
        PROVIDER_RECOMMENDED_SIZES.put("satellogic", 121.0); // 11km x 11km
        PROVIDER_RECOMMENDED_SIZES.put("umbra", 25.0);      // 5km x 5km
        PROVIDER_RECOMMENDED_SIZES.put("geosat", 36.0);     // 6km x 6km
        PROVIDER_RECOMMENDED_SIZES.put("planet", 121.0);    // 11km x 11km
        PROVIDER_RECOMMENDED_SIZES.put("impro", 81.0);      // 9km x 9km
    }
    
    /**
     * Validate AOI size and return detailed compatibility information
     */
    public static ValidationResult validateAOISize(double areaKm2) {
        List<ProviderCompatibility> results = new ArrayList<>();
        int compatibleCount = 0;
        
        // Check each provider
        for (Map.Entry<String, Double> entry : PROVIDER_MIN_SIZES.entrySet()) {
            String provider = entry.getKey();
            double minSize = entry.getValue();
            double recommendedSize = PROVIDER_RECOMMENDED_SIZES.get(provider);
            
            boolean isCompatible = areaKm2 >= minSize;
            String reason;
            
            if (isCompatible) {
                compatibleCount++;
                if (areaKm2 >= recommendedSize) {
                    reason = "Excellent coverage";
                } else {
                    reason = String.format("Minimum met (%.0f km² min)", minSize);
                }
            } else {
                double shortage = minSize - areaKm2;
                reason = String.format("Too small (need %.0f km² more)", shortage);
            }
            
            results.add(new ProviderCompatibility(provider, isCompatible, reason, minSize, recommendedSize));
        }
        
        // Determine overall status
        String overallWarning;
        int warningLevel;
        
        if (areaKm2 > 2000) {
            overallWarning = String.format("Area too large! Maximum allowed is 2000 km² (current: %.0f km²)", areaKm2);
            warningLevel = 2; // Error
        } else if (compatibleCount == 0) {
            overallWarning = "Area too small for all providers! Increase AOI size.";
            warningLevel = 2; // Error
        } else if (compatibleCount < 3) {
            overallWarning = String.format("Limited provider options (%d of %d compatible)", 
                                         compatibleCount, PROVIDER_MIN_SIZES.size());
            warningLevel = 1; // Warning
        } else {
            overallWarning = String.format("Good compatibility (%d providers available)", compatibleCount);
            warningLevel = 0; // OK
        }
        
        return new ValidationResult(compatibleCount > 0, results, overallWarning, warningLevel);
    }
    
    /**
     * Check if a specific provider is compatible with the AOI size
     */
    public static boolean isProviderCompatible(String provider, double areaKm2) {
        Double minSize = PROVIDER_MIN_SIZES.get(provider.toLowerCase());
        return minSize != null && areaKm2 >= minSize;
    }
    
    /**
     * Get minimum size for a specific provider
     */
    public static double getProviderMinimumSize(String provider) {
        Double minSize = PROVIDER_MIN_SIZES.get(provider.toLowerCase());
        return minSize != null ? minSize : 25.0; // Default fallback
    }
    
    /**
     * Get a formatted string describing provider requirements
     */
    public static String getProviderRequirements(String provider, double currentAreaKm2) {
        Double minSize = PROVIDER_MIN_SIZES.get(provider.toLowerCase());
        if (minSize == null) {
            return "Unknown provider requirements";
        }
        
        double sideLength = Math.sqrt(minSize);
        String status = currentAreaKm2 >= minSize ? "✓" : "✗";
        
        return String.format("%s %s: Min %.0f km² (%.1f×%.1f km)", 
                           status, 
                           provider.substring(0, 1).toUpperCase() + provider.substring(1),
                           minSize, 
                           sideLength, 
                           sideLength);
    }
    
    /**
     * Get color code for warning level
     */
    public static int getWarningColor(int warningLevel) {
        switch (warningLevel) {
            case 0: return 0xFF4CAF50; // Green
            case 1: return 0xFFFF9800; // Orange
            case 2: return 0xFFF44336; // Red
            default: return 0xFF757575; // Grey
        }
    }
    
    /**
     * Format area with appropriate units
     */
    public static String formatArea(double areaKm2) {
        if (areaKm2 < 1) {
            return String.format("%.0f m²", areaKm2 * 1000000);
        } else if (areaKm2 < 10) {
            return String.format("%.2f km²", areaKm2);
        } else if (areaKm2 < 100) {
            return String.format("%.1f km²", areaKm2);
        } else {
            return String.format("%.0f km²", areaKm2);
        }
    }
}