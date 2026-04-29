package com.locus.service.impl;

import com.locus.dao.PropertyDAO;
import com.locus.model.dto.TimeRange;
import com.locus.model.dto.TrendPoint;
import com.locus.model.dto.TrendStatistics;
import com.locus.service.PriceTrendService;
import com.locus.service.validation.InputValidator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * Real implementation of {@link PriceTrendService} (UC-6).
 *
 * <p>Queries monthly aggregated price data from the DAO and computes
 * statistics including CAGR, highest/lowest prices, and current average.
 * Falls back to city-level data if locality has insufficient data.</p>
 *
 * @author Arham Manzoor (24i-0640)
 */
public class PriceTrendServiceImpl implements PriceTrendService {

    private static final int MIN_DATA_POINTS = 6;
    private final PropertyDAO propertyDAO;

    public PriceTrendServiceImpl(PropertyDAO propertyDAO) {
        this.propertyDAO = propertyDAO;
    }

    @Override
    public List<TrendPoint> getTrend(String city, String locality, String propertyType, TimeRange timeRange) {

        // ── Validation ──────────────────────────────
        new InputValidator()
                .validateNotBlank("city", city)
                .validateNotNull("timeRange", timeRange)
                .throwIfInvalid();

        LocalDate fromDate = timeRange.getFromDate();
        LocalDate toDate = timeRange.getToDate();

        // ── Query with locality ─────────────────────
        List<TrendPoint> points = propertyDAO.findAggregatedByMonth(
                city, locality, propertyType, fromDate, toDate
        );

        // ── Fallback to city level if insufficient data ──
        if (locality != null && !locality.isBlank() && points.size() < MIN_DATA_POINTS) {
            System.out.println("[PriceTrendService] Insufficient locality data (" +
                    points.size() + " points). Falling back to city-level.");
            points = propertyDAO.findAggregatedByMonth(
                    city, null, propertyType, fromDate, toDate
            );
        }

        // Sort by period
        points.sort(Comparator.comparing(TrendPoint::getPeriod));

        return points;
    }

    @Override
    public TrendStatistics computeStatistics(List<TrendPoint> trendPoints) {

        if (trendPoints == null || trendPoints.isEmpty()) {
            return new TrendStatistics(0, 0, null, 0, null, 0);
        }

        // ── Find highest and lowest ─────────────────
        TrendPoint highest = trendPoints.stream()
                .max(Comparator.comparingDouble(TrendPoint::getAveragePrice))
                .orElse(trendPoints.get(0));

        TrendPoint lowest = trendPoints.stream()
                .min(Comparator.comparingDouble(TrendPoint::getAveragePrice))
                .orElse(trendPoints.get(0));

        // ── Current average (last data point) ───────
        TrendPoint last = trendPoints.get(trendPoints.size() - 1);
        double currentAverage = last.getAveragePrice();

        // ── CAGR (Compound Annual Growth Rate) ──────
        TrendPoint first = trendPoints.get(0);
        double firstPrice = first.getAveragePrice();
        double lastPrice = last.getAveragePrice();

        double years = trendPoints.size() / 12.0; // approximate
        double annualAppreciation = 0;

        if (years > 0 && firstPrice > 0 && lastPrice > 0) {
            annualAppreciation = (Math.pow(lastPrice / firstPrice, 1.0 / years) - 1) * 100.0;
        }

        // ── Parse period strings to dates for stats ──
        LocalDate highestDate = parsePeriod(highest.getPeriod());
        LocalDate lowestDate = parsePeriod(lowest.getPeriod());

        return new TrendStatistics(
                annualAppreciation,
                highest.getAveragePrice(),
                highestDate,
                lowest.getAveragePrice(),
                lowestDate,
                currentAverage
        );
    }

    /**
     * Parses a period string like "2024-01" to LocalDate (first of month).
     */
    private LocalDate parsePeriod(String period) {
        if (period == null || period.length() < 7) return null;
        try {
            return LocalDate.parse(period + "-01", DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            return null;
        }
    }
}
