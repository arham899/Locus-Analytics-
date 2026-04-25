package com.locus.service;

import com.locus.model.Property;
import com.locus.model.dto.PagedResult;
import com.locus.model.dto.SearchFilter;

/**
 * Service for CRUD management of property listings (UC-11).
 *
 * <p>Wraps PropertyDAO operations with business-level validation
 * and cascade verification for deletions.</p>
 */
public interface ListingManagementService {

    /**
     * Adds a new property listing.
     *
     * @param property the property to add
     * @return the created property with generated ID
     * @throws com.locus.exception.ValidationException if property fields are invalid
     */
    Property addListing(Property property);

    /**
     * Updates an existing property listing.
     *
     * @param property the property with updated fields
     * @return the updated property
     * @throws com.locus.exception.ValidationException if property not found or fields invalid
     */
    Property updateListing(Property property);

    /**
     * Deletes a property listing by ID.
     * Verifies no dependent records exist before deletion.
     *
     * @param propertyId the property ID to delete
     * @return true if successfully deleted
     * @throws com.locus.exception.ValidationException if property has dependent records
     */
    boolean deleteListing(String propertyId);

    /**
     * Searches listings using the given filter (delegates to SearchService internally).
     *
     * @param filter search criteria
     * @return paginated listing results
     */
    PagedResult<Property> searchListings(SearchFilter filter);
}
