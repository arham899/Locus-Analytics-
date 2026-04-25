package com.locus.service.impl;

import com.locus.model.ETLJob;
import com.locus.service.ETLService;

import java.time.LocalDate;

/**
 * Stub implementation of {@link ETLService}.
 */
public class ETLServiceStub implements ETLService {

    @Override
    public ETLJob triggerPipeline() {
        ETLJob job = new ETLJob();
        job.setStatus("running");
        job.setCurrentStage("Extracting");
        job.setProgressPercent(0);
        return job;
    }

    @Override
    public ETLJob getCurrentStatus(String jobId) {
        ETLJob job = new ETLJob();
        job.setJobId(jobId);
        job.setStatus("success");
        job.setCurrentStage("Loading");
        job.setProgressPercent(100);
        job.setRecordsExtracted(350);
        job.setRecordsCleaned(340);
        job.setRecordsLoaded(335);
        job.setErrors(5);
        return job;
    }

    @Override
    public ETLJob getLastRunSummary() {
        ETLJob job = new ETLJob();
        job.setRunDate(LocalDate.now().minusDays(3));
        job.setStatus("success");
        job.setRecordsExtracted(500);
        job.setRecordsCleaned(490);
        job.setRecordsLoaded(485);
        job.setErrors(10);
        return job;
    }
}
