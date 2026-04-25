package com.locus.service.impl;

import com.locus.model.InvestmentCluster;
import com.locus.model.dto.ClusterParams;
import com.locus.service.InvestmentClusterService;

import java.util.ArrayList;
import java.util.List;

/**
 * Stub implementation of {@link InvestmentClusterService}.
 */
public class InvestmentClusterServiceStub implements InvestmentClusterService {

    @Override
    public List<InvestmentCluster> identifyClusters(ClusterParams params) {
        List<InvestmentCluster> clusters = new ArrayList<>();
        String city = params.getCity() != null ? params.getCity() : "Karachi";

        if ("Karachi".equalsIgnoreCase(city)) {
            clusters.add(makeCluster(city, "DHA Phase 6", 92, 15.2, 22.5, 8.1));
            clusters.add(makeCluster(city, "Bahria Town", 85, 12.8, 18.3, 7.5));
            clusters.add(makeCluster(city, "Clifton", 78, 10.5, 14.2, 6.8));
            clusters.add(makeCluster(city, "PECHS", 65, 8.2, 10.1, 5.5));
        } else if ("Islamabad".equalsIgnoreCase(city)) {
            clusters.add(makeCluster(city, "DHA Islamabad", 95, 18.5, 25.0, 9.2));
            clusters.add(makeCluster(city, "Bahria Town", 88, 14.2, 20.1, 8.0));
            clusters.add(makeCluster(city, "F-7", 82, 11.0, 12.5, 7.2));
        } else {
            clusters.add(makeCluster(city, "DHA Phase 5", 90, 16.0, 21.0, 8.5));
            clusters.add(makeCluster(city, "Bahria Town", 83, 13.5, 17.8, 7.8));
            clusters.add(makeCluster(city, "Gulberg", 72, 9.0, 11.5, 6.0));
        }
        return clusters;
    }

    private InvestmentCluster makeCluster(String city, String locality,
            double score, double appreciation, double volume, double rental) {
        InvestmentCluster c = new InvestmentCluster();
        c.setCity(city);
        c.setLocality(locality);
        c.setInvestmentScore(score);
        c.setPriceAppreciation(appreciation);
        c.setListingVolumeGrowth(volume);
        c.setRentalTrend(rental);
        return c;
    }
}
