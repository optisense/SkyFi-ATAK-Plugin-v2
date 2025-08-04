package com.skyfi.atak.plugin.ai.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Base class for all AI responses
 */
public abstract class AIResponse {
    @SerializedName("request_id")
    private String requestId;
    
    @SerializedName("response_id")
    private String responseId;
    
    @SerializedName("timestamp")
    private long timestamp;
    
    @SerializedName("status")
    private Status status;
    
    @SerializedName("confidence_score")
    private double confidenceScore;
    
    @SerializedName("processing_time_ms")
    private long processingTimeMs;
    
    @SerializedName("error_message")
    private String errorMessage;
    
    @SerializedName("metadata")
    private ResponseMetadata metadata;
    
    public enum Status {
        SUCCESS, PROCESSING, ERROR, TIMEOUT, CANCELLED
    }
    
    public AIResponse() {
        this.timestamp = System.currentTimeMillis();
        this.responseId = generateResponseId();
        this.status = Status.PROCESSING;
    }
    
    private String generateResponseId() {
        return "ai_resp_" + System.currentTimeMillis() + "_" + 
               (int)(Math.random() * 10000);
    }
    
    // Getters and setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    
    public String getResponseId() { return responseId; }
    public void setResponseId(String responseId) { this.responseId = responseId; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    
    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
    
    public long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public ResponseMetadata getMetadata() { return metadata; }
    public void setMetadata(ResponseMetadata metadata) { this.metadata = metadata; }
    
    public static class ResponseMetadata {
        @SerializedName("model_version")
        private String modelVersion;
        
        @SerializedName("service_name")
        private String serviceName;
        
        @SerializedName("cache_hit")
        private boolean cacheHit;
        
        // Getters and setters
        public String getModelVersion() { return modelVersion; }
        public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
        
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        
        public boolean isCacheHit() { return cacheHit; }
        public void setCacheHit(boolean cacheHit) { this.cacheHit = cacheHit; }
    }
}