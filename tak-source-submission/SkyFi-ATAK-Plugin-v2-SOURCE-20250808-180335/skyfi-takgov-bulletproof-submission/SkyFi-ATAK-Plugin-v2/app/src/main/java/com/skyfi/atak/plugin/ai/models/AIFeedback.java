package com.skyfi.atak.plugin.ai.models;

import com.google.gson.annotations.SerializedName;

/**
 * AI Feedback model for user feedback on AI responses
 */
public class AIFeedback {
    @SerializedName("feedback_id")
    private String feedbackId;
    
    @SerializedName("request_id")
    private String requestId;
    
    @SerializedName("response_id")
    private String responseId;
    
    @SerializedName("user_id")
    private String userId;
    
    @SerializedName("rating")
    private Rating rating;
    
    @SerializedName("feedback_type")
    private FeedbackType feedbackType;
    
    @SerializedName("comments")
    private String comments;
    
    @SerializedName("accuracy_rating")
    private int accuracyRating; // 1-5 scale
    
    @SerializedName("usefulness_rating")
    private int usefulnessRating; // 1-5 scale
    
    @SerializedName("timestamp")
    private long timestamp;
    
    public enum Rating {
        VERY_POOR, POOR, FAIR, GOOD, EXCELLENT
    }
    
    public enum FeedbackType {
        ACCURACY, USEFULNESS, PERFORMANCE, BUG_REPORT, FEATURE_REQUEST, GENERAL
    }
    
    public AIFeedback() {
        this.feedbackId = generateFeedbackId();
        this.timestamp = System.currentTimeMillis();
    }
    
    private String generateFeedbackId() {
        return "feedback_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }
    
    // Getters and setters
    public String getFeedbackId() { return feedbackId; }
    public void setFeedbackId(String feedbackId) { this.feedbackId = feedbackId; }
    
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    
    public String getResponseId() { return responseId; }
    public void setResponseId(String responseId) { this.responseId = responseId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public Rating getRating() { return rating; }
    public void setRating(Rating rating) { this.rating = rating; }
    
    public FeedbackType getFeedbackType() { return feedbackType; }
    public void setFeedbackType(FeedbackType feedbackType) { this.feedbackType = feedbackType; }
    
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    
    public int getAccuracyRating() { return accuracyRating; }
    public void setAccuracyRating(int accuracyRating) { this.accuracyRating = accuracyRating; }
    
    public int getUsefulnessRating() { return usefulnessRating; }
    public void setUsefulnessRating(int usefulnessRating) { this.usefulnessRating = usefulnessRating; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}