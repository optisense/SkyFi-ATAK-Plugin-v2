package com.skyfi.atak.plugin.skyfiapi;

public class TaskingOrder {
    String aio; // required
    boolean priorityItem;
    String productType; // required
    String resolution; // required
    int maxCloudCoveragePercent;
    int maxOffNadirAngle;
    String windowStart; // required
    String windowEnd; // required
    String deliveryDriver;
    DeliveryParams deliveryParams;

    public String getAio() {
        return aio;
    }

    public void setAio(String aio) {
        this.aio = aio;
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

    public int getMaxCloudCoveragePercent() {
        return maxCloudCoveragePercent;
    }

    public void setMaxCloudCoveragePercent(int maxCloudCoveragePercent) {
        this.maxCloudCoveragePercent = maxCloudCoveragePercent;
    }

    public int getMaxOffNadirAngle() {
        return maxOffNadirAngle;
    }

    public void setMaxOffNadirAngle(int maxOffNadirAngle) {
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
}
