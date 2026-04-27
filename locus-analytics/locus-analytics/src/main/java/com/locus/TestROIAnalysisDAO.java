package com.locus;

import com.locus.dao.ROIAnalysisDAO;
import com.locus.dao.impl.ROIAnalysisDAOImpl;
import com.locus.model.ROIAnalysis;
import java.time.LocalDateTime;
import java.time.LocalDate;

public class TestROIAnalysisDAO {

    public static void main(String[] args) {

        ROIAnalysisDAO dao = new ROIAnalysisDAOImpl();

        // ─────────────────────────────
        // 1. CREATE TEST OBJECT
        // ─────────────────────────────
        ROIAnalysis r = new ROIAnalysis();
        r.setPropertyId("P100");
        System.out.println("BEFORE INSERT: " + r.getPropertyId());


        r.setAnalysisDate(LocalDate.now());
        r.setPurchaseDate(LocalDate.of(2023, 1, 1));

        r.setPurchasePrice(5000000);
        r.setCurrentValue(8000000);

        r.setCumulativeRentalIncome(1200000);
        r.setTotalExpenses(300000);

        // derived values (you can compute later in service layer)
        r.setTotalReturn(3900000);
        r.setRoiPercentage(78.0);
        r.setAnnualizedROI(22.5);
        r.setCreatedAt(LocalDateTime.now());
        r.setUpdatedAt(LocalDateTime.now());
        //dao.insert(r);

        // ─────────────────────────────
        // 2. INSERT TEST
        // ─────────────────────────────
        System.out.println("INSERT RESULT: " + dao.insert(r));
        System.out.println("FETCH TEST: " + dao.findByProperty(r.getPropertyId()));

        // ─────────────────────────────
        // 3. FETCH TEST
        // ─────────────────────────────
        System.out.println(dao.findByProperty("P100"));
    }
}