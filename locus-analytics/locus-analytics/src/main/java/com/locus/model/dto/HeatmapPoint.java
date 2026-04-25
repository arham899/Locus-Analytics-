package com.locus.model.dto;

/**
 * Represents a single data point for the property heatmap visualization (UC-7).
 *
 * <p>Contains geographic coordinates and a normalized weight value (0–1)
 * used to render the Google Maps heatmap layer gradient.</p>
 */
public class HeatmapPoint {

    private double latitude;
    private double longitude;
    private double weight;
    private String locality;

    // ── Constructors ──────────────────────────────────────────────────

    public HeatmapPoint() {
    }

    public HeatmapPoint(double latitude, double longitude, double weight, String locality) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.weight = weight;
        this.locality = locality;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    @Override
    public String toString() {
        return "HeatmapPoint{" +
                "lat=" + latitude +
                ", lng=" + longitude +
                ", weight=" + weight +
                ", locality='" + locality + '\'' +
                '}';
    }
}
