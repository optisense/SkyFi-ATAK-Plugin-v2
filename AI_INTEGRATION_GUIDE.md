# SkyFi ATAK Plugin AI Integration Guide

## Overview

This document provides comprehensive guidance for frontend developers to integrate with the AI backend services implemented in the SkyFi ATAK Plugin. The AI enhancement brings powerful geospatial intelligence capabilities including object detection, natural language processing, and predictive analytics.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        Frontend (UI Layer)                      │
├─────────────────────────────────────────────────────────────────┤
│                     AIAnalysisManager                          │
│                   (Central Orchestrator)                       │
├─────────────────┬──────────────────┬─────────────────────────────┤
│  GoogleAIService │  TAKMCPClient   │  Local AI Services         │
│                 │                  │                             │
│  • Remote Sensing│  • WebSocket    │  • ObjectDetectionService  │
│  • PDFM         │  • Certificate  │  • NaturalLanguageProcessor│
│  • WeatherNext  │    Auth         │  • PredictionEngine         │
│                 │                  │  • AICacheManager           │
└─────────────────┴──────────────────┴─────────────────────────────┘
```

## Core Components

### 1. AIAnalysisManager
**Path**: `com.skyfi.atak.plugin.ai.services.AIAnalysisManager`

The central orchestrator for all AI operations. Use this as your primary interface.

```java
// Initialize AI services
AIAnalysisManager aiManager = AIAnalysisManager.getInstance(context);
aiManager.initialize(takServerUrl, clientCertPath, clientKeyPath);

// Add listener for AI events
aiManager.addAnalysisListener(new AIAnalysisManager.AIAnalysisListener() {
    @Override
    public void onAnalysisStarted(String requestId, String analysisType) {
        // Update UI to show analysis started
        showProgressDialog("Starting " + analysisType + "...");
    }
    
    @Override
    public void onAnalysisProgress(String requestId, int progress, String status) {
        // Update progress bar
        updateProgress(progress, status);
    }
    
    @Override
    public void onAnalysisCompleted(String requestId, AIResponse response) {
        // Handle completed analysis
        displayResults(response);
        hideProgressDialog();
    }
    
    @Override
    public void onAnalysisError(String requestId, String error) {
        // Handle error
        showErrorDialog("Analysis failed: " + error);
    }
});
```

### 2. Object Detection Integration

```java
// Create object detection request
ObjectDetectionRequest request = new ObjectDetectionRequest();
request.setUserId("current_user_id");

// Set imagery data
ObjectDetectionRequest.ImageryData imagery = new ObjectDetectionRequest.ImageryData();
imagery.setImageUrl("https://example.com/satellite-image.jpg");
imagery.setResolutionMetersPerPixel(0.5);
imagery.setCaptureDate("2024-01-15T10:30:00Z");
request.setImageryData(imagery);

// Set detection parameters
request.setDetectionTypes(Arrays.asList(
    ObjectDetectionRequest.ObjectType.VEHICLES,
    ObjectDetectionRequest.ObjectType.BUILDINGS
));
request.setSensitivityThreshold(0.8);

// Set area of interest
ObjectDetectionRequest.AreaOfInterest aoi = new ObjectDetectionRequest.AreaOfInterest();
aoi.setWkt("POLYGON((-77.0 38.0, -76.9 38.0, -76.9 38.1, -77.0 38.1, -77.0 38.0))");
request.setAreaOfInterest(aoi);

// Execute detection
CompletableFuture<ObjectDetectionResponse> future = aiManager.performObjectDetection(
    request, 
    new AIAnalysisManager.AIAnalysisListener() {
        @Override
        public void onAnalysisCompleted(String requestId, AIResponse response) {
            ObjectDetectionResponse objResponse = (ObjectDetectionResponse) response;
            displayDetectedObjects(objResponse.getDetectedObjects());
        }
    }
);
```

### 3. Natural Language Processing

```java
// Create natural language request
NaturalLanguageRequest nlRequest = new NaturalLanguageRequest();
nlRequest.setQueryText("Show me all vehicles within 2 kilometers");
nlRequest.setQueryType(NaturalLanguageRequest.QueryType.SEARCH);
nlRequest.setLanguage("en");

