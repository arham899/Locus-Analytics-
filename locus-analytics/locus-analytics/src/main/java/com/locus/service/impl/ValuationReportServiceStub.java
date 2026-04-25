package com.locus.service.impl;

import com.locus.model.ValuationReport;
import com.locus.service.ValuationReportService;

import java.time.LocalDate;
import java.util.List;

/**
 * Stub implementation of {@link ValuationReportService}.
 */
public class ValuationReportServiceStub implements ValuationReportService {

    @Override
    public ValuationReport assembleReport(String propertyId, List<String> sections, String notes) {
        ValuationReport r = new ValuationReport();
        r.setPropertyId(propertyId);
        r.setAnalystId("analyst-001");
        r.setGenerationDate(LocalDate.now());
        r.setIncludedSections(sections);
        r.setAnalystNotes(notes != null ? notes : "No additional notes.");
        return r;
    }
}
