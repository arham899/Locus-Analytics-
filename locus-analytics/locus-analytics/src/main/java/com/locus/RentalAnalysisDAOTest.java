package com.locus;

import com.locus.dao.RentalAnalysisDAO;
import com.locus.dao.impl.RentalAnalysisDAOImpl;
import com.locus.model.RentalAnalysis;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class RentalAnalysisDAOTest {

    public static void main(String[] args) {

        RentalAnalysisDAO dao = new RentalAnalysisDAOImpl();

        System.out.println("INSERT TEST");

        RentalAnalysis r = new RentalAnalysis();

        r.setAnalysisId("A001");
        r.setPropertyId("P100");

        r.setExpectedRent(600000);     // annual_rent
        r.setAnnualExpenses(80000);

        r.setGrossYield(8.5);
        r.setNetYield(7.2);

        r.setAnalystId("ANL01");
        r.setAnalysisDate(LocalDate.now());

        r.setCreatedAt(LocalDateTime.now());
        r.setUpdatedAt(LocalDateTime.now());

        dao.insert(r);

        System.out.println("\nFIND BY PROPERTY");

        List<RentalAnalysis> list = dao.findByProperty("P100");

        for (RentalAnalysis x : list) {
            System.out.println(
                    x.getAnalysisId() + " | " +
                            x.getExpectedRent() + " | " +
                            x.getNetYield()
            );
        }

        System.out.println("\nLATEST");

        RentalAnalysis latest = dao.findLatestByProperty("P100");

        System.out.println(latest != null
                ? latest.getAnalysisId() + " | " + latest.getGrossYield()
                : "No data");
    }
}