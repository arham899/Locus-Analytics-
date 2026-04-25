package com.locus.model.dto;

import java.time.LocalDate;

/**
 * Represents a single data point in a price trend time series (UC-6).
 * Typically one point per month, showing average price and listing volume.
 */
public class TrendPoint {

    private String period;          // e.g. "2024-01", "2024-02"
    private double averagePrice;
    private long listingCount;

    // ── Constructors ──────────────────────────────────────────────────

    public TrendPoint() {
    }

    public TrendPoint(String period, double averagePrice, long listingCount) {
        this.period = period;
        this.averagePrice = averagePrice;
        this.listingCount = listingCount;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public double getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(double averagePrice) {
        this.averagePrice = averagePrice;
    }

    public long getListingCount() {
        return listingCount;
    }

    public void setListingCount(long listingCount) {
        this.listingCount = listingCount;
    }

    @Override
    public String toString() {
        return "TrendPoint{" +
                "period='" + period + '\'' +
                ", avgPrice=" + averagePrice +
                ", listings=" + listingCount +
                '}';
    }
}
