package com.locus.service.impl;

import com.locus.dao.InvestmentClusterDAO;
import com.locus.dao.PropertyDAO;
import com.locus.model.InvestmentCluster;
import com.locus.model.Property;
import com.locus.model.dto.ClusterParams;
import com.locus.model.dto.SearchFilter;
import com.locus.service.InvestmentClusterService;
import com.locus.service.validation.InputValidator;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Real implementation of {@link InvestmentClusterService} (UC-9).
 *
 * <p>Identifies high-potential investment localities by computing a composite
 * Investment Score = 0.5 × priceAppreciation + 0.3 × volumeGrowth + 0.2 × rentalTrend,
 * normalized to 0–100.</p>
 *
 * @author Arham Manzoor (24i-0640)
 */
public class InvestmentClusterServiceImpl implements InvestmentClusterService {

    private final PropertyDAO propertyDAO;
    private final InvestmentClusterDAO clusterDAO;
    private final com.locus.dao.RentalAnalysisDAO rentalAnalysisDAO;

    public InvestmentClusterServiceImpl(PropertyDAO propertyDAO, InvestmentClusterDAO clusterDAO, 
                                        com.locus.dao.RentalAnalysisDAO rentalAnalysisDAO) {
        this.propertyDAO = propertyDAO;
        this.clusterDAO = clusterDAO;
        this.rentalAnalysisDAO = rentalAnalysisDAO;
    }

    @Override
    public List<InvestmentCluster> identifyClusters(ClusterParams params) {

        // ── Validation ──────────────────────────────
        if (params == null) {
            throw new com.locus.exception.ValidationException("Parameters are required", java.util.Map.of("params", "Parameters are required"));
        }

        new InputValidator()
                .validateCity(params.getCity())
                .throwIfInvalid();

        int periodYears = params.getAnalysisPeriodYears();
        int minListings = params.getMinListingCount();

        // ── Fetch metrics from DB ───────────────────
        List<com.locus.model.dto.LocalityMetric> metrics = 
                propertyDAO.getLocalityMetrics(params.getCity(), periodYears, minListings);

        if (metrics.isEmpty()) {
            return Collections.emptyList();
        }

        List<InvestmentCluster> clusters = new ArrayList<>();

        for (com.locus.model.dto.LocalityMetric m : metrics) {
            String locality = m.getLocality();
            double priceAppreciation = m.getPriceAppreciation();
            double volumeGrowth = m.getVolumeGrowth();

            // ── Real Rental Trend ───────────────────
            // We compare current yield vs city benchmark to see demand
            double localityYield = rentalAnalysisDAO.getLocalityAverageYield(params.getCity(), locality);
            double cityYield = rentalAnalysisDAO.getCityAverageYield(params.getCity());
            
            double rentalTrend = (cityYield > 0) ? (localityYield / cityYield) * 10.0 : 5.0;

            // ── Composite Score ─────────────────────
            // 0.5 * priceAppreciation + 0.3 * volumeGrowth + 0.2 * rentalTrend
            double rawScore = (0.5 * priceAppreciation) + (0.3 * volumeGrowth) + (0.2 * rentalTrend);

            InvestmentCluster cluster = new InvestmentCluster();
            cluster.setCity(params.getCity());
            cluster.setLocality(locality);
            cluster.setPriceAppreciation(Math.round(priceAppreciation * 100.0) / 100.0);
            cluster.setListingVolumeGrowth(Math.round(volumeGrowth * 100.0) / 100.0);
            cluster.setRentalTrend(Math.round(rentalTrend * 100.0) / 100.0);
            cluster.setInvestmentScore(rawScore); 

            clusters.add(cluster);
        }

        // ── Sort descending by score BEFORE rounding ───────
        clusters.sort(Comparator.comparingDouble(InvestmentCluster::getInvestmentScore).reversed());

        // ── Normalize and Round scores to 0–100 ───────────────
        if (!clusters.isEmpty()) {
            double maxScore = clusters.stream()
                    .mapToDouble(InvestmentCluster::getInvestmentScore).max().orElse(1.0);
            double minScore = clusters.stream()
                    .mapToDouble(InvestmentCluster::getInvestmentScore).min().orElse(0.0);
            double range = maxScore - minScore;

            for (InvestmentCluster c : clusters) {
                double normalized = (range > 0)
                        ? ((c.getInvestmentScore() - minScore) / range) * 100.0
                        : 50.0;
                c.setInvestmentScore(Math.round(normalized * 100.0) / 100.0);
            }
        }

        // ── Persist top results ─────────────────────
        for (InvestmentCluster cluster : clusters) {
            try {
                clusterDAO.insert(cluster);
            } catch (Exception e) {
                System.err.println("[ClusterService] Warning: could not persist cluster: " + e.getMessage());
            }
        }

        return clusters;
    }
}
