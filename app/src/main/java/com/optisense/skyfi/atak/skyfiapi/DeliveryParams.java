package com.optisense.skyfi.atak.skyfiapi;

import java.util.ArrayList;

public class DeliveryParams {
    String label;
    String orderLabel;
    Object metadata;
    String webhookUrl;
    String windowStart;
    String windowEnd;
    String productType;
    String resolution;
    boolean priorityItem;
    int maxCloudCoverPercent;
    int maxOffNadirAngle;
    String requiredProvider;
    ArrayList<String> sarProductTypes;
    String sarPolarization;
    float sarGrazingAngleMin;
    float sarGrazingAngleMax;
    float sarAzimuthAngleMin;
    float sarAzimuthAngleMax;
    int sarNumberOfLooks;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getMaxCloudCoverPercent() {
        return maxCloudCoverPercent;
    }

    public void setMaxCloudCoverPercent(int maxCloudCoverPercent) {
        this.maxCloudCoverPercent = maxCloudCoverPercent;
    }

    public int getMaxOffNadirAngle() {
        return maxOffNadirAngle;
    }

    public void setMaxOffNadirAngle(int maxOffNadirAngle) {
        this.maxOffNadirAngle = maxOffNadirAngle;
    }

    public Object getMetadata() {
        return metadata;
    }

    public void setMetadata(Object metadata) {
        this.metadata = metadata;
    }

    public String getOrderLabel() {
        return orderLabel;
    }

    public void setOrderLabel(String orderLabel) {
        this.orderLabel = orderLabel;
    }

    public boolean isPriorityItem() {
        return priorityItem;
    }

    public void setPriorityItem(boolean priorityItem) {
        this.priorityItem = priorityItem;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getRequiredProvider() {
        return requiredProvider;
    }

    public void setRequiredProvider(String requiredProvider) {
        this.requiredProvider = requiredProvider;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public float getSarAzimuthAngleMax() {
        return sarAzimuthAngleMax;
    }

    public void setSarAzimuthAngleMax(float sarAzimuthAngleMax) {
        this.sarAzimuthAngleMax = sarAzimuthAngleMax;
    }

    public float getSarAzimuthAngleMin() {
        return sarAzimuthAngleMin;
    }

    public void setSarAzimuthAngleMin(float sarAzimuthAngleMin) {
        this.sarAzimuthAngleMin = sarAzimuthAngleMin;
    }

    public float getSarGrazingAngleMax() {
        return sarGrazingAngleMax;
    }

    public void setSarGrazingAngleMax(float sarGrazingAngleMax) {
        this.sarGrazingAngleMax = sarGrazingAngleMax;
    }

    public float getSarGrazingAngleMin() {
        return sarGrazingAngleMin;
    }

    public void setSarGrazingAngleMin(float sarGrazingAngleMin) {
        this.sarGrazingAngleMin = sarGrazingAngleMin;
    }

    public int getSarNumberOfLooks() {
        return sarNumberOfLooks;
    }

    public void setSarNumberOfLooks(int sarNumberOfLooks) {
        this.sarNumberOfLooks = sarNumberOfLooks;
    }

    public String getSarPolarization() {
        return sarPolarization;
    }

    public void setSarPolarization(String sarPolarization) {
        this.sarPolarization = sarPolarization;
    }

    public ArrayList<String> getSarProductTypes() {
        return sarProductTypes;
    }

    public void setSarProductTypes(ArrayList<String> sarProductTypes) {
        this.sarProductTypes = sarProductTypes;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public String getWindowEnd() {
        return windowEnd;
    }

    public void setWindowEnd(String windowEnd) {
        this.windowEnd = windowEnd;
    }

    public String getWindowStart() {
        return windowStart;
    }

    public void setWindowStart(String windowStart) {
        this.windowStart = windowStart;
    }
}
