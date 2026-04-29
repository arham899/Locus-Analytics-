package com.locus.service.impl;

import com.locus.dao.PropertyDAO;
import com.locus.dao.ValuationDAO;
import com.locus.model.Property;
import com.locus.model.Valuation;
import com.locus.model.dto.ComparisonResult;
import com.locus.model.dto.ComparisonResult.BestWorstFlag;
import com.locus.service.CompareService;
import com.locus.service.validation.InputValidator;

import java.util.*;
import java.util.function.ToDoubleFunction;

/**
 * Real implementation of {@link CompareService} (UC-5).
 *
 * <p>Compares 2–4 properties side-by-side, computing best/worst flags
 * for each numeric metric. Cross-references existing FMV valuations.</p>
 *
 * @author Arham Manzoor (24i-0640)
 */
public class CompareServiceImpl implements CompareService {

    private final PropertyDAO propertyDAO;
    private final ValuationDAO valuationDAO;

    public CompareServiceImpl(PropertyDAO propertyDAO, ValuationDAO valuationDAO) {
        this.propertyDAO = propertyDAO;
        this.valuationDAO = valuationDAO;
    }

    @Override
    public ComparisonResult compare(List<String> propertyIds) {

        // ── Validation ──────────────────────────────
        new InputValidator()
                .validateListSize("propertyIds", propertyIds, 2, 4)
                .throwIfInvalid();

        // ── Fetch properties ────────────────────────
        List<Property> properties = new ArrayList<>();
        for (String id : propertyIds) {
            propertyDAO.findById(id).ifPresent(properties::add);
        }

        if (properties.size() < 2) {
            throw new com.locus.exception.ValidationException(
                    "Could not find enough properties to compare"
            );
        }

        // ── Compute rankings ────────────────────────
        Map<String, Map<String, BestWorstFlag>> rankings = new LinkedHashMap<>();

        // Price: lower is BEST (more affordable)
        rankings.put("price", computeRanking(properties, Property::getPrice, true));

        // Area: higher is BEST (more space)
        rankings.put("area", computeRanking(properties, Property::getArea, false));

        // Price per sq.ft.: lower is BEST (better value)
        rankings.put("pricePerSqft", computeRanking(properties, Property::getPricePerSqft, true));

        // Bedrooms: higher is BEST
        rankings.put("bedrooms", computeRanking(properties, p -> p.getBedrooms(), false));

        // Bathrooms: higher is BEST
        rankings.put("bathrooms", computeRanking(properties, p -> p.getBathrooms(), false));

        // ── Cross-reference FMV (optional) ──────────
        // Try to fetch existing valuations for each property
        for (Property p : properties) {
            try {
                Valuation v = valuationDAO.findByPropertyId(p.getPropertyId());
                if (v != null) {
                    // FMV data exists — could enhance comparison
                    // For now, we just log it
                    System.out.println("[CompareService] FMV available for " +
                            p.getPropertyId() + ": " + v.getEstimatedFmv());
                }
            } catch (Exception e) {
                // No valuation found — that's fine
            }
        }

        return new ComparisonResult(properties, rankings);
    }

    /**
     * Computes BEST/WORST/NEUTRAL flags for a single metric.
     *
     * @param properties   the properties to compare
     * @param extractor    function to extract the metric value
     * @param lowerIsBest  if true, the lowest value is BEST; otherwise highest is BEST
     * @return map of propertyId → BestWorstFlag
     */
    private Map<String, BestWorstFlag> computeRanking(
            List<Property> properties,
            ToDoubleFunction<Property> extractor,
            boolean lowerIsBest) {

        Map<String, BestWorstFlag> result = new LinkedHashMap<>();

        double bestVal = lowerIsBest ? Double.MAX_VALUE : Double.MIN_VALUE;
        double worstVal = lowerIsBest ? Double.MIN_VALUE : Double.MAX_VALUE;
        String bestId = null;
        String worstId = null;

        // Find best and worst
        for (Property p : properties) {
            double val = extractor.applyAsDouble(p);
            if (lowerIsBest) {
                if (val < bestVal) { bestVal = val; bestId = p.getPropertyId(); }
                if (val > worstVal) { worstVal = val; worstId = p.getPropertyId(); }
            } else {
                if (val > bestVal) { bestVal = val; bestId = p.getPropertyId(); }
                if (val < worstVal) { worstVal = val; worstId = p.getPropertyId(); }
            }
        }

        // Assign flags
        for (Property p : properties) {
            String id = p.getPropertyId();
            if (id.equals(bestId)) {
                result.put(id, BestWorstFlag.BEST);
            } else if (id.equals(worstId)) {
                result.put(id, BestWorstFlag.WORST);
            } else {
                result.put(id, BestWorstFlag.NEUTRAL);
            }
        }

        return result;
    }
}
