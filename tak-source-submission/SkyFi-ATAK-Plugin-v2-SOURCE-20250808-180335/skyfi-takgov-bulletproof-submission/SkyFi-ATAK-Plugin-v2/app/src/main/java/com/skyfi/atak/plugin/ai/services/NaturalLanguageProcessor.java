package com.skyfi.atak.plugin.ai.services;

import android.content.Context;
import com.atakmap.coremap.log.Log;
import com.skyfi.atak.plugin.ai.models.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Natural Language Processor for handling voice/text queries with context awareness
 * Processes user queries and extracts intent, entities, and generates appropriate responses
 */
public class NaturalLanguageProcessor {
    private static final String TAG = "NaturalLanguageProcessor";
    
    private static NaturalLanguageProcessor instance;
    private final Context context;
    private final AICacheManager cacheManager;
    
    // Intent patterns for command recognition
    private final Map<String, Pattern> intentPatterns;
    private final Map<String, List<String>> intentKeywords;
    
    // Entity extraction patterns
    private final Map<String, Pattern> entityPatterns;
    
    private NaturalLanguageProcessor(Context context) {
        this.context = context;
        this.cacheManager = AICacheManager.getInstance(context);
        this.intentPatterns = initializeIntentPatterns();
        this.intentKeywords = initializeIntentKeywords();
        this.entityPatterns = initializeEntityPatterns();
    }
    
