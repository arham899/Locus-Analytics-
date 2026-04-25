package com.locus.service;

import com.locus.model.Property;
import com.locus.model.Valuation;

import java.util.List;

/**
 * Service for estimating Fair Market Value (FMV) of properties (UC-1).
 *
 * <p>Uses a trained ML model (linear regression) to predict FMV, enhanced with
 * comparable property analysis. Returns confidence intervals and key factors.</p>
 */
public interface ValuationService {

    /**
     * Estimates the Fair Market Value of a property using the ML predictor.
     *
     * @param property the property to estimate (must have city, locality, type, area)
     * @return a Valuation containing estimated FMV, confidence interval, and key factors
     * @throws com.locus.exception.ValidationException if property fields are invalid
     *         (e.g. unsupported city or locality — extension 3a)
     */
    Valuation estimateFMV(Property property);

    /**
     * Finds comparable properties for a given property.
     * Comparables share the same city, locality, and type with area within ±20%.
     *
     * @param property the reference property
     * @return list of up to 5 comparable properties sorted by recency
     */
    List<Property> findComparables(Property property);
}
