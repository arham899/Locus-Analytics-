package com.locus.service;

import com.locus.dao.PropertyDAO;
import com.locus.dao.ValuationDAO;
import com.locus.exception.ValidationException;
import com.locus.model.Property;
import com.locus.model.Valuation;
import com.locus.model.dto.ComparisonResult;
import com.locus.model.dto.ComparisonResult.BestWorstFlag;
import com.locus.model.dto.LocalityMetric;
import com.locus.model.dto.SearchFilter;
import com.locus.model.dto.TrendPoint;
import com.locus.service.impl.CompareServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CompareServiceImpl} (UC-5).
 *
 * @author Arham Manzoor (24i-0640)
 */
class CompareServiceTest {

    private CompareServiceImpl service;
    private StubPropertyDAO dao;

    @BeforeEach
    void setUp() {
        dao = new StubPropertyDAO();
        service = new CompareServiceImpl(dao, new StubValuationDAO());
    }

    // ── Valid comparisons ──────────────────────────────────────────────

    @Test
    void compare_twoProperties_returnsComparisonResult() {
        Property cheap = property("p1", 5_000_000, 1000, 2, 2);
        Property pricey = property("p2", 20_000_000, 3000, 4, 4);
        dao.store("p1", cheap);
        dao.store("p2", pricey);

        ComparisonResult result = service.compare(List.of("p1", "p2"));

        assertEquals(2, result.getProperties().size());
        assertNotNull(result.getRankings());
    }

    @Test
    void compare_twoProperties_priceRankingCorrect() {
        // Lower price is BEST
        dao.store("cheap", property("cheap", 5_000_000, 1000, 2, 2));
        dao.store("pricey", property("pricey", 20_000_000, 3000, 4, 4));

        ComparisonResult result = service.compare(List.of("cheap", "pricey"));

        assertEquals(BestWorstFlag.BEST,  result.getFlag("price", "cheap"));
        assertEquals(BestWorstFlag.WORST, result.getFlag("price", "pricey"));
    }

    @Test
    void compare_twoProperties_areaRankingCorrect() {
        // Higher area is BEST
        dao.store("small", property("small", 10_000_000, 500, 2, 2));
        dao.store("large", property("large", 10_000_000, 3000, 4, 3));

        ComparisonResult result = service.compare(List.of("small", "large"));

        assertEquals(BestWorstFlag.BEST,  result.getFlag("area", "large"));
        assertEquals(BestWorstFlag.WORST, result.getFlag("area", "small"));
    }

    @Test
    void compare_threeProperties_middleValueIsNeutral() {
        dao.store("low",  property("low",  5_000_000, 1000, 2, 2));
        dao.store("mid",  property("mid",  10_000_000, 2000, 3, 2));
        dao.store("high", property("high", 20_000_000, 4000, 4, 4));

        ComparisonResult result = service.compare(List.of("low", "mid", "high"));

        assertEquals(BestWorstFlag.NEUTRAL, result.getFlag("price", "mid"));
    }

    @Test
    void compare_fourProperties_maxAllowed() {
        for (int i = 1; i <= 4; i++) {
            dao.store("p" + i, property("p" + i, i * 5_000_000L, i * 500, i + 1, i + 1));
        }

        ComparisonResult result = service.compare(List.of("p1", "p2", "p3", "p4"));

        assertEquals(4, result.getProperties().size());
    }

    @Test
    void compare_ranksAllFiveMetrics() {
        dao.store("p1", property("p1", 5_000_000, 1000, 2, 1));
        dao.store("p2", property("p2", 8_000_000, 2000, 3, 2));

        ComparisonResult result = service.compare(List.of("p1", "p2"));

        assertTrue(result.getRankings().containsKey("price"));
        assertTrue(result.getRankings().containsKey("area"));
        assertTrue(result.getRankings().containsKey("pricePerSqft"));
        assertTrue(result.getRankings().containsKey("bedrooms"));
        assertTrue(result.getRankings().containsKey("bathrooms"));
    }

    // ── Validation ─────────────────────────────────────────────────────

    @Test
    void compare_oneProperty_throwsValidationException() {
        assertThrows(ValidationException.class, () -> service.compare(List.of("p1")));
    }

    @Test
    void compare_fiveProperties_throwsValidationException() {
        assertThrows(ValidationException.class,
                () -> service.compare(List.of("p1", "p2", "p3", "p4", "p5")));
    }

    @Test
    void compare_emptyList_throwsValidationException() {
        assertThrows(ValidationException.class, () -> service.compare(Collections.emptyList()));
    }

    @Test
    void compare_nullList_throwsValidationException() {
        assertThrows(ValidationException.class, () -> service.compare(null));
    }

    @Test
    void compare_idsNotFound_throwsValidationException() {
        // DAO returns nothing — fewer than 2 properties found
        assertThrows(ValidationException.class, () -> service.compare(List.of("missing1", "missing2")));
    }

    // ─────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────

    private static Property property(String id, double price, double area, int beds, int baths) {
        Property p = new Property();
        p.setPropertyId(id);
        p.setPrice(price);
        p.setArea(area);
        p.setBedrooms(beds);
        p.setBathrooms(baths);
        return p;
    }

    // ─────────────────────────────────────────────────────────────────
    // STUB DAOs
    // ─────────────────────────────────────────────────────────────────

    static class StubPropertyDAO implements PropertyDAO {
        private final Map<String, Property> store = new LinkedHashMap<>();

        void store(String id, Property p) { store.put(id, p); }

        @Override public Optional<Property> findById(String id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<Property> search(SearchFilter f) { return Collections.emptyList(); }
        @Override public int countByFilter(SearchFilter f) { return 0; }
        @Override public List<Property> findComparables(String c, String l, String t, double a) { return Collections.emptyList(); }
        @Override public boolean insert(Property p) { return true; }
        @Override public boolean update(Property p) { return true; }
        @Override public boolean delete(String id) { return true; }
        @Override public List<Property> findByLocality(String c, String l) { return Collections.emptyList(); }
        @Override public List<TrendPoint> findAggregatedByMonth(String c, String l, String t, LocalDate s, LocalDate e) { return Collections.emptyList(); }
        @Override public List<LocalityMetric> getLocalityMetrics(String c, int y, int m) { return Collections.emptyList(); }
        @Override public List<String> findDistinctCities() { return Collections.emptyList(); }
        @Override public List<String> findDistinctLocalities(String city) { return Collections.emptyList(); }
    }

    static class StubValuationDAO implements ValuationDAO {
        @Override public Valuation findByPropertyId(String id) { return null; }
        @Override public boolean insert(Valuation v) { return true; }
        @Override public List<Valuation> findByDateRange(java.time.LocalDateTime s, java.time.LocalDateTime e) { return Collections.emptyList(); }
    }
}
