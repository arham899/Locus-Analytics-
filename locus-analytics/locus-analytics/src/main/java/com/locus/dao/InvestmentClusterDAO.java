package com.locus.dao;
import com.locus.model.InvestmentCluster;
import java.util.List;

public interface InvestmentClusterDAO {

    List<InvestmentCluster> findByCity(String city);

    List<InvestmentCluster> findTopClusters(int limit);

    boolean insert(InvestmentCluster cluster);
}
