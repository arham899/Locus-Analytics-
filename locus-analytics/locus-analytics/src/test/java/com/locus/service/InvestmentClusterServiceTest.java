package com.locus.service;

import com.locus.dao.InvestmentClusterDAO;
import com.locus.dao.PropertyDAO;
import com.locus.dao.RentalAnalysisDAO;
import com.locus.exception.ValidationException;
import com.locus.model.InvestmentCluster;
import com.locus.model.Property;
import com.locus.model.RentalAnalysis;
import com.locus.model.dto.ClusterParams;
import com.locus.model.dto.LocalityMetric;
import com.locus.model.dto.SearchFilter;
import com.locus.model.dto.TrendPoint;
import com.locus.service.impl.InvestmentClusterServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link InvestmentClusterServiceImpl} (UC-9).
 *
 * @author 
 */
class InvestmentClusterServiceTest {

    private InvestmentClusterServiceImpl service;
    private StubPropertyDAO propertyDAO;
    private StubRentalAnalysisDAO rentalDAO;

    @BeforeEach
    void setUp() {
        propertyDAO = new StubPropertyDAO();
        rentalDAO = new StubRentalAnalysisDAO();
        service = new InvestmentClusterServiceImpl(propertyDAO, new StubClusterDAO(), rentalDAO);
    }

    // ── identifyClusters ──────────────────────────────────────────────

    @Test
    void identifyClusters_emptyMetrics_returnsEmptyList() {
        propertyDAO.metrics = Collections.emptyList();

        List<InvestmentCluster> result = service.identifyClusters(params("Karachi", 1, 5));

        assertTrue(result.isEmpty());
    }

    @Test
    void identifyClusters_multipleLocalities_returnsRankedList() {
        propertyDAO.metrics = List.of(
                metric("DHA Phase 5", 15.0, 20.0),
                metric("Gulberg",     5.0,  8.0),
                metric("Bahria Town", 10.0, 12.0)
        );

        List<InvestmentCluster> result = service.identifyClusters(params("Karachi", 1, 5));

        assertEquals(3, result.size(), "Should have exactly 3 clusters");
        
        // Expected order: DHA (highest), Bahria (mid), Gulberg (lowest)
        assertEquals("DHA Phase 5", result.get(0).getLocality(), "Top cluster should be DHA");
        assertEquals("Bahria Town", result.get(1).getLocality(), "Second cluster should be Bahria");
        assertEquals("Gulberg",     result.get(2).getLocality(), "Third cluster should be Gulberg");

        assertTrue(result.get(0).getInvestmentScore() >= result.get(1).getInvestmentScore(), "DHA score >= Bahria");
        assertTrue(result.get(1).getInvestmentScore() >= result.get(2).getInvestmentScore(), "Bahria score >= Gulberg");
    }

    @Test
    void identifyClusters_scoresNormalizedToZeroOneHundred() {
        propertyDAO.metrics = List.of(
                metric("A", 20.0, 30.0),
                metric("B", 5.0,  5.0)
        );

        List<InvestmentCluster> result = service.identifyClusters(params("Lahore", 1, 3));

        double highest = result.stream().mapToDouble(InvestmentCluster::getInvestmentScore).max().orElse(0);
        double lowest  = result.stream().mapToDouble(InvestmentCluster::getInvestmentScore).min().orElse(0);

        assertEquals(100.0, highest, 0.001);
        assertEquals(0.0,   lowest,  0.001);
    }

    @Test
    void identifyClusters_singleLocality_scoreIsFifty() {
        propertyDAO.metrics = List.of(metric("Only Area", 10.0, 10.0));

        List<InvestmentCluster> result = service.identifyClusters(params("Islamabad", 1, 3));

        assertEquals(1, result.size());
        assertEquals(50.0, result.get(0).getInvestmentScore(), 0.001);
    }

    @Test
    void identifyClusters_cityAndLocalityPropagated() {
        propertyDAO.metrics = List.of(metric("DHA Phase 5", 12.0, 10.0));

        List<InvestmentCluster> result = service.identifyClusters(params("Karachi", 1, 5));

        assertEquals("Karachi",    result.get(0).getCity());
        assertEquals("DHA Phase 5", result.get(0).getLocality());
    }

