package com.locus.model.dto;

import java.time.LocalDate;

/**
 * Aggregated statistics computed from a list of {@link TrendPoint} values.
 * Used by {@code PriceTrendService.computeStatistics()}.
 */
public class TrendStatistics {

    private double annualAppreciationRate;
    private double highestPrice;
    private LocalDate highestDate;
    private double lowestPrice;
    private LocalDate lowestDate;
    private double currentAverage;

    // ── Constructors ──────────────────────────────────────────────────

    public TrendStatistics() {
    }

    public TrendStatistics(double annualAppreciationRate, double highestPrice,
                           LocalDate highestDate, double lowestPrice,
                           LocalDate lowestDate, double currentAverage) {
        this.annualAppreciationRate = annualAppreciationRate;
        this.highestPrice = highestPrice;
        this.highestDate = highestDate;
        this.lowestPrice = lowestPrice;
        this.lowestDate = lowestDate;
        this.currentAverage = currentAverage;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public double getAnnualAppreciationRate() {
        return annualAppreciationRate;
    }

    public void setAnnualAppreciationRate(double annualAppreciationRate) {
        this.annualAppreciationRate = annualAppreciationRate;
    }

    public double getHighestPrice() {
        return highestPrice;
    }

    public void setHighestPrice(double highestPrice) {
        this.highestPrice = highestPrice;
    }

    public LocalDate getHighestDate() {
        return highestDate;
    }

    public void setHighestDate(LocalDate highestDate) {
        this.highestDate = highestDate;
    }

    public double getLowestPrice() {
        return lowestPrice;
    }

    public void setLowestPrice(double lowestPrice) {
        this.lowestPrice = lowestPrice;
    }

    public LocalDate getLowestDate() {
        return lowestDate;
    }

    public void setLowestDate(LocalDate lowestDate) {
        this.lowestDate = lowestDate;
    }

    public double getCurrentAverage() {
        return currentAverage;
    }

    public void setCurrentAverage(double currentAverage) {
        this.currentAverage = currentAverage;
    }

    @Override
    public String toString() {
        return "TrendStatistics{" +
                "appreciation=" + annualAppreciationRate + "%" +
                ", highest=" + highestPrice + " on " + highestDate +
                ", lowest=" + lowestPrice + " on " + lowestDate +
                ", currentAvg=" + currentAverage +
                '}';
    }
}
