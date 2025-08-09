package com.optisense.skyfi.atak.skyfiapi;

import androidx.annotation.NonNull;

public class PricingQuery {
    String aoi;

    public PricingQuery(String aoi) {
        this.aoi = aoi;
    }

    @NonNull
    @Override
    public String toString() {
        return "PricingQuery{" +
                "aoi='" + aoi + '\'' +
                '}';
    }

    public String getAoi() {
        return aoi;
    }

    public void setAoi(String aoi) {
        this.aoi = aoi;
    }
}
