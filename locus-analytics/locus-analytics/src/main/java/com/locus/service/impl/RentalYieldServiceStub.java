package com.locus.service.impl;

import com.locus.model.RentalAnalysis;
import com.locus.service.RentalYieldService;

/**
 * Stub implementation of {@link RentalYieldService} returning realistic hardcoded data.
 */
public class RentalYieldServiceStub implements RentalYieldService {

    @Override
    public RentalAnalysis calculate(double propertyValue, double monthlyRent, double annualExpenses) {
        double annualRent = monthlyRent * 12;
        double grossYield = (annualRent / propertyValue) * 100;
        double netYield = ((annualRent - annualExpenses) / propertyValue) * 100;

        RentalAnalysis ra = new RentalAnalysis();
        ra.setPropertyValue(propertyValue);
        ra.setExpectedRent(monthlyRent);
        ra.setAnnualExpenses(annualExpenses);
        ra.setGrossYield(Math.round(grossYield * 100.0) / 100.0);   // e.g. 6.5%
        ra.setNetYield(Math.round(netYield * 100.0) / 100.0);       // e.g. 5.8%
        ra.setCityAverage(6.2);
        return ra;
    }

    @Override
    public double getCityAverageYield(String city, String locality) {
        // Stub: return realistic average per city
        switch (city.toLowerCase()) {
            case "karachi":    return 6.2;
            case "islamabad":  return 5.8;
            case "lahore":     return 6.0;
            default:           return 6.0;
        }
    }
}
