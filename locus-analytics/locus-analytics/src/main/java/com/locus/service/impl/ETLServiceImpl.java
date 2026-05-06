package com.locus.service.impl;

import com.locus.dao.ETLJobDAO;
import com.locus.model.ETLJob;
import com.locus.service.ETLService;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Real implementation of {@link ETLService}.
 */
public class ETLServiceImpl implements ETLService {

    private final ETLJobDAO etlJobDAO;

    public ETLServiceImpl(ETLJobDAO etlJobDAO) {
        this.etlJobDAO = etlJobDAO;
    }

    @Override
    public ETLJob triggerPipeline() {
        ETLJob job = new ETLJob();
        job.setJobId("etl-" + UUID.randomUUID().toString().substring(0, 8));
        job.setRunDate(LocalDate.now());
        job.setStatus("running");
        job.setCurrentStage("Extracting");
        job.setProgressPercent(0);
        
        etlJobDAO.insert(job);
        
        // Launch Python ETL script asynchronously
        new Thread(() -> runPythonScript(job)).start();
        
        return job;
    }

    private void runPythonScript(ETLJob job) {
        try {
            // Note: This relies on python being in PATH and the script being at etl/run_etl.py
            ProcessBuilder pb = new ProcessBuilder("python", "etl/run_etl.py");
            pb.directory(new File(".")); 
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[ETL-SCRIPT] " + line);
                
                // Polling the database to get real progress updates from the Python script
                ETLJob currentStatus = etlJobDAO.findLatest(); // Simplified: assume only one run at a time
                if (currentStatus != null) {
                    job.setStatus(currentStatus.getStatus());
                    job.setCurrentStage(currentStatus.getCurrentStage());
                    job.setProgressPercent(currentStatus.getProgressPercent());
                    job.setRecordsExtracted(currentStatus.getRecordsExtracted());
                    job.setRecordsLoaded(currentStatus.getRecordsLoaded());
                }
            }
            
            int exitCode = process.waitFor();
            
            ETLJob finalSummary = etlJobDAO.findLatest();
            if (exitCode == 0 && finalSummary != null) {
                job.setStatus("success");
                job.setCurrentStage("Finished");
                job.setProgressPercent(100);
                job.setRecordsExtracted(finalSummary.getRecordsExtracted());
                job.setRecordsCleaned(finalSummary.getRecordsCleaned());
                job.setRecordsLoaded(finalSummary.getRecordsLoaded());
                job.setErrors(finalSummary.getErrors());
            } else {
                job.setStatus("failed");
                job.setErrors(1);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            job.setStatus("failed");
            job.setErrors(1);
        }
    }

    @Override
    public ETLJob getCurrentStatus(String jobId) {
        // In a complete implementation we'd find by ID, but for now we'll just get latest
        return etlJobDAO.findLatest();
    }

    @Override
    public ETLJob getLastRunSummary() {
        return etlJobDAO.findLatest();
    }
}