    public static synchronized NaturalLanguageProcessor getInstance(Context context) {
        if (instance == null) {
            instance = new NaturalLanguageProcessor(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Process natural language query and generate response
     */
    public NaturalLanguageResponse processQuery(NaturalLanguageRequest request) {
        Log.d(TAG, "Processing natural language query: " + request.getQueryText());
        
        try {
            // Check cache first
            String cacheKey = generateCacheKey(request);
            NaturalLanguageResponse cachedResponse = cacheManager.get(cacheKey, NaturalLanguageResponse.class);
            
            if (cachedResponse != null) {
                Log.d(TAG, "Returning cached natural language response");
                return cachedResponse;
            }
            
            // Create response
            NaturalLanguageResponse response = new NaturalLanguageResponse();
            response.setRequestId(request.getRequestId());
            response.setStatus(AIResponse.Status.SUCCESS);
            response.setTimestamp(System.currentTimeMillis());
            
            // Extract intent from query
            NaturalLanguageResponse.Intent intent = extractIntent(request.getQueryText());
            response.setIntent(intent);
            
            // Extract entities
            List<NaturalLanguageResponse.Entity> entities = extractEntities(request.getQueryText());
            response.setEntities(entities);
            
            // Generate response text
            String responseText = generateResponseText(request, intent, entities);
            response.setResponseText(responseText);
            
            // Generate suggested actions
            List<NaturalLanguageResponse.SuggestedAction> suggestedActions = 
                generateSuggestedActions(intent, entities, request.getGeospatialContext());
            response.setSuggestedActions(suggestedActions);
            
            // Generate follow-up questions
            List<String> followUpQuestions = generateFollowUpQuestions(intent, entities);
            response.setFollowUpQuestions(followUpQuestions);
            
            // Set confidence score
            response.setConfidenceScore(calculateOverallConfidence(intent, entities));
            
            // Cache the result
            cacheManager.put(cacheKey, response, 1800); // Cache for 30 minutes
            
            Log.d(TAG, "Natural language processing completed with intent: " + intent.getName());
            return response;
            
        } catch (Exception e) {
            Log.e(TAG, "Error processing natural language query", e);
            return createErrorResponse(request, e.getMessage());
        }
    }
    
    /**
     * Extract suggested commands from natural language input
     */
    public List<String> getSuggestedCommands(String partialQuery) {
        List<String> suggestions = new ArrayList<>();
        
        if (partialQuery == null || partialQuery.trim().isEmpty()) {
            return getDefaultSuggestions();
        }
        
        String lowercaseQuery = partialQuery.toLowerCase().trim();
        
        // Match against known command patterns
        for (Map.Entry<String, List<String>> entry : intentKeywords.entrySet()) {
            String intentName = entry.getKey();
            List<String> keywords = entry.getValue();
            
            for (String keyword : keywords) {
                if (keyword.startsWith(lowercaseQuery) || keyword.contains(lowercaseQuery)) {
                    suggestions.addAll(generateCommandSuggestions(intentName, keyword));
                }
            }
        }
        
        // Remove duplicates and limit results
        return suggestions.stream()
            .distinct()
            .limit(5)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Validate if query is supported
     */
    public boolean isQuerySupported(String queryText) {
        if (queryText == null || queryText.trim().isEmpty()) {
            return false;
        }
        
        NaturalLanguageResponse.Intent intent = extractIntent(queryText);
        return intent.getConfidence() > 0.3; // Minimum confidence threshold
    }
    
    private Map<String, Pattern> initializeIntentPatterns() {
        Map<String, Pattern> patterns = new HashMap<>();
        
        // Search and find patterns
        patterns.put("SEARCH", Pattern.compile(
            "(?i)\\b(show|find|search|locate|display|get)\\s+(me\\s+)?(all\\s+)?" +
            "(.*?)\\s*(in|within|near|around|at)", Pattern.CASE_INSENSITIVE));
        
        // Analysis patterns
        patterns.put("ANALYSIS", Pattern.compile(
            "(?i)\\b(analyze|examine|inspect|check|assess|evaluate)\\s+(.*?)" +
            "(area|region|zone|imagery|image)", Pattern.CASE_INSENSITIVE));
        
        // Command patterns
        patterns.put("COMMAND", Pattern.compile(
            "(?i)\\b(create|generate|make|build|start|begin|initiate)\\s+(.*?)" +
            "(order|task|mission|route|plan)", Pattern.CASE_INSENSITIVE));
        
        // Question patterns
        patterns.put("QUESTION", Pattern.compile(
            "(?i)\\b(what|where|when|how|why|which|who)\\s+(.*?)" +
            "(is|are|was|were|can|could|would|should)", Pattern.CASE_INSENSITIVE));
        
        // Report generation patterns
        patterns.put("REPORT_GENERATION", Pattern.compile(
            "(?i)\\b(report|summary|brief|overview)\\s+(on|about|of)\\s+(.*?)" +
            "(activity|movement|changes|status)", Pattern.CASE_INSENSITIVE));
        
        return patterns;
    }
    
    private Map<String, List<String>> initializeIntentKeywords() {
        Map<String, List<String>> keywords = new HashMap<>();
        
        keywords.put("SEARCH", List.of(
            "show me", "find", "search for", "locate", "display", "get",
            "list", "identify", "detect", "spot"));
        
        keywords.put("ANALYSIS", List.of(
            "analyze", "examine", "inspect", "check", "assess", "evaluate",
            "review", "study", "investigate"));
        
        keywords.put("COMMAND", List.of(
            "create", "generate", "make", "build", "start", "begin",
            "initiate", "launch", "execute", "run"));
        
        keywords.put("QUESTION", List.of(
            "what is", "where is", "when did", "how many", "why is",
            "which", "who", "can you", "could you", "would you"));
        
        keywords.put("REPORT_GENERATION", List.of(
            "report on", "summary of", "brief about", "overview of",
            "status report", "activity report", "movement report"));
        
        return keywords;
    }
    
    private Map<String, Pattern> initializeEntityPatterns() {
        Map<String, Pattern> patterns = new HashMap<>();
        
        // Distance patterns
        patterns.put("DISTANCE", Pattern.compile(
            "(?i)\\b(\\d+(?:\\.\\d+)?)\\s*(km|kilometers?|mi|miles?|m|meters?|ft|feet)\\b"));
        
        // Coordinate patterns
        patterns.put("COORDINATES", Pattern.compile(
            "(?i)\\b(-?\\d+(?:\\.\\d+)?)[°,\\s]+(-?\\d+(?:\\.\\d+)?)[°]?\\b"));
        
        // Time patterns
        patterns.put("TIME", Pattern.compile(
            "(?i)\\b(last|past|next|in)\\s+(\\d+)\\s*(minutes?|hours?|days?|weeks?)\\b"));
        
        // Object type patterns
        patterns.put("OBJECT_TYPE", Pattern.compile(
            "(?i)\\b(vehicles?|aircraft|buildings?|ships?|personnel|infrastructure|tanks?)\\b"));
        
        // Area patterns
        patterns.put("AREA", Pattern.compile(
            "(?i)\\b(area|region|zone|sector|grid|square)\\s*(\\w+|\\d+)\\b"));
        
        return patterns;
    }
    
    private NaturalLanguageResponse.Intent extractIntent(String queryText) {
        NaturalLanguageResponse.Intent intent = new NaturalLanguageResponse.Intent();
        
        String bestIntentName = "QUESTION"; // Default intent
        double bestConfidence = 0.0;
        Map<String, Object> parameters = new HashMap<>();
        
        // Check each intent pattern
        for (Map.Entry<String, Pattern> entry : intentPatterns.entrySet()) {
            String intentName = entry.getKey();
            Pattern pattern = entry.getValue();
            
            Matcher matcher = pattern.matcher(queryText);
            if (matcher.find()) {
                double confidence = calculateIntentConfidence(queryText, intentName);
                if (confidence > bestConfidence) {
                    bestIntentName = intentName;
                    bestConfidence = confidence;
                    
                    // Extract parameters from the match
                    parameters = extractIntentParameters(matcher, intentName);
                }
            }
        }
        
        // If no pattern matched well, try keyword matching
        if (bestConfidence < 0.5) {
            for (Map.Entry<String, List<String>> entry : intentKeywords.entrySet()) {
                String intentName = entry.getKey();
                List<String> keywords = entry.getValue();
                
                double confidence = calculateKeywordConfidence(queryText, keywords);
                if (confidence > bestConfidence) {
                    bestIntentName = intentName;
                    bestConfidence = confidence;
                }
            }
        }
        
        intent.setName(bestIntentName);
        intent.setConfidence(bestConfidence);
        intent.setParameters(parameters);
        
        return intent;
    }
    
    private List<NaturalLanguageResponse.Entity> extractEntities(String queryText) {
        List<NaturalLanguageResponse.Entity> entities = new ArrayList<>();
        
        for (Map.Entry<String, Pattern> entry : entityPatterns.entrySet()) {
            String entityType = entry.getKey();
            Pattern pattern = entry.getValue();
            
            Matcher matcher = pattern.matcher(queryText);
            while (matcher.find()) {
                NaturalLanguageResponse.Entity entity = new NaturalLanguageResponse.Entity();
                entity.setEntity(entityType);
                entity.setValue(matcher.group());
                entity.setStart(matcher.start());
                entity.setEnd(matcher.end());
                entity.setConfidence(0.8 + (Math.random() * 0.2)); // 0.8-1.0
                
                entities.add(entity);
            }
        }
        
        return entities;
    }
    
    private String generateResponseText(NaturalLanguageRequest request,
                                       NaturalLanguageResponse.Intent intent,
                                       List<NaturalLanguageResponse.Entity> entities) {
        
        String queryText = request.getQueryText();
        String intentName = intent.getName();
        
        switch (intentName) {
            case "SEARCH":
                return generateSearchResponse(queryText, entities, request.getGeospatialContext());
            case "ANALYSIS":
                return generateAnalysisResponse(queryText, entities);
            case "COMMAND":
                return generateCommandResponse(queryText, entities);
            case "QUESTION":
                return generateQuestionResponse(queryText, entities, request.getGeospatialContext());
            case "REPORT_GENERATION":
                return generateReportResponse(queryText, entities);
            default:
                return "I understand you want to " + queryText.toLowerCase() + ". " +
                       "Let me help you with that by suggesting some actions you can take.";
        }
    }
    
    private String generateSearchResponse(String query, List<NaturalLanguageResponse.Entity> entities,
                                        NaturalLanguageRequest.GeospatialContext context) {
        
        StringBuilder response = new StringBuilder();
        
        // Extract what the user is looking for
        String searchTarget = extractSearchTarget(query, entities);
        
        if (context != null && context.getCurrentLocation() != null) {
            response.append("I'll search for ").append(searchTarget)
                   .append(" near your current location (")
                   .append(String.format("%.4f, %.4f", 
                          context.getCurrentLocation().getLatitude(),
                          context.getCurrentLocation().getLongitude()))
                   .append("). ");
        } else {
            response.append("I'll search for ").append(searchTarget).append(" in the current map view. ");
        }
        
        // Add distance context if available
        String distance = extractDistance(entities);
        if (distance != null) {
            response.append("Searching within ").append(distance).append(". ");
        }
        
        response.append("This may take a moment to analyze the imagery and identify relevant objects.");
        
        return response.toString();
    }
    
    private String generateAnalysisResponse(String query, List<NaturalLanguageResponse.Entity> entities) {
        return "I'll analyze the selected area using AI-powered object detection and pattern recognition. " +
               "This will include identifying vehicles, buildings, infrastructure, and other objects of interest. " +
               "The analysis will provide confidence scores and detailed attributes for each detected object.";
    }
    
    private String generateCommandResponse(String query, List<NaturalLanguageResponse.Entity> entities) {
        return "I'll help you create the requested task. Based on your command, I can generate the necessary " +
               "orders, configure the parameters, and initiate the process. Please review the suggested actions " +
               "below to proceed.";
    }
    
    private String generateQuestionResponse(String query, List<NaturalLanguageResponse.Entity> entities,
                                          NaturalLanguageRequest.GeospatialContext context) {
        
        if (query.toLowerCase().contains("how many")) {
            return "I can help you count objects in the specified area. I'll analyze the imagery to identify " +
                   "and count the requested objects, providing you with accurate numbers and locations.";
        } else if (query.toLowerCase().contains("where")) {
            return "I can help you locate objects or areas of interest. I'll search the current map view and " +
                   "highlight relevant locations based on your criteria.";
        } else if (query.toLowerCase().contains("what")) {
            return "I can identify and classify objects in the imagery. I'll analyze the visual data and " +
                   "provide detailed information about what's visible in the selected area.";
        } else {
            return "I understand your question. Let me analyze the available data and provide you with " +
                   "the most relevant information based on the current geospatial context.";
        }
    }
    
    private String generateReportResponse(String query, List<NaturalLanguageResponse.Entity> entities) {
        return "I'll generate a comprehensive report based on the available intelligence data. " +
               "The report will include analysis results, object detections, movement patterns, " +
               "and key insights relevant to your area of interest.";
    }
    
    private List<NaturalLanguageResponse.SuggestedAction> generateSuggestedActions(
            NaturalLanguageResponse.Intent intent,
            List<NaturalLanguageResponse.Entity> entities,
            NaturalLanguageRequest.GeospatialContext context) {
        
        List<NaturalLanguageResponse.SuggestedAction> actions = new ArrayList<>();
        
        switch (intent.getName()) {
            case "SEARCH":
                actions.add(createAction("object_detection", "Detect Objects", 
                    "Run AI object detection on the specified area", 0.9));
                actions.add(createAction("create_aoi", "Create AOI", 
                    "Create an Area of Interest for the search area", 0.8));
                break;
                
            case "ANALYSIS":
                actions.add(createAction("full_analysis", "Full Area Analysis", 
                    "Perform comprehensive AI analysis of the area", 0.9));
                actions.add(createAction("threat_assessment", "Threat Assessment", 
                    "Analyze potential threats in the area", 0.8));
                break;
                
            case "COMMAND":
                actions.add(createAction("create_tasking_order", "Create Tasking Order", 
                    "Generate a new satellite tasking order", 0.9));
                actions.add(createAction("mission_planning", "Mission Planning", 
                    "Start mission planning workflow", 0.7));
                break;
                
            case "QUESTION":
                actions.add(createAction("search_data", "Search Available Data", 
                    "Search existing intelligence data", 0.8));
                actions.add(createAction("analyze_imagery", "Analyze Imagery", 
                    "Analyze available satellite imagery", 0.7));
                break;
                
            case "REPORT_GENERATION":
                actions.add(createAction("generate_report", "Generate Report", 
                    "Create intelligence report", 0.9));
                actions.add(createAction("export_data", "Export Data", 
                    "Export analysis results", 0.6));
                break;
        }
        
        return actions;
    }
    
    private List<String> generateFollowUpQuestions(NaturalLanguageResponse.Intent intent,
                                                  List<NaturalLanguageResponse.Entity> entities) {
        
        List<String> questions = new ArrayList<>();
        
        switch (intent.getName()) {
            case "SEARCH":
                questions.add("Would you like to specify a particular type of object to search for?");
                questions.add("Should I include objects with lower confidence scores?");
                questions.add("Do you want to search in a specific time range?");
                break;
                
            case "ANALYSIS":
                questions.add("Would you like to focus on any specific aspects of the analysis?");
                questions.add("Should I include predictive analysis for future patterns?");
                questions.add("Do you want to compare with historical data?");
                break;
                
            case "COMMAND":
                questions.add("What priority level should this task have?");
                questions.add("Are there any specific requirements or constraints?");
                questions.add("Would you like to schedule this for a specific time?");
                break;
                
            case "QUESTION":
                questions.add("Would you like more detailed information about any specific aspect?");
                questions.add("Should I search for related information as well?");
                break;
                
            case "REPORT_GENERATION":
                questions.add("What format would you prefer for the report?");
                questions.add("Should I include visual elements like maps and charts?");
                questions.add("Do you need this report shared with anyone else?");
                break;
        }
        
        return questions;
    }
    
    private double calculateIntentConfidence(String queryText, String intentName) {
        // Simple confidence calculation based on keyword matching
        List<String> keywords = intentKeywords.get(intentName);
        if (keywords == null) return 0.0;
        
        String lowerQuery = queryText.toLowerCase();
        int matches = 0;
        
        for (String keyword : keywords) {
            if (lowerQuery.contains(keyword.toLowerCase())) {
                matches++;
            }
        }
        
        return Math.min(1.0, (double) matches / keywords.size() + 0.3);
    }
    
    private double calculateKeywordConfidence(String queryText, List<String> keywords) {
        String lowerQuery = queryText.toLowerCase();
        int matches = 0;
        
        for (String keyword : keywords) {
            if (lowerQuery.contains(keyword.toLowerCase())) {
                matches++;
            }
        }
        
        return Math.min(0.8, (double) matches / keywords.size());
    }
    
    private Map<String, Object> extractIntentParameters(Matcher matcher, String intentName) {
        Map<String, Object> parameters = new HashMap<>();
        
        // Extract parameters based on the matched groups
        if (matcher.groupCount() > 0) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String group = matcher.group(i);
                if (group != null && !group.trim().isEmpty()) {
                    parameters.put("param" + i, group.trim());
                }
            }
        }
        
        return parameters;
    }
    
    private double calculateOverallConfidence(NaturalLanguageResponse.Intent intent,
                                            List<NaturalLanguageResponse.Entity> entities) {
        
        double intentConfidence = intent.getConfidence();
        double entityConfidence = entities.isEmpty() ? 0.5 : 
            entities.stream().mapToDouble(NaturalLanguageResponse.Entity::getConfidence).average().orElse(0.5);
        
        return (intentConfidence * 0.7) + (entityConfidence * 0.3);
    }
    
    private String extractSearchTarget(String query, List<NaturalLanguageResponse.Entity> entities) {
        // Look for object type entities
        for (NaturalLanguageResponse.Entity entity : entities) {
            if ("OBJECT_TYPE".equals(entity.getEntity())) {
                return entity.getValue();
            }
        }
        
        // Fallback: extract from query text
        String[] commonTargets = {"vehicles", "buildings", "aircraft", "ships", "personnel", "infrastructure"};
        String lowerQuery = query.toLowerCase();
        
        for (String target : commonTargets) {
            if (lowerQuery.contains(target)) {
                return target;
            }
        }
        
        return "objects of interest";
    }
    
    private String extractDistance(List<NaturalLanguageResponse.Entity> entities) {
        for (NaturalLanguageResponse.Entity entity : entities) {
            if ("DISTANCE".equals(entity.getEntity())) {
                return entity.getValue();
            }
        }
        return null;
    }
    
    private NaturalLanguageResponse.SuggestedAction createAction(String type, String description, 
                                                               String details, double confidence) {
        NaturalLanguageResponse.SuggestedAction action = new NaturalLanguageResponse.SuggestedAction();
        action.setActionType(type);
        action.setDescription(description);
        action.setConfidence(confidence);
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("description", details);
        action.setParameters(parameters);
        
        return action;
    }
    
    private List<String> getDefaultSuggestions() {
        return List.of(
            "Show me all vehicles in this area",
            "Analyze this region for threats",
            "Find buildings within 5 km",
            "Create a tasking order for this location",
            "Generate a report on recent activity"
        );
    }
    
    private List<String> generateCommandSuggestions(String intentName, String keyword) {
        List<String> suggestions = new ArrayList<>();
        
        switch (intentName) {
            case "SEARCH":
                suggestions.add(keyword + " vehicles in this area");
                suggestions.add(keyword + " buildings within 2 km");
                suggestions.add(keyword + " aircraft at this location");
                break;
            case "ANALYSIS":
                suggestions.add(keyword + " this area for threats");
                suggestions.add(keyword + " recent changes in the region");
                break;
            case "COMMAND":
                suggestions.add(keyword + " a tasking order");
                suggestions.add(keyword + " a mission plan");
                break;
        }
        
        return suggestions;
    }
    
    private String generateCacheKey(NaturalLanguageRequest request) {
        return "nl_" + request.getQueryText().hashCode() + "_" + request.getLanguage();
    }
    
    private NaturalLanguageResponse createErrorResponse(NaturalLanguageRequest request, String error) {
        NaturalLanguageResponse response = new NaturalLanguageResponse();
        response.setRequestId(request.getRequestId());
        response.setStatus(AIResponse.Status.ERROR);
        response.setTimestamp(System.currentTimeMillis());
        response.setErrorMessage(error);
        response.setResponseText("I'm sorry, I encountered an error processing your request: " + error);
        
        return response;
    }
}