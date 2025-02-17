package com.skyfi.atak.plugin.skyfiapi;

import java.util.Arrays;
import java.util.Date;

public class Order {
    private String id;
    private String orderType;
    private int orderCost;
    private String ownerId;
    private String status;
    private float aoiSqkm;
    private String tilesUrl;
    private String downloadImageUrl;
    private String downloadPayloadUrl;
    private Date createdAt;
    private String aoi;
    private String deliveryDriver;
    private String webhookUrl;
    private Date windowStart;
    private Date windowEnd;
    private String productType;
    private String resolution;
    private boolean priorityItem;
    private int maxCloudCoveragePercent;
    private int maxOffNadirAngle;
    private String requiredProvider;
    private String[] sarProductTypes;
    private String sarPolarisation;
    private int sarGrazingAngleMin;
    private int sarGrazingAngleMax;
    private int sarAzimuthAngleMin;
    private int sarAzimuthAngleMax;
    private int sarNumberOfLooks;
    private Archive archive;

    @Override
    public String toString() {
        return "Order{" +
                "aio='" + aoi + '\'' +
                ", id='" + id + '\'' +
                ", orderType='" + orderType + '\'' +
                ", orderCost=" + orderCost +
                ", ownerId='" + ownerId + '\'' +
                ", status='" + status + '\'' +
                ", aoiSqkm=" + aoiSqkm +
                ", tilesUrl='" + tilesUrl + '\'' +
                ", downloadImageUrl='" + downloadImageUrl + '\'' +
                ", downloadPayloadUrl='" + downloadPayloadUrl + '\'' +
                ", createdAt=" + createdAt +
                ", deliveryDriver='" + deliveryDriver + '\'' +
                ", webhookUrl='" + webhookUrl + '\'' +
                ", windowStart=" + windowStart +
                ", windowEnd=" + windowEnd +
                ", productType='" + productType + '\'' +
                ", resolution='" + resolution + '\'' +
                ", priorityItem=" + priorityItem +
                ", maxCloudCoveragePercent=" + maxCloudCoveragePercent +
                ", maxOffNadirAngle=" + maxOffNadirAngle +
                ", requiredProvider='" + requiredProvider + '\'' +
                ", sarProductTypes=" + Arrays.toString(sarProductTypes) +
                ", sarPolarisation='" + sarPolarisation + '\'' +
                ", sarGrazingAngleMin=" + sarGrazingAngleMin +
                ", sarGrazingAngleMax=" + sarGrazingAngleMax +
                ", sarAzimuthAngleMin=" + sarAzimuthAngleMin +
                ", sarAzimuthAngleMax=" + sarAzimuthAngleMax +
                ", sarNumberOfLooks=" + sarNumberOfLooks +
                '}';
    }

    public String getAoi() {
        return aoi;
    }

    public void setAoi(String aoi) {
        this.aoi = aoi;
    }

    public float getAoiSqkm() {
        return aoiSqkm;
    }

    public void setAoiSqkm(float aoiSqkm) {
        this.aoiSqkm = aoiSqkm;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getDeliveryDriver() {
        return deliveryDriver;
    }

    public void setDeliveryDriver(String deliveryDriver) {
        this.deliveryDriver = deliveryDriver;
    }

    public String getDownloadImageUrl() {
        return downloadImageUrl;
    }

    public void setDownloadImageUrl(String downloadImageUrl) {
        this.downloadImageUrl = downloadImageUrl;
    }

    public String getDownloadPayloadUrl() {
        return downloadPayloadUrl;
    }

    public void setDownloadPayloadUrl(String downloadPayloadUrl) {
        this.downloadPayloadUrl = downloadPayloadUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public int getOrderCost() {
        return orderCost;
    }

    public void setOrderCost(int orderCost) {
        this.orderCost = orderCost;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
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

    public int getSarAzimuthAngleMax() {
        return sarAzimuthAngleMax;
    }

    public void setSarAzimuthAngleMax(int sarAzimuthAngleMax) {
        this.sarAzimuthAngleMax = sarAzimuthAngleMax;
    }

    public int getSarAzimuthAngleMin() {
        return sarAzimuthAngleMin;
    }

    public void setSarAzimuthAngleMin(int sarAzimuthAngleMin) {
        this.sarAzimuthAngleMin = sarAzimuthAngleMin;
    }

    public int getSarGrazingAngleMax() {
        return sarGrazingAngleMax;
    }

    public void setSarGrazingAngleMax(int sarGrazingAngleMax) {
        this.sarGrazingAngleMax = sarGrazingAngleMax;
    }

    public int getSarGrazingAngleMin() {
        return sarGrazingAngleMin;
    }

    public void setSarGrazingAngleMin(int sarGrazingAngleMin) {
        this.sarGrazingAngleMin = sarGrazingAngleMin;
    }

    public int getSarNumberOfLooks() {
        return sarNumberOfLooks;
    }

    public void setSarNumberOfLooks(int sarNumberOfLooks) {
        this.sarNumberOfLooks = sarNumberOfLooks;
    }

    public String getSarPolarisation() {
        return sarPolarisation;
    }

    public void setSarPolarisation(String sarPolarisation) {
        this.sarPolarisation = sarPolarisation;
    }

    public String[] getSarProductTypes() {
        return sarProductTypes;
    }

    public void setSarProductTypes(String[] sarProductTypes) {
        this.sarProductTypes = sarProductTypes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTilesUrl() {
        return tilesUrl;
    }

    public void setTilesUrl(String tilesUrl) {
        this.tilesUrl = tilesUrl;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public Date getWindowEnd() {
        return windowEnd;
    }

    public void setWindowEnd(Date windowEnd) {
        this.windowEnd = windowEnd;
    }

    public Date getWindowStart() {
        return windowStart;
    }

    public void setWindowStart(Date windowStart) {
        this.windowStart = windowStart;
    }

    public Archive getArchive() {
        return archive;
    }

    public void setArchive(Archive archive) {
        this.archive = archive;
    }
}
