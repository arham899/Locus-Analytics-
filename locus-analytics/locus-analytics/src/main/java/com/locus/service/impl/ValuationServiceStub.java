package com.locus.service.impl;

import com.locus.model.Property;
import com.locus.model.Valuation;
import com.locus.service.ValuationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Stub implementation of {@link ValuationService} returning realistic hardcoded data.
 * Allows Ayaan to wire UI screens before the ML model is ready.
 */
public class ValuationServiceStub implements ValuationService {

    @Override
    public Valuation estimateFMV(Property property) {
        Valuation v = new Valuation();
        v.setPropertyId(property.getPropertyId());
        v.setCalculationDate(LocalDateTime.now());
        v.setEstimatedFmv(25_000_000);
        v.setConfidenceIntervalLow(22_000_000);
        v.setConfidenceIntervalHigh(28_000_000);
        v.setKeyFactors(Arrays.asList(
                "Area: 2250 sq.ft. (+12% impact)",
                "Locality: DHA Phase 6 (+18% premium)",
                "Bedrooms: 4 (+8% impact)"
        ));
        return v;
    }

    @Override
    public List<Property> findComparables(Property property) {
        Property p1 = new Property("comp-1", "Karachi", "DHA Phase 6", "house",
                2100, 23_500_000, 4, 3, LocalDate.of(2025, 6, 15), 24.81, 67.03, "hash1");
        Property p2 = new Property("comp-2", "Karachi", "DHA Phase 6", "house",
                2400, 27_000_000, 5, 3, LocalDate.of(2025, 5, 20), 24.82, 67.04, "hash2");
        Property p3 = new Property("comp-3", "Karachi", "DHA Phase 6", "house",
                2000, 22_000_000, 3, 2, LocalDate.of(2025, 4, 10), 24.80, 67.02, "hash3");
        Property p4 = new Property("comp-4", "Karachi", "DHA Phase 5", "house",
                2300, 24_500_000, 4, 3, LocalDate.of(2025, 3, 5), 24.79, 67.01, "hash4");
        Property p5 = new Property("comp-5", "Karachi", "DHA Phase 6", "house",
                2500, 26_800_000, 4, 4, LocalDate.of(2025, 2, 28), 24.83, 67.05, "hash5");
        return Arrays.asList(p1, p2, p3, p4, p5);
    }
}
