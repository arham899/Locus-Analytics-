package com.locus.model.dto;

import java.time.LocalDate;

/**
 * Data Transfer Object for ROI calculation input.
 * Carries user-provided values for computing Return on Investment.
 */
public class ROIInput {

    private double purchasePrice;
    private LocalDate purchaseDate;
    private double currentValue;
    private double cumulativeRentalIncome;
    private double totalExpenses;

    // ── Constructors ──────────────────────────────────────────────────

    public ROIInput() {
    }

    public ROIInput(double purchasePrice, LocalDate purchaseDate, double currentValue,
                    double cumulativeRentalIncome, double totalExpenses) {
        this.purchasePrice = purchasePrice;
        this.purchaseDate = purchaseDate;
        this.currentValue = currentValue;
        this.cumulativeRentalIncome = cumulativeRentalIncome;
        this.totalExpenses = totalExpenses;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

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

    @Override
    public String toString() {
        return "ROIInput{" +
                "purchasePrice=" + purchasePrice +
                ", purchaseDate=" + purchaseDate +
                ", currentValue=" + currentValue +
                ", rentalIncome=" + cumulativeRentalIncome +
                ", expenses=" + totalExpenses +
                '}';
    }
}
