package com.locus.service.impl;

import com.locus.dao.PropertyDAO;
import com.locus.model.Property;
import com.locus.model.dto.PagedResult;
import com.locus.model.dto.SearchFilter;
import com.locus.service.ListingManagementService;
import com.locus.service.validation.InputValidator;

import java.util.UUID;

/**
 * Real implementation of {@link ListingManagementService}.
 */
public class ListingManagementServiceImpl implements ListingManagementService {

    private final PropertyDAO propertyDAO;

    public ListingManagementServiceImpl(PropertyDAO propertyDAO) {
        this.propertyDAO = propertyDAO;
    }

    @Override
    public Property addListing(Property property) {
        // Validate
        new InputValidator()
                .validateCity(property.getCity())
                .validatePropertyType(property.getPropertyType())
                .validatePositive("area", property.getArea())
                .validateNonNegative("price", property.getPrice())
                .validatePositive("bedrooms", property.getBedrooms())
                .validatePositive("bathrooms", property.getBathrooms())
                .throwIfInvalid();

        // Assign unique ID if new
        if (property.getPropertyId() == null || property.getPropertyId().isEmpty()) {
            property.setPropertyId("list-" + UUID.randomUUID().toString().substring(0, 8));
        }

        propertyDAO.insert(property);
        return property;
    }

    @Override
    public Property updateListing(Property property) {
        new InputValidator()
                .validateNotNull("propertyId", property.getPropertyId())
                .throwIfInvalid();

        // Ensure property exists
        propertyDAO.findById(property.getPropertyId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Listing not found: " + property.getPropertyId()));

        propertyDAO.update(property);
        return property;
    }

    @Override
    public boolean deleteListing(String propertyId) {
        new InputValidator()
                .validateNotNull("propertyId", propertyId)
                .throwIfInvalid();
        return propertyDAO.delete(propertyId);
    }

    @Override
    public PagedResult<Property> searchListings(SearchFilter filter) {
        // Reuse search logic from PropertyDAO
        java.util.List<Property> results = propertyDAO.search(filter);
        return new PagedResult<>(
                results,
                results.size(),
                filter.getPageNumber(),
                filter.getPageSize()
        );
    }
}
