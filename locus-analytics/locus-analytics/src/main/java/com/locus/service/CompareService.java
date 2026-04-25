package com.locus.service;

import com.locus.model.dto.ComparisonResult;

import java.util.List;

/**
 * Service for side-by-side comparison of 2–4 properties (UC-5).
 *
 * <p>Computes best/worst flags per metric (price, area, price per sq.ft., etc.)
 * and optionally cross-references existing FMV valuations.</p>
 */
public interface CompareService {

    /**
     * Compares the specified properties side-by-side.
     *
     * @param propertyIds list of 2–4 property IDs to compare
     * @return ComparisonResult with properties and per-metric best/worst flags
     * @throws com.locus.exception.ValidationException if fewer than 2 or more than 4
     *         property IDs are provided (extensions 3a/3b)
     */
    ComparisonResult compare(List<String> propertyIds);
}
