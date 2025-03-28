package com.skyfi.atak.plugin.skyfiapi;

public class TaskingOrder {
    String aoi; // required
    boolean priorityItem = false;
    String productType; // required
    String resolution; // required
    String requiredProvider; // Ironically not required
    Float maxCloudCoveragePercent;
    Float maxOffNadirAngle;
    String windowStart; // required
    String windowEnd; // required
    String deliveryDriver;
    DeliveryParams deliveryParams;

    @Override
    public String toString() {
        return "TaskingOrder{" +
                "aoi='" + aoi + '\'' +
                ", priorityItem=" + priorityItem +
                ", productType='" + productType + '\'' +
                ", resolution='" + resolution + '\'' +
                ", requiredProvider='" + requiredProvider + '\'' +
                ", maxCloudCoveragePercent=" + maxCloudCoveragePercent +
                ", maxOffNadirAngle=" + maxOffNadirAngle +
                ", windowStart='" + windowStart + '\'' +
                ", windowEnd='" + windowEnd + '\'' +
                ", deliveryDriver='" + deliveryDriver + '\'' +
                ", deliveryParams=" + deliveryParams +
                '}';
    }

    public String getAoi() {
        return aoi;
    }

    public void setAoi(String aoi) {
        this.aoi = aoi;
    }

    public String getDeliveryDriver() {
        return deliveryDriver;
    }

    public void setDeliveryDriver(String deliveryDriver) {
        this.deliveryDriver = deliveryDriver;
    }

    public DeliveryParams getDeliveryParams() {
        return deliveryParams;
    }

    public void setDeliveryParams(DeliveryParams deliveryParams) {
        this.deliveryParams = deliveryParams;
    }

    public Float getMaxCloudCoveragePercent() {
        return maxCloudCoveragePercent;
    }

    public void setMaxCloudCoveragePercent(Float maxCloudCoveragePercent) {
        this.maxCloudCoveragePercent = maxCloudCoveragePercent;
    }

    public Float getMaxOffNadirAngle() {
        return maxOffNadirAngle;
    }

    public void setMaxOffNadirAngle(Float maxOffNadirAngle) {
        this.maxOffNadirAngle = maxOffNadirAngle;
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

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
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

    public String getRequiredProvider() {
        return requiredProvider;
    }

    public void setRequiredProvider(String requiredProvider) {
        this.requiredProvider = requiredProvider;
    }
}
