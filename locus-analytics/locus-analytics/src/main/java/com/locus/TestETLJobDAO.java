package com.locus;

import com.locus.dao.ETLJobDAO;
import com.locus.dao.impl.ETLJobDAOImpl;
import com.locus.model.ETLJob;

import java.time.LocalDate;

public class TestETLJobDAO {

    public static void main(String[] args) {

        ETLJobDAO dao = new ETLJobDAOImpl();

        // ─────────────────────────────
        // 1. INSERT TEST
        // ─────────────────────────────
        ETLJob job = new ETLJob();

        job.setJobId("test-job-001");
        job.setAdminId("ANL01");
        job.setRunDate(LocalDate.now());

        job.setRecordsExtracted(100);
        job.setRecordsCleaned(90);
        job.setRecordsLoaded(85);
        job.setErrors(10);

        job.setStatus("running");
        job.setCurrentStage("Extracting");
        job.setProgressPercent(40);

        boolean inserted = dao.insert(job);

        System.out.println("INSERT SUCCESS = " + inserted);

        // ─────────────────────────────
        // 2. FIND ALL TEST
        // ─────────────────────────────
        System.out.println("\n--- ALL ETL JOBS ---");

        dao.findAll()
                .forEach(System.out::println);

        // ─────────────────────────────
        // 3. FIND LATEST TEST
        // ─────────────────────────────
        System.out.println("\n--- LATEST ETL JOB ---");

        System.out.println(dao.findLatest());
    }
}