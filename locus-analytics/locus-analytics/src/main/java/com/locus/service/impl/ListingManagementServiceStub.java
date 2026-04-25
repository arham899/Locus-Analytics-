package com.locus.service.impl;

import com.locus.model.Property;
import com.locus.model.dto.PagedResult;
import com.locus.model.dto.SearchFilter;
import com.locus.service.ListingManagementService;

/**
 * Stub implementation of {@link ListingManagementService}.
 */
public class ListingManagementServiceStub implements ListingManagementService {

    private final SearchServiceStub searchStub = new SearchServiceStub();

    @Override
    public Property addListing(Property property) {
        System.out.println("[STUB] Added listing: " + property.getPropertyId());
        return property;
    }

    @Override
    public Property updateListing(Property property) {
        System.out.println("[STUB] Updated listing: " + property.getPropertyId());
        return property;
    }

    @Override
    public boolean deleteListing(String propertyId) {
        System.out.println("[STUB] Deleted listing: " + propertyId);
        return true;
    }

    @Override
    public PagedResult<Property> searchListings(SearchFilter filter) {
        return searchStub.search(filter);
    }
}
