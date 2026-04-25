package com.locus.service;

import com.locus.model.RentalAnalysis;

/**
 * Service for calculating rental yield of properties (UC-2).
 *
 * <p>Gross yield = (annual rent / property value) × 100.<br>
 * Net yield = ((annual rent − expenses) / property value) × 100.<br>
 * Also provides city/locality average comparisons.</p>
 */
public interface RentalYieldService {

    /**
     * Calculates gross and net rental yield for a property.
     *
     * @param propertyValue  current market value of the property (must be &gt; 0)
     * @param monthlyRent    expected monthly rent (must be &gt; 0)
     * @param annualExpenses annual maintenance/expenses (must be &ge; 0)
     * @return RentalAnalysis with computed yields and city average comparison
     * @throws com.locus.exception.ValidationException if rent &gt; propertyValue or values negative
     */
    RentalAnalysis calculate(double propertyValue, double monthlyRent, double annualExpenses);

    /**
     * Retrieves the average rental yield for a city/locality for benchmarking.
     * Falls back to city-level average if locality data is insufficient (extension 6a).
     *
     * @param city     city name (Karachi, Islamabad, or Lahore)
     * @param locality locality name (optional, can be null for city-level)
     * @return average rental yield percentage
     */
    double getCityAverageYield(String city, String locality);
}
