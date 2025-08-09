package com.optisense.skyfi.atak.skyfiapi;

import java.util.Arrays;
import java.util.Date;

import androidx.annotation.NonNull;

public class Order {
    private String id;
    private String orderType;
    private float orderCost;
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
    private float maxCloudCoveragePercent;
    private float maxOffNadirAngle;
    private String requiredProvider;
    private String[] sarProductTypes;
    private String sarPolarisation;
    private float sarGrazingAngleMin;
    private float sarGrazingAngleMax;
    private float sarAzimuthAngleMin;
    private float sarAzimuthAngleMax;
    private int sarNumberOfLooks;
    private String geocodeLocation;
    private String orderLabel;
    private String orderCode;
    private Archive archive;
    private String label;

    @NonNull
    @Override
    public String toString() {
        return "Order{" +
                "aoi='" + aoi + '\'' +
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
                ", geocodeLocation='" + geocodeLocation + '\'' +
                ", orderLabel='" + orderLabel + '\'' +
                ", orderCode='" + orderCode + '\'' +
                ", archive=" + archive +
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

    public float getMaxCloudCoveragePercent() {
        return maxCloudCoveragePercent;
    }

    public void setMaxCloudCoveragePercent(float maxCloudCoveragePercent) {
        this.maxCloudCoveragePercent = maxCloudCoveragePercent;
    }

    public float getMaxOffNadirAngle() {
        return maxOffNadirAngle;
    }

    public void setMaxOffNadirAngle(float maxOffNadirAngle) {
        this.maxOffNadirAngle = maxOffNadirAngle;
    }

    public float getOrderCost() {
        return orderCost;
    }

    public void setOrderCost(float orderCost) {
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

    public String getGeocodeLocation() {
        return geocodeLocation;
    }

    public void setGeocodeLocation(String geocodeLocation) {
        this.geocodeLocation = geocodeLocation;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getOrderLabel() {
        return orderLabel;
    }

    public void setOrderLabel(String orderLabel) {
        this.orderLabel = orderLabel;
    }

    // Order name is its user created label if there is one, otherwise use the geocoded location
    public String getOrderName() {
        if (orderLabel != null && !orderLabel.isEmpty() && !orderLabel.equals("Platform Order"))
            return orderLabel;
        else
            return geocodeLocation + " - " + orderCode;
    }

    public Archive getArchive() {
        return archive;
    }

    public void setArchive(Archive archive) {
        this.archive = archive;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
