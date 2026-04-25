package com.locus.service.impl;

import com.locus.model.Property;
import com.locus.model.Valuation;
import com.locus.service.ValuationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Stub implementation of {@link ValuationService} returning realistic hardcoded data.
 * Allows Ayaan to wire UI screens before the ML model is ready.
 */
public class ValuationServiceStub implements ValuationService {

    @Override
    public Valuation estimateFMV(Property property) {
        double area = Math.max(1, property.getArea());
        int beds = Math.max(1, property.getBedrooms());
        int baths = Math.max(1, property.getBathrooms());

        double localityPremium = switch (String.valueOf(property.getLocality()).toLowerCase()) {
            case "dha phase 6" -> 1.22;
            case "clifton" -> 1.18;
            case "bahria town" -> 1.05;
            case "f-7" -> 1.20;
            case "gulberg" -> 1.15;
            default -> 1.00;
        };
        double typeFactor = switch (String.valueOf(property.getPropertyType()).toLowerCase()) {
            case "house" -> 1.00;
            case "apartment" -> 0.88;
            case "plot" -> 0.72;
            case "commercial" -> 1.28;
            default -> 1.00;
        };
        double roomFactor = 1.0 + (beds * 0.03) + (baths * 0.015);
        double baseRatePerSqft = 9800.0;
        double estimated = area * baseRatePerSqft * localityPremium * typeFactor * roomFactor;

        Valuation v = new Valuation();
        v.setPropertyId(property.getPropertyId());
        v.setCalculationDate(LocalDateTime.now());
        v.setEstimatedFmv(estimated);
        v.setConfidenceIntervalLow(estimated * 0.90);
        v.setConfidenceIntervalHigh(estimated * 1.10);
        v.setKeyFactors(Arrays.asList(
                "Area: " + (int) area + " sq.ft.",
                "Locality premium factor: " + String.format("%.2fx", localityPremium),
                "Property type factor: " + String.format("%.2fx", typeFactor),
                "Room mix factor: " + String.format("%.2fx", roomFactor)
        ));
        return v;
    }

    @Override
    public List<Property> findComparables(Property property) {
        double area = Math.max(1, property.getArea());
        double basePrice = estimateFMV(property).getEstimatedFmv();
        String city = property.getCity() == null ? "Karachi" : property.getCity();
        String locality = property.getLocality() == null ? "DHA Phase 6" : property.getLocality();
        String type = property.getPropertyType() == null ? "house" : property.getPropertyType();

        List<Property> comps = new ArrayList<>();
        double[] areaFactors = {0.90, 0.96, 1.00, 1.05, 1.12};
        double[] priceFactors = {0.88, 0.94, 1.00, 1.06, 1.11};
        for (int i = 0; i < areaFactors.length; i++) {
            Property p = new Property("comp-" + (i + 1), city, locality, type,
                    Math.round(area * areaFactors[i]),
                    Math.round(basePrice * priceFactors[i]),
                    Math.max(1, property.getBedrooms() + (i % 2 == 0 ? 0 : 1)),
                    Math.max(1, property.getBathrooms()),
                    LocalDate.now().minusWeeks(2L * (i + 1)),
                    24.80 + (i * 0.01),
                    67.00 + (i * 0.01),
                    "hash" + (i + 1));
            comps.add(p);
        }
        return comps;
    }
}
