package com.locus.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a rental yield analysis for a property.
 * Maps to the {@code rental_analysis} table in the database.
 *
 * <p>Gross yield = (annual rent / property value) × 100.<br>
 * Net yield = ((annual rent − annual expenses) / property value) × 100.</p>
 */
public class RentalAnalysis {

    private String analysisId;
    private String propertyId;
    private LocalDate analysisDate;
    private double propertyValue;
    private double expectedRent;
    private double annualExpenses;
    private double grossYield;
    private double netYield;
    private double cityAverage;
    private String analystId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Constructors ──────────────────────────────────────────────────

    /** Default no-arg constructor. Generates a random UUID for analysisId. */
    public RentalAnalysis() {
        this.analysisId = UUID.randomUUID().toString();
        this.analysisDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /** Full constructor for database retrieval. */
    public RentalAnalysis(String analysisId, String propertyId, LocalDate analysisDate,
                          double annualRent, double annualExpenses,
                          double grossYield, double netYield, String analystId) {
        this.analysisId = analysisId;
        this.propertyId = propertyId;
        this.analysisDate = analysisDate;
        this.expectedRent = annualRent;
        this.annualExpenses = annualExpenses;
        this.grossYield = grossYield;
        this.netYield = netYield;
        this.analystId = analystId;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public String getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(String analysisId) {
        this.analysisId = analysisId;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public LocalDate getAnalysisDate() {
        return analysisDate;
    }

    public void setAnalysisDate(LocalDate analysisDate) {
        this.analysisDate = analysisDate;
    }

    public double getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(double propertyValue) {
        this.propertyValue = propertyValue;
    }

    public double getExpectedRent() {
        return expectedRent;
    }

    public void setExpectedRent(double expectedRent) {
        this.expectedRent = expectedRent;
    }

    public double getAnnualExpenses() {
        return annualExpenses;
    }

    public void setAnnualExpenses(double annualExpenses) {
        this.annualExpenses = annualExpenses;
    }

    public double getGrossYield() {
        return grossYield;
    }

    public void setGrossYield(double grossYield) {
        this.grossYield = grossYield;
    }

    public double getNetYield() {
        return netYield;
    }

    public void setNetYield(double netYield) {
        this.netYield = netYield;
    }

    public double getCityAverage() {
        return cityAverage;
    }

    public void setCityAverage(double cityAverage) {
        this.cityAverage = cityAverage;
    }

    public String getAnalystId() {
        return analystId;
    }

    public void setAnalystId(String analystId) {
        this.analystId = analystId;
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
        RentalAnalysis that = (RentalAnalysis) o;
        return Objects.equals(analysisId, that.analysisId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(analysisId);
    }

    @Override
    public String toString() {
        return "RentalAnalysis{" +
                "analysisId='" + analysisId + '\'' +
                ", propertyId='" + propertyId + '\'' +
                ", grossYield=" + grossYield +
                ", netYield=" + netYield +
                ", cityAverage=" + cityAverage +
                ", analysisDate=" + analysisDate +
                '}';
    }
}