package com.locus.service;

import com.locus.dao.PropertyDAO;
import com.locus.dao.ValuationDAO;
import com.locus.exception.ValidationException;
import com.locus.ml.LinearRegressionPredictor;
import com.locus.model.Property;
import com.locus.model.Valuation;
import com.locus.model.dto.PagedResult;
import com.locus.model.dto.SearchFilter;
import com.locus.model.dto.TimeRange;
import com.locus.model.dto.TrendPoint;
import com.locus.model.dto.LocalityMetric;
import com.locus.service.impl.ValuationServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ValuationServiceImpl} (UC-1).
 *
 * Uses stub DAO/predictor implementations — no database required.
 *
 * @author Arham Manzoor (24i-0640)
 */
class ValuationServiceTest {

    private ValuationServiceImpl service;
    private StubPropertyDAO propertyDAO;
    private StubValuationDAO valuationDAO;

    @BeforeEach
    void setUp() {
        // Loads from classpath: src/test/resources/test_model.json
        LinearRegressionPredictor predictor = new LinearRegressionPredictor("test_model.json");
        propertyDAO = new StubPropertyDAO();
        valuationDAO = new StubValuationDAO();
        service = new ValuationServiceImpl(propertyDAO, valuationDAO, predictor);
    }

    // ── estimateFMV ───────────────────────────────────────────────────

    @Test
    void estimateFMV_validProperty_returnsFMVWithConfidenceInterval() {
        Property p = house("Karachi", "DHA Phase 5", 2000, 3, 3);

        Valuation result = service.estimateFMV(p);

        assertTrue(result.getEstimatedFmv() > 0, "FMV must be positive");
        assertTrue(result.getConfidenceIntervalLow() < result.getEstimatedFmv(),
                "Lower bound must be below FMV");
        assertTrue(result.getConfidenceIntervalHigh() > result.getEstimatedFmv(),
                "Upper bound must be above FMV");
    }

    @Test
    void estimateFMV_validProperty_returnsKeyFactors() {
        Property p = house("Karachi", "DHA Phase 5", 3000, 4, 3);

        Valuation result = service.estimateFMV(p);

        assertNotNull(result.getKeyFactors());
        assertFalse(result.getKeyFactors().isEmpty(), "Should have at least one key factor");
    }

    @Test
    void estimateFMV_dataDensityEqualsComparableCount() {
        propertyDAO.comparables = List.of(new Property(), new Property(), new Property());
        Property p = house("Karachi", "DHA Phase 5", 2000, 3, 3);

        Valuation result = service.estimateFMV(p);

        assertEquals(3, result.getDataDensity());
    }

    @Test
    void estimateFMV_noComparables_dataDensityIsZero() {
        propertyDAO.comparables = Collections.emptyList();
        Property p = house("Karachi", "DHA Phase 5", 2000, 3, 3);

        Valuation result = service.estimateFMV(p);

        assertEquals(0, result.getDataDensity());
    }

    @Test
    void estimateFMV_nullProperty_throwsValidationException() {
        assertThrows(ValidationException.class, () -> service.estimateFMV(null));
    }

    @Test
    void estimateFMV_invalidCity_throwsValidationException() {
        Property p = house("London", "DHA Phase 5", 2000, 3, 3);

        assertThrows(ValidationException.class, () -> service.estimateFMV(p));
    }

    @Test
    void estimateFMV_blankLocality_throwsValidationException() {
        Property p = house("Karachi", "", 2000, 3, 3);

        assertThrows(ValidationException.class, () -> service.estimateFMV(p));
    }

    @Test
    void estimateFMV_zeroArea_throwsValidationException() {
        Property p = house("Karachi", "DHA Phase 5", 0, 3, 3);

        assertThrows(ValidationException.class, () -> service.estimateFMV(p));
    }

    @Test
    void estimateFMV_unsupportedLocality_throwsValidationException() {
        Property p = house("Karachi", "Unknown Remote Area", 2000, 3, 3);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.estimateFMV(p));
        assertTrue(ex.getMessage().contains("not supported"));
    }

    @Test
    void estimateFMV_largerAreaProducesHigherFMV() {
        Property small = house("Karachi", "DHA Phase 5", 1000, 2, 2);
        Property large = house("Karachi", "DHA Phase 5", 5000, 5, 4);

        double fmvSmall = service.estimateFMV(small).getEstimatedFmv();
        double fmvLarge = service.estimateFMV(large).getEstimatedFmv();

        assertTrue(fmvLarge > fmvSmall, "Larger property should have higher FMV");
    }

    // ── findComparables ───────────────────────────────────────────────

    @Test
    void findComparables_validProperty_delegatesToDAO() {
        Property stub1 = new Property();
        stub1.setPropertyId("p1");
        propertyDAO.comparables = List.of(stub1);

        Property p = house("Karachi", "DHA Phase 5", 2000, 3, 3);
        List<Property> result = service.findComparables(p);

        assertEquals(1, result.size());
        assertEquals("p1", result.get(0).getPropertyId());
    }

    @Test
    void findComparables_nullProperty_throwsValidationException() {
        assertThrows(ValidationException.class, () -> service.findComparables(null));
    }

    // ─────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────

    private static Property house(String city, String locality, double area,
                                   int bedrooms, int bathrooms) {
        Property p = new Property();
        p.setCity(city);
        p.setLocality(locality);
        p.setPropertyType("house");
        p.setArea(area);
        p.setBedrooms(bedrooms);
        p.setBathrooms(bathrooms);
        return p;
    }

    // ─────────────────────────────────────────────────────────────────
    // STUB DAOS
    // ─────────────────────────────────────────────────────────────────

    static class StubPropertyDAO implements PropertyDAO {
        List<Property> comparables = Collections.emptyList();

        @Override public List<Property> findComparables(String city, String locality,
                                                        String type, double area) {
            return comparables;
        }
        @Override public Optional<Property> findById(String id) { return Optional.empty(); }
        @Override public List<Property> search(SearchFilter f) { return Collections.emptyList(); }
        @Override public int countByFilter(SearchFilter f) { return 0; }
        @Override public boolean insert(Property p) { return true; }
        @Override public boolean update(Property p) { return true; }
        @Override public boolean delete(String id) { return true; }
        @Override public List<Property> findByLocality(String city, String locality) { return Collections.emptyList(); }
        @Override public List<TrendPoint> findAggregatedByMonth(String city, String locality,
                                                                String type, java.time.LocalDate s,
                                                                java.time.LocalDate e) {
            return Collections.emptyList();
        }
        @Override public List<LocalityMetric> getLocalityMetrics(String city, int years, int min) {
            return Collections.emptyList();
        }
        @Override public List<String> findDistinctCities() { return Collections.emptyList(); }
        @Override public List<String> findDistinctLocalities(String city) { return Collections.emptyList(); }
    }

    static class StubValuationDAO implements ValuationDAO {
        @Override public Valuation findByPropertyId(String id) { return null; }
        @Override public boolean insert(Valuation v) { return true; }
        @Override public List<Valuation> findByDateRange(java.time.LocalDateTime s,
                                                          java.time.LocalDateTime e) {
            return Collections.emptyList();
        }
    }
}
