package com.locus;

import com.locus.dao.InvestmentClusterDAO;
import com.locus.dao.impl.InvestmentClusterDAOImpl;
import com.locus.model.InvestmentCluster;

public class TestInvestmentClusterDAO {

    public static void main(String[] args) {

        InvestmentClusterDAO dao = new InvestmentClusterDAOImpl();

        // ─────────────────────────────
        // 1. INSERT TEST
        // ─────────────────────────────
        InvestmentCluster c = new InvestmentCluster();

        c.setCity("Lahore");
        c.setLocality("DHA Phase 6");

        c.setInvestmentScore(85.5);
        c.setPriceAppreciation(0.12);
        c.setListingVolumeGrowth(0.08);
        c.setRentalTrend(0.10);

        boolean inserted = dao.insert(c);

        System.out.println("INSERT SUCCESS = " + inserted);

        // ─────────────────────────────
        // 2. FIND BY CITY TEST
        // ─────────────────────────────
        System.out.println("\n--- Clusters in Lahore ---");

        dao.findByCity("Lahore")
                .forEach(System.out::println);

        // ─────────────────────────────
        // 3. TOP CLUSTERS TEST
        // ─────────────────────────────
        System.out.println("\n--- TOP 3 CLUSTERS ---");

        dao.findTopClusters(3)
                .forEach(System.out::println);
    }
}