package com.locus.service;

import com.locus.dao.ROIAnalysisDAO;
import com.locus.exception.ValidationException;
import com.locus.model.ROIAnalysis;
import com.locus.model.dto.ROIInput;
import com.locus.service.impl.ROIServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ROIServiceImpl} (UC-3).
 *
 * @author Arham Manzoor (24i-0640)
 */
class ROIServiceTest {

    private ROIServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ROIServiceImpl(new StubROIAnalysisDAO());
    }

    // ── Core calculations ──────────────────────────────────────────────

    @Test
    void calculate_positiveReturn_correctTotalReturn() {
        // purchasePrice=10M, currentValue=14M, rental=2M, expenses=0.5M
        // capitalAppreciation=4M, netRental=1.5M, total=5.5M
        ROIInput input = input(10_000_000, date(3), 14_000_000, 2_000_000, 500_000);

        ROIAnalysis result = service.calculate(input);

        assertEquals(5_500_000, result.getTotalReturn(), 1.0);
    }

    @Test
    void calculate_positiveReturn_correctROIPercent() {
        // totalReturn=5.5M / purchasePrice=10M * 100 = 55%
        ROIInput input = input(10_000_000, date(3), 14_000_000, 2_000_000, 500_000);

        ROIAnalysis result = service.calculate(input);

        assertEquals(55.0, result.getRoiPercentage(), 0.01);
    }

    @Test
    void calculate_negativeReturn_negativeROI() {
        // currentValue less than purchase, no rental income
        ROIInput input = input(10_000_000, date(2), 8_000_000, 0, 0);

        ROIAnalysis result = service.calculate(input);

        assertTrue(result.getRoiPercentage() < 0, "Declining value should produce negative ROI");
    }

    @Test
    void calculate_noRentalNoExpenses_roiEqualsCapitalAppreciation() {
        // totalReturn = (currentValue - purchasePrice) only
        ROIInput input = input(5_000_000, date(2), 7_000_000, 0, 0);

        ROIAnalysis result = service.calculate(input);

        assertEquals(2_000_000, result.getTotalReturn(), 1.0);
        assertEquals(40.0, result.getRoiPercentage(), 0.01);
    }

    @Test
    void calculate_multiYearHolding_annualizedROIIsPositive() {
        ROIInput input = input(10_000_000, date(5), 18_000_000, 3_000_000, 1_000_000);

        ROIAnalysis result = service.calculate(input);

        assertTrue(result.getAnnualizedROI() > 0, "Annualized ROI should be positive");
        assertTrue(result.getAnnualizedROI() < result.getRoiPercentage(),
                "Annualized ROI < total ROI for multi-year holding");
    }

    @Test
    void calculate_lessThanOneYear_stillComputes() {
        // 6 months ago
        ROIInput input = input(10_000_000,
                LocalDate.now().minusMonths(6),
                11_000_000, 300_000, 50_000);

        // Should not throw — just logs a warning
        ROIAnalysis result = service.calculate(input);

        assertNotNull(result);
        assertTrue(result.getRoiPercentage() > 0);
    }

    // ── Validation ─────────────────────────────────────────────────────

    @Test
    void calculate_nullInput_throwsValidationException() {
        assertThrows(ValidationException.class, () -> service.calculate(null));
    }

    @Test
    void calculate_zeroPurchasePrice_throwsValidationException() {
        ROIInput input = input(0, date(2), 5_000_000, 0, 0);
        assertThrows(ValidationException.class, () -> service.calculate(input));
    }

    @Test
    void calculate_futurePurchaseDate_throwsValidationException() {
        ROIInput input = input(10_000_000, LocalDate.now().plusDays(1), 12_000_000, 0, 0);
        assertThrows(ValidationException.class, () -> service.calculate(input));
    }

    @Test
    void calculate_zeroCurrentValue_throwsValidationException() {
        ROIInput input = input(10_000_000, date(2), 0, 0, 0);
        assertThrows(ValidationException.class, () -> service.calculate(input));
    }

    @Test
    void calculate_negativeRentalIncome_throwsValidationException() {
        ROIInput input = input(10_000_000, date(2), 12_000_000, -1, 0);
        assertThrows(ValidationException.class, () -> service.calculate(input));
    }

    @Test
    void calculate_negativeExpenses_throwsValidationException() {
        ROIInput input = input(10_000_000, date(2), 12_000_000, 0, -1);
        assertThrows(ValidationException.class, () -> service.calculate(input));
    }

    // ─────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────

    private static LocalDate date(int yearsAgo) {
        return LocalDate.now().minusYears(yearsAgo);
    }

    private static ROIInput input(double purchase, LocalDate purchaseDate,
                                   double current, double rental, double expenses) {
        ROIInput i = new ROIInput();
        i.setPurchasePrice(purchase);
        i.setPurchaseDate(purchaseDate);
        i.setCurrentValue(current);
        i.setCumulativeRentalIncome(rental);
        i.setTotalExpenses(expenses);
        return i;
    }

    // ─────────────────────────────────────────────────────────────────
    // STUB DAO
    // ─────────────────────────────────────────────────────────────────

    static class StubROIAnalysisDAO implements ROIAnalysisDAO {
        @Override public ROIAnalysis findByProperty(String id) { return null; }
        @Override public boolean insert(ROIAnalysis a) { return true; }
    }
}
