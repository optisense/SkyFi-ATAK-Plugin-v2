package com.optisense.skyfi.atak.skyfiapi;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;

public class PricingResponse {
    ArrayList<ProductType> productTypes;

    @NonNull
    @Override
    public String toString() {
        return "PricingResponse{" +
                "productTypes=" + productTypes +
                '}';
    }

    public ArrayList<ProductType> getProductTypes() {
        return productTypes;
    }

    public void setProductTypes(ArrayList<ProductType> productTypes) {
        this.productTypes = productTypes;
    }

    public class ProductType {
        String productType;
        ArrayList<Resolution> resolutions;

        @NonNull
        @Override
        public String toString() {
            return "ProductType{" +
                    "productType='" + productType + '\'' +
                    ", resolutions=" + resolutions +
                    '}';
        }

        public String getProductType() {
            return productType;
        }

        public void setProductType(String productType) {
            this.productType = productType;
        }

        public ArrayList<Resolution> getResolutions() {
            return resolutions;
        }

        public void setResolutions(ArrayList<Resolution> resolutions) {
            this.resolutions = resolutions;
        }

        public class Resolution {
            String resolution;
            Boolean isSupported;
            Boolean isComingSoon;
            Pricing pricing;
            HashMap<String, Provider> providers;

            @NonNull
            @Override
            public String toString() {
                return "Resolution{" +
                        "isComingSoon=" + isComingSoon +
                        ", resolution='" + resolution + '\'' +
                        ", isSupported=" + isSupported +
                        ", pricing=" + pricing +
                        ", providers=" + providers +
                        '}';
            }

            public Boolean getComingSoon() {
                return isComingSoon;
            }

            public void setComingSoon(Boolean comingSoon) {
                isComingSoon = comingSoon;
            }

            public Boolean getSupported() {
                return isSupported;
            }

            public void setSupported(Boolean supported) {
                isSupported = supported;
            }

            public Pricing getPricing() {
                return pricing;
            }

            public void setPricing(Pricing pricing) {
                this.pricing = pricing;
            }

            public HashMap<String, Provider> getProviders() {
                return providers;
            }

            public void setProviders(HashMap<String, Provider> providers) {
                this.providers = providers;
            }

            public String getResolution() {
                return resolution;
            }

            public void setResolution(String resolution) {
                this.resolution = resolution;
            }
        }

        public class Pricing {
            Float taskingMaxAoiSideKm;
            Float taskingMinSqkm;
            Float taskingMaxSqkm;
            Float taskingPriceOneSqkm;
            Boolean isPriorityEnabled;
            Float priorityTaskingPriceOneSqkm;
            Boolean isAssuredTaskingEnabled;
            Float assuredTaskingPriceOneSqkm;

            @NonNull
            @Override
            public String toString() {
                return "Pricing{" +
                        "isPriorityEnabled=" + isPriorityEnabled +
                        ", taskingMaxAoiSideKm=" + taskingMaxAoiSideKm +
                        ", taskingMinSqkm=" + taskingMinSqkm +
                        ", taskingMaxSqkm=" + taskingMaxSqkm +
                        ", taskingPriceOneSqkm=" + taskingPriceOneSqkm +
                        ", priorityTaskingPriceOneSqkm=" + priorityTaskingPriceOneSqkm +
                        ", isAssuredTaskingEnabled=" + isAssuredTaskingEnabled +
                        ", assuredTaskingPriceOneSqkm=" + assuredTaskingPriceOneSqkm +
                        '}';
            }

            public Boolean getPriorityEnabled() {
                return isPriorityEnabled;
            }

            public void setPriorityEnabled(Boolean priorityEnabled) {
                isPriorityEnabled = priorityEnabled;
            }

            public Float getPriorityTaskingPriceOneSqkm() {
                return priorityTaskingPriceOneSqkm;
            }

            public void setPriorityTaskingPriceOneSqkm(Float priorityTaskingPriceOneSqkm) {
                this.priorityTaskingPriceOneSqkm = priorityTaskingPriceOneSqkm;
            }

            public Float getTaskingMaxAoiSideKm() {
                return taskingMaxAoiSideKm;
            }

            public void setTaskingMaxAoiSideKm(Float taskingMaxAoiSideKm) {
                this.taskingMaxAoiSideKm = taskingMaxAoiSideKm;
            }

            public Float getTaskingMaxSqkm() {
                return taskingMaxSqkm;
            }

            public void setTaskingMaxSqkm(Float taskingMaxSqkm) {
                this.taskingMaxSqkm = taskingMaxSqkm;
            }

            public Float getTaskingMinSqkm() {
                return taskingMinSqkm;
            }

