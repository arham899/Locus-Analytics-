package com.locus.service;

import com.locus.dao.PropertyDAO;
import com.locus.exception.ValidationException;
import com.locus.model.Property;
import com.locus.model.dto.LocalityMetric;
import com.locus.model.dto.PagedResult;
import com.locus.model.dto.SearchFilter;
import com.locus.model.dto.TimeRange;
import com.locus.model.dto.TrendPoint;
import com.locus.model.dto.TrendStatistics;
import com.locus.service.impl.PriceTrendServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PriceTrendServiceImpl} (UC-6).
 *
 * @author Arham Manzoor (24i-0640)
 */
class PriceTrendServiceTest {

    private PriceTrendServiceImpl service;
    private StubPropertyDAO dao;

    @BeforeEach
    void setUp() {
        dao = new StubPropertyDAO();
        service = new PriceTrendServiceImpl(dao);
    }

    // ── getTrend ───────────────────────────────────────────────────────

    @Test
    void getTrend_localityHasSufficientData_returnsLocalityPoints() {
        dao.localityPoints = monthlyPoints(8, 10_000_000);
        dao.cityPoints     = monthlyPoints(12, 8_000_000);

        List<TrendPoint> result = service.getTrend("Karachi", "Clifton", "house", TimeRange.ONE_YEAR);

        assertEquals(8, result.size());
        assertEquals(10_000_000, result.get(0).getAveragePrice(), 1.0);
    }

    @Test
    void getTrend_localityHasInsufficientData_fallsBackToCity() {
        // Fewer than 6 points for locality → should use city
        dao.localityPoints = monthlyPoints(3, 10_000_000);
        dao.cityPoints     = monthlyPoints(12, 8_000_000);

        List<TrendPoint> result = service.getTrend("Karachi", "Clifton", "house", TimeRange.ONE_YEAR);

        assertEquals(12, result.size()); // city-level data
    }

    @Test
    void getTrend_noLocality_queriesCityDirectly() {
        dao.cityPoints = monthlyPoints(10, 9_000_000);

        List<TrendPoint> result = service.getTrend("Lahore", null, "house", TimeRange.ONE_YEAR);

        assertEquals(10, result.size());
    }

    @Test
    void getTrend_resultIsSortedByPeriod() {
        List<TrendPoint> unsorted = new ArrayList<>();
        unsorted.add(new TrendPoint("2024-03", 10_000_000, 20));
        unsorted.add(new TrendPoint("2024-01", 9_000_000,  18));
        unsorted.add(new TrendPoint("2024-02", 9_500_000,  19));
        dao.cityPoints = unsorted;

        List<TrendPoint> result = service.getTrend("Islamabad", null, null, TimeRange.ONE_YEAR);

        assertEquals("2024-01", result.get(0).getPeriod());
        assertEquals("2024-02", result.get(1).getPeriod());
        assertEquals("2024-03", result.get(2).getPeriod());
    }

    @Test
    void getTrend_blankCity_throwsValidationException() {
        assertThrows(ValidationException.class,
                () -> service.getTrend("", "Clifton", "house", TimeRange.ONE_YEAR));
    }

    @Test
    void getTrend_nullTimeRange_throwsValidationException() {
        assertThrows(ValidationException.class,
                () -> service.getTrend("Karachi", null, null, null));
    }

    // ── computeStatistics ─────────────────────────────────────────────

    @Test
    void computeStatistics_emptyList_returnsZeroStats() {
        TrendStatistics stats = service.computeStatistics(Collections.emptyList());

        assertEquals(0, stats.getCurrentAverage(), 0.001);
        assertEquals(0, stats.getHighestPrice(), 0.001);
        assertEquals(0, stats.getLowestPrice(), 0.001);
    }

    @Test
    void computeStatistics_singlePoint_zeroAppreciation() {
        List<TrendPoint> points = List.of(new TrendPoint("2024-01", 10_000_000, 10));

        TrendStatistics stats = service.computeStatistics(points);

        assertEquals(0, stats.getAnnualAppreciationRate(), 0.001);
        assertEquals(10_000_000, stats.getCurrentAverage(), 1.0);
    }

    @Test
    void computeStatistics_risingPrices_positiveAppreciation() {
        // Rising monthly prices over 12 months
        List<TrendPoint> points = monthlyPoints(12, 10_000_000, 1.01);

        TrendStatistics stats = service.computeStatistics(points);

        assertTrue(stats.getAnnualAppreciationRate() > 0, "Rising prices → positive CAGR");
    }

    @Test
    void computeStatistics_identifiesHighestAndLowest() {
        List<TrendPoint> points = List.of(
                new TrendPoint("2024-01", 8_000_000,  10),
                new TrendPoint("2024-06", 15_000_000, 20),
                new TrendPoint("2024-12", 12_000_000, 15)
        );

        TrendStatistics stats = service.computeStatistics(points);

        assertEquals(15_000_000, stats.getHighestPrice(), 1.0);
        assertEquals(8_000_000,  stats.getLowestPrice(),  1.0);
        assertEquals(12_000_000, stats.getCurrentAverage(), 1.0); // last point
    }

    // ─────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────

    private static List<TrendPoint> monthlyPoints(int count, double basePrice) {
        return monthlyPoints(count, basePrice, 1.0);
    }

    private static List<TrendPoint> monthlyPoints(int count, double basePrice, double monthlyFactor) {
        List<TrendPoint> points = new ArrayList<>();
        double price = basePrice;
        for (int i = 0; i < count; i++) {
            String period = String.format("2024-%02d", i + 1);
            points.add(new TrendPoint(period, price, 10 + i));
            price *= monthlyFactor;
        }
        return points;
    }

    // ─────────────────────────────────────────────────────────────────
    // STUB DAO
    // ─────────────────────────────────────────────────────────────────

    static class StubPropertyDAO implements PropertyDAO {
        List<TrendPoint> localityPoints = Collections.emptyList();
        List<TrendPoint> cityPoints     = Collections.emptyList();

        @Override
        public List<TrendPoint> findAggregatedByMonth(String city, String locality,
                                                       String type, LocalDate s, LocalDate e) {
            return (locality != null && !locality.isBlank()) ? localityPoints : cityPoints;
        }

        @Override public Optional<Property> findById(String id) { return Optional.empty(); }
        @Override public List<Property> search(SearchFilter f) { return Collections.emptyList(); }
        @Override public int countByFilter(SearchFilter f) { return 0; }
        @Override public List<Property> findComparables(String c, String l, String t, double a) { return Collections.emptyList(); }
        @Override public boolean insert(Property p) { return true; }
        @Override public boolean update(Property p) { return true; }
        @Override public boolean delete(String id) { return true; }
        @Override public List<Property> findByLocality(String c, String l) { return Collections.emptyList(); }
        @Override public List<LocalityMetric> getLocalityMetrics(String c, int y, int m) { return Collections.emptyList(); }
        @Override public List<String> findDistinctCities() { return Collections.emptyList(); }
        @Override public List<String> findDistinctLocalities(String city) { return Collections.emptyList(); }
    }
}
