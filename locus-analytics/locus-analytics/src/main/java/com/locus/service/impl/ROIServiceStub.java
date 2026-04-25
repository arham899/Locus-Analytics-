package com.locus.service.impl;

import com.locus.model.ROIAnalysis;
import com.locus.model.dto.ROIInput;
import com.locus.service.ROIService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Stub implementation of {@link ROIService} returning realistic hardcoded data.
 */
public class ROIServiceStub implements ROIService {

    @Override
    public ROIAnalysis calculate(ROIInput input) {
        // Compute real formulas even in the stub so UI sees plausible numbers
        double capitalAppreciation = input.getCurrentValue() - input.getPurchasePrice();
        double netRentalIncome = input.getCumulativeRentalIncome() - input.getTotalExpenses();
        double totalReturn = capitalAppreciation + netRentalIncome;
        double roiPercentage = (totalReturn / input.getPurchasePrice()) * 100;

        long daysBetween = ChronoUnit.DAYS.between(input.getPurchaseDate(), LocalDate.now());
        double holdingYears = daysBetween / 365.25;
        double annualizedROI = 0;
        if (holdingYears > 0) {
            annualizedROI = (Math.pow(1 + roiPercentage / 100, 1.0 / holdingYears) - 1) * 100;
        }

        ROIAnalysis roi = new ROIAnalysis();
        roi.setPurchasePrice(input.getPurchasePrice());
        roi.setPurchaseDate(input.getPurchaseDate());
        roi.setCurrentValue(input.getCurrentValue());
        roi.setCumulativeRentalIncome(input.getCumulativeRentalIncome());
        roi.setTotalExpenses(input.getTotalExpenses());
        roi.setTotalReturn(Math.round(totalReturn * 100.0) / 100.0);
        roi.setRoiPercentage(Math.round(roiPercentage * 100.0) / 100.0);
        roi.setAnnualizedROI(Math.round(annualizedROI * 100.0) / 100.0);
        return roi;
    }
}
