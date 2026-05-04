package com.locus.service;

import com.locus.dao.PropertyDAO;
import com.locus.exception.ValidationException;
import com.locus.model.Property;
import com.locus.model.dto.LocalityMetric;
import com.locus.model.dto.PagedResult;
import com.locus.model.dto.SearchFilter;
import com.locus.model.dto.TrendPoint;
import com.locus.service.impl.SearchServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SearchServiceImpl} (UC-4).
 *
 * @author Arham Manzoor (24i-0640)
 */
class SearchServiceTest {

    private SearchServiceImpl service;
    private StubPropertyDAO dao;

    @BeforeEach
    void setUp() {
        dao = new StubPropertyDAO();
        service = new SearchServiceImpl(dao);
    }

    // ── Results ────────────────────────────────────────────────────────

    @Test
    void search_emptyFilter_returnsAllResults() {
        dao.results = List.of(property("p1"), property("p2"), property("p3"));

        PagedResult<Property> result = service.search(new SearchFilter());

        assertEquals(3, result.getItems().size());
    }

    @Test
    void search_emptyDatabase_returnsEmptyPagedResult() {
        dao.results = Collections.emptyList();

        PagedResult<Property> result = service.search(new SearchFilter());

        assertTrue(result.getItems().isEmpty());
        assertEquals(0, result.getTotalCount());
    }

    @Test
    void search_filterPassedToDAO() {
        SearchFilter filter = new SearchFilter();
        filter.setCity("Karachi");
        filter.setPropertyType("apartment");
        dao.results = List.of(property("p1"));

        PagedResult<Property> result = service.search(filter);

        assertNotNull(result);
        // Verifies delegation to DAO without error
        assertEquals(1, result.getItems().size());
    }

    @Test
    void search_fullPage_totalCountEstimatedHigher() {
        SearchFilter filter = new SearchFilter();
        filter.setPageSize(3);
        filter.setPageNumber(1);
        // Return exactly pageSize results → more pages likely
        dao.results = List.of(property("p1"), property("p2"), property("p3"));

        PagedResult<Property> result = service.search(filter);

        assertTrue(result.getTotalCount() >= 3);
    }

    @Test
    void search_partialPage_totalCountEqualsResultSize() {
        SearchFilter filter = new SearchFilter();
        filter.setPageSize(10);
        dao.results = List.of(property("p1"), property("p2")); // < pageSize

        PagedResult<Property> result = service.search(filter);

        assertEquals(2, result.getTotalCount());
    }

    @Test
    void search_paginationFieldsPreserved() {
        SearchFilter filter = new SearchFilter();
        filter.setPageNumber(2);
        filter.setPageSize(5);
        dao.results = Collections.emptyList();

        PagedResult<Property> result = service.search(filter);

        assertEquals(2, result.getPageNumber());
        assertEquals(5, result.getPageSize());
    }

    // ── Validation ─────────────────────────────────────────────────────

    @Test
    void search_nullFilter_throwsValidationException() {
        assertThrows(ValidationException.class, () -> service.search(null));
    }

    @Test
    void search_minPriceGreaterThanMaxPrice_throwsValidationException() {
        SearchFilter filter = new SearchFilter();
        filter.setMinPrice(5_000_000.0);
        filter.setMaxPrice(1_000_000.0);

        assertThrows(ValidationException.class, () -> service.search(filter));
    }

    @Test
    void search_minAreaGreaterThanMaxArea_throwsValidationException() {
        SearchFilter filter = new SearchFilter();
        filter.setMinArea(5000.0);
        filter.setMaxArea(1000.0);

        assertThrows(ValidationException.class, () -> service.search(filter));
    }

    @Test
    void search_equalMinMaxPrice_isValid() {
        SearchFilter filter = new SearchFilter();
        filter.setMinPrice(5_000_000.0);
        filter.setMaxPrice(5_000_000.0);
        dao.results = Collections.emptyList();

        assertDoesNotThrow(() -> service.search(filter));
    }

    // ─────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────

    private static Property property(String id) {
        Property p = new Property();
        p.setPropertyId(id);
        return p;
    }

    // ─────────────────────────────────────────────────────────────────
    // STUB DAO
    // ─────────────────────────────────────────────────────────────────

    static class StubPropertyDAO implements PropertyDAO {
        List<Property> results = Collections.emptyList();

        @Override public List<Property> search(SearchFilter f) { return new ArrayList<>(results); }
        @Override public int countByFilter(SearchFilter f) { return results.size(); }
        @Override public Optional<Property> findById(String id) { return Optional.empty(); }
        @Override public List<Property> findComparables(String city, String loc, String type, double area) { return Collections.emptyList(); }
        @Override public boolean insert(Property p) { return true; }
        @Override public boolean update(Property p) { return true; }
        @Override public boolean delete(String id) { return true; }
        @Override public List<Property> findByLocality(String city, String loc) { return Collections.emptyList(); }
        @Override public List<TrendPoint> findAggregatedByMonth(String city, String loc, String type, LocalDate s, LocalDate e) { return Collections.emptyList(); }
        @Override public List<LocalityMetric> getLocalityMetrics(String city, int years, int min) { return Collections.emptyList(); }
        @Override public List<String> findDistinctCities() { return Collections.emptyList(); }
        @Override public List<String> findDistinctLocalities(String city) { return Collections.emptyList(); }
    }
}
