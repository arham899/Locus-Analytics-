package com.locus.model.dto;

/**
 * Data Transfer Object for property search filters.
 * Supports filtering, pagination, and sorting for the Search Properties use case (UC-4).
 *
 * <p>Nullable wrapper types (Double, Integer) are used so that un-set filters
 * can be detected as {@code null} and excluded from the dynamic WHERE clause.</p>
 */
public class SearchFilter {

    private String city;
    private String locality;
    private String propertyType;
    private Double minPrice;
    private Double maxPrice;
    private Double minArea;
    private Double maxArea;
    private Integer bedrooms;
    private Integer bathrooms;
    private int pageNumber = 1;
    private int pageSize = 20;
    private String sortBy = "listing_date";
    private String sortOrder = "DESC";

    // ── Constructors ──────────────────────────────────────────────────

    public SearchFilter() {
    }

    // ── Getters & Setters ─────────────────────────────────────────────

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

    public Double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Double minPrice) {
        this.minPrice = minPrice;
    }

    public Double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Double getMinArea() {
        return minArea;
    }

    public void setMinArea(Double minArea) {
        this.minArea = minArea;
    }

    public Double getMaxArea() {
        return maxArea;
    }

    public void setMaxArea(Double maxArea) {
        this.maxArea = maxArea;
    }

    public Integer getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(Integer bedrooms) {
        this.bedrooms = bedrooms;
    }

    public Integer getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(Integer bathrooms) {
        this.bathrooms = bathrooms;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * Computes the SQL OFFSET value from pageNumber and pageSize.
     */
    public int getOffset() {
        return (pageNumber - 1) * pageSize;
    }

    @Override
    public String toString() {
        return "SearchFilter{" +
                "city='" + city + '\'' +
                ", locality='" + locality + '\'' +
                ", type='" + propertyType + '\'' +
                ", price=[" + minPrice + "–" + maxPrice + "]" +
                ", area=[" + minArea + "–" + maxArea + "]" +
                ", beds=" + bedrooms +
                ", baths=" + bathrooms +
                ", page=" + pageNumber +
                ", size=" + pageSize +
                ", sort=" + sortBy + " " + sortOrder +
                '}';
    }
}
