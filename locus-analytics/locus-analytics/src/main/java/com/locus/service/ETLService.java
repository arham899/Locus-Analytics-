package com.locus.service;

import com.locus.model.ETLJob;

/**
 * Service for managing the ETL (Extract-Transform-Load) pipeline (UC-10).
 *
 * <p>Triggers the Python scraper via ProcessBuilder, polls job status
 * for real-time progress updates, and provides run summaries.</p>
 */
public interface ETLService {

    /**
     * Triggers a new ETL pipeline run.
     *
     * @return the created ETLJob with status "running"
     */
    ETLJob triggerPipeline();

    /**
     * Gets the current status of a running or completed ETL job.
     * Used for polling the progress bar (every 2 seconds).
     *
     * @param jobId the job identifier
     * @return current ETLJob state with progress, stage, and record counts
     */
    ETLJob getCurrentStatus(String jobId);

    /**
     * Gets a summary of the most recent ETL job run.
     *
     * @return the last completed ETLJob, or null if no jobs have run
     */
    ETLJob getLastRunSummary();
}
