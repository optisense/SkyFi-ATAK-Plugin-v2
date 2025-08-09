package com.skyfi.atak.plugin.ai.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

/**
 * Base class for all AI requests
 */
public abstract class AIRequest {
    @SerializedName("request_id")
    private String requestId;
    
    @SerializedName("timestamp")
    private long timestamp;
    
    @SerializedName("user_id")
    private String userId;
    
    @SerializedName("context")
    private Map<String, Object> context;
    
    @SerializedName("priority")
    private Priority priority = Priority.NORMAL;
    
    public enum Priority {
        LOW, NORMAL, HIGH, URGENT
    }
    
    public AIRequest() {
        this.timestamp = System.currentTimeMillis();
        this.requestId = generateRequestId();
    }
    
    private String generateRequestId() {
        return "ai_req_" + System.currentTimeMillis() + "_" + 
               (int)(Math.random() * 10000);
    }
    
    // Getters and setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public Map<String, Object> getContext() { return context; }
    public void setContext(Map<String, Object> context) { this.context = context; }
    
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
}