package com.skyfi.atak.plugin.skyfiapi;

import java.util.Date;

public class Archive {
    private String archiveId;
    private String provider;
    private String constellation;
    private String productType;
    private float platformResolution;
    private String resolution;
    private Date captureTimestamp;
    private float cloudCoveragePercent;
    private float offNadirAngle;
    private String footprint;
    private float minSqKm;
    private float maxSqKm;
    private float priceForOneSquareKm;
    private float priceFullScene;
    private boolean openData;
    private float totalAreaSquareKm;
    private float deliveryTimeHours;
    private float gsd;
    private String titlesUrl;
    private float overlapRatio;
    private float overlapSqkm;

    public String getArchiveId() {
        return archiveId;
    }

    public void setArchiveId(String archiveId) {
        this.archiveId = archiveId;
    }

    public Date getCaptureTimestamp() {
        return captureTimestamp;
    }

    public void setCaptureTimestamp(Date captureTimestamp) {
        this.captureTimestamp = captureTimestamp;
    }

    public float getCloudCoveragePercent() {
        return cloudCoveragePercent;
    }

    public void setCloudCoveragePercent(float cloudCoveragePercent) {
        this.cloudCoveragePercent = cloudCoveragePercent;
    }

    public String getConstellation() {
        return constellation;
    }

    public void setConstellation(String constellation) {
        this.constellation = constellation;
    }

    public float getDeliveryTimeHours() {
        return deliveryTimeHours;
    }

    public void setDeliveryTimeHours(float deliveryTimeHours) {
        this.deliveryTimeHours = deliveryTimeHours;
    }

    public String getFootprint() {
        return footprint;
    }

    public void setFootprint(String footprint) {
        this.footprint = footprint;
    }

    public float getGsd() {
        return gsd;
    }

    public void setGsd(float gsd) {
        this.gsd = gsd;
    }

    public float getMaxSqKm() {
        return maxSqKm;
    }

    public void setMaxSqKm(float maxSqKm) {
        this.maxSqKm = maxSqKm;
    }

    public float getMinSqKm() {
        return minSqKm;
    }

    public void setMinSqKm(float minSqKm) {
        this.minSqKm = minSqKm;
    }

    public float getOffNadirAngle() {
        return offNadirAngle;
    }

    public void setOffNadirAngle(float offNadirAngle) {
        this.offNadirAngle = offNadirAngle;
    }

    public boolean isOpenData() {
        return openData;
    }

    public void setOpenData(boolean openData) {
        this.openData = openData;
    }

    public float getOverlapRatio() {
        return overlapRatio;
    }

    public void setOverlapRatio(float overlapRatio) {
        this.overlapRatio = overlapRatio;
    }

    public float getOverlapSqkm() {
        return overlapSqkm;
    }

    public void setOverlapSqkm(float overlapSqkm) {
        this.overlapSqkm = overlapSqkm;
    }

    public float getPlatformResolution() {
        return platformResolution;
    }

    public void setPlatformResolution(float platformResolution) {
        this.platformResolution = platformResolution;
    }

    public float getPriceForOneSquareKm() {
        return priceForOneSquareKm;
    }

    public void setPriceForOneSquareKm(float priceForOneSquareKm) {
        this.priceForOneSquareKm = priceForOneSquareKm;
    }

    public float getPriceFullScene() {
        return priceFullScene;
    }

    public void setPriceFullScene(float priceFullScene) {
        this.priceFullScene = priceFullScene;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getTitlesUrl() {
        return titlesUrl;
    }

    public void setTitlesUrl(String titlesUrl) {
        this.titlesUrl = titlesUrl;
    }

    public float getTotalAreaSquareKm() {
        return totalAreaSquareKm;
    }

    public void setTotalAreaSquareKm(float totalAreaSquareKm) {
        this.totalAreaSquareKm = totalAreaSquareKm;
    }
}
