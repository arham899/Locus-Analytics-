package com.locus.dao;

import com.locus.model.Property;
import com.locus.model.SearchFilter;
import com.locus.model.TrendPoint;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PropertyDAO {

    /**
     * Fetch a property by its unique ID.
     *
     * @param propertyId unique identifier of property
     * @return Property wrapped in Optional if found, otherwise empty
     * @throws DataAccessException if database error occurs
     */
    Optional<Property> findById(String propertyId);

    /**
     * Search properties using flexible filters.
     * Supports city, locality, price range, area range, type, etc.
     *
     * @param filter search criteria object
     * @return list of matching properties
     * @throws DataAccessException if query execution fails
     */
    List<Property> search(SearchFilter filter);

    /**
     * Find comparable properties for valuation model.
     *
     * @param city city of target property
     * @param locality locality of target property
     * @param propertyType type of property (house, apartment, etc.)
     * @param area area in sq.ft.
     * @return list of comparable properties
     * @throws DataAccessException if query fails
     */
    List<Property> findComparables(String city, String locality, String propertyType, double area);

    /**
     * Insert a new property into database.
     *
     * @param property property object to insert
     * @return true if insert successful
     * @throws DataAccessException if constraint violation or DB error occurs
     */
    boolean insert(Property property);

    /**
     * Update existing property.
     *
     * @param property updated property object
     * @return true if update successful
     */
    boolean update(Property property);

    /**
     * Delete property by ID.
     *
     * @param propertyId id of property
     * @return true if deleted
     */
    boolean delete(String propertyId);

    /**
     * Get all properties in a locality.
     *
     * @param city city name
     * @param locality locality name
     * @return list of properties
     */
    List<Property> findByLocality(String city, String locality);

    /**
     * Aggregate property data by month (for UC-6 trend analysis).
     *
     * @param city city filter
     * @param locality locality filter (optional)
     * @param propertyType type filter (optional)
     * @param startDate start date
     * @param endDate end date
     * @return aggregated monthly statistics
     */
    List<TrendPoint> findAggregatedByMonth(
            String city,
            String locality,
            String propertyType,
            LocalDate startDate,
            LocalDate endDate
    );
}