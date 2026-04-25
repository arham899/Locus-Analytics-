package com.locus.service;

import com.locus.model.Property;
import com.locus.model.dto.PagedResult;
import com.locus.model.dto.SearchFilter;

/**
 * Service for searching properties with flexible filters (UC-4).
 *
 * <p>Supports filtering by city, locality, type, price range, area range,
 * bedrooms, bathrooms, with pagination and sorting.</p>
 */
public interface SearchService {

    /**
     * Searches properties matching the given filter criteria.
     *
     * @param filter search criteria with pagination and sorting options
     * @return paginated result containing matching properties and total count
     * @throws com.locus.exception.ValidationException if filter ranges are invalid
     *         (e.g. minPrice &gt; maxPrice)
     */
    PagedResult<Property> search(SearchFilter filter);
}
