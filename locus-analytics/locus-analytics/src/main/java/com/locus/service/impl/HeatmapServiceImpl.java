package com.locus.service.impl;

import com.locus.dao.PropertyDAO;
import com.locus.model.Property;
import com.locus.model.dto.HeatmapPoint;
import com.locus.model.dto.SearchFilter;
import com.locus.service.HeatmapService;
import com.locus.service.validation.InputValidator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Real implementation of {@link HeatmapService} (UC-7).
 *
 * <p>Fetches geocoded properties from the database, groups by locality,
 * computes the requested metric per locality, normalizes weights to 0–1,
 * and returns data points for Google Maps heatmap rendering.</p>
 *
 * @author Arham Manzoor (24i-0640)
 */
public class HeatmapServiceImpl implements HeatmapService {

    private final PropertyDAO propertyDAO;

    public HeatmapServiceImpl(PropertyDAO propertyDAO) {
        this.propertyDAO = propertyDAO;
    }

    @Override
    public List<HeatmapPoint> getHeatmapData(String city, String metric, String propertyType) {

        // ── Validation ──────────────────────────────
        new InputValidator()
                .validateCity(city)
                .validateNotBlank("metric", metric)
                .throwIfInvalid();

        // ── Fetch all properties for the city ───────
        SearchFilter filter = new SearchFilter();
        filter.setCity(city);
        if (propertyType != null && !propertyType.isBlank()) {
            filter.setPropertyType(propertyType);
        }
        filter.setPageSize(10000); // Get all properties for heatmap

        List<Property> properties = propertyDAO.search(filter);

        if (properties.isEmpty()) {
            return Collections.emptyList();
        }

        // ── Group by locality ───────────────────────
        Map<String, List<Property>> byLocality = properties.stream()
                .filter(p -> p.getLocality() != null)
                .collect(Collectors.groupingBy(Property::getLocality));

        // ── Compute metric per locality ─────────────
        Map<String, Double> localityMetrics = new LinkedHashMap<>();
        Map<String, double[]> localityCoords = new LinkedHashMap<>(); // lat, lng averages

        for (Map.Entry<String, List<Property>> entry : byLocality.entrySet()) {
            String locality = entry.getKey();
            List<Property> localityProps = entry.getValue();

            double metricValue = computeMetric(localityProps, metric);
            localityMetrics.put(locality, metricValue);

            // Average lat/lng for the locality
            double avgLat = localityProps.stream().mapToDouble(Property::getLatitude).average().orElse(0);
            double avgLng = localityProps.stream().mapToDouble(Property::getLongitude).average().orElse(0);
            localityCoords.put(locality, new double[]{avgLat, avgLng});
        }

        // ── Normalize to 0–1 ────────────────────────
        double maxMetric = localityMetrics.values().stream().mapToDouble(v -> v).max().orElse(1);
        double minMetric = localityMetrics.values().stream().mapToDouble(v -> v).min().orElse(0);
        double range = maxMetric - minMetric;

        // ── Build heatmap points ────────────────────
        List<HeatmapPoint> points = new ArrayList<>();
        for (String locality : localityMetrics.keySet()) {
            double raw = localityMetrics.get(locality);
            double weight = (range > 0) ? (raw - minMetric) / range : 0.5;

            double[] coords = localityCoords.get(locality);
            if (coords[0] != 0 && coords[1] != 0) { // Only include geocoded localities
                points.add(new HeatmapPoint(coords[0], coords[1], weight, locality));
            }
        }

        // Sort by weight descending
        points.sort(Comparator.comparingDouble(HeatmapPoint::getWeight).reversed());

        return points;
    }

    /**
     * Computes the requested metric for a list of properties in the same locality.
     */
    private double computeMetric(List<Property> properties, String metric) {
        return switch (metric.toLowerCase()) {
            case "price_per_sqft" -> properties.stream()
                    .filter(p -> p.getArea() > 0)
                    .mapToDouble(p -> p.getPrice() / p.getArea())
                    .average().orElse(0);

            case "listing_density" -> (double) properties.size();

            case "rental_demand" ->
                // Placeholder — would need rental data
                // For now, use listing count as a proxy
                    (double) properties.size();

            default -> (double) properties.size();
        };
    }
}
