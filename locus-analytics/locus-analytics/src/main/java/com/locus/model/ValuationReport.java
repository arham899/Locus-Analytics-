package com.locus.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a generated valuation report for a property.
 * Maps to the {@code valuation_report} table in the database.
 *
 * <p>A report aggregates multiple analysis results (FMV, rental yield, ROI, etc.)
 * into a single PDF document with analyst annotations.</p>
 */
public class ValuationReport {

    private String reportId;
    private String propertyId;
    private String analystId;
    private LocalDate generationDate;
    private List<String> includedSections;
    private String analystNotes;
    private String pdfFilePath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Constructors ──────────────────────────────────────────────────

    /** Default no-arg constructor. Generates a random UUID for reportId. */
    public ValuationReport() {
        this.reportId = UUID.randomUUID().toString();
        this.generationDate = LocalDate.now();
        this.includedSections = new ArrayList<>();
    }

    /** Full constructor for database retrieval. */
    public ValuationReport(String reportId, String propertyId, String analystId,
                           LocalDate generationDate, String includedSections,
                           String analystNotes, String pdfFilePath) {
        this.reportId = reportId;
        this.propertyId = propertyId;
        this.analystId = analystId;
        this.generationDate = generationDate;
        this.includedSections = parseSections(includedSections);
        this.analystNotes = analystNotes;
        this.pdfFilePath = pdfFilePath;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public String getAnalystId() {
        return analystId;
    }

    public void setAnalystId(String analystId) {
        this.analystId = analystId;
    }

    public LocalDate getGenerationDate() {
        return generationDate;
    }

    public void setGenerationDate(LocalDate generationDate) {
        this.generationDate = generationDate;
    }

    public List<String> getIncludedSections() {
        return includedSections;
    }

    public void setIncludedSections(List<String> includedSections) {
        this.includedSections = includedSections;
    }

    /**
     * Returns sections as a comma-separated string for database storage.
     */
    public String getIncludedSectionsAsString() {
        return (includedSections != null) ? String.join(",", includedSections) : "";
    }

    /**
     * Sets sections from a comma-separated string (for database retrieval).
     */
    public void setIncludedSectionsFromString(String sections) {
        this.includedSections = parseSections(sections);
    }

    public String getAnalystNotes() {
        return analystNotes;
    }

    public void setAnalystNotes(String analystNotes) {
        this.analystNotes = analystNotes;
    }

    public String getPdfFilePath() {
        return pdfFilePath;
    }

    public void setPdfFilePath(String pdfFilePath) {
        this.pdfFilePath = pdfFilePath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ── Helper Methods ────────────────────────────────────────────────

    private static List<String> parseSections(String sections) {
        List<String> list = new ArrayList<>();
        if (sections != null && !sections.isBlank()) {
            for (String s : sections.split(",")) {
                list.add(s.trim());
            }
        }
        return list;
    }

    // ── equals / hashCode / toString ──────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValuationReport that = (ValuationReport) o;
        return Objects.equals(reportId, that.reportId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportId);
    }

    @Override
    public String toString() {
        return "ValuationReport{" +
                "reportId='" + reportId + '\'' +
                ", propertyId='" + propertyId + '\'' +
                ", generationDate=" + generationDate +
                ", sections=" + includedSections +
                ", pdfFilePath='" + pdfFilePath + '\'' +
                '}';
    }
}
