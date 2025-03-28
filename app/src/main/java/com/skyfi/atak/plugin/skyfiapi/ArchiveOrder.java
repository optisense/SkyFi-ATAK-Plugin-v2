package com.skyfi.atak.plugin.skyfiapi;

import androidx.annotation.NonNull;

public class ArchiveOrder {
    String archiveId; // required
    String aoi; // required
    String deliveryDriver;
    DeliveryParams deliveryParams;
    String label = null;
    String orderLabel = null;
    Object metadata;
    String webhookUrl;

    @NonNull
    @Override
    public String toString() {
        return "ArchiveOrder{" +
                "aoi='" + aoi + '\'' +
                ", archiveId='" + archiveId + '\'' +
                ", deliveryDriver='" + deliveryDriver + '\'' +
                ", deliveryParams=" + deliveryParams +
                ", label='" + label + '\'' +
                ", orderLabel='" + orderLabel + '\'' +
                ", metadata=" + metadata +
                ", webhookUrl='" + webhookUrl + '\'' +
                '}';
    }

    public String getAoi() {
        return aoi;
    }

    public void setAoi(String aoi) {
        this.aoi = aoi;
    }

    public String getArchiveId() {
        return archiveId;
    }

    public void setArchiveId(String archiveId) {
        this.archiveId = archiveId;
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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
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

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }
}
