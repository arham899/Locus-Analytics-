package com.locus.service.impl;

import com.locus.dao.PropertyDAO;
import com.locus.dao.ValuationDAO;
import com.locus.model.ValuationReport;
import com.locus.service.ValuationReportService;
import com.locus.service.validation.InputValidator;

import java.time.LocalDate;
import java.util.List;

/**
 * Real implementation of {@link ValuationReportService}.
 */
public class ValuationReportServiceImpl implements ValuationReportService {

    private final PropertyDAO propertyDAO;
    private final ValuationDAO valuationDAO;

    public ValuationReportServiceImpl(PropertyDAO propertyDAO, ValuationDAO valuationDAO) {
        this.propertyDAO = propertyDAO;
        this.valuationDAO = valuationDAO;
    }

    @Override
    public ValuationReport assembleReport(String propertyId, List<String> sections, String notes) {
        new InputValidator()
                .validateNotNull("propertyId", propertyId)
                .throwIfInvalid();

        propertyDAO.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Property not found for ID: " + propertyId));

        // Fetch latest valuation for this property (kept for future embedding in report)
        valuationDAO.findByPropertyId(propertyId);

        ValuationReport report = new ValuationReport();
        report.setPropertyId(propertyId);
        report.setAnalystId("analyst-system"); // Typically retrieved from session
        report.setGenerationDate(LocalDate.now());
        report.setIncludedSections(sections);
        report.setAnalystNotes(notes != null ? notes : "System generated report.");
        
        // A complete report would embed the Property and Valuation details.
        // For the current model, returning the configured object is enough.
        // PDF generation happens at the UI level.
        
        return report;
    }
}
