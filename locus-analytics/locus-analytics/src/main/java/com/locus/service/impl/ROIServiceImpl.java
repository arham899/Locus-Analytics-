package com.locus.service.impl;

import com.locus.dao.ROIAnalysisDAO;
import com.locus.model.ROIAnalysis;
import com.locus.model.dto.ROIInput;
import com.locus.service.ROIService;
import com.locus.service.validation.InputValidator;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Real implementation of {@link ROIService} (UC-3).
 *
 * <p>Calculates total return, ROI%, and annualized ROI from user-provided
 * investment data. Flags a warning if holding period is less than 1 year.</p>
 *
 * @author Arham Manzoor (24i-0640)
 */
public class ROIServiceImpl implements ROIService {

    private final ROIAnalysisDAO roiAnalysisDAO;

    public ROIServiceImpl(ROIAnalysisDAO roiAnalysisDAO) {
        this.roiAnalysisDAO = roiAnalysisDAO;
    }

    @Override
    public ROIAnalysis calculate(ROIInput input) {

        // ── Validation ──────────────────────────────
        new InputValidator()
                .validateNotNull("input", input)
                .throwIfInvalid();

        new InputValidator()
                .validatePositive("purchasePrice", input.getPurchasePrice())
                .validateNotNull("purchaseDate", input.getPurchaseDate())
                .validateNotFuture("purchaseDate", input.getPurchaseDate())
                .validatePositive("currentValue", input.getCurrentValue())
                .validateNonNegative("cumulativeRentalIncome", input.getCumulativeRentalIncome())
                .validateNonNegative("totalExpenses", input.getTotalExpenses())
                .throwIfInvalid();

        // ── Core calculations ───────────────────────
        double purchasePrice = input.getPurchasePrice();
        double currentValue = input.getCurrentValue();
        double rentalIncome = input.getCumulativeRentalIncome();
        double expenses = input.getTotalExpenses();

        // Total Return = Capital Appreciation + Net Rental Income
        double capitalAppreciation = currentValue - purchasePrice;
        double netRentalIncome = rentalIncome - expenses;
        double totalReturn = capitalAppreciation + netRentalIncome;

        // ROI %
        double roiPercentage = (totalReturn / purchasePrice) * 100.0;

        // Holding period in years
        long daysBetween = ChronoUnit.DAYS.between(input.getPurchaseDate(), LocalDate.now());
        double holdingYears = daysBetween / 365.25;

        // Annualized ROI
        double annualizedROI;
        if (holdingYears >= 1.0) {
            annualizedROI = (Math.pow(1 + roiPercentage / 100.0, 1.0 / holdingYears) - 1) * 100.0;
        } else if (holdingYears > 0) {
            // Less than 1 year — extrapolate but flag warning
            annualizedROI = (Math.pow(1 + roiPercentage / 100.0, 1.0 / holdingYears) - 1) * 100.0;
            System.out.println("[ROIService] Warning: holding period < 1 year (" +
                    String.format("%.1f months", holdingYears * 12) + "). Annualized ROI may be misleading.");
        } else {
            annualizedROI = 0;
        }

        // ── Build result ────────────────────────────
        ROIAnalysis analysis = new ROIAnalysis();
        analysis.setPurchasePrice(purchasePrice);
        analysis.setPurchaseDate(input.getPurchaseDate());
        analysis.setCurrentValue(currentValue);
        analysis.setCumulativeRentalIncome(rentalIncome);
        analysis.setTotalExpenses(expenses);
        analysis.setTotalReturn(totalReturn);
        analysis.setRoiPercentage(roiPercentage);
        analysis.setAnnualizedROI(annualizedROI);
        
        // Ensure propertyId is not null to satisfy DB constraint
        if (analysis.getPropertyId() == null) {
            analysis.setPropertyId("AD_HOC_ROI_" + java.util.UUID.randomUUID().toString().substring(0, 8));
        }

        // ── Persist ─────────────────────────────────
        try {
            roiAnalysisDAO.insert(analysis);
        } catch (Exception e) {
            System.err.println("[ROIService] Warning: could not persist analysis: " + e.getMessage());
        }

        return analysis;
    }
}
