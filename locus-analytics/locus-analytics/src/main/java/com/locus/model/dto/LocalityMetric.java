package com.locus.model.dto;

public class LocalityMetric {

    private String locality;
    private double avgPricePast;
    private double avgPriceRecent;
    private double priceAppreciation=0.0;
    private long pastCount;
    private long recentCount;
    private double volumeGrowth=0.0;

    // getters + setters
    public String getLocality() {
        return locality;
    }
    public void setLocality(String locality) {
        this.locality = locality;
    }

    public double getAvgPricePast() {
        return avgPricePast;
    }
    public void setAvgPricePast(double avgPricePast) {
        this.avgPricePast = avgPricePast;
    }
    public double getAvgPriceRecent() {
        return avgPriceRecent;
    }
    public void setAvgPriceRecent(double avgPriceRecent) {
        this.avgPriceRecent = avgPriceRecent;
    }
    public double getPriceAppreciation() {
        return priceAppreciation;
    }

    public void setPriceAppreciation(double priceAppreciation) {
        this.priceAppreciation = priceAppreciation;
    }
    public long getPastCount() {
        return pastCount;
    }
    public void setPastCount(long pastCount) {
        this.pastCount = pastCount;
    }
    public long getRecentCount() {
        return recentCount;
    }
    public void setRecentCount(long recentCount) {
        this.recentCount = recentCount;
    }
    public double getVolumeGrowth() {
        return volumeGrowth;
    }
    public void setVolumeGrowth(double volumeGrowth) {
        this.volumeGrowth = volumeGrowth;
    }

}