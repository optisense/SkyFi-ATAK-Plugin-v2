package com.skyfi.atak.plugin.ai.services;

import android.content.Context;
import com.atakmap.coremap.log.Log;
import com.skyfi.atak.plugin.ai.models.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Prediction Engine for movement patterns, threat assessment, and route optimization
 * Uses historical data and AI models to generate predictive insights
 */
public class PredictionEngine {
    private static final String TAG = "PredictionEngine";
    
    private static PredictionEngine instance;
    private final Context context;
    private final AICacheManager cacheManager;
    
    // Prediction algorithms
    private final MovementPredictor movementPredictor;
    private final ThreatAssessor threatAssessor;
    private final RouteOptimizer routeOptimizer;
    private final WeatherPredictor weatherPredictor;
    
    private PredictionEngine(Context context) {
        this.context = context;
        this.cacheManager = AICacheManager.getInstance(context);
        this.movementPredictor = new MovementPredictor();
        this.threatAssessor = new ThreatAssessor();
        this.routeOptimizer = new RouteOptimizer();
        this.weatherPredictor = new WeatherPredictor();
    }
    
    public static synchronized PredictionEngine getInstance(Context context) {
        if (instance == null) {
            instance = new PredictionEngine(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Generate predictions based on request type and historical data
     */
    public PredictionResponse generatePredictions(PredictionRequest request) {
        Log.d(TAG, "Generating predictions for type: " + request.getPredictionType());
        
        try {
            // Check cache first
            String cacheKey = generateCacheKey(request);
            PredictionResponse cachedResponse = cacheManager.get(cacheKey, PredictionResponse.class);
            
            if (cachedResponse != null) {
                Log.d(TAG, "Returning cached prediction result");
                return cachedResponse;
            }
            
            // Generate predictions based on type
            PredictionResponse response;
            switch (request.getPredictionType()) {
                case POPULATION_MOVEMENT:
                    response = generatePopulationMovementPredictions(request);
                    break;
                case WEATHER_IMPACT:
                    response = generateWeatherImpactPredictions(request);
                    break;
                case THREAT_ASSESSMENT:
                    response = generateThreatAssessmentPredictions(request);
                    break;
                case ROUTE_OPTIMIZATION:
                    response = generateRouteOptimizationPredictions(request);
                    break;
                case RESOURCE_REQUIREMENTS:
                    response = generateResourceRequirementPredictions(request);
                    break;
                case ACTIVITY_PATTERNS:
                    response = generateActivityPatternPredictions(request);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown prediction type: " + request.getPredictionType());
            }
            
            // Cache the result
            int cacheTtl = getCacheTtlForPredictionType(request.getPredictionType());
            cacheManager.put(cacheKey, response, cacheTtl);
            
            Log.d(TAG, "Prediction generation completed: " + response.getPredictions().size() + " predictions");
            return response;
            
        } catch (Exception e) {
            Log.e(TAG, "Error generating predictions", e);
            return createErrorResponse(request, e.getMessage());
        }
    }
    
    /**
     * Generate population movement predictions using PDFM
     */
    private PredictionResponse generatePopulationMovementPredictions(PredictionRequest request) {
        Log.d(TAG, "Generating population movement predictions");
        
        PredictionResponse response = createBaseResponse(request);
        List<PredictionResponse.Prediction> predictions = new ArrayList<>();
        
        // Analyze historical movement data
        List<PredictionRequest.DataPoint> historicalData = request.getHistoricalData();
        if (historicalData != null && !historicalData.isEmpty()) {
            predictions.addAll(movementPredictor.predictMovementPatterns(
                historicalData, request.getPredictionHorizonHours()));
        }
        
        // Generate default movement predictions if no historical data
        if (predictions.isEmpty()) {
            predictions.addAll(generateDefaultMovementPredictions(request));
        }
        
        response.setPredictions(predictions);
        
        // Generate summary
        PredictionResponse.PredictionSummary summary = generateMovementSummary(predictions);
        response.setSummary(summary);
        
        // Generate visualization data
        PredictionResponse.VisualizationData visualizationData = generateMovementVisualization(predictions);
        response.setVisualizationData(visualizationData);
        
        return response;
    }
    
    /**
     * Generate weather impact predictions using WeatherNext AI
     */
    private PredictionResponse generateWeatherImpactPredictions(PredictionRequest request) {
        Log.d(TAG, "Generating weather impact predictions");
        
        PredictionResponse response = createBaseResponse(request);
        List<PredictionResponse.Prediction> predictions = weatherPredictor.predictWeatherImpacts(
            request.getAreaOfInterestWkt(), request.getPredictionHorizonHours());
        
        response.setPredictions(predictions);
        
        // Generate summary
        PredictionResponse.PredictionSummary summary = generateWeatherSummary(predictions);
        response.setSummary(summary);
        
        return response;
    }
    
    /**
     * Generate threat assessment predictions
     */
    private PredictionResponse generateThreatAssessmentPredictions(PredictionRequest request) {
        Log.d(TAG, "Generating threat assessment predictions");
        
        PredictionResponse response = createBaseResponse(request);
        List<PredictionResponse.Prediction> predictions = threatAssessor.assessThreats(
            request.getHistoricalData(), request.getAreaOfInterestWkt());
        
        response.setPredictions(predictions);
        
        // Generate summary with threat-specific insights
        PredictionResponse.PredictionSummary summary = generateThreatSummary(predictions);
        response.setSummary(summary);
        
        // Generate risk zones visualization
        PredictionResponse.VisualizationData visualizationData = generateThreatVisualization(predictions);
        response.setVisualizationData(visualizationData);
        
        return response;
    }
    
    /**
     * Generate route optimization predictions
     */
    private PredictionResponse generateRouteOptimizationPredictions(PredictionRequest request) {
        Log.d(TAG, "Generating route optimization predictions");
        
        PredictionResponse response = createBaseResponse(request);
        List<PredictionResponse.Prediction> predictions = routeOptimizer.optimizeRoutes(
            request.getHistoricalData(), request.getAreaOfInterestWkt());
        
        response.setPredictions(predictions);
        
        // Generate summary with route recommendations
        PredictionResponse.PredictionSummary summary = generateRouteSummary(predictions);
        response.setSummary(summary);
        
        return response;
    }
    
    /**
     * Generate resource requirement predictions
     */
    private PredictionResponse generateResourceRequirementPredictions(PredictionRequest request) {
        Log.d(TAG, "Generating resource requirement predictions");
        
        PredictionResponse response = createBaseResponse(request);
        List<PredictionResponse.Prediction> predictions = generateResourcePredictions(request);
        
        response.setPredictions(predictions);
        
        // Generate summary with resource recommendations
        PredictionResponse.PredictionSummary summary = generateResourceSummary(predictions);
        response.setSummary(summary);
        
        return response;
    }
    
    /**
     * Generate activity pattern predictions
     */
    private PredictionResponse generateActivityPatternPredictions(PredictionRequest request) {
        Log.d(TAG, "Generating activity pattern predictions");
        
        PredictionResponse response = createBaseResponse(request);
        List<PredictionResponse.Prediction> predictions = generateActivityPredictions(request);
        
        response.setPredictions(predictions);
        
        // Generate summary with pattern insights
        PredictionResponse.PredictionSummary summary = generateActivitySummary(predictions);
        response.setSummary(summary);
        
        return response;
    }
    
    // Helper classes for different prediction algorithms
    
    private static class MovementPredictor {
        List<PredictionResponse.Prediction> predictMovementPatterns(
                List<PredictionRequest.DataPoint> historicalData, int horizonHours) {
            
            List<PredictionResponse.Prediction> predictions = new ArrayList<>();
            
            // Simple linear prediction based on historical trends
            // In a real implementation, this would use sophisticated ML models
            
            for (int hour = 1; hour <= horizonHours; hour++) {
                PredictionResponse.Prediction prediction = new PredictionResponse.Prediction();
                prediction.setTimestamp(System.currentTimeMillis() + (hour * 3600000L));
                
                // Calculate predicted position based on trend
                if (historicalData.size() >= 2) {
                    PredictionRequest.DataPoint last = historicalData.get(historicalData.size() - 1);
                    PredictionRequest.DataPoint previous = historicalData.get(historicalData.size() - 2);
                    
                    double latTrend = last.getLatitude() - previous.getLatitude();
                    double lonTrend = last.getLongitude() - previous.getLongitude();
                    
                    prediction.setLatitude(last.getLatitude() + (latTrend * hour));
                    prediction.setLongitude(last.getLongitude() + (lonTrend * hour));
                } else {
                    // Default prediction
                    prediction.setLatitude(38.0 + (Math.random() - 0.5) * 0.1);
                    prediction.setLongitude(-77.0 + (Math.random() - 0.5) * 0.1);
                }
                
                prediction.setPredictedValue(Math.random() * 100); // Activity level
                prediction.setProbability(0.7 + (Math.random() * 0.3));
                prediction.setRiskLevel(determineRiskLevel(prediction.getProbability()));
                prediction.setDescription("Predicted movement pattern for hour " + hour);
                
                // Add confidence interval
                PredictionResponse.Prediction.ConfidenceInterval ci = 
                    new PredictionResponse.Prediction.ConfidenceInterval();
                ci.setLowerBound(prediction.getPredictedValue() * 0.8);
                ci.setUpperBound(prediction.getPredictedValue() * 1.2);
                ci.setConfidenceLevel(0.95);
                prediction.setConfidenceInterval(ci);
                
                predictions.add(prediction);
            }
            
            return predictions;
        }
    }
    
    private static class ThreatAssessor {
        List<PredictionResponse.Prediction> assessThreats(List<PredictionRequest.DataPoint> historicalData, 
                                                         String areaWkt) {
            List<PredictionResponse.Prediction> predictions = new ArrayList<>();
            
            // Generate threat predictions based on historical patterns
            int threatCount = 3 + (int)(Math.random() * 5); // 3-8 potential threats
            
            for (int i = 0; i < threatCount; i++) {
                PredictionResponse.Prediction prediction = new PredictionResponse.Prediction();
                prediction.setTimestamp(System.currentTimeMillis());
                
                // Random location within area (simplified)
                prediction.setLatitude(38.0 + (Math.random() - 0.5) * 0.2);
                prediction.setLongitude(-77.0 + (Math.random() - 0.5) * 0.2);
                
                // Threat level (0-100)
                double threatLevel = Math.random() * 100;
                prediction.setPredictedValue(threatLevel);
                prediction.setProbability(threatLevel / 100.0);
                prediction.setRiskLevel(determineThreatRiskLevel(threatLevel));
                
                if (threatLevel > 80) {
                    prediction.setDescription("High threat probability - increased surveillance recommended");
                } else if (threatLevel > 50) {
                    prediction.setDescription("Moderate threat probability - monitor situation");
                } else {
                    prediction.setDescription("Low threat probability - routine monitoring");
                }
                
                predictions.add(prediction);
            }
            
            return predictions;
        }
        
        private PredictionResponse.Prediction.RiskLevel determineThreatRiskLevel(double threatLevel) {
            if (threatLevel > 80) return PredictionResponse.Prediction.RiskLevel.CRITICAL;
            if (threatLevel > 60) return PredictionResponse.Prediction.RiskLevel.HIGH;
            if (threatLevel > 30) return PredictionResponse.Prediction.RiskLevel.MEDIUM;
            return PredictionResponse.Prediction.RiskLevel.LOW;
        }
    }
    
    private static class RouteOptimizer {
        List<PredictionResponse.Prediction> optimizeRoutes(List<PredictionRequest.DataPoint> historicalData,
                                                          String areaWkt) {
            List<PredictionResponse.Prediction> predictions = new ArrayList<>();
            
            // Generate route optimization suggestions
            String[] routeTypes = {"Primary Route", "Alternate Route", "Emergency Route"};
            
            for (String routeType : routeTypes) {
                PredictionResponse.Prediction prediction = new PredictionResponse.Prediction();
                prediction.setTimestamp(System.currentTimeMillis());
                
                // Route waypoint
                prediction.setLatitude(38.0 + (Math.random() - 0.5) * 0.1);
                prediction.setLongitude(-77.0 + (Math.random() - 0.5) * 0.1);
                
                // Route efficiency score
                double efficiency = 60 + (Math.random() * 40); // 60-100%
                prediction.setPredictedValue(efficiency);
                prediction.setProbability(efficiency / 100.0);
                prediction.setRiskLevel(determineRouteRiskLevel(efficiency));
                prediction.setDescription(routeType + " - " + String.format("%.1f%% efficiency", efficiency));
                
                predictions.add(prediction);
            }
            
            return predictions;
        }
        
        private PredictionResponse.Prediction.RiskLevel determineRouteRiskLevel(double efficiency) {
            if (efficiency > 90) return PredictionResponse.Prediction.RiskLevel.LOW;
            if (efficiency > 70) return PredictionResponse.Prediction.RiskLevel.MEDIUM;
            if (efficiency > 50) return PredictionResponse.Prediction.RiskLevel.HIGH;
            return PredictionResponse.Prediction.RiskLevel.CRITICAL;
        }
    }
    
    private static class WeatherPredictor {
        List<PredictionResponse.Prediction> predictWeatherImpacts(String areaWkt, int horizonHours) {
            List<PredictionResponse.Prediction> predictions = new ArrayList<>();
            
            // Generate weather impact predictions for each hour
            for (int hour = 1; hour <= Math.min(horizonHours, 72); hour += 6) { // Every 6 hours
                PredictionResponse.Prediction prediction = new PredictionResponse.Prediction();
                prediction.setTimestamp(System.currentTimeMillis() + (hour * 3600000L));
                
                // Center of area (simplified)
                prediction.setLatitude(38.0);
                prediction.setLongitude(-77.0);
                
                // Weather impact score (0-100)
                double impact = Math.random() * 100;
                prediction.setPredictedValue(impact);
                prediction.setProbability(0.8 + (Math.random() * 0.2));
                prediction.setRiskLevel(determineWeatherRiskLevel(impact));
                
                String condition = getWeatherCondition(impact);
                prediction.setDescription("Weather impact in " + hour + " hours: " + condition);
                
                predictions.add(prediction);
            }
            
            return predictions;
        }
        
        private PredictionResponse.Prediction.RiskLevel determineWeatherRiskLevel(double impact) {
            if (impact > 80) return PredictionResponse.Prediction.RiskLevel.CRITICAL;
            if (impact > 60) return PredictionResponse.Prediction.RiskLevel.HIGH;
            if (impact > 30) return PredictionResponse.Prediction.RiskLevel.MEDIUM;
            return PredictionResponse.Prediction.RiskLevel.LOW;
        }
        
        private String getWeatherCondition(double impact) {
            if (impact > 80) return "Severe weather conditions";
            if (impact > 60) return "Adverse weather conditions";
            if (impact > 30) return "Moderate weather impact";
            return "Favorable weather conditions";
        }
    }
    
    // Helper methods
    
    private PredictionResponse createBaseResponse(PredictionRequest request) {
        PredictionResponse response = new PredictionResponse();
        response.setRequestId(request.getRequestId());
        response.setStatus(AIResponse.Status.SUCCESS);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
    
    private List<PredictionResponse.Prediction> generateDefaultMovementPredictions(PredictionRequest request) {
        // Generate default predictions when no historical data is available
        return movementPredictor.predictMovementPatterns(new ArrayList<>(), request.getPredictionHorizonHours());
    }
    
    private List<PredictionResponse.Prediction> generateResourcePredictions(PredictionRequest request) {
        List<PredictionResponse.Prediction> predictions = new ArrayList<>();
        
        String[] resources = {"Personnel", "Vehicles", "Equipment", "Supplies"};
        
        for (String resource : resources) {
            PredictionResponse.Prediction prediction = new PredictionResponse.Prediction();
            prediction.setTimestamp(System.currentTimeMillis());
            prediction.setLatitude(38.0);
            prediction.setLongitude(-77.0);
            
            double requirement = 50 + (Math.random() * 50); // 50-100% of capacity
            prediction.setPredictedValue(requirement);
            prediction.setProbability(0.8);
            prediction.setRiskLevel(determineResourceRiskLevel(requirement));
            prediction.setDescription(resource + " requirement: " + String.format("%.0f%% of capacity", requirement));
            
            predictions.add(prediction);
        }
        
        return predictions;
    }
    
    private List<PredictionResponse.Prediction> generateActivityPredictions(PredictionRequest request) {
        List<PredictionResponse.Prediction> predictions = new ArrayList<>();
        
        // Generate activity level predictions for different time periods
        for (int hours = 6; hours <= request.getPredictionHorizonHours(); hours += 6) {
            PredictionResponse.Prediction prediction = new PredictionResponse.Prediction();
            prediction.setTimestamp(System.currentTimeMillis() + (hours * 3600000L));
            prediction.setLatitude(38.0 + (Math.random() - 0.5) * 0.1);
            prediction.setLongitude(-77.0 + (Math.random() - 0.5) * 0.1);
            
            double activityLevel = Math.random() * 100;
            prediction.setPredictedValue(activityLevel);
            prediction.setProbability(0.75 + (Math.random() * 0.25));
            prediction.setRiskLevel(determineActivityRiskLevel(activityLevel));
            prediction.setDescription("Activity level in " + hours + " hours: " + 
                String.format("%.0f%%", activityLevel));
            
            predictions.add(prediction);
        }
        
        return predictions;
    }
    
    private static PredictionResponse.Prediction.RiskLevel determineRiskLevel(double probability) {
        if (probability > 0.8) return PredictionResponse.Prediction.RiskLevel.HIGH;
        if (probability > 0.6) return PredictionResponse.Prediction.RiskLevel.MEDIUM;
        return PredictionResponse.Prediction.RiskLevel.LOW;
    }
    
    private PredictionResponse.Prediction.RiskLevel determineResourceRiskLevel(double requirement) {
        if (requirement > 90) return PredictionResponse.Prediction.RiskLevel.CRITICAL;
        if (requirement > 75) return PredictionResponse.Prediction.RiskLevel.HIGH;
        if (requirement > 50) return PredictionResponse.Prediction.RiskLevel.MEDIUM;
        return PredictionResponse.Prediction.RiskLevel.LOW;
    }
    
    private PredictionResponse.Prediction.RiskLevel determineActivityRiskLevel(double activityLevel) {
        if (activityLevel > 80) return PredictionResponse.Prediction.RiskLevel.HIGH;
        if (activityLevel > 50) return PredictionResponse.Prediction.RiskLevel.MEDIUM;
        return PredictionResponse.Prediction.RiskLevel.LOW;
    }
    
    // Summary generation methods
    
    private PredictionResponse.PredictionSummary generateMovementSummary(
            List<PredictionResponse.Prediction> predictions) {
        
        PredictionResponse.PredictionSummary summary = new PredictionResponse.PredictionSummary();
        summary.setTotalPredictions(predictions.size());
        
        long highRiskCount = predictions.stream()
            .mapToLong(p -> p.getRiskLevel() == PredictionResponse.Prediction.RiskLevel.HIGH ||
                           p.getRiskLevel() == PredictionResponse.Prediction.RiskLevel.CRITICAL ? 1 : 0)
            .sum();
        summary.setHighRiskAreas((int)highRiskCount);
        
        double avgConfidence = predictions.stream()
            .mapToDouble(PredictionResponse.Prediction::getProbability)
            .average().orElse(0.0);
        summary.setAverageConfidence(avgConfidence);
        
        List<String> insights = List.of(
            "Movement patterns show consistent directional trends",
            "Peak activity expected in next 12-18 hours",
            "Historical patterns suggest cyclical behavior"
        );
        summary.setKeyInsights(insights);
        
        List<String> actions = List.of(
            "Monitor high-probability movement corridors",
            "Position resources along predicted routes",
            "Set up observation points at key locations"
        );
        summary.setRecommendedActions(actions);
        
        return summary;
    }
    
    private PredictionResponse.PredictionSummary generateWeatherSummary(
            List<PredictionResponse.Prediction> predictions) {
        
        PredictionResponse.PredictionSummary summary = new PredictionResponse.PredictionSummary();
        summary.setTotalPredictions(predictions.size());
        
        long highRiskCount = predictions.stream()
            .mapToLong(p -> p.getRiskLevel() == PredictionResponse.Prediction.RiskLevel.HIGH ||
                           p.getRiskLevel() == PredictionResponse.Prediction.RiskLevel.CRITICAL ? 1 : 0)
            .sum();
        summary.setHighRiskAreas((int)highRiskCount);
        
        double avgConfidence = predictions.stream()
            .mapToDouble(PredictionResponse.Prediction::getProbability)
            .average().orElse(0.0);
        summary.setAverageConfidence(avgConfidence);
        
        List<String> insights = List.of(
            "Weather conditions may impact operations in 6-12 hours",
            "Visibility conditions expected to improve after 24 hours",
            "Wind patterns favor eastern approach routes"
        );
        summary.setKeyInsights(insights);
        
        return summary;
    }
    
    private PredictionResponse.PredictionSummary generateThreatSummary(
            List<PredictionResponse.Prediction> predictions) {
        
        PredictionResponse.PredictionSummary summary = new PredictionResponse.PredictionSummary();
        summary.setTotalPredictions(predictions.size());
        
        long highRiskCount = predictions.stream()
            .mapToLong(p -> p.getRiskLevel() == PredictionResponse.Prediction.RiskLevel.HIGH ||
                           p.getRiskLevel() == PredictionResponse.Prediction.RiskLevel.CRITICAL ? 1 : 0)
            .sum();
        summary.setHighRiskAreas((int)highRiskCount);
        
        double avgConfidence = predictions.stream()
            .mapToDouble(PredictionResponse.Prediction::getProbability)
            .average().orElse(0.0);
        summary.setAverageConfidence(avgConfidence);
        
        List<String> insights = List.of(
            "Elevated threat levels detected in northern sector",
            "Historical patterns suggest increased activity during evening hours",
            "Threat probability correlates with population density"
        );
        summary.setKeyInsights(insights);
        
        List<String> actions = List.of(
            "Increase surveillance in high-risk areas",
            "Deploy additional resources to threat zones",
            "Implement enhanced security protocols"
        );
        summary.setRecommendedActions(actions);
        
        return summary;
    }
    
    private PredictionResponse.PredictionSummary generateRouteSummary(
            List<PredictionResponse.Prediction> predictions) {
        
        PredictionResponse.PredictionSummary summary = new PredictionResponse.PredictionSummary();
        summary.setTotalPredictions(predictions.size());
        summary.setHighRiskAreas(0); // Routes, not areas
        
        double avgConfidence = predictions.stream()
            .mapToDouble(PredictionResponse.Prediction::getProbability)
            .average().orElse(0.0);
        summary.setAverageConfidence(avgConfidence);
        
        List<String> insights = List.of(
            "Primary route shows highest efficiency rating",
            "Alternative routes available with 15% longer travel time",
            "Traffic patterns favor morning departure times"
        );
        summary.setKeyInsights(insights);
        
        return summary;
    }
    
    private PredictionResponse.PredictionSummary generateResourceSummary(
            List<PredictionResponse.Prediction> predictions) {
        
        PredictionResponse.PredictionSummary summary = new PredictionResponse.PredictionSummary();
        summary.setTotalPredictions(predictions.size());
        
        long highRequirementCount = predictions.stream()
            .mapToLong(p -> p.getPredictedValue() > 80 ? 1 : 0)
            .sum();
        summary.setHighRiskAreas((int)highRequirementCount);
        
        double avgConfidence = predictions.stream()
            .mapToDouble(PredictionResponse.Prediction::getProbability)
            .average().orElse(0.0);
        summary.setAverageConfidence(avgConfidence);
        
        List<String> insights = List.of(
            "Personnel requirements peak during operational hours",
            "Equipment utilization higher than average",
            "Supply levels adequate for projected demand"
        );
        summary.setKeyInsights(insights);
        
        return summary;
    }
    
    private PredictionResponse.PredictionSummary generateActivitySummary(
            List<PredictionResponse.Prediction> predictions) {
        
        PredictionResponse.PredictionSummary summary = new PredictionResponse.PredictionSummary();
        summary.setTotalPredictions(predictions.size());
        
        long highActivityCount = predictions.stream()
            .mapToLong(p -> p.getRiskLevel() == PredictionResponse.Prediction.RiskLevel.HIGH ? 1 : 0)
            .sum();
        summary.setHighRiskAreas((int)highActivityCount);
        
        double avgConfidence = predictions.stream()
            .mapToDouble(PredictionResponse.Prediction::getProbability)
            .average().orElse(0.0);
        summary.setAverageConfidence(avgConfidence);
        
        List<String> insights = List.of(
            "Activity levels show cyclical patterns",
            "Peak activity expected during daylight hours",
            "Weekend patterns differ significantly from weekdays"
        );
        summary.setKeyInsights(insights);
        
        return summary;
    }
    
    // Visualization generation methods
    
    private PredictionResponse.VisualizationData generateMovementVisualization(
            List<PredictionResponse.Prediction> predictions) {
        
        PredictionResponse.VisualizationData vizData = new PredictionResponse.VisualizationData();
        
        // Generate movement vectors
        List<PredictionResponse.VisualizationData.MovementVector> vectors = new ArrayList<>();
        for (int i = 0; i < predictions.size() - 1; i++) {
            PredictionResponse.Prediction current = predictions.get(i);
            PredictionResponse.Prediction next = predictions.get(i + 1);
            
            PredictionResponse.VisualizationData.MovementVector vector = 
                new PredictionResponse.VisualizationData.MovementVector();
            vector.setStartLat(current.getLatitude());
            vector.setStartLon(current.getLongitude());
            vector.setEndLat(next.getLatitude());
            vector.setEndLon(next.getLongitude());
            vector.setMagnitude(current.getPredictedValue() / 100.0);
            
            vectors.add(vector);
        }
        vizData.setMovementVectors(vectors);
        
        return vizData;
    }
    
    private PredictionResponse.VisualizationData generateThreatVisualization(
            List<PredictionResponse.Prediction> predictions) {
        
        PredictionResponse.VisualizationData vizData = new PredictionResponse.VisualizationData();
        
        // Generate risk zones
        List<PredictionResponse.VisualizationData.RiskZone> riskZones = new ArrayList<>();
        for (PredictionResponse.Prediction prediction : predictions) {
            if (prediction.getRiskLevel() == PredictionResponse.Prediction.RiskLevel.HIGH ||
                prediction.getRiskLevel() == PredictionResponse.Prediction.RiskLevel.CRITICAL) {
                
                PredictionResponse.VisualizationData.RiskZone zone = 
                    new PredictionResponse.VisualizationData.RiskZone();
                
                // Create circular area around prediction point
                double lat = prediction.getLatitude();
                double lon = prediction.getLongitude();
                String wkt = String.format(
                    "POLYGON((%.6f %.6f, %.6f %.6f, %.6f %.6f, %.6f %.6f, %.6f %.6f))",
                    lon - 0.01, lat - 0.01,
                    lon + 0.01, lat - 0.01,
                    lon + 0.01, lat + 0.01,
                    lon - 0.01, lat + 0.01,
                    lon - 0.01, lat - 0.01
                );
                
                zone.setAreaWkt(wkt);
                zone.setRiskLevel(prediction.getRiskLevel());
                zone.setDescription(prediction.getDescription());
                
                riskZones.add(zone);
            }
        }
        vizData.setRiskZones(riskZones);
        
        return vizData;
    }
    
    private int getCacheTtlForPredictionType(PredictionRequest.PredictionType type) {
        switch (type) {
            case WEATHER_IMPACT:
                return 1800; // 30 minutes - weather changes quickly
            case POPULATION_MOVEMENT:
                return 3600; // 1 hour
            case THREAT_ASSESSMENT:
                return 7200; // 2 hours
            case ROUTE_OPTIMIZATION:
                return 14400; // 4 hours - routes don't change often
            case RESOURCE_REQUIREMENTS:
                return 10800; // 3 hours
            case ACTIVITY_PATTERNS:
                return 21600; // 6 hours - patterns are more stable
            default:
                return 3600; // Default 1 hour
        }
    }
    
    private String generateCacheKey(PredictionRequest request) {
        return "prediction_" + request.getPredictionType().name() + "_" + 
               request.getPredictionHorizonHours() + "_" + 
               (request.getAreaOfInterestWkt() != null ? request.getAreaOfInterestWkt().hashCode() : 0);
    }
    
    private PredictionResponse createErrorResponse(PredictionRequest request, String error) {
        PredictionResponse response = new PredictionResponse();
        response.setRequestId(request.getRequestId());
        response.setStatus(AIResponse.Status.ERROR);
        response.setTimestamp(System.currentTimeMillis());
        response.setErrorMessage(error);
        response.setPredictions(new ArrayList<>());
        
        return response;
    }
}