package com.locus.service;

import com.locus.model.dto.TimeRange;
import com.locus.model.dto.TrendPoint;
import com.locus.model.dto.TrendStatistics;

import java.util.List;

/**
 * Service for analyzing property price trends over time (UC-6).
 *
 * <p>Aggregates monthly price data and computes statistics including annual
 * appreciation rate (CAGR), highs, lows, and current averages.</p>
 */
public interface PriceTrendService {

    /**
     * Retrieves monthly price trend data for the given filters and time range.
     * Falls back to city-level data if locality has fewer than 6 data points (extension 4a).
     *
     * @param city         city name (required)
     * @param locality     locality name (optional — null for city-wide trend)
     * @param propertyType property type filter (optional — null for all types)
     * @param timeRange    predefined or custom time range
     * @return list of monthly trend data points
     */
    List<TrendPoint> getTrend(String city, String locality, String propertyType, TimeRange timeRange);

    /**
     * Computes summary statistics from a list of trend data points.
     *
     * @param trendPoints the trend time series
     * @return statistics including CAGR, highest/lowest prices and dates, current average
     */
    TrendStatistics computeStatistics(List<TrendPoint> trendPoints);
}
