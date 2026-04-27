package com.locus.dao.impl;

import com.locus.config.DBConnection;
import com.locus.dao.InvestmentClusterDAO;
import com.locus.model.InvestmentCluster;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvestmentClusterDAOImpl implements InvestmentClusterDAO {

    private final DataSource dataSource = DBConnection.getDataSource();

    // ─────────────────────────────────────────────
    // 1. INSERT
    // ─────────────────────────────────────────────
    @Override
    public boolean insert(InvestmentCluster c) {

        String sql = """
            INSERT INTO investment_cluster (
                cluster_id,
                city,
                locality,
                investment_score,
                price_appreciation,
                listing_volume_growth,
                rental_trend
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, c.getClusterId());
            stmt.setString(2, c.getCity());
            stmt.setString(3, c.getLocality());
            stmt.setDouble(4, c.getInvestmentScore());
            stmt.setDouble(5, c.getPriceAppreciation());
            stmt.setDouble(6, c.getListingVolumeGrowth());
            stmt.setDouble(7, c.getRentalTrend());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Insert cluster failed", e);
        }
    }

    // ─────────────────────────────────────────────
    // 2. FIND BY CITY
    // ─────────────────────────────────────────────
    @Override
    public List<InvestmentCluster> findByCity(String city) {

        List<InvestmentCluster> list = new ArrayList<>();

        String sql = """
            SELECT *
            FROM investment_cluster
            WHERE city = ?
            ORDER BY investment_score DESC
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, city);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("findByCity failed", e);
        }

        return list;
    }

    // ─────────────────────────────────────────────
    // 3. TOP CLUSTERS
    // ─────────────────────────────────────────────
    @Override
    public List<InvestmentCluster> findTopClusters(int limit) {

        List<InvestmentCluster> list = new ArrayList<>();

        String sql = """
            SELECT *
            FROM investment_cluster
            ORDER BY investment_score DESC
            LIMIT ?
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("findTopClusters failed", e);
        }

        return list;
    }

    // ─────────────────────────────────────────────
    // 4. MAPPER
    // ─────────────────────────────────────────────
    private InvestmentCluster mapRow(ResultSet rs) throws SQLException {

        InvestmentCluster c = new InvestmentCluster();

        c.setClusterId(rs.getString("cluster_id"));
        c.setCity(rs.getString("city"));
        c.setLocality(rs.getString("locality"));

        c.setInvestmentScore(rs.getDouble("investment_score"));
        c.setPriceAppreciation(rs.getDouble("price_appreciation"));
        c.setListingVolumeGrowth(rs.getDouble("listing_volume_growth"));
        c.setRentalTrend(rs.getDouble("rental_trend"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            c.setCreatedAt(created.toLocalDateTime());
        }

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            c.setUpdatedAt(updated.toLocalDateTime());
        }

        return c;
    }
}