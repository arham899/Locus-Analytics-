package com.locus;

import com.locus.dao.ValuationDAO;
import com.locus.dao.impl.ValuationDAOImpl;
import com.locus.model.Valuation;

import java.time.LocalDateTime;
import java.util.List;

public class TestValuationDAO {

    public static void main(String[] args) {

        ValuationDAO dao = new ValuationDAOImpl();

        // 1. INSERT TEST
        Valuation v = new Valuation();
        v.setPropertyId("P100");

        v.setEstimatedFmv(8500000);
        v.setConfidenceIntervalLow(8000000);
        v.setConfidenceIntervalHigh(9000000);

        v.setCreatedAt(LocalDateTime.now());
        v.setUpdatedAt(LocalDateTime.now());

        System.out.println("Insert: " + dao.insert(v));

        System.out.println(dao.findByPropertyId("P100"));

        System.out.println(
                dao.findByDateRange(
                        LocalDateTime.now().minusDays(7),
                        LocalDateTime.now()
                )
        );
    }
}
