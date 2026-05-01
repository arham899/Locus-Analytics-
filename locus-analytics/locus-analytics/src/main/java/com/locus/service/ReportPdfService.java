package com.locus.service;

import com.locus.model.ValuationReport;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Generates PDF documents for valuation reports.
 */
public interface ReportPdfService {

    /**
     * Generates a PDF file for the provided report and returns its path.
     */
    Path generatePdf(ValuationReport report) throws IOException;
}
