package com.locus.service.impl;

import com.locus.dao.PropertyDAO;
import com.locus.dao.ROIAnalysisDAO;
import com.locus.dao.RentalAnalysisDAO;
import com.locus.dao.ValuationDAO;
import com.locus.dao.ValuationReportDAO;
import com.locus.model.Property;
import com.locus.model.ROIAnalysis;
import com.locus.model.RentalAnalysis;
import com.locus.model.Valuation;
import com.locus.model.ValuationReport;
import com.locus.model.dto.TimeRange;
import com.locus.model.dto.TrendPoint;
import com.locus.model.dto.TrendStatistics;
import com.locus.service.PriceTrendService;
import com.locus.service.ValuationReportService;
import com.locus.service.validation.InputValidator;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Full implementation of {@link ValuationReportService} (UC-8).
 *
 * <p>Fetches and embeds data for each requested section into the
 * {@link ValuationReport} DTO so the UI layer can render the PDF
 * without making any additional service calls.</p>
 *
 * <p>Sections that have not yet been computed (no persisted analysis)
 * are silently skipped — the corresponding transient field is left null,
 * and the UI renders a "Not calculated" notice for that section.</p>
 *
 * @author Arham Manzoor (24i-0640)
 */
public class ValuationReportServiceImpl implements ValuationReportService {

    private final PropertyDAO propertyDAO;
    private final ValuationDAO valuationDAO;
    private final RentalAnalysisDAO rentalAnalysisDAO;
    private final ROIAnalysisDAO roiAnalysisDAO;
    private final PriceTrendService priceTrendService;
    private final ValuationReportDAO reportDAO;

    public ValuationReportServiceImpl(PropertyDAO propertyDAO,
                                      ValuationDAO valuationDAO,
                                      RentalAnalysisDAO rentalAnalysisDAO,
                                      ROIAnalysisDAO roiAnalysisDAO,
                                      PriceTrendService priceTrendService,
                                      ValuationReportDAO reportDAO) {
        this.propertyDAO = propertyDAO;
        this.valuationDAO = valuationDAO;
        this.rentalAnalysisDAO = rentalAnalysisDAO;
        this.roiAnalysisDAO = roiAnalysisDAO;
        this.priceTrendService = priceTrendService;
        this.reportDAO = reportDAO;
    }

    @Override
    public ValuationReport assembleReport(String propertyId,
                                          List<String> includedSections,
                                          String analystNotes) {

        new InputValidator()
                .validateNotBlank("propertyId", propertyId)
                .throwIfInvalid();

        Property property = propertyDAO.findById(propertyId)
                .orElseThrow(() -> new com.locus.exception.ValidationException(
                        "Property not found for ID: " + propertyId));

        ValuationReport report = new ValuationReport();
        report.setPropertyId(propertyId);
        report.setAnalystId("analyst-system");
        report.setGenerationDate(LocalDate.now());
        report.setIncludedSections(includedSections != null ? includedSections : Collections.emptyList());
        report.setAnalystNotes(analystNotes != null ? analystNotes : "");

        report.setProperty(property);

        List<String> sections = report.getIncludedSections();

        if (sections.contains("fmv") || sections.contains("comparables")) {
            populateFmvSection(report, property, sections);
        }

        if (sections.contains("rental_yield")) {
            populateRentalSection(report, propertyId);
        }

        if (sections.contains("roi")) {
            populateRoiSection(report, propertyId);
        }

        if (sections.contains("price_trend")) {
            populatePriceTrendSection(report, property);
        }

        try {
            reportDAO.insert(report);
        } catch (Exception e) {
            System.err.println("[ReportService] Warning: could not persist report metadata: " + e.getMessage());
        }

        return report;
    }

    private void populateFmvSection(ValuationReport report, Property property, List<String> sections) {
        if (sections.contains("fmv")) {
            try {
                Valuation v = valuationDAO.findByPropertyId(property.getPropertyId());
                report.setValuation(v);
            } catch (Exception e) {
                System.err.println("[ReportService] No FMV found for property " + property.getPropertyId());
            }
        }

        if (sections.contains("comparables")) {
            try {
                List<Property> comps = propertyDAO.findComparables(
                        property.getCity(),
                        property.getLocality(),
                        property.getPropertyType(),
                        property.getArea()
                );
                report.setComparables(comps);
            } catch (Exception e) {
                System.err.println("[ReportService] Could not fetch comparables: " + e.getMessage());
            }
        }
    }

    private void populateRentalSection(ValuationReport report, String propertyId) {
        try {
            RentalAnalysis ra = rentalAnalysisDAO.findLatestByProperty(propertyId);
            report.setRentalAnalysis(ra);
        } catch (Exception e) {
            System.err.println("[ReportService] No rental analysis found for property " + propertyId);
        }
    }

    private void populateRoiSection(ValuationReport report, String propertyId) {
        try {
            ROIAnalysis roi = roiAnalysisDAO.findByProperty(propertyId);
            report.setRoiAnalysis(roi);
        } catch (Exception e) {
            System.err.println("[ReportService] No ROI analysis found for property " + propertyId);
        }
    }

    private void populatePriceTrendSection(ValuationReport report, Property property) {
        try {
            List<TrendPoint> points = priceTrendService.getTrend(
                    property.getCity(),
                    property.getLocality(),
                    property.getPropertyType(),
                    TimeRange.ONE_YEAR
            );
            report.setPriceTrendPoints(points);

            if (!points.isEmpty()) {
                TrendStatistics stats = priceTrendService.computeStatistics(points);
                report.setTrendStatistics(stats);
            }
        } catch (Exception e) {
            System.err.println("[ReportService] Could not fetch price trend: " + e.getMessage());
        }
    }
}
