package com.locus.service;

import com.locus.model.InvestmentCluster;
import com.locus.model.dto.ClusterParams;

import java.util.List;

/**
 * Service for identifying high-potential investment clusters/localities (UC-9).
 *
 * <p>Scores localities using a composite metric:
 * Investment Score = 0.5 × priceAppreciation + 0.3 × volumeGrowth + 0.2 × rentalTrend,
 * normalized to 0–100.</p>
 */
public interface InvestmentClusterService {

    /**
     * Identifies and ranks investment clusters based on the given parameters.
     *
     * @param params configuration including city, property type, analysis period,
     *               and minimum listing count threshold
     * @return list of investment clusters ranked by score (descending)
     */
    List<InvestmentCluster> identifyClusters(ClusterParams params);
}
