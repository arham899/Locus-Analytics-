package com.locus.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an ETL (Extract-Transform-Load) pipeline job run.
 * Maps to the {@code etl_job} table in the database.
 *
 * <p>Tracks the progress and results of data scraping operations from Zameen.com,
 * including record counts per stage and error tracking.</p>
 */
public class ETLJob {

    private String jobId;
    private String adminId;
    private LocalDate runDate;
    private int recordsExtracted;
    private int recordsCleaned;
    private int recordsLoaded;
    private int errors;
    private String status;        // 'success', 'failed', 'running'
    private String currentStage;  // Extracting, Transforming, Loading
    private int progressPercent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Constructors ──────────────────────────────────────────────────

    /** Default no-arg constructor. Generates a random UUID for jobId. */
    public ETLJob() {
        this.jobId = UUID.randomUUID().toString();
        this.runDate = LocalDate.now();
        this.status = "running";
        this.currentStage = "Extracting";
        this.progressPercent = 0;
    }

    /** Full constructor for database retrieval. */
    public ETLJob(String jobId, String adminId, LocalDate runDate,
                  int recordsExtracted, int recordsCleaned, int recordsLoaded,
                  int errors, String status) {
        this.jobId = jobId;
        this.adminId = adminId;
        this.runDate = runDate;
        this.recordsExtracted = recordsExtracted;
        this.recordsCleaned = recordsCleaned;
        this.recordsLoaded = recordsLoaded;
        this.errors = errors;
        this.status = status;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public LocalDate getRunDate() {
        return runDate;
    }

    public void setRunDate(LocalDate runDate) {
        this.runDate = runDate;
    }

    public int getRecordsExtracted() {
        return recordsExtracted;
    }

    public void setRecordsExtracted(int recordsExtracted) {
        this.recordsExtracted = recordsExtracted;
    }

    public int getRecordsCleaned() {
        return recordsCleaned;
    }

    public void setRecordsCleaned(int recordsCleaned) {
        this.recordsCleaned = recordsCleaned;
    }

    public int getRecordsLoaded() {
        return recordsLoaded;
    }

    public void setRecordsLoaded(int recordsLoaded) {
        this.recordsLoaded = recordsLoaded;
    }

    public int getErrors() {
        return errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(String currentStage) {
        this.currentStage = currentStage;
    }

    public int getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(int progressPercent) {
        this.progressPercent = progressPercent;
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

    // ── equals / hashCode / toString ──────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ETLJob etlJob = (ETLJob) o;
        return Objects.equals(jobId, etlJob.jobId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId);
    }

    @Override
    public String toString() {
        return "ETLJob{" +
                "jobId='" + jobId + '\'' +
                ", status='" + status + '\'' +
                ", stage='" + currentStage + '\'' +
                ", progress=" + progressPercent + "%" +
                ", extracted=" + recordsExtracted +
                ", cleaned=" + recordsCleaned +
                ", loaded=" + recordsLoaded +
                ", errors=" + errors +
                '}';
    }
}