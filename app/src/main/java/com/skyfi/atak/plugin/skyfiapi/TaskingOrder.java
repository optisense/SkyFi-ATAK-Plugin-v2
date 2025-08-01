package com.skyfi.atak.plugin.skyfiapi;

public class TaskingOrder {
    String aoi; // required
    boolean priorityItem = false;
    boolean assuredTasking = false; // NEW: Support for assured tasking
    String productType; // required
    String resolution; // required
    String requiredProvider; // Ironically not required
    Float maxCloudCoveragePercent;
    Float maxOffNadirAngle;
    String windowStart; // required
    String windowEnd; // required
    String deliveryDriver;
    DeliveryParams deliveryParams;
    
    // Advanced tasking parameters
    String[] selectedPasses; // Pass selection for specific satellite passes
    String frequency = "once"; // Frequency: once, daily, weekly
    String[] polarizations; // SAR polarizations: HH, VV, HV, VH
    boolean analyticsObjectDetection = false; // Object detection analytics
    boolean analyticsShipIdentification = false; // Ship identification analytics
    String sensorType = "ASAP"; // Sensor type: ASAP, EO, SAR, ADS-B
    int priorityLevel = 50; // Priority level 0-100
    int stereoImages = 1; // Number of stereo images: 1, 2, 4

    @Override
    public String toString() {
        return "TaskingOrder{" +
                "aoi='" + aoi + '\'' +
                ", priorityItem=" + priorityItem +
                ", assuredTasking=" + assuredTasking +
                ", productType='" + productType + '\'' +
                ", resolution='" + resolution + '\'' +
                ", requiredProvider='" + requiredProvider + '\'' +
                ", maxCloudCoveragePercent=" + maxCloudCoveragePercent +
                ", maxOffNadirAngle=" + maxOffNadirAngle +
                ", windowStart='" + windowStart + '\'' +
                ", windowEnd='" + windowEnd + '\'' +
                ", deliveryDriver='" + deliveryDriver + '\'' +
                ", deliveryParams=" + deliveryParams +
                ", selectedPasses=" + java.util.Arrays.toString(selectedPasses) +
                ", frequency='" + frequency + '\'' +
                ", polarizations=" + java.util.Arrays.toString(polarizations) +
                ", analyticsObjectDetection=" + analyticsObjectDetection +
                ", analyticsShipIdentification=" + analyticsShipIdentification +
                ", sensorType='" + sensorType + '\'' +
                ", priorityLevel=" + priorityLevel +
                ", stereoImages=" + stereoImages +
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
    
    public boolean isAssuredTasking() {
        return assuredTasking;
    }
    
    public void setAssuredTasking(boolean assuredTasking) {
        this.assuredTasking = assuredTasking;
    }
    
    public String[] getSelectedPasses() {
        return selectedPasses;
    }
    
    public void setSelectedPasses(String[] selectedPasses) {
        this.selectedPasses = selectedPasses;
    }
    
    public String getFrequency() {
        return frequency;
    }
    
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }
    
    public String[] getPolarizations() {
        return polarizations;
    }
    
    public void setPolarizations(String[] polarizations) {
        this.polarizations = polarizations;
    }
    
    public boolean isAnalyticsObjectDetection() {
        return analyticsObjectDetection;
    }
    
    public void setAnalyticsObjectDetection(boolean analyticsObjectDetection) {
        this.analyticsObjectDetection = analyticsObjectDetection;
    }
    
    public boolean isAnalyticsShipIdentification() {
        return analyticsShipIdentification;
    }
    
    public void setAnalyticsShipIdentification(boolean analyticsShipIdentification) {
        this.analyticsShipIdentification = analyticsShipIdentification;
    }
    
    public String getSensorType() {
        return sensorType;
    }
    
    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }
    
    public int getPriorityLevel() {
        return priorityLevel;
    }
    
    public void setPriorityLevel(int priorityLevel) {
        this.priorityLevel = priorityLevel;
    }
    
    public int getStereoImages() {
        return stereoImages;
    }
    
    public void setStereoImages(int stereoImages) {
        this.stereoImages = stereoImages;
    }
}
