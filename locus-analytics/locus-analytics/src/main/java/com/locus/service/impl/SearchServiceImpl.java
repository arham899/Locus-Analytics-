package com.locus.service.impl;

import com.locus.dao.PropertyDAO;
import com.locus.model.Property;
import com.locus.model.dto.PagedResult;
import com.locus.model.dto.SearchFilter;
import com.locus.service.SearchService;
import com.locus.service.validation.InputValidator;

import java.util.List;

/**
 * Real implementation of {@link SearchService} (UC-4).
 *
 * <p>Validates filter ranges, delegates to PropertyDAO.search(),
 * and wraps results in a PagedResult.</p>
 *
 * @author Arham Manzoor (24i-0640)
 */
public class SearchServiceImpl implements SearchService {

    private final PropertyDAO propertyDAO;

    public SearchServiceImpl(PropertyDAO propertyDAO) {
        this.propertyDAO = propertyDAO;
    }

    @Override
    public PagedResult<Property> search(SearchFilter filter) {

        // ── Validation ──────────────────────────────
        new InputValidator()
                .validateNotNull("filter", filter)
                .throwIfInvalid();

        new InputValidator()
                .validateRange("price", filter.getMinPrice(), filter.getMaxPrice())
                .validateRange("area", filter.getMinArea(), filter.getMaxArea())
                .throwIfInvalid();

        // ── Execute search via DAO ──────────────────
        List<Property> results = propertyDAO.search(filter);
        int totalCount = propertyDAO.countByFilter(filter);

        return new PagedResult<>(
                results,
                totalCount,
                filter.getPageNumber(),
                filter.getPageSize()
        );
    }
}
