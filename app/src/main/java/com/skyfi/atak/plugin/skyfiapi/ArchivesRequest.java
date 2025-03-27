package com.skyfi.atak.plugin.skyfiapi;

import java.util.ArrayList;

public class ArchivesRequest {
    private String aoi;
    private String fromDate;
    private String toDate;
    private float maxCloudCoveragePercent;
    private float maxOffNadirAngle;
    private ArrayList<String> resolutions = new ArrayList<>();
    private ArrayList<String> productTypes = new ArrayList<>();
    private ArrayList<String> providers = new ArrayList<>();
    private boolean openData;
    private float minOverlapRatio;
    private int pageNumber;
    private int pageSize = 10;

    public String getAoi() {
        return aoi;
    }

    public void setAoi(String aoi) {
        this.aoi = aoi;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
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

    public float getMinOverlapRatio() {
        return minOverlapRatio;
    }

    public void setMinOverlapRatio(float minOverlapRatio) {
        this.minOverlapRatio = minOverlapRatio;
    }

    public boolean isOpenData() {
        return openData;
    }

    public void setOpenData(boolean openData) {
        this.openData = openData;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public ArrayList<String> getProductTypes() {
        return productTypes;
    }

    public void addProductType(String productType) {
        productTypes.add(productType);
    }

    public void removeProductType(String productType) {
        productTypes.remove(productType);
    }

    public void setProductTypes(ArrayList<String> productTypes) {
        this.productTypes = productTypes;
    }

    public ArrayList<String> getProviders() {
        return providers;
    }

    public void addProvider(String provider) {
        providers.add(provider);
    }

    public void removeProvider(String provider) {
        providers.remove(provider);;
    }

    public void setProviders(ArrayList<String> providers) {
        this.providers = providers;
    }

    public ArrayList<String> getResolutions() {
        return resolutions;
    }

    public void addResolution(String resolution) {
        resolutions.add(resolution);
    }

    public void removeResolution(String resolution) {
        resolutions.remove(resolution);
    }

    public void setResolutions(ArrayList<String> resolutions) {
        this.resolutions = resolutions;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }
}