// Add geospatial context
NaturalLanguageRequest.GeospatialContext context = new NaturalLanguageRequest.GeospatialContext();
NaturalLanguageRequest.GeospatialContext.Location currentLoc = 
    new NaturalLanguageRequest.GeospatialContext.Location();
currentLoc.setLatitude(38.0);
currentLoc.setLongitude(-77.0);
context.setCurrentLocation(currentLoc);

NaturalLanguageRequest.GeospatialContext.MapBounds bounds = 
    new NaturalLanguageRequest.GeospatialContext.MapBounds();
bounds.setNorth(38.1);
bounds.setSouth(37.9);
bounds.setEast(-76.9);
bounds.setWest(-77.1);
context.setMapBounds(bounds);

nlRequest.setGeospatialContext(context);

// Process query
CompletableFuture<NaturalLanguageResponse> future = aiManager.processNaturalLanguageQuery(
    nlRequest,
    new AIAnalysisManager.AIAnalysisListener() {
        @Override
        public void onAnalysisCompleted(String requestId, AIResponse response) {
            NaturalLanguageResponse nlResponse = (NaturalLanguageResponse) response;
            
            // Display response text
            showResponseText(nlResponse.getResponseText());
            
            // Show suggested actions
            if (nlResponse.getSuggestedActions() != null) {
                displaySuggestedActions(nlResponse.getSuggestedActions());
            }
            
            // Execute appropriate action based on intent
            String intentName = nlResponse.getIntent().getName();
            switch (intentName) {
                case "SEARCH":
                    // Trigger object detection or search
                    executeSearchAction(nlResponse);
                    break;
                case "ANALYSIS":
                    // Trigger area analysis
                    executeAnalysisAction(nlResponse);
                    break;
                // Handle other intents...
            }
        }
    }
);
```

### 4. Predictive Analytics

```java
// Create prediction request
PredictionRequest predRequest = new PredictionRequest();
predRequest.setPredictionType(PredictionRequest.PredictionType.POPULATION_MOVEMENT);
predRequest.setPredictionHorizonHours(24);
predRequest.setAreaOfInterestWkt("POLYGON((-77.0 38.0, -76.9 38.0, -76.9 38.1, -77.0 38.1, -77.0 38.0))");

// Add historical data if available
List<PredictionRequest.DataPoint> historicalData = new ArrayList<>();
// Populate with historical movement data...
predRequest.setHistoricalData(historicalData);

// Set prediction parameters
PredictionRequest.PredictionParameters params = new PredictionRequest.PredictionParameters();
params.setConfidenceLevel(0.95);
params.setResolutionMeters(100.0);
params.setIncludeUncertainty(true);
predRequest.setParameters(params);

// Generate predictions
CompletableFuture<PredictionResponse> future = aiManager.generatePredictions(
    predRequest,
    new AIAnalysisManager.AIAnalysisListener() {
        @Override
        public void onAnalysisCompleted(String requestId, AIResponse response) {
            PredictionResponse predResponse = (PredictionResponse) response;
            
            // Display predictions on map
            displayPredictionsOnMap(predResponse.getPredictions());
            
            // Show visualization data
            if (predResponse.getVisualizationData() != null) {
                displayVisualizationData(predResponse.getVisualizationData());
            }
            
            // Show summary and insights
            displayPredictionSummary(predResponse.getSummary());
        }
    }
);
```

## UI Integration Examples

### 1. AI Quick Actions Menu

```java
public class AIQuickActionsMenu extends RadialMenu {
    private final AIAnalysisManager aiManager;
    
    private static final String[] AI_ACTIONS = {
        "Analyze Area",
        "Detect Threats", 
        "Track Movement",
        "Predict Pattern",
        "Generate Report"
    };
    
    public AIQuickActionsMenu(Context context, AIAnalysisManager aiManager) {
        super(context);
        this.aiManager = aiManager;
        setupActions();
    }
    
    @Override
    public void onActionSelected(String action) {
        GeoPoint selectedPoint = getSelectedMapPoint();
        String areaWkt = createAreaAroundPoint(selectedPoint, 1000); // 1km radius
        
        switch (action) {
            case "Analyze Area":
                performAreaAnalysis(areaWkt);
                break;
            case "Detect Threats":
                performThreatDetection(areaWkt);
                break;
            case "Track Movement":
                performMovementTracking(areaWkt);
                break;
            case "Predict Pattern":
                performPatternPrediction(areaWkt);
                break;
            case "Generate Report":
                generateIntelligenceReport(areaWkt);
                break;
        }
    }
    
