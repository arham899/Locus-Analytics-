package com.locus.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a Fair Market Value (FMV) estimation for a property.
 * Maps to the {@code valuation} table in the database.
 *
 * <p>A valuation stores the estimated FMV along with a confidence interval
 * (lower and upper bounds) and the key factors that influenced the estimate.</p>
 */
public class Valuation {

    private String valuationId;
    private String propertyId;
    private LocalDateTime calculationDate;
    private double estimatedFmv;
    private double confidenceIntervalLow;
    private double confidenceIntervalHigh;
    private List<String> keyFactors;
    private int dataDensity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Constructors ──────────────────────────────────────────────────

    /** Default no-arg constructor. Generates a random UUID for valuationId. */
    public Valuation() {
        this.valuationId = UUID.randomUUID().toString();
        this.keyFactors = new ArrayList<>();
        this.calculationDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /** Full constructor for database retrieval. */
    public Valuation(String valuationId, String propertyId, LocalDateTime calculationDate,
                     double estimatedFmv, double confidenceIntervalLow,
                     double confidenceIntervalHigh) {
        this.valuationId = valuationId;
        this.propertyId = propertyId;
        this.calculationDate = calculationDate;
        this.estimatedFmv = estimatedFmv;
        this.confidenceIntervalLow = confidenceIntervalLow;
        this.confidenceIntervalHigh = confidenceIntervalHigh;
        this.keyFactors = new ArrayList<>();
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public String getValuationId() {
        return valuationId;
    }

    public void setValuationId(String valuationId) {
        this.valuationId = valuationId;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public LocalDateTime getCalculationDate() {
        return calculationDate;
    }

    public void setCalculationDate(LocalDateTime calculationDate) {
        this.calculationDate = calculationDate;
    }

    public double getEstimatedFmv() {
        return estimatedFmv;
    }

    public void setEstimatedFmv(double estimatedFmv) {
        this.estimatedFmv = estimatedFmv;
    }

    public double getConfidenceIntervalLow() {
        return confidenceIntervalLow;
    }

    public void setConfidenceIntervalLow(double confidenceIntervalLow) {
        this.confidenceIntervalLow = confidenceIntervalLow;
    }

    public double getConfidenceIntervalHigh() {
        return confidenceIntervalHigh;
    }

    public void setConfidenceIntervalHigh(double confidenceIntervalHigh) {
        this.confidenceIntervalHigh = confidenceIntervalHigh;
    }

    public List<String> getKeyFactors() {
        return keyFactors;
    }

    public void setKeyFactors(List<String> keyFactors) {
        this.keyFactors = keyFactors;
    }

    public int getDataDensity() {
        return dataDensity;
    }

    public void setDataDensity(int dataDensity) {
        this.dataDensity = dataDensity;
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
        Valuation valuation = (Valuation) o;
        return Objects.equals(valuationId, valuation.valuationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valuationId);
    }

    @Override
    public String toString() {
        return "Valuation{" +
                "valuationId='" + valuationId + '\'' +
                ", propertyId='" + propertyId + '\'' +
                ", estimatedFmv=" + estimatedFmv +
                ", confidenceInterval=[" + confidenceIntervalLow + ", " + confidenceIntervalHigh + "]" +
                ", calculationDate=" + calculationDate +
                '}';
    }
}