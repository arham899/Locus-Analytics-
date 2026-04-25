package com.locus.service.impl;

import com.locus.model.Property;
import com.locus.model.dto.PagedResult;
import com.locus.model.dto.SearchFilter;
import com.locus.service.SearchService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Stub implementation of {@link SearchService} returning hardcoded sample results.
 */
public class SearchServiceStub implements SearchService {

    @Override
    public PagedResult<Property> search(SearchFilter filter) {
        List<Property> results = new ArrayList<>();

        results.add(new Property("p-001", "Karachi", "DHA Phase 6", "house",
                2250, 25_000_000, 4, 3, LocalDate.of(2025, 8, 10), 24.81, 67.03, "h1"));
        results.add(new Property("p-002", "Karachi", "Clifton", "apartment",
                1200, 12_500_000, 3, 2, LocalDate.of(2025, 7, 22), 24.86, 67.02, "h2"));
        results.add(new Property("p-003", "Islamabad", "F-7", "house",
                4500, 75_000_000, 6, 5, LocalDate.of(2025, 6, 15), 33.72, 73.04, "h3"));
        results.add(new Property("p-004", "Lahore", "DHA Phase 5", "house",
                3600, 42_000_000, 5, 4, LocalDate.of(2025, 5, 30), 31.37, 74.22, "h4"));
        results.add(new Property("p-005", "Karachi", "Gulshan-e-Iqbal", "apartment",
                900, 8_500_000, 2, 2, LocalDate.of(2025, 9, 1), 24.92, 67.08, "h5"));

        // Filter by city if provided
        if (filter.getCity() != null && !filter.getCity().isBlank()) {
            results.removeIf(p -> !p.getCity().equalsIgnoreCase(filter.getCity()));
        }

        return new PagedResult<>(results, results.size(), filter.getPageNumber(), filter.getPageSize());
    }
}