            public void setTaskingMinSqkm(Float taskingMinSqkm) {
                this.taskingMinSqkm = taskingMinSqkm;
            }

            public Float getTaskingPriceOneSqkm() {
                return taskingPriceOneSqkm;
            }

            public void setTaskingPriceOneSqkm(Float taskingPriceOneSqkm) {
                this.taskingPriceOneSqkm = taskingPriceOneSqkm;
            }

            public Boolean getAssuredTaskingEnabled() {
                return isAssuredTaskingEnabled;
            }

            public void setAssuredTaskingEnabled(Boolean assuredTaskingEnabled) {
                isAssuredTaskingEnabled = assuredTaskingEnabled;
            }

            public Float getAssuredTaskingPriceOneSqkm() {
                return assuredTaskingPriceOneSqkm;
            }

            public void setAssuredTaskingPriceOneSqkm(Float assuredTaskingPriceOneSqkm) {
                this.assuredTaskingPriceOneSqkm = assuredTaskingPriceOneSqkm;
            }
        }

        public class Provider {
            Float taskingMaxAoiSideKm;
            Float taskingMinSqkm;
            Float taskingMaxSqkm;
            Float taskingPriceOneSqkm;
            Boolean isPriorityEnabled;
            Float priorityTaskingPriceOneSqkm;
            Boolean isAssuredTaskingEnabled;
            Float assuredTaskingPriceOneSqkm;
            String provider;
            String providerOperational;

            @NonNull
            @Override
            public String toString() {
                return "Provider{" +
                        "isPriorityEnabled=" + isPriorityEnabled +
                        ", taskingMaxAoiSideKm=" + taskingMaxAoiSideKm +
                        ", taskingMinSqkm=" + taskingMinSqkm +
                        ", taskingMaxSqkm=" + taskingMaxSqkm +
                        ", taskingPriceOneSqkm=" + taskingPriceOneSqkm +
                        ", priorityTaskingPriceOneSqkm=" + priorityTaskingPriceOneSqkm +
                        ", isAssuredTaskingEnabled=" + isAssuredTaskingEnabled +
                        ", assuredTaskingPriceOneSqkm=" + assuredTaskingPriceOneSqkm +
                        ", provider='" + provider + '\'' +
                        ", providerOperational='" + providerOperational + '\'' +
                        '}';
            }

            public Boolean getPriorityEnabled() {
                return isPriorityEnabled;
            }

            public void setPriorityEnabled(Boolean priorityEnabled) {
                isPriorityEnabled = priorityEnabled;
            }

            public Float getPriorityTaskingPriceOneSqkm() {
                return priorityTaskingPriceOneSqkm;
            }

            public void setPriorityTaskingPriceOneSqkm(Float priorityTaskingPriceOneSqkm) {
                this.priorityTaskingPriceOneSqkm = priorityTaskingPriceOneSqkm;
            }

            public String getProvider() {
                return provider;
            }

            public void setProvider(String provider) {
                this.provider = provider;
            }

            public String getProviderOperational() {
                return providerOperational;
            }

            public void setProviderOperational(String providerOperational) {
                this.providerOperational = providerOperational;
            }

            public Float getTaskingMaxAoiSideKm() {
                return taskingMaxAoiSideKm;
            }

            public void setTaskingMaxAoiSideKm(Float taskingMaxAoiSideKm) {
                this.taskingMaxAoiSideKm = taskingMaxAoiSideKm;
            }

            public Float getTaskingMaxSqkm() {
                return taskingMaxSqkm;
            }

            public void setTaskingMaxSqkm(Float taskingMaxSqkm) {
                this.taskingMaxSqkm = taskingMaxSqkm;
            }

            public Float getTaskingMinSqkm() {
                return taskingMinSqkm;
            }

            public void setTaskingMinSqkm(Float taskingMinSqkm) {
                this.taskingMinSqkm = taskingMinSqkm;
            }

            public Float getTaskingPriceOneSqkm() {
                return taskingPriceOneSqkm;
            }

            public void setTaskingPriceOneSqkm(Float taskingPriceOneSqkm) {
                this.taskingPriceOneSqkm = taskingPriceOneSqkm;
            }

            public Boolean getAssuredTaskingEnabled() {
                return isAssuredTaskingEnabled;
            }

            public void setAssuredTaskingEnabled(Boolean assuredTaskingEnabled) {
                isAssuredTaskingEnabled = assuredTaskingEnabled;
            }

            public Float getAssuredTaskingPriceOneSqkm() {
                return assuredTaskingPriceOneSqkm;
            }

            public void setAssuredTaskingPriceOneSqkm(Float assuredTaskingPriceOneSqkm) {
                this.assuredTaskingPriceOneSqkm = assuredTaskingPriceOneSqkm;
            }
        }
    }
}