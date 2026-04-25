package com.locus.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an investment cluster — a locality scored and ranked by investment potential.
 * Maps to the {@code investment_cluster} table in the database.
 *
 * <p>Investment Score = 0.5 × priceAppreciation + 0.3 × volumeGrowth + 0.2 × rentalTrend,
 * normalized to a 0–100 scale.</p>
 */
public class InvestmentCluster {

    private String clusterId;
    private String city;
    private String locality;
    private double investmentScore;
    private double priceAppreciation;
    private double listingVolumeGrowth;
    private double rentalTrend;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Constructors ──────────────────────────────────────────────────

    /** Default no-arg constructor. Generates a random UUID for clusterId. */
    public InvestmentCluster() {
        this.clusterId = UUID.randomUUID().toString();
    }

    /** Full constructor for database retrieval. */
    public InvestmentCluster(String clusterId, String city, String locality,
                             double investmentScore, double priceAppreciation,
                             double listingVolumeGrowth) {
        this.clusterId = clusterId;
        this.city = city;
        this.locality = locality;
        this.investmentScore = investmentScore;
        this.priceAppreciation = priceAppreciation;
        this.listingVolumeGrowth = listingVolumeGrowth;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public double getInvestmentScore() {
        return investmentScore;
    }

    public void setInvestmentScore(double investmentScore) {
        this.investmentScore = investmentScore;
    }

    public double getPriceAppreciation() {
        return priceAppreciation;
    }

    public void setPriceAppreciation(double priceAppreciation) {
        this.priceAppreciation = priceAppreciation;
    }

    public double getListingVolumeGrowth() {
        return listingVolumeGrowth;
    }

    public void setListingVolumeGrowth(double listingVolumeGrowth) {
        this.listingVolumeGrowth = listingVolumeGrowth;
    }

    public double getRentalTrend() {
        return rentalTrend;
    }

    public void setRentalTrend(double rentalTrend) {
        this.rentalTrend = rentalTrend;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ── equals / hashCode / toString ──────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvestmentCluster that = (InvestmentCluster) o;
        return Objects.equals(clusterId, that.clusterId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clusterId);
    }

    @Override
    public String toString() {
        return "InvestmentCluster{" +
                "clusterId='" + clusterId + '\'' +
                ", city='" + city + '\'' +
                ", locality='" + locality + '\'' +
                ", investmentScore=" + investmentScore +
                ", priceAppreciation=" + priceAppreciation +
                ", volumeGrowth=" + listingVolumeGrowth +
                '}';
    }
}