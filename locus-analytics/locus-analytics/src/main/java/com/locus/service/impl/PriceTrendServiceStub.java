package com.locus.service.impl;

import com.locus.model.dto.TimeRange;
import com.locus.model.dto.TrendPoint;
import com.locus.model.dto.TrendStatistics;
import com.locus.service.PriceTrendService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Stub implementation of {@link PriceTrendService} returning hardcoded trend data.
 */
public class PriceTrendServiceStub implements PriceTrendService {

    @Override
    public List<TrendPoint> getTrend(String city, String locality, String propertyType, TimeRange timeRange) {
        List<TrendPoint> points = new ArrayList<>();

        // Generate 12 months of realistic trend data
        double basePrice = 15_000_000;
        if ("Islamabad".equalsIgnoreCase(city)) basePrice = 25_000_000;
        if ("Lahore".equalsIgnoreCase(city)) basePrice = 18_000_000;

        for (int i = 11; i >= 0; i--) {
            LocalDate month = LocalDate.now().minusMonths(i);
            String period = month.getYear() + "-" + String.format("%02d", month.getMonthValue());
            // Simulate 8% annual appreciation
            double price = basePrice * (1 + 0.08 * (12 - i) / 12.0);
            int listings = 40 + (int)(Math.random() * 30);
            points.add(new TrendPoint(period, Math.round(price), listings));
        }
        return points;
    }

    @Override
    public TrendStatistics computeStatistics(List<TrendPoint> trendPoints) {
        if (trendPoints == null || trendPoints.isEmpty()) {
            return new TrendStatistics();
        }

        double highest = Double.MIN_VALUE;
        double lowest = Double.MAX_VALUE;
        String highPeriod = "", lowPeriod = "";

        for (TrendPoint tp : trendPoints) {
            if (tp.getAveragePrice() > highest) {
                highest = tp.getAveragePrice();
                highPeriod = tp.getPeriod();
            }
            if (tp.getAveragePrice() < lowest) {
                lowest = tp.getAveragePrice();
                lowPeriod = tp.getPeriod();
            }
        }

        double first = trendPoints.get(0).getAveragePrice();
        double last = trendPoints.get(trendPoints.size() - 1).getAveragePrice();
        double years = trendPoints.size() / 12.0;
        double cagr = years > 0 ? (Math.pow(last / first, 1.0 / years) - 1) * 100 : 0;

        TrendStatistics stats = new TrendStatistics();
        stats.setAnnualAppreciationRate(Math.round(cagr * 100.0) / 100.0);
        stats.setHighestPrice(highest);
        stats.setHighestDate(LocalDate.parse(highPeriod + "-01"));
        stats.setLowestPrice(lowest);
        stats.setLowestDate(LocalDate.parse(lowPeriod + "-01"));
        stats.setCurrentAverage(last);
        return stats;
    }
}
