package com.skyfi.atak.plugin.ai.models;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

/**
 * AI Service Status response model
 */
public class AIServiceStatus {
    @SerializedName("service_name")
    private String serviceName;
    
    @SerializedName("status")
    private ServiceStatus status;
    
    @SerializedName("version")
    private String version;
    
    @SerializedName("uptime_seconds")
    private long uptimeSeconds;
    
    @SerializedName("active_requests")
    private int activeRequests;
    
    @SerializedName("total_requests_processed")
    private long totalRequestsProcessed;
    
    @SerializedName("average_response_time_ms")
    private double averageResponseTimeMs;
    
    @SerializedName("error_rate_percent")
    private double errorRatePercent;
    
    @SerializedName("capabilities")
    private Map<String, Boolean> capabilities;
    
    @SerializedName("last_updated")
    private long lastUpdated;
    
    public enum ServiceStatus {
        ONLINE, OFFLINE, DEGRADED, MAINTENANCE
    }
    
    public AIServiceStatus() {
        this.lastUpdated = System.currentTimeMillis();
    }
    
    // Getters and setters
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    
    public ServiceStatus getStatus() { return status; }
    public void setStatus(ServiceStatus status) { this.status = status; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    public long getUptimeSeconds() { return uptimeSeconds; }
    public void setUptimeSeconds(long uptimeSeconds) { this.uptimeSeconds = uptimeSeconds; }
    
    public int getActiveRequests() { return activeRequests; }
    public void setActiveRequests(int activeRequests) { this.activeRequests = activeRequests; }
    
    public long getTotalRequestsProcessed() { return totalRequestsProcessed; }
    public void setTotalRequestsProcessed(long totalRequestsProcessed) { this.totalRequestsProcessed = totalRequestsProcessed; }
    
    public double getAverageResponseTimeMs() { return averageResponseTimeMs; }
    public void setAverageResponseTimeMs(double averageResponseTimeMs) { this.averageResponseTimeMs = averageResponseTimeMs; }
    
    public double getErrorRatePercent() { return errorRatePercent; }
    public void setErrorRatePercent(double errorRatePercent) { this.errorRatePercent = errorRatePercent; }
    
    public Map<String, Boolean> getCapabilities() { return capabilities; }
    public void setCapabilities(Map<String, Boolean> capabilities) { this.capabilities = capabilities; }
    
    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }
}