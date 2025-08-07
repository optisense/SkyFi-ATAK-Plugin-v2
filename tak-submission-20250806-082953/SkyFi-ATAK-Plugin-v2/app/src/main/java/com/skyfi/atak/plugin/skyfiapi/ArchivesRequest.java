package com.skyfi.atak.plugin.skyfiapi;

import java.io.Serializable;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import com.skyfi.atak.plugin.Constants;

public class ArchivesRequest implements Serializable {
    private String aoi;
    private String fromDate;
    private String toDate;
    private Float maxCloudCoveragePercent;
    private Float maxOffNadirAngle;
    private ArrayList<String> resolutions = new ArrayList<>();
    private ArrayList<String> productTypes = new ArrayList<>();
    private ArrayList<String> providers = new ArrayList<>();
    private Boolean openData;
    private Float minOverlapRatio;
    private int pageNumber;
    private int pageSize = 10;

    public ArchivesRequest() {
        resolutions.add(Constants.LOW);
        resolutions.add(Constants.MEDIUM);
        resolutions.add(Constants.HIGH);
        resolutions.add(Constants.VERY_HIGH);
        resolutions.add(Constants.SUPER_HIGH);
        resolutions.add(Constants.ULTRA_HIGH);

        productTypes.add(Constants.DAY);
        productTypes.add(Constants.NIGHT);
        productTypes.add(Constants.VIDEO);
        productTypes.add(Constants.SAR);
        productTypes.add(Constants.HYPERSPECTRAL);
        productTypes.add(Constants.MULTISPECTRAL);
        productTypes.add(Constants.STEREO);

        providers.add(Constants.SIWEI);
        providers.add(Constants.SATELLOGIC);
        providers.add(Constants.UMBRA);
        providers.add(Constants.TAILWIND);
        providers.add(Constants.GEOSAT);
        providers.add(Constants.SENTINEL2);
        providers.add(Constants.SENTINEL2_CREODIAS);
        providers.add(Constants.PLANET);
        providers.add(Constants.IMPRO);
        providers.add(Constants.URBAN_SKY);
        providers.add(Constants.NSL);
        providers.add(Constants.VEXCEL);
    }

    @NonNull
    @Override
    public String toString() {
        return "ArchivesRequest{" +
                "aoi='" + aoi + '\'' +
                ", fromDate='" + fromDate + '\'' +
                ", toDate='" + toDate + '\'' +
                ", maxCloudCoveragePercent=" + maxCloudCoveragePercent +
                ", maxOffNadirAngle=" + maxOffNadirAngle +
                ", resolutions=" + resolutions +
                ", productTypes=" + productTypes +
                ", providers=" + providers +
                ", openData=" + openData +
                ", minOverlapRatio=" + minOverlapRatio +
                ", pageNumber=" + pageNumber +
                ", pageSize=" + pageSize +
                '}';
    }

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

    public Float getMinOverlapRatio() {
        return minOverlapRatio;
    }

    public void setMinOverlapRatio(Float minOverlapRatio) {
        this.minOverlapRatio = minOverlapRatio;
    }

    public Boolean isOpenData() {
        return openData;
    }

    public void setOpenData(Boolean openData) {
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
