package com.locus.service;

import com.locus.model.ValuationReport;

import java.util.List;

/**
 * Service for assembling valuation reports (UC-8).
 *
 * <p>Aggregates data from multiple analysis sources (FMV, rental yield, ROI,
 * price trends) into a single report object that the UI converts to PDF.</p>
 */
public interface ValuationReportService {

    /**
     * Assembles a complete valuation report for the given property.
     *
     * @param propertyId       the property to report on
     * @param includedSections list of section names to include
     *                         (e.g. "fmv", "comparables", "rental_yield", "roi", "price_trend", "heatmap")
     * @param analystNotes     free-text notes from the analyst
     * @return fully populated ValuationReport ready for PDF generation
     * @throws com.locus.exception.ValidationException if propertyId not found
     */
    ValuationReport assembleReport(String propertyId, List<String> includedSections, String analystNotes);
}
