package com.skyfi.atak.plugin.skyfiapi;

public class ArchivesRequest {
    private String aoi;
    private String fromDate;
    private String toDate;
    private float maxCloudCoveragePercent;
    private float maxOffNadirAngle;
    private String[] resolutions;
    private String[] productTypes;
    private String[] providers;
    private boolean openData;
    private float minOverlapRatio;
    private int pageNumber;
    private int pageSize;

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

    public String[] getProductTypes() {
        return productTypes;
    }

    public void setProductTypes(String[] productTypes) {
        this.productTypes = productTypes;
    }

    public String[] getProviders() {
        return providers;
    }

    public void setProviders(String[] providers) {
        this.providers = providers;
    }

    public String[] getResolutions() {
        return resolutions;
    }

    public void setResolutions(String[] resolutions) {
        this.resolutions = resolutions;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }
}
