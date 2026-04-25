package com.locus.model.dto;

import com.locus.model.Property;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Result of comparing 2–4 properties side-by-side (UC-5).
 *
 * <p>Contains the list of compared properties and a map indicating which property
 * is best/worst for each comparison metric (price, area, price per sq.ft., etc.).</p>
 */
public class ComparisonResult {

    /**
     * Enum indicating whether a property is the best or worst for a given metric.
     */
    public enum BestWorstFlag {
        BEST,
        WORST,
        NEUTRAL
    }

    private List<Property> properties;

    /**
     * Map from metric name (e.g. "price", "area", "pricePerSqft") to a nested map
     * of propertyId → BestWorstFlag.
     */
    private Map<String, Map<String, BestWorstFlag>> rankings;

    // ── Constructors ──────────────────────────────────────────────────

    public ComparisonResult() {
        this.rankings = new HashMap<>();
    }

    public ComparisonResult(List<Property> properties,
                            Map<String, Map<String, BestWorstFlag>> rankings) {
        this.properties = properties;
        this.rankings = rankings;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public Map<String, Map<String, BestWorstFlag>> getRankings() {
        return rankings;
    }

    public void setRankings(Map<String, Map<String, BestWorstFlag>> rankings) {
        this.rankings = rankings;
    }

    /**
     * Convenience method to get the flag for a specific metric and property.
     */
    public BestWorstFlag getFlag(String metric, String propertyId) {
        Map<String, BestWorstFlag> metricMap = rankings.get(metric);
        if (metricMap == null) return BestWorstFlag.NEUTRAL;
        return metricMap.getOrDefault(propertyId, BestWorstFlag.NEUTRAL);
    }

    @Override
    public String toString() {
        return "ComparisonResult{" +
                "propertyCount=" + (properties != null ? properties.size() : 0) +
                ", metrics=" + rankings.keySet() +
                '}';
    }
}
