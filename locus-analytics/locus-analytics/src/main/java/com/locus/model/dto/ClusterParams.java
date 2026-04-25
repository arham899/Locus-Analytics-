package com.locus.model.dto;

/**
 * Data Transfer Object for investment cluster analysis parameters.
 * Used by {@code InvestmentClusterService.identifyClusters()}.
 */
public class ClusterParams {

    private String city;
    private String propertyType;
    private int analysisPeriodYears = 3;
    private int minListingCount = 10;

    // ── Constructors ──────────────────────────────────────────────────

    public ClusterParams() {
    }

    public ClusterParams(String city, String propertyType, int analysisPeriodYears,
                         int minListingCount) {
        this.city = city;
        this.propertyType = propertyType;
        this.analysisPeriodYears = analysisPeriodYears;
        this.minListingCount = minListingCount;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public int getAnalysisPeriodYears() {
        return analysisPeriodYears;
    }

    public void setAnalysisPeriodYears(int analysisPeriodYears) {
        this.analysisPeriodYears = analysisPeriodYears;
    }

    public int getMinListingCount() {
        return minListingCount;
    }

    public void setMinListingCount(int minListingCount) {
        this.minListingCount = minListingCount;
    }

    @Override
    public String toString() {
        return "ClusterParams{" +
                "city='" + city + '\'' +
                ", propertyType='" + propertyType + '\'' +
                ", periodYears=" + analysisPeriodYears +
                ", minListings=" + minListingCount +
                '}';
    }
}
