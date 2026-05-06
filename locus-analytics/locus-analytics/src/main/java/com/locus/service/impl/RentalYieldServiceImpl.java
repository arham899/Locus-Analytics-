package com.locus.service.impl;

import com.locus.dao.RentalAnalysisDAO;
import com.locus.model.RentalAnalysis;
import com.locus.service.RentalYieldService;
import com.locus.service.validation.InputValidator;

/**
 * Real implementation of {@link RentalYieldService} (UC-2).
 *
 * <p>Calculates gross and net rental yield and persists the analysis.
 * City/locality average is computed from existing rental data in the DAO.</p>
 *
 * @author Arham Manzoor (24i-0640)
 */
public class RentalYieldServiceImpl implements RentalYieldService {

    private final RentalAnalysisDAO rentalAnalysisDAO;

    public RentalYieldServiceImpl(RentalAnalysisDAO rentalAnalysisDAO) {
        this.rentalAnalysisDAO = rentalAnalysisDAO;
    }

    @Override
    public RentalAnalysis calculate(double propertyValue, double monthlyRent, double annualExpenses) {

        // ── Validation ──────────────────────────────
        double annualRent = monthlyRent * 12;

        new InputValidator()
                .validatePositive("propertyValue", propertyValue)
                .validatePositive("monthlyRent", monthlyRent)
                .validateNonNegative("annualExpenses", annualExpenses)
                .throwIfInvalid();

        if (annualRent > propertyValue) {
            new InputValidator()
                    .validatePositive("rent", -1) // force error
                    .throwIfInvalid();
            // Direct exception as fallback
            throw new com.locus.exception.ValidationException(
                    "Annual rent cannot exceed property value"
            );
        }

        // ── Compute yields ──────────────────────────
        double grossYield = (annualRent / propertyValue) * 100.0;
        double netYield = ((annualRent - annualExpenses) / propertyValue) * 100.0;

        // ── City average (fetched from real DAO data) ──
        double cityAverage = rentalAnalysisDAO.getCityAverageYield("Lahore"); // Default or current city

        // ── Build result ────────────────────────────
        RentalAnalysis analysis = new RentalAnalysis();
        analysis.setPropertyValue(propertyValue);
        analysis.setExpectedRent(annualRent);
        analysis.setAnnualExpenses(annualExpenses);
        analysis.setGrossYield(grossYield);
        analysis.setNetYield(netYield);
        analysis.setCityAverage(cityAverage);
        
        // Ensure propertyId is not null to satisfy DB constraint
        if (analysis.getPropertyId() == null) {
            analysis.setPropertyId("AD_HOC_PROP_" + java.util.UUID.randomUUID().toString().substring(0, 8));
        }

        // ── Persist ─────────────────────────────────
        try {
            rentalAnalysisDAO.insert(analysis);
        } catch (Exception e) {
            System.err.println("[RentalYieldService] Warning: could not persist analysis: " + e.getMessage());
        }

        return analysis;
    }

    @Override
    public double getCityAverageYield(String city, String locality) {
        if (locality != null && !locality.isBlank()) {
            return rentalAnalysisDAO.getLocalityAverageYield(city, locality);
        }
        return rentalAnalysisDAO.getCityAverageYield(city);
    }
}
