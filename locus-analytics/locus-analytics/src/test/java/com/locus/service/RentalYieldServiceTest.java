package com.locus.service;

import com.locus.dao.RentalAnalysisDAO;
import com.locus.exception.ValidationException;
import com.locus.model.RentalAnalysis;
import com.locus.service.impl.RentalYieldServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RentalYieldServiceImpl} (UC-2).
 *
 * @author Arham Manzoor (24i-0640)
 */
class RentalYieldServiceTest {

    private RentalYieldServiceImpl service;
    private StubRentalAnalysisDAO dao;

    @BeforeEach
    void setUp() {
        dao = new StubRentalAnalysisDAO();
        service = new RentalYieldServiceImpl(dao);
    }

    // ── Gross / net yield formulas ─────────────────────────────────────

    @Test
    void calculate_validInputs_grossYieldFormula() {
        // grossYield = (annual rent / property value) * 100
        // monthlyRent=50_000 → annual=600_000; propertyValue=10_000_000
        // expected gross = 6.0%
        RentalAnalysis result = service.calculate(10_000_000, 50_000, 0);

        assertEquals(6.0, result.getGrossYield(), 0.001);
    }

    @Test
    void calculate_validInputs_netYieldFormula() {
        // netYield = ((annual rent - expenses) / property value) * 100
        // annual=600_000 expenses=120_000 → net = 480_000/10_000_000 * 100 = 4.8%
        RentalAnalysis result = service.calculate(10_000_000, 50_000, 120_000);

        assertEquals(4.8, result.getNetYield(), 0.001);
    }

    @Test
    void calculate_zeroExpenses_grossEqualsNet() {
        RentalAnalysis result = service.calculate(5_000_000, 30_000, 0);

        assertEquals(result.getGrossYield(), result.getNetYield(), 0.0001);
    }

    @Test
    void calculate_returnsPropertyValueAndRentInResult() {
        RentalAnalysis result = service.calculate(8_000_000, 40_000, 50_000);

        assertEquals(8_000_000, result.getPropertyValue(), 0.01);
        assertEquals(480_000, result.getExpectedRent(), 0.01); // stored as annual
    }

    // ── Validation ─────────────────────────────────────────────────────

    @Test
    void calculate_zeroPropertyValue_throwsValidationException() {
        assertThrows(ValidationException.class, () -> service.calculate(0, 30_000, 0));
    }

    @Test
    void calculate_zeroMonthlyRent_throwsValidationException() {
        assertThrows(ValidationException.class, () -> service.calculate(5_000_000, 0, 0));
    }

    @Test
    void calculate_negativeExpenses_throwsValidationException() {
        assertThrows(ValidationException.class, () -> service.calculate(5_000_000, 30_000, -1));
    }

    @Test
    void calculate_annualRentExceedsPropertyValue_throwsValidationException() {
        // monthly=100_000 → annual=1_200_000 > propertyValue=1_000_000
        assertThrows(ValidationException.class, () -> service.calculate(1_000_000, 100_000, 0));
    }

    // ── City / locality average ─────────────────────────────────────────

    @Test
    void getCityAverageYield_withLocality_usesLocalityAverage() {
        dao.localityYield = 7.5;

        double result = service.getCityAverageYield("Lahore", "Gulberg");

        assertEquals(7.5, result, 0.001);
    }

    @Test
    void getCityAverageYield_withoutLocality_usesCityAverage() {
        dao.cityYield = 6.2;

        double result = service.getCityAverageYield("Islamabad", null);

        assertEquals(6.2, result, 0.001);
    }

    @Test
    void getCityAverageYield_emptyLocality_usesCityAverage() {
        dao.cityYield = 5.8;

        double result = service.getCityAverageYield("Karachi", "");

        assertEquals(5.8, result, 0.001);
    }

    // ─────────────────────────────────────────────────────────────────
    // STUB DAO
    // ─────────────────────────────────────────────────────────────────

    static class StubRentalAnalysisDAO implements RentalAnalysisDAO {
        double cityYield    = 6.0;
        double localityYield = 6.5;

        @Override public RentalAnalysis findLatestByProperty(String propertyId) { return null; }
        @Override public List<RentalAnalysis> findByProperty(String propertyId) { return Collections.emptyList(); }
        @Override public boolean insert(RentalAnalysis analysis) { return true; }
        @Override public double getCityAverageYield(String city) { return cityYield; }
        @Override public double getLocalityAverageYield(String city, String locality) { return localityYield; }
    }
}
