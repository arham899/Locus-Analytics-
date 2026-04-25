package com.locus.service.impl;

import com.locus.model.Property;
import com.locus.model.dto.ComparisonResult;
import com.locus.model.dto.ComparisonResult.BestWorstFlag;
import com.locus.service.CompareService;

import java.time.LocalDate;
import java.util.*;

/**
 * Stub implementation of {@link CompareService} returning hardcoded comparison data.
 */
public class CompareServiceStub implements CompareService {

    @Override
    public ComparisonResult compare(List<String> propertyIds) {
        // Generate stub properties matching the requested IDs
        List<Property> properties = new ArrayList<>();
        double[] prices = {25_000_000, 18_000_000, 32_000_000, 22_000_000};
        double[] areas = {2250, 1800, 3200, 2000};
        String[] localities = {"DHA Phase 6", "Clifton", "F-7", "DHA Phase 5"};
        String[] cities = {"Karachi", "Karachi", "Islamabad", "Lahore"};

        for (int i = 0; i < Math.min(propertyIds.size(), 4); i++) {
            properties.add(new Property(propertyIds.get(i), cities[i], localities[i], "house",
                    areas[i], prices[i], 3 + i, 2 + (i % 2),
                    LocalDate.of(2025, 6 + i, 10), 24.8 + i * 0.1, 67.0 + i * 0.1, "ch" + i));
        }

        // Compute best/worst for key metrics
        Map<String, Map<String, BestWorstFlag>> rankings = new HashMap<>();
        computeRanking(rankings, "price", properties, true);         // lower is better
        computeRanking(rankings, "area", properties, false);         // higher is better
        computeRanking(rankings, "pricePerSqft", properties, true);  // lower is better

        return new ComparisonResult(properties, rankings);
    }

    private void computeRanking(Map<String, Map<String, BestWorstFlag>> rankings,
                                String metric, List<Property> properties, boolean lowerIsBetter) {
        Map<String, BestWorstFlag> flags = new HashMap<>();
        double best = lowerIsBetter ? Double.MAX_VALUE : Double.MIN_VALUE;
        double worst = lowerIsBetter ? Double.MIN_VALUE : Double.MAX_VALUE;
        String bestId = null, worstId = null;

        for (Property p : properties) {
            double val = getMetricValue(p, metric);
            if ((lowerIsBetter && val < best) || (!lowerIsBetter && val > best)) {
                best = val;
                bestId = p.getPropertyId();
            }
            if ((lowerIsBetter && val > worst) || (!lowerIsBetter && val < worst)) {
                worst = val;
                worstId = p.getPropertyId();
            }
        }

        for (Property p : properties) {
            if (p.getPropertyId().equals(bestId)) {
                flags.put(p.getPropertyId(), BestWorstFlag.BEST);
            } else if (p.getPropertyId().equals(worstId)) {
                flags.put(p.getPropertyId(), BestWorstFlag.WORST);
            } else {
                flags.put(p.getPropertyId(), BestWorstFlag.NEUTRAL);
            }
        }
        rankings.put(metric, flags);
    }

    private double getMetricValue(Property p, String metric) {
        switch (metric) {
            case "price": return p.getPrice();
            case "area": return p.getArea();
            case "pricePerSqft": return p.getPrice() / p.getArea();
            default: return 0;
        }
    }
}