    @Test
    void identifyClusters_scoreIncludesPriceVolumeAndRental() {
        propertyDAO.metrics = List.of(
                metric("HighPrice", 50.0,  0.0),
                metric("HighVolume", 0.0, 50.0)
        );
        rentalDAO.yieldByLocality.put("HighPrice",  6.0);
        rentalDAO.yieldByLocality.put("HighVolume", 6.0);
        rentalDAO.cityYield = 6.0;

        List<InvestmentCluster> result = service.identifyClusters(params("Karachi", 1, 1));

        assertEquals("HighPrice", result.get(0).getLocality());
    }

    // ── Validation ─────────────────────────────────────────────────────

    @Test
    void identifyClusters_nullParams_throwsValidationException() {
        assertThrows(ValidationException.class, () -> service.identifyClusters(null));
    }

    @Test
    void identifyClusters_invalidCity_throwsValidationException() {
        assertThrows(ValidationException.class,
                () -> service.identifyClusters(params("Dubai", 1, 5)));
    }

    // ─────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────

    private static ClusterParams params(String city, int periodYears, int minListings) {
        ClusterParams p = new ClusterParams();
        p.setCity(city);
        p.setAnalysisPeriodYears(periodYears);
        p.setMinListingCount(minListings);
        return p;
    }

    private static LocalityMetric metric(String locality, double priceAppreciation, double volumeGrowth) {
        LocalityMetric m = new LocalityMetric();
        m.setLocality(locality);
        m.setPriceAppreciation(priceAppreciation);
        m.setVolumeGrowth(volumeGrowth);
        return m;
    }

    // ─────────────────────────────────────────────────────────────────
    // STUB DAOs
    // ─────────────────────────────────────────────────────────────────

    static class StubPropertyDAO implements PropertyDAO {
        List<LocalityMetric> metrics = Collections.emptyList();

        @Override public List<LocalityMetric> getLocalityMetrics(String city, int years, int minListings) { return metrics; }
        @Override public Optional<Property> findById(String id) { return Optional.empty(); }
        @Override public List<Property> search(SearchFilter f) { return Collections.emptyList(); }
        @Override public int countByFilter(SearchFilter f) { return 0; }
        @Override public List<Property> findComparables(String c, String l, String t, double a) { return Collections.emptyList(); }
        @Override public boolean insert(Property p) { return true; }
        @Override public boolean update(Property p) { return true; }
        @Override public boolean delete(String id) { return true; }
        @Override public List<Property> findByLocality(String c, String l) { return Collections.emptyList(); }
        @Override public List<TrendPoint> findAggregatedByMonth(String c, String l, String t, LocalDate s, LocalDate e) { return Collections.emptyList(); }
        @Override public List<String> findDistinctCities() { return Collections.emptyList(); }
        @Override public List<String> findDistinctLocalities(String city) { return Collections.emptyList(); }
    }

    static class StubRentalAnalysisDAO implements RentalAnalysisDAO {
        double cityYield = 6.0;
        Map<String, Double> yieldByLocality = new HashMap<>();

        @Override public double getCityAverageYield(String city) { return cityYield; }
        @Override public double getLocalityAverageYield(String city, String locality) {
            return yieldByLocality.getOrDefault(locality, cityYield);
        }
        @Override public RentalAnalysis findLatestByProperty(String id) { return null; }
        @Override public List<RentalAnalysis> findByProperty(String id) { return Collections.emptyList(); }
        @Override public boolean insert(RentalAnalysis a) { return true; }
    }

    static class StubClusterDAO implements InvestmentClusterDAO {
        @Override public boolean insert(InvestmentCluster c) { return true; }
        @Override public List<InvestmentCluster> findByCity(String city) { return Collections.emptyList(); }
        @Override public List<InvestmentCluster> findTopClusters(int limit) { return Collections.emptyList(); }
    }
}
