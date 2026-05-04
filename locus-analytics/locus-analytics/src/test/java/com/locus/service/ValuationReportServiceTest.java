package com.locus.service;

import com.locus.dao.*;
import com.locus.exception.ValidationException;
import com.locus.model.*;
import com.locus.model.dto.TimeRange;
import com.locus.model.dto.TrendPoint;
import com.locus.model.dto.TrendStatistics;
import com.locus.service.impl.ValuationReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ValuationReportServiceImpl} (UC-8).
 *
 * @author Arham Manzoor (24i-0640)
 */
class ValuationReportServiceTest {

    private ValuationReportServiceImpl service;
    private StubPropertyDAO propertyDAO;
    private StubValuationDAO valuationDAO;
    private StubRentalAnalysisDAO rentalDAO;
    private StubROIAnalysisDAO roiDAO;
    private StubPriceTrendService trendService;
    private StubValuationReportDAO reportDAO;

    @BeforeEach
    void setUp() {
        propertyDAO = new StubPropertyDAO();
        valuationDAO = new StubValuationDAO();
        rentalDAO = new StubRentalAnalysisDAO();
        roiDAO = new StubROIAnalysisDAO();
        trendService = new StubPriceTrendService();
        reportDAO = new StubValuationReportDAO();

        service = new ValuationReportServiceImpl(
                propertyDAO, valuationDAO, rentalDAO, roiDAO, trendService, reportDAO
        );
    }

    @Test
    void assembleReport_validInput_returnsPopulatedReport() {
        Property p = new Property();
        p.setPropertyId("prop-123");
        p.setCity("Karachi");
        propertyDAO.stubProperty = Optional.of(p);

        ValuationReport report = service.assembleReport("prop-123", List.of("fmv", "roi"), "Some notes");

        assertNotNull(report);
        assertEquals("prop-123", report.getPropertyId());
        assertEquals("Some notes", report.getAnalystNotes());
        assertEquals(2, report.getIncludedSections().size());
        assertNotNull(report.getProperty());
    }

    @Test
    void assembleReport_propertyNotFound_throwsException() {
        propertyDAO.stubProperty = Optional.empty();
        assertThrows(ValidationException.class, () -> service.assembleReport("invalid", List.of(), ""));
    }

    @Test
    void assembleReport_nullPropertyId_throwsException() {
        assertThrows(ValidationException.class, () -> service.assembleReport(null, List.of(), ""));
    }

    @Test
    void assembleReport_includesFMVSection_fetchesFromDAO() {
        Property p = new Property();
        p.setPropertyId("p1");
        propertyDAO.stubProperty = Optional.of(p);
        
        Valuation v = new Valuation();
        v.setEstimatedFmv(5000000);
        valuationDAO.stubValuation = v;

        ValuationReport report = service.assembleReport("p1", List.of("fmv"), "");

        assertNotNull(report.getValuation());
        assertEquals(5000000, report.getValuation().getEstimatedFmv());
    }

    @Test
    void assembleReport_includesROISection_fetchesFromDAO() {
        Property p = new Property();
        p.setPropertyId("p1");
        propertyDAO.stubProperty = Optional.of(p);
        
        ROIAnalysis roi = new ROIAnalysis();
        roi.setRoiPercentage(15.5);
        roiDAO.stubRoi = roi;

        ValuationReport report = service.assembleReport("p1", List.of("roi"), "");

        assertNotNull(report.getRoiAnalysis());
        assertEquals(15.5, report.getRoiAnalysis().getRoiPercentage());
    }

    @Test
    void assembleReport_missingOptionalData_leavesFieldNull() {
        Property p = new Property();
        p.setPropertyId("p1");
        propertyDAO.stubProperty = Optional.of(p);
        
        // No ROI or Rental Analysis set in stubs
        ValuationReport report = service.assembleReport("p1", List.of("roi", "rental_yield"), "");

        assertNull(report.getRoiAnalysis());
        assertNull(report.getRentalAnalysis());
    }

    // ── STUBS ──────────────────────────────────────────────────────────

    static class StubPropertyDAO implements PropertyDAO {
        Optional<Property> stubProperty = Optional.empty();
        @Override public Optional<Property> findById(String id) { return stubProperty; }
        @Override public List<Property> findComparables(String c, String l, String t, double a) { return Collections.emptyList(); }
        @Override public List<Property> search(com.locus.model.dto.SearchFilter f) { return Collections.emptyList(); }
        @Override public int countByFilter(com.locus.model.dto.SearchFilter f) { return 0; }
        @Override public boolean insert(Property p) { return true; }
        @Override public boolean update(Property p) { return true; }
        @Override public boolean delete(String id) { return true; }
        @Override public List<Property> findByLocality(String city, String locality) { return Collections.emptyList(); }
        @Override public List<TrendPoint> findAggregatedByMonth(String c, String l, String t, LocalDate s, LocalDate e) { return Collections.emptyList(); }
        @Override public List<com.locus.model.dto.LocalityMetric> getLocalityMetrics(String c, int y, int m) { return Collections.emptyList(); }
        @Override public List<String> findDistinctCities() { return Collections.emptyList(); }
        @Override public List<String> findDistinctLocalities(String city) { return Collections.emptyList(); }
    }

    static class StubValuationDAO implements ValuationDAO {
        Valuation stubValuation = null;
        @Override public Valuation findByPropertyId(String id) { return stubValuation; }
        @Override public boolean insert(Valuation v) { return true; }
        @Override public List<Valuation> findByDateRange(java.time.LocalDateTime s, java.time.LocalDateTime e) { return Collections.emptyList(); }
    }

    static class StubRentalAnalysisDAO implements RentalAnalysisDAO {
        @Override public RentalAnalysis findLatestByProperty(String id) { return null; }
        @Override public List<RentalAnalysis> findByProperty(String id) { return Collections.emptyList(); }
        @Override public boolean insert(RentalAnalysis a) { return true; }
        @Override public double getLocalityAverageYield(String c, String l) { return 0; }
        @Override public double getCityAverageYield(String c) { return 0; }
    }

    static class StubROIAnalysisDAO implements ROIAnalysisDAO {
        ROIAnalysis stubRoi = null;
        @Override public ROIAnalysis findByProperty(String id) { return stubRoi; }
        @Override public boolean insert(ROIAnalysis a) { return true; }
    }

    static class StubPriceTrendService implements PriceTrendService {
        @Override public List<TrendPoint> getTrend(String c, String l, String t, TimeRange r) { return Collections.emptyList(); }
        @Override public TrendStatistics computeStatistics(List<TrendPoint> p) { return new TrendStatistics(); }
    }

    static class StubValuationReportDAO implements ValuationReportDAO {
        @Override public boolean insert(ValuationReport r) { return true; }
        @Override public Optional<ValuationReport> findById(String id) { return Optional.empty(); }
        @Override public List<ValuationReport> findByProperty(String id) { return Collections.emptyList(); }
        @Override public List<ValuationReport> findByAnalyst(String id) { return Collections.emptyList(); }
        @Override public boolean updateFilePath(String id, String p) { return true; }
    }
}
