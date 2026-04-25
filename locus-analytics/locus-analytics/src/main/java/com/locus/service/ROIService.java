package com.locus.service;

import com.locus.model.ROIAnalysis;
import com.locus.model.dto.ROIInput;

/**
 * Service for calculating Return on Investment for properties (UC-3).
 *
 * <p>ROI% = (totalReturn / purchasePrice) × 100.<br>
 * Annualized ROI = ((1 + ROI/100)^(1/holdingPeriodYears) − 1) × 100.</p>
 */
public interface ROIService {

    /**
     * Calculates comprehensive ROI metrics from user-provided investment data.
     *
     * @param input ROI input containing purchase price, date, current value,
     *              rental income, and expenses
     * @return ROIAnalysis with total return, ROI%, annualized ROI, and breakdowns
     * @throws com.locus.exception.ValidationException if purchaseDate is in the future,
     *         or values are negative
     */
    ROIAnalysis calculate(ROIInput input);
}
