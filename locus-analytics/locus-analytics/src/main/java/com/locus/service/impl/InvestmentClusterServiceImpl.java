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

    public InvestmentClusterServiceImpl(PropertyDAO propertyDAO, InvestmentClusterDAO clusterDAO) {
        this.propertyDAO = propertyDAO;
        this.clusterDAO = clusterDAO;
    }

    @Override
    public List<InvestmentCluster> identifyClusters(ClusterParams params) {

        // ── Validation ──────────────────────────────
        new InputValidator()
                .validateNotNull("params", params)
                .validateCity(params.getCity())
                .throwIfInvalid();

        int periodYears = params.getAnalysisPeriodYears();
        int minListings = params.getMinListingCount();

        LocalDate now = LocalDate.now();
        LocalDate midpoint = now.minusYears(periodYears / 2);
        LocalDate start = now.minusYears(periodYears);

        // ── Fetch all properties in city ────────────
        SearchFilter recentFilter = new SearchFilter();
        recentFilter.setCity(params.getCity());
        if (params.getPropertyType() != null && !params.getPropertyType().isBlank()) {
            recentFilter.setPropertyType(params.getPropertyType());
        }
        recentFilter.setPageSize(10000);

        List<Property> allProperties = propertyDAO.search(recentFilter);

        if (allProperties.isEmpty()) {
            return Collections.emptyList();
        }

        // ── Group by locality ───────────────────────
        Map<String, List<Property>> byLocality = allProperties.stream()
                .filter(p -> p.getLocality() != null)
                .collect(Collectors.groupingBy(Property::getLocality));

        // ── Compute metrics per locality ────────────
        List<InvestmentCluster> clusters = new ArrayList<>();

        for (Map.Entry<String, List<Property>> entry : byLocality.entrySet()) {
            String locality = entry.getKey();
            List<Property> props = entry.getValue();

            // Filter out localities below minimum listing count
            if (props.size() < minListings) {
                continue;
            }

            // Split into "old" and "recent" halves based on listing date
            List<Property> oldProps = props.stream()
                    .filter(p -> p.getListingDate() != null && p.getListingDate().isBefore(midpoint))
                    .collect(Collectors.toList());

            List<Property> recentProps = props.stream()
                    .filter(p -> p.getListingDate() != null && !p.getListingDate().isBefore(midpoint))
                    .collect(Collectors.toList());

            // Price appreciation
            double oldAvgPrice = oldProps.stream().mapToDouble(Property::getPrice).average().orElse(0);
            double recentAvgPrice = recentProps.stream().mapToDouble(Property::getPrice).average().orElse(0);
            double priceAppreciation = (oldAvgPrice > 0)
                    ? ((recentAvgPrice - oldAvgPrice) / oldAvgPrice) * 100.0
                    : 0;

            // Volume growth
            double oldCount = oldProps.size();
            double recentCount = recentProps.size();
            double volumeGrowth = (oldCount > 0)
                    ? ((recentCount - oldCount) / oldCount) * 100.0
                    : 0;

            // Rental trend (placeholder — would need rental data)
            double rentalTrend = 0;

            // Composite score
            double rawScore = 0.5 * priceAppreciation + 0.3 * volumeGrowth + 0.2 * rentalTrend;

            InvestmentCluster cluster = new InvestmentCluster();
            cluster.setCity(params.getCity());
            cluster.setLocality(locality);
            cluster.setPriceAppreciation(Math.round(priceAppreciation * 100.0) / 100.0);
            cluster.setListingVolumeGrowth(Math.round(volumeGrowth * 100.0) / 100.0);
            cluster.setRentalTrend(rentalTrend);
            cluster.setInvestmentScore(rawScore); // Will be normalized below

            clusters.add(cluster);
        }

        // ── Normalize scores to 0–100 ───────────────
        if (!clusters.isEmpty()) {
            double maxScore = clusters.stream()
                    .mapToDouble(InvestmentCluster::getInvestmentScore).max().orElse(1);
            double minScore = clusters.stream()
                    .mapToDouble(InvestmentCluster::getInvestmentScore).min().orElse(0);
            double range = maxScore - minScore;

            for (InvestmentCluster c : clusters) {
                double normalized = (range > 0)
                        ? ((c.getInvestmentScore() - minScore) / range) * 100.0
                        : 50.0;
                c.setInvestmentScore(Math.round(normalized * 100.0) / 100.0);
            }
        }

        // ── Sort descending by score ────────────────
        clusters.sort(Comparator.comparingDouble(InvestmentCluster::getInvestmentScore).reversed());

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
