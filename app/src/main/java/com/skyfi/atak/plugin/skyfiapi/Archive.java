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
    private int offNadirAngle;
    private String footprint;
    private int minSqKm;
    private int maxSqKm;
    private int priceForOneSquareKm;
    private int priceFullScene;
    private boolean openData;
    private float totalAreaSquareKm;
    private int deliveryTimeHours;
    private float gsd;
    private String titlesUrl;
    private int overlapRatio;
    private int overlapSqkm;

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

    public int getDeliveryTimeHours() {
        return deliveryTimeHours;
    }

    public void setDeliveryTimeHours(int deliveryTimeHours) {
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

    public int getMaxSqKm() {
        return maxSqKm;
    }

    public void setMaxSqKm(int maxSqKm) {
        this.maxSqKm = maxSqKm;
    }

    public int getMinSqKm() {
        return minSqKm;
    }

    public void setMinSqKm(int minSqKm) {
        this.minSqKm = minSqKm;
    }

    public int getOffNadirAngle() {
        return offNadirAngle;
    }

    public void setOffNadirAngle(int offNadirAngle) {
        this.offNadirAngle = offNadirAngle;
    }

    public boolean isOpenData() {
        return openData;
    }

    public void setOpenData(boolean openData) {
        this.openData = openData;
    }

    public int getOverlapRatio() {
        return overlapRatio;
    }

    public void setOverlapRatio(int overlapRatio) {
        this.overlapRatio = overlapRatio;
    }

    public int getOverlapSqkm() {
        return overlapSqkm;
    }

    public void setOverlapSqkm(int overlapSqkm) {
        this.overlapSqkm = overlapSqkm;
    }

    public float getPlatformResolution() {
        return platformResolution;
    }

    public void setPlatformResolution(float platformResolution) {
        this.platformResolution = platformResolution;
    }

    public int getPriceForOneSquareKm() {
        return priceForOneSquareKm;
    }

    public void setPriceForOneSquareKm(int priceForOneSquareKm) {
        this.priceForOneSquareKm = priceForOneSquareKm;
    }

    public int getPriceFullScene() {
        return priceFullScene;
    }

    public void setPriceFullScene(int priceFullScene) {
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
