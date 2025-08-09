package com.skyfi.atak.plugin.ai.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response for natural language processing and queries
 */
public class NaturalLanguageResponse extends AIResponse {
    @SerializedName("response_text")
    private String responseText;
    
    @SerializedName("intent")
    private Intent intent;
    
    @SerializedName("entities")
    private List<Entity> entities;
    
    @SerializedName("suggested_actions")
    private List<SuggestedAction> suggestedActions;
    
    @SerializedName("follow_up_questions")
    private List<String> followUpQuestions;
    
    public static class Intent {
        @SerializedName("name")
        private String name;
        
        @SerializedName("confidence")
        private double confidence;
        
        @SerializedName("parameters")
        private java.util.Map<String, Object> parameters;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        
        public java.util.Map<String, Object> getParameters() { return parameters; }
        public void setParameters(java.util.Map<String, Object> parameters) { this.parameters = parameters; }
    }
    
    public static class Entity {
        @SerializedName("entity")
        private String entity;
        
        @SerializedName("value")
        private String value;
        
        @SerializedName("confidence")
        private double confidence;
        
        @SerializedName("start")
        private int start;
        
        @SerializedName("end")
        private int end;
        
        // Getters and setters
        public String getEntity() { return entity; }
        public void setEntity(String entity) { this.entity = entity; }
        
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        
        public int getStart() { return start; }
        public void setStart(int start) { this.start = start; }
        
        public int getEnd() { return end; }
        public void setEnd(int end) { this.end = end; }
    }
    
    public static class SuggestedAction {
        @SerializedName("action_type")
        private String actionType;
        
        @SerializedName("description")
        private String description;
        
        @SerializedName("parameters")
        private java.util.Map<String, Object> parameters;
        
        @SerializedName("confidence")
        private double confidence;
        
        // Getters and setters
        public String getActionType() { return actionType; }
        public void setActionType(String actionType) { this.actionType = actionType; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public java.util.Map<String, Object> getParameters() { return parameters; }
        public void setParameters(java.util.Map<String, Object> parameters) { this.parameters = parameters; }
        
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
    }
    
    // Getters and setters
    public String getResponseText() { return responseText; }
    public void setResponseText(String responseText) { this.responseText = responseText; }
    
    public Intent getIntent() { return intent; }
    public void setIntent(Intent intent) { this.intent = intent; }
    
    public List<Entity> getEntities() { return entities; }
    public void setEntities(List<Entity> entities) { this.entities = entities; }
    
    public List<SuggestedAction> getSuggestedActions() { return suggestedActions; }
    public void setSuggestedActions(List<SuggestedAction> suggestedActions) { this.suggestedActions = suggestedActions; }
    
    public List<String> getFollowUpQuestions() { return followUpQuestions; }
    public void setFollowUpQuestions(List<String> followUpQuestions) { this.followUpQuestions = followUpQuestions; }
}