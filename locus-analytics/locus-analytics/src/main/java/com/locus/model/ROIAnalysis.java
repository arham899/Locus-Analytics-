package com.locus.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a Return on Investment (ROI) analysis for a property.
 * Maps to the {@code roi_analysis} table in the database.
 *
 * <p>ROI% = (totalReturn / purchasePrice) × 100.<br>
 * Annualized ROI = ((1 + ROI/100)^(1/holdingPeriodYears) − 1) × 100.</p>
 */
public class ROIAnalysis {

    private String analysisId;
    private String propertyId;
    private LocalDate analysisDate;
    private double purchasePrice;
    private LocalDate purchaseDate;
    private double currentValue;
    private double cumulativeRentalIncome;
    private double totalExpenses;
    private double totalReturn;
    private double roiPercentage;
    private double annualizedROI;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Constructors ──────────────────────────────────────────────────

    /** Default no-arg constructor. Generates a random UUID for analysisId. */
    public ROIAnalysis() {
        this.analysisId = UUID.randomUUID().toString();
        this.analysisDate = LocalDate.now();
    }

    /** Full constructor for database retrieval. */
    public ROIAnalysis(String analysisId, String propertyId, LocalDate analysisDate,
                       double purchasePrice, LocalDate purchaseDate,
                       double cumulativeRentalIncome, double totalExpenses,
                       double totalReturn, double roiPercentage, double annualizedROI) {
        this.analysisId = analysisId;
        this.propertyId = propertyId;
        this.analysisDate = analysisDate;
        this.purchasePrice = purchasePrice;
        this.purchaseDate = purchaseDate;
        this.cumulativeRentalIncome = cumulativeRentalIncome;
        this.totalExpenses = totalExpenses;
        this.totalReturn = totalReturn;
        this.roiPercentage = roiPercentage;
        this.annualizedROI = annualizedROI;
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

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public double getCumulativeRentalIncome() {
        return cumulativeRentalIncome;
    }

    public void setCumulativeRentalIncome(double cumulativeRentalIncome) {
        this.cumulativeRentalIncome = cumulativeRentalIncome;
    }

    public double getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(double totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public double getTotalReturn() {
        return totalReturn;
    }

    public void setTotalReturn(double totalReturn) {
        this.totalReturn = totalReturn;
    }

    public double getRoiPercentage() {
        return roiPercentage;
    }

    public void setRoiPercentage(double roiPercentage) {
        this.roiPercentage = roiPercentage;
    }

    public double getAnnualizedROI() {
        return annualizedROI;
    }

    public void setAnnualizedROI(double annualizedROI) {
        this.annualizedROI = annualizedROI;
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
        ROIAnalysis that = (ROIAnalysis) o;
        return Objects.equals(analysisId, that.analysisId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(analysisId);
    }

    @Override
    public String toString() {
        return "ROIAnalysis{" +
                "analysisId='" + analysisId + '\'' +
                ", propertyId='" + propertyId + '\'' +
                ", roiPercentage=" + roiPercentage +
                ", annualizedROI=" + annualizedROI +
                ", totalReturn=" + totalReturn +
                ", analysisDate=" + analysisDate +
                '}';
    }
}