package com.skyfi.atak.plugin.ai.services;

import android.content.Context;
import com.atakmap.coremap.log.Log;
import com.skyfi.atak.plugin.ai.models.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * AI Analysis Manager - Central orchestrator for all AI operations
 * Manages the flow between different AI services and handles request routing
 */
public class AIAnalysisManager {
    private static final String TAG = "AIAnalysisManager";
    
    private static AIAnalysisManager instance;
    private final Context context;
    
    // AI Service components
    private final GoogleAIService googleAIService;
    private final TAKMCPClient takMCPClient;
    private final AICacheManager cacheManager;
    private final ObjectDetectionService objectDetectionService;
    private final NaturalLanguageProcessor naturalLanguageProcessor;
    private final PredictionEngine predictionEngine;
    
    // Request tracking and management
    private final Map<String, ActiveRequest> activeRequests;
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);
    
    // Listeners for AI events
    private final List<AIAnalysisListener> listeners;
    
    public interface AIAnalysisListener {
        void onAnalysisStarted(String requestId, String analysisType);
        void onAnalysisProgress(String requestId, int progress, String status);
        void onAnalysisCompleted(String requestId, AIResponse response);
        void onAnalysisError(String requestId, String error);
    }
    
    private static class ActiveRequest {
        final String requestId;
        final String requestType;
        final long startTime;
        final AIAnalysisListener callback;
        volatile int progress;
        volatile String status;
        
        ActiveRequest(String requestId, String requestType, AIAnalysisListener callback) {
            this.requestId = requestId;
            this.requestType = requestType;
            this.startTime = System.currentTimeMillis();
            this.callback = callback;
            this.progress = 0;
            this.status = "Starting";
        }
    }
    
    private AIAnalysisManager(Context context) {
        this.context = context;
        this.activeRequests = new ConcurrentHashMap<>();
        this.listeners = new ArrayList<>();
        
        // Initialize AI service components
        this.googleAIService = GoogleAIService.getInstance(context);
        this.takMCPClient = TAKMCPClient.getInstance(context);
        this.cacheManager = AICacheManager.getInstance(context);
        this.objectDetectionService = ObjectDetectionService.getInstance(context);
        this.naturalLanguageProcessor = NaturalLanguageProcessor.getInstance(context);
        this.predictionEngine = PredictionEngine.getInstance(context);
    }
    
    public static synchronized AIAnalysisManager getInstance(Context context) {
        if (instance == null) {
            instance = new AIAnalysisManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Initialize the AI Analysis Manager with configuration
     */
    public void initialize(String takServerUrl, String clientCertPath, String clientKeyPath) {
        if (isInitialized.get()) {
            Log.d(TAG, "AI Analysis Manager already initialized");
            return;
        }
        
        Log.d(TAG, "Initializing AI Analysis Manager");
        
        // Initialize TAK MCP Client
        takMCPClient.initialize(takServerUrl, clientCertPath, clientKeyPath);
        takMCPClient.setConnectionStatusListener(new TAKMCPClient.ConnectionStatusListener() {
            @Override
            public void onConnected() {
                Log.d(TAG, "TAK MCP Client connected");
                notifyListeners(null, "TAK MCP Client connected");
            }
            
            @Override
            public void onDisconnected() {
                Log.d(TAG, "TAK MCP Client disconnected");
                notifyListeners(null, "TAK MCP Client disconnected");
            }
            
            @Override
            public void onReconnecting() {
                Log.d(TAG, "TAK MCP Client reconnecting");
                notifyListeners(null, "TAK MCP Client reconnecting");
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "TAK MCP Client error: " + error);
                notifyListeners(null, "TAK MCP Client error: " + error);
            }
        });
        
        // Connect to TAK Server
        takMCPClient.connect();
        
        isInitialized.set(true);
        Log.d(TAG, "AI Analysis Manager initialized successfully");
    }
    
    /**
     * Perform object detection analysis
     */
    public CompletableFuture<ObjectDetectionResponse> performObjectDetection(
            ObjectDetectionRequest request, AIAnalysisListener listener) {
        
        Log.d(TAG, "Starting object detection analysis: " + request.getRequestId());
        
        // Track the request
        ActiveRequest activeRequest = new ActiveRequest(
            request.getRequestId(), "ObjectDetection", listener);
        activeRequests.put(request.getRequestId(), activeRequest);
        
        // Notify listeners
        notifyAnalysisStarted(request.getRequestId(), "Object Detection");
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                updateProgress(request.getRequestId(), 10, "Validating request");
                
                // Validate request
                if (!validateObjectDetectionRequest(request)) {
                    throw new IllegalArgumentException("Invalid object detection request");
                }
                
                updateProgress(request.getRequestId(), 20, "Checking cache");
                
                // Check cache first
                String cacheKey = generateCacheKey("object_detection", request);
                ObjectDetectionResponse cachedResponse = cacheManager.get(cacheKey, ObjectDetectionResponse.class);
                
                if (cachedResponse != null) {
                    updateProgress(request.getRequestId(), 100, "Completed (cached)");
                    notifyAnalysisCompleted(request.getRequestId(), cachedResponse);
                    return cachedResponse;
                }
                
                updateProgress(request.getRequestId(), 30, "Processing imagery");
                
                // Choose processing method based on connectivity and preferences
                ObjectDetectionResponse response;
                
                if (shouldUseTAKMCP()) {
                    response = processViaTAKMCP(request);
                } else if (shouldUseGoogleAI()) {
                    response = processViaGoogleAI(request);
                } else {
                    response = processLocally(request);
                }
                
                updateProgress(request.getRequestId(), 90, "Finalizing results");
                
                // Post-process and validate response
                response = postProcessObjectDetectionResponse(response, request);
                
                // Cache the result
                cacheManager.put(cacheKey, response, 3600); // Cache for 1 hour
                
                updateProgress(request.getRequestId(), 100, "Completed");
                notifyAnalysisCompleted(request.getRequestId(), response);
                
                return response;
                
            } catch (Exception e) {
                Log.e(TAG, "Error in object detection analysis", e);
                notifyAnalysisError(request.getRequestId(), e.getMessage());
                throw new RuntimeException(e);
            } finally {
                activeRequests.remove(request.getRequestId());
            }
        });
    }
    
    /**
     * Process natural language query
     */
    public CompletableFuture<NaturalLanguageResponse> processNaturalLanguageQuery(
            NaturalLanguageRequest request, AIAnalysisListener listener) {
        
        Log.d(TAG, "Processing natural language query: " + request.getRequestId());
        
        ActiveRequest activeRequest = new ActiveRequest(
            request.getRequestId(), "NaturalLanguage", listener);
        activeRequests.put(request.getRequestId(), activeRequest);
        
        notifyAnalysisStarted(request.getRequestId(), "Natural Language Processing");
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                updateProgress(request.getRequestId(), 10, "Parsing query");
                
                // Validate and preprocess the query
                if (!validateNaturalLanguageRequest(request)) {
                    throw new IllegalArgumentException("Invalid natural language request");
                }
                
                updateProgress(request.getRequestId(), 30, "Analyzing intent");
                
                // Process using natural language processor
                NaturalLanguageResponse response = naturalLanguageProcessor.processQuery(request);
                
                updateProgress(request.getRequestId(), 70, "Generating response");
                
                // Enhance response with contextual information
                response = enhanceNaturalLanguageResponse(response, request);
                
                updateProgress(request.getRequestId(), 100, "Completed");
                notifyAnalysisCompleted(request.getRequestId(), response);
                
                return response;
                
            } catch (Exception e) {
                Log.e(TAG, "Error in natural language processing", e);
                notifyAnalysisError(request.getRequestId(), e.getMessage());
                throw new RuntimeException(e);
            } finally {
                activeRequests.remove(request.getRequestId());
            }
        });
    }
    
    /**
     * Generate predictions
     */
    public CompletableFuture<PredictionResponse> generatePredictions(
            PredictionRequest request, AIAnalysisListener listener) {
        
        Log.d(TAG, "Generating predictions: " + request.getRequestId());
        
        ActiveRequest activeRequest = new ActiveRequest(
            request.getRequestId(), "Prediction", listener);
        activeRequests.put(request.getRequestId(), activeRequest);
        
        notifyAnalysisStarted(request.getRequestId(), "Predictive Analysis");
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                updateProgress(request.getRequestId(), 10, "Validating data");
                
                if (!validatePredictionRequest(request)) {
                    throw new IllegalArgumentException("Invalid prediction request");
                }
                
                updateProgress(request.getRequestId(), 30, "Analyzing historical data");
                
                // Process using prediction engine
                PredictionResponse response = predictionEngine.generatePredictions(request);
                
                updateProgress(request.getRequestId(), 80, "Calculating confidence intervals");
                
                // Post-process predictions
                response = postProcessPredictionResponse(response, request);
                
                updateProgress(request.getRequestId(), 100, "Completed");
                notifyAnalysisCompleted(request.getRequestId(), response);
                
                return response;
                
            } catch (Exception e) {
                Log.e(TAG, "Error in prediction generation", e);
                notifyAnalysisError(request.getRequestId(), e.getMessage());
                throw new RuntimeException(e);
            } finally {
                activeRequests.remove(request.getRequestId());
            }
        });
    }
    
    /**
     * Execute AI action based on natural language command
     */
    public CompletableFuture<AIResponse> executeAIAction(String action, Map<String, Object> parameters) {
        Log.d(TAG, "Executing AI action: " + action);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                switch (action.toLowerCase()) {
                    case "analyze_area":
                        return handleAnalyzeAreaAction(parameters);
                    case "detect_threats":
                        return handleDetectThreatsAction(parameters);
                    case "track_movement":
                        return handleTrackMovementAction(parameters);
                    case "predict_pattern":
                        return handlePredictPatternAction(parameters);
                    case "generate_report":
                        return handleGenerateReportAction(parameters);
                    default:
                        throw new IllegalArgumentException("Unknown AI action: " + action);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error executing AI action: " + action, e);
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Add analysis listener
     */
    public void addAnalysisListener(AIAnalysisListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove analysis listener
     */
    public void removeAnalysisListener(AIAnalysisListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Get active requests
     */
    public Map<String, String> getActiveRequests() {
        Map<String, String> result = new java.util.HashMap<>();
        for (ActiveRequest request : activeRequests.values()) {
            result.put(request.requestId, request.requestType + " (" + request.progress + "%)");
        }
        return result;
    }
    
    /**
     * Cancel active request
     */
    public boolean cancelRequest(String requestId) {
        ActiveRequest request = activeRequests.remove(requestId);
        if (request != null) {
            Log.d(TAG, "Cancelled request: " + requestId);
            notifyAnalysisError(requestId, "Request cancelled by user");
            return true;
        }
        return false;
    }
    
    // Private helper methods
    
    private boolean shouldUseTAKMCP() {
        return takMCPClient.isConnected() && isInitialized.get();
    }
    
    private boolean shouldUseGoogleAI() {
        // Check if Google AI credentials are configured
        return true; // Simplified for now
    }
    
    private ObjectDetectionResponse processViaTAKMCP(ObjectDetectionRequest request) {
        // Process via TAK MCP Client
        Log.d(TAG, "Processing object detection via TAK MCP");
        // Implementation would use TAK MCP client
        return objectDetectionService.detectObjects(request);
    }
    
    private ObjectDetectionResponse processViaGoogleAI(ObjectDetectionRequest request) {
        // Process via Google AI Service
        Log.d(TAG, "Processing object detection via Google AI");
        try {
            return googleAIService.detectObjects(request, null).get();
        } catch (Exception e) {
            throw new RuntimeException("Google AI processing failed", e);
        }
    }
    
    private ObjectDetectionResponse processLocally(ObjectDetectionRequest request) {
        // Process using local/offline AI models
        Log.d(TAG, "Processing object detection locally");
        return objectDetectionService.detectObjects(request);
    }
    
    private boolean validateObjectDetectionRequest(ObjectDetectionRequest request) {
        if (request.getImageryData() == null) {
            Log.e(TAG, "Object detection request missing imagery data");
            return false;
        }
        
        if (request.getImageryData().getImageUrl() == null && 
            request.getImageryData().getImageBase64() == null) {
            Log.e(TAG, "Object detection request missing image data");
            return false;
        }
        
        return true;
    }
    
    private boolean validateNaturalLanguageRequest(NaturalLanguageRequest request) {
        if (request.getQueryText() == null || request.getQueryText().trim().isEmpty()) {
            Log.e(TAG, "Natural language request missing query text");
            return false;
        }
        
        return true;
    }
    
    private boolean validatePredictionRequest(PredictionRequest request) {
        if (request.getPredictionType() == null) {
            Log.e(TAG, "Prediction request missing prediction type");
            return false;
        }
        
        return true;
    }
    
    private ObjectDetectionResponse postProcessObjectDetectionResponse(
            ObjectDetectionResponse response, ObjectDetectionRequest request) {
        // Add any post-processing logic here
        // For example: filtering by confidence, grouping objects, etc.
        return response;
    }
    
    private NaturalLanguageResponse enhanceNaturalLanguageResponse(
            NaturalLanguageResponse response, NaturalLanguageRequest request) {
        // Add contextual enhancements to the response
        return response;
    }
    
    private PredictionResponse postProcessPredictionResponse(
            PredictionResponse response, PredictionRequest request) {
        // Add any post-processing for predictions
        return response;
    }
    
    private AIResponse handleAnalyzeAreaAction(Map<String, Object> parameters) {
        // Implementation for area analysis
        return new ObjectDetectionResponse(); // Placeholder
    }
    
    private AIResponse handleDetectThreatsAction(Map<String, Object> parameters) {
        // Implementation for threat detection
        return new ObjectDetectionResponse(); // Placeholder
    }
    
    private AIResponse handleTrackMovementAction(Map<String, Object> parameters) {
        // Implementation for movement tracking
        return new PredictionResponse(); // Placeholder
    }
    
    private AIResponse handlePredictPatternAction(Map<String, Object> parameters) {
        // Implementation for pattern prediction
        return new PredictionResponse(); // Placeholder
    }
    
    private AIResponse handleGenerateReportAction(Map<String, Object> parameters) {
        // Implementation for report generation
        return new NaturalLanguageResponse(); // Placeholder
    }
    
    private String generateCacheKey(String operation, AIRequest request) {
        return operation + "_" + request.getClass().getSimpleName() + "_" + 
               Integer.toHexString(request.toString().hashCode());
    }
    
    private void updateProgress(String requestId, int progress, String status) {
        ActiveRequest request = activeRequests.get(requestId);
        if (request != null) {
            request.progress = progress;
            request.status = status;
            
            // Notify specific request listener
            if (request.callback != null) {
                request.callback.onAnalysisProgress(requestId, progress, status);
            }
            
            // Notify all listeners
            for (AIAnalysisListener listener : listeners) {
                try {
                    listener.onAnalysisProgress(requestId, progress, status);
                } catch (Exception e) {
                    Log.e(TAG, "Error notifying listener", e);
                }
            }
        }
    }
    
    private void notifyAnalysisStarted(String requestId, String analysisType) {
        for (AIAnalysisListener listener : listeners) {
            try {
                listener.onAnalysisStarted(requestId, analysisType);
            } catch (Exception e) {
                Log.e(TAG, "Error notifying listener", e);
            }
        }
    }
    
    private void notifyAnalysisCompleted(String requestId, AIResponse response) {
        ActiveRequest request = activeRequests.get(requestId);
        if (request != null && request.callback != null) {
            request.callback.onAnalysisCompleted(requestId, response);
        }
        
        for (AIAnalysisListener listener : listeners) {
            try {
                listener.onAnalysisCompleted(requestId, response);
            } catch (Exception e) {
                Log.e(TAG, "Error notifying listener", e);
            }
        }
    }
    
    private void notifyAnalysisError(String requestId, String error) {
        ActiveRequest request = activeRequests.get(requestId);
        if (request != null && request.callback != null) {
            request.callback.onAnalysisError(requestId, error);
        }
        
        for (AIAnalysisListener listener : listeners) {
            try {
                listener.onAnalysisError(requestId, error);
            } catch (Exception e) {
                Log.e(TAG, "Error notifying listener", e);
            }
        }
    }
    
    private void notifyListeners(String requestId, String message) {
        Log.d(TAG, message);
        // Could extend this to notify listeners of general status updates
    }
    
    /**
     * Shutdown the AI Analysis Manager
     */
    public void shutdown() {
        Log.d(TAG, "Shutting down AI Analysis Manager");
        
        // Cancel all active requests
        for (String requestId : new ArrayList<>(activeRequests.keySet())) {
            cancelRequest(requestId);
        }
        
        // Shutdown components
        if (takMCPClient != null) {
            takMCPClient.disconnect();
        }
        
        if (googleAIService != null) {
            googleAIService.shutdown();
        }
        
        if (cacheManager != null) {
            cacheManager.shutdown();
        }
        
        listeners.clear();
        isInitialized.set(false);
        
        Log.d(TAG, "AI Analysis Manager shutdown completed");
    }
}