    private void performAreaAnalysis(String areaWkt) {
        ObjectDetectionRequest request = new ObjectDetectionRequest();
        // Configure request for general area analysis
        request.setDetectionTypes(Arrays.asList(ObjectDetectionRequest.ObjectType.ALL));
        
        ObjectDetectionRequest.AreaOfInterest aoi = new ObjectDetectionRequest.AreaOfInterest();
        aoi.setWkt(areaWkt);
        request.setAreaOfInterest(aoi);
        
        aiManager.performObjectDetection(request, new AreaAnalysisListener());
    }
}
```

### 2. AI Chat Interface

```java
public class AIChatFragment extends Fragment {
    private EditText chatInput;
    private RecyclerView chatHistory;
    private ChatAdapter chatAdapter;
    private AIAnalysisManager aiManager;
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        aiManager = AIAnalysisManager.getInstance(requireContext());
        setupChatInterface();
    }
    
    private void setupChatInterface() {
        chatInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                processChatMessage(chatInput.getText().toString());
                return true;
            }
            return false;
        });
    }
    
    private void processChatMessage(String message) {
        // Add user message to chat
        chatAdapter.addMessage(new ChatMessage(message, ChatMessage.Type.USER));
        
        // Clear input
        chatInput.setText("");
        
        // Create natural language request
        NaturalLanguageRequest request = new NaturalLanguageRequest();
        request.setQueryText(message);
        request.setQueryType(determineQueryType(message));
        request.setGeospatialContext(getCurrentGeospatialContext());
        
        // Show typing indicator
        chatAdapter.addMessage(new ChatMessage("AI is thinking...", ChatMessage.Type.SYSTEM));
        
        // Process with AI
        aiManager.processNaturalLanguageQuery(request, new ChatResponseListener());
    }
    
    private class ChatResponseListener implements AIAnalysisManager.AIAnalysisListener {
        @Override
        public void onAnalysisCompleted(String requestId, AIResponse response) {
            NaturalLanguageResponse nlResponse = (NaturalLanguageResponse) response;
            
            // Remove typing indicator
            chatAdapter.removeLastMessage();
            
            // Add AI response
            chatAdapter.addMessage(new ChatMessage(
                nlResponse.getResponseText(), 
                ChatMessage.Type.AI
            ));
            
            // Add suggested actions as buttons
            if (nlResponse.getSuggestedActions() != null) {
                chatAdapter.addSuggestedActions(nlResponse.getSuggestedActions());
            }
        }
        
        @Override
        public void onAnalysisError(String requestId, String error) {
            chatAdapter.removeLastMessage();
            chatAdapter.addMessage(new ChatMessage(
                "Sorry, I encountered an error: " + error, 
                ChatMessage.Type.ERROR
            ));
        }
    }
}
```

### 3. AI Overlay System

```java
public class AIOverlayManager {
    private final MapView mapView;
    private final AIAnalysisManager aiManager;
    private final Map<String, MapOverlay> activeOverlays;
    
    public enum OverlayType {
        OBJECT_DETECTION,
        MOVEMENT_PREDICTION,
        THREAT_HEATMAP,
        POPULATION_DENSITY,
        INFRASTRUCTURE_STATUS
    }
    
    public void toggleOverlay(OverlayType type, boolean enabled) {
        if (enabled) {
            showOverlay(type);
        } else {
            hideOverlay(type);
        }
    }
    
    private void showOverlay(OverlayType type) {
        switch (type) {
            case OBJECT_DETECTION:
                showObjectDetectionOverlay();
                break;
            case MOVEMENT_PREDICTION:
                showMovementPredictionOverlay();
                break;
            case THREAT_HEATMAP:
                showThreatHeatmapOverlay();
                break;
            // Handle other overlay types...
        }
    }
    
    private void showObjectDetectionOverlay() {
        // Get current map bounds
        String mapBoundsWkt = getCurrentMapBoundsAsWkt();
        
        // Create object detection request for current view
        ObjectDetectionRequest request = new ObjectDetectionRequest();
        request.setDetectionTypes(Arrays.asList(ObjectDetectionRequest.ObjectType.ALL));
        
        ObjectDetectionRequest.AreaOfInterest aoi = new ObjectDetectionRequest.AreaOfInterest();
        aoi.setWkt(mapBoundsWkt);
        request.setAreaOfInterest(aoi);
        
        // Request object detection
        aiManager.performObjectDetection(request, new AIAnalysisManager.AIAnalysisListener() {
            @Override
            public void onAnalysisCompleted(String requestId, AIResponse response) {
                ObjectDetectionResponse objResponse = (ObjectDetectionResponse) response;
                
                // Create overlay with detected objects
                ObjectDetectionOverlay overlay = new ObjectDetectionOverlay();
                overlay.setDetectedObjects(objResponse.getDetectedObjects());
                
                // Add to map
                mapView.getMapOverlayManager().addOverlay(overlay);
                activeOverlays.put("object_detection", overlay);
            }
        });
    }
}
```

## Configuration

### 1. AI Service Configuration

Update your preferences to include AI configuration:

```java
public class AIConfigurationActivity extends AppCompatActivity {
    private Preferences preferences;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = new Preferences();
        setupAIConfiguration();
    }
    
    private void setupAIConfiguration() {
        // TAK Server Configuration
        EditText takServerUrl = findViewById(R.id.tak_server_url);
        takServerUrl.setText(preferences.getTakServerUrl());
        
        // Google AI Configuration
        EditText googleProjectId = findViewById(R.id.google_project_id);
        googleProjectId.setText(preferences.getGoogleProjectId());
        
        // AI Settings
        Switch aiEnabled = findViewById(R.id.ai_enabled);
        aiEnabled.setChecked(preferences.isAIEnabled());
        
        Switch offlineAI = findViewById(R.id.offline_ai_enabled);
        offlineAI.setChecked(preferences.isOfflineAIEnabled());
        
        SeekBar confidenceThreshold = findViewById(R.id.confidence_threshold);
        confidenceThreshold.setProgress((int)(preferences.getAIConfidenceThreshold() * 100));
        
        SeekBar cacheSize = findViewById(R.id.cache_size);
        cacheSize.setProgress((int)preferences.getAICacheSizeMB());
    }
    
    private void saveConfiguration() {
        preferences.setTakServerUrl(takServerUrl.getText().toString());
        preferences.setGoogleProjectId(googleProjectId.getText().toString());
        preferences.setAIEnabled(aiEnabled.isChecked());
        preferences.setOfflineAIEnabled(offlineAI.isChecked());
        preferences.setAIConfidenceThreshold(confidenceThreshold.getProgress() / 100.0);
        preferences.setAICacheSizeMB(cacheSize.getProgress());
        
        // Restart AI services with new configuration
        AIAnalysisManager.getInstance(this).shutdown();
        AIAnalysisManager.getInstance(this).initialize(
            preferences.getTakServerUrl(),
            preferences.getClientCertPath(),
            preferences.getClientKeyPath()
        );
    }
}
```

### 2. Initialization in Plugin

Update your main plugin class to initialize AI services:

```java
public class SkyFiPlugin implements IPlugin {
    private AIAnalysisManager aiManager;
    
    @Override
    public void onStart() {
        // ... existing code ...
        
        // Initialize AI services
        initializeAIServices();
    }
    
    private void initializeAIServices() {
        Preferences prefs = new Preferences();
        
        if (prefs.isAIEnabled()) {
            aiManager = AIAnalysisManager.getInstance(pluginContext);
            
            // Initialize with configuration
            aiManager.initialize(
                prefs.getTakServerUrl(),
                prefs.getClientCertPath(),
                prefs.getClientKeyPath()
            );
            
            // Add global AI event listener
            aiManager.addAnalysisListener(new GlobalAIListener());
            
            Log.d(TAG, "AI services initialized successfully");
        } else {
            Log.d(TAG, "AI services disabled in preferences");
        }
    }
    
    @Override
    public void onStop() {
        // ... existing code ...
        
        // Shutdown AI services
        if (aiManager != null) {
            aiManager.shutdown();
        }
    }
}
```

## Error Handling and Best Practices

### 1. Robust Error Handling

```java
public class AIServiceHelper {
    private static final String TAG = "AIServiceHelper";
    
    public static void executeWithErrorHandling(
            Runnable aiOperation, 
            Context context,
            String operationName) {
        
        try {
            aiOperation.run();
        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied for AI operation: " + operationName, e);
            showErrorDialog(context, "Permission denied", 
                "AI services require additional permissions. Please check settings.");
        } catch (NetworkException e) {
            Log.e(TAG, "Network error in AI operation: " + operationName, e);
            showErrorDialog(context, "Network Error", 
                "Unable to connect to AI services. Check your internet connection.");
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in AI operation: " + operationName, e);
            showErrorDialog(context, "AI Service Error", 
                "An unexpected error occurred: " + e.getMessage());
        }
    }
    
    public static boolean isAIServiceAvailable(Context context) {
        try {
            AIAnalysisManager aiManager = AIAnalysisManager.getInstance(context);
            return aiManager != null && aiManager.isInitialized();
        } catch (Exception e) {
            Log.w(TAG, "AI service availability check failed", e);
            return false;
        }
    }
    
    private static void showErrorDialog(Context context, String title, String message) {
        new AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }
}
```

### 2. Performance Optimization

```java
public class AIPerformanceOptimizer {
    
    // Cache frequently used AI requests
    private static final LruCache<String, AIResponse> responseCache = 
        new LruCache<>(50); // Cache last 50 responses
    
    public static CompletableFuture<ObjectDetectionResponse> optimizedObjectDetection(
            ObjectDetectionRequest request, AIAnalysisManager aiManager) {
        
        // Check cache first
        String cacheKey = generateCacheKey(request);
        ObjectDetectionResponse cached = (ObjectDetectionResponse) responseCache.get(cacheKey);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        
        // Batch multiple requests if possible
        if (shouldBatchRequest(request)) {
            return batchObjectDetection(request, aiManager);
        }
        
        // Execute single request
        return aiManager.performObjectDetection(request, null)
            .thenApply(response -> {
                // Cache successful responses
                responseCache.put(cacheKey, response);
                return response;
            });
    }
    
    // Reduce AI request frequency for real-time operations
    public static class AIRequestThrottler {
        private long lastRequestTime = 0;
        private static final long MIN_REQUEST_INTERVAL = 2000; // 2 seconds
        
        public boolean shouldAllowRequest() {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastRequestTime > MIN_REQUEST_INTERVAL) {
                lastRequestTime = currentTime;
                return true;
            }
            return false;
        }
    }
}
```

## Testing

### 1. Unit Testing AI Integration

```java
@RunWith(MockitoJUnitRunner.class)
public class AIIntegrationTest {
    
    @Mock
    private AIAnalysisManager mockAIManager;
    
    @Test
    public void testObjectDetectionRequest() {
        // Arrange
        ObjectDetectionRequest request = new ObjectDetectionRequest();
        request.setDetectionTypes(Arrays.asList(ObjectDetectionRequest.ObjectType.VEHICLES));
        
        ObjectDetectionResponse expectedResponse = new ObjectDetectionResponse();
        expectedResponse.setStatus(AIResponse.Status.SUCCESS);
        
        CompletableFuture<ObjectDetectionResponse> future = 
            CompletableFuture.completedFuture(expectedResponse);
        
        when(mockAIManager.performObjectDetection(any(), any())).thenReturn(future);
        
        // Act
        CompletableFuture<ObjectDetectionResponse> result = 
            mockAIManager.performObjectDetection(request, null);
        
        // Assert
        ObjectDetectionResponse response = result.join();
        assertEquals(AIResponse.Status.SUCCESS, response.getStatus());
        verify(mockAIManager).performObjectDetection(any(), any());
    }
}
```

## Conclusion

This integration guide provides the foundation for implementing AI features in the SkyFi ATAK Plugin frontend. The backend services are designed to handle complex AI operations while providing a clean, easy-to-use interface for UI developers.

Key points to remember:
- Always use AIAnalysisManager as your primary interface
- Implement proper error handling and user feedback
- Cache responses when appropriate to improve performance
- Test AI integrations thoroughly with mock data
- Follow ATAK UI/UX patterns for consistency

For additional support or questions, refer to the individual service documentation or contact the backend development team.