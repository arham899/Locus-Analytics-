package com.locus.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a real estate property listing in the LOCUS Analytics system.
 * Maps to the {@code property} table in the database.
 *
 * <p>The {@code pricePerSqft} field is a derived (generated) column in the database,
 * computed as {@code price / area}. In Java, it is kept in sync via the setter or
 * recalculated on demand.</p>
 */
public class Property {

    private String propertyId;
    private String city;
    private String locality;
    private String propertyType;
    private double area;
    private double price;
    private int bedrooms;
    private int bathrooms;
    private LocalDate listingDate;
    private double latitude;
    private double longitude;
    private double pricePerSqft;
    private String urlHash;
    private List<String> amenities;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Constructors ──────────────────────────────────────────────────

    /** Default no-arg constructor. Generates a random UUID for propertyId. */
    public Property() {
        this.propertyId = UUID.randomUUID().toString();
        this.amenities = new ArrayList<>();
    }

    /** Full constructor matching all database columns (except generated pricePerSqft). */
    public Property(String propertyId, String city, String locality, String propertyType,
                    double area, double price, int bedrooms, int bathrooms,
                    LocalDate listingDate, double latitude, double longitude,
                    String urlHash) {
        this.propertyId = propertyId;
        this.city = city;
        this.locality = locality;
        this.propertyType = propertyType;
        this.area = area;
        this.price = price;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.listingDate = listingDate;
        this.latitude = latitude;
        this.longitude = longitude;
        this.urlHash = urlHash;
        this.amenities = new ArrayList<>();
        this.pricePerSqft = (area > 0) ? price / area : 0;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(int bedrooms) {
        this.bedrooms = bedrooms;
    }

    public int getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(int bathrooms) {
        this.bathrooms = bathrooms;
    }

    public LocalDate getListingDate() {
        return listingDate;
    }

    public void setListingDate(LocalDate listingDate) {
        this.listingDate = listingDate;
    }

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

    public double getPricePerSqft() {
        return pricePerSqft;
    }

    public void setPricePerSqft(double pricePerSqft) {
        this.pricePerSqft = pricePerSqft;
    }

    public String getUrlHash() {
        return urlHash;
    }

    public void setUrlHash(String urlHash) {
        this.urlHash = urlHash;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ── equals / hashCode / toString ──────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Property property = (Property) o;
        return Objects.equals(propertyId, property.propertyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(propertyId);
    }

    @Override
    public String toString() {
        return "Property{" +
                "propertyId='" + propertyId + '\'' +
                ", city='" + city + '\'' +
                ", locality='" + locality + '\'' +
                ", propertyType='" + propertyType + '\'' +
                ", area=" + area +
                ", price=" + price +
                ", bedrooms=" + bedrooms +
                ", bathrooms=" + bathrooms +
                ", listingDate=" + listingDate +
                ", pricePerSqft=" + pricePerSqft +
                '}';
    }
}