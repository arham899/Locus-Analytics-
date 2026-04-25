package com.locus.service.impl;

import com.locus.model.dto.HeatmapPoint;
import com.locus.service.HeatmapService;

import java.util.ArrayList;
import java.util.List;

/**
 * Stub implementation of {@link HeatmapService} returning hardcoded heatmap data.
 */
public class HeatmapServiceStub implements HeatmapService {

    @Override
    public List<HeatmapPoint> getHeatmapData(String city, String metric, String propertyType) {
        List<HeatmapPoint> points = new ArrayList<>();

        if ("Karachi".equalsIgnoreCase(city)) {
            points.add(new HeatmapPoint(24.8607, 67.0011, 0.95, "DHA Phase 6"));
            points.add(new HeatmapPoint(24.8534, 67.0184, 0.88, "DHA Phase 5"));
            points.add(new HeatmapPoint(24.8660, 67.0099, 0.72, "Clifton"));
            points.add(new HeatmapPoint(24.9262, 67.0845, 0.55, "Gulshan-e-Iqbal"));
            points.add(new HeatmapPoint(24.9400, 67.0900, 0.40, "Gulistan-e-Johar"));
            points.add(new HeatmapPoint(24.8930, 67.0280, 0.65, "PECHS"));
            points.add(new HeatmapPoint(24.8750, 67.0650, 0.82, "Bahria Town Karachi"));
        } else if ("Islamabad".equalsIgnoreCase(city)) {
            points.add(new HeatmapPoint(33.7294, 73.0931, 0.92, "F-7"));
            points.add(new HeatmapPoint(33.7167, 73.0567, 0.85, "F-6"));
            points.add(new HeatmapPoint(33.6844, 73.0479, 0.78, "E-11"));
            points.add(new HeatmapPoint(33.6600, 73.0100, 0.90, "DHA Islamabad"));
            points.add(new HeatmapPoint(33.5651, 73.1571, 0.88, "Bahria Town Islamabad"));
            points.add(new HeatmapPoint(33.6938, 73.0300, 0.45, "I-8"));
        } else if ("Lahore".equalsIgnoreCase(city)) {
            points.add(new HeatmapPoint(31.3690, 74.2140, 0.90, "DHA Phase 5"));
            points.add(new HeatmapPoint(31.3770, 74.2220, 0.87, "DHA Phase 6"));
            points.add(new HeatmapPoint(31.5204, 74.3587, 0.70, "Gulberg"));
            points.add(new HeatmapPoint(31.4500, 74.3900, 0.60, "Johar Town"));
            points.add(new HeatmapPoint(31.3750, 74.1800, 0.85, "Bahria Town Lahore"));
            points.add(new HeatmapPoint(31.5100, 74.3400, 0.55, "Model Town"));
        }

        return points;
    }
}
