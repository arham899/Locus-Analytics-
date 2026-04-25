package com.locus.service;

import com.locus.model.dto.HeatmapPoint;

import java.util.List;

/**
 * Service for generating property heatmap visualization data (UC-7).
 *
 * <p>Aggregates property data by locality and produces normalized weights
 * for rendering on a Google Maps heatmap layer.</p>
 */
public interface HeatmapService {

    /**
     * Generates heatmap data points for the given city and metric.
     *
     * @param city         city name (Karachi, Islamabad, or Lahore)
     * @param metric       the metric to visualize: "price_per_sqft", "listing_density",
     *                     or "rental_demand"
     * @param propertyType property type filter (optional — null for all types)
     * @return list of heatmap points with lat/lng, normalized weight (0–1), and locality
     */
    List<HeatmapPoint> getHeatmapData(String city, String metric, String propertyType);
}
