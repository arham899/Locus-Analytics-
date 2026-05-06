package com.locus.dao.impl;

import com.locus.config.DBConnection;
import com.locus.dao.ValuationDAO;
import com.locus.model.Valuation;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ValuationDAOImpl implements ValuationDAO {

    private final DataSource dataSource = DBConnection.getDataSource();

    // ─────────────────────────────────────────────
    // 1. FIND BY PROPERTY ID
    // ─────────────────────────────────────────────
    @Override
    public Valuation findByPropertyId(String propertyId) {

        String sql = "SELECT * FROM valuation WHERE property_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, propertyId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("findByPropertyId failed", e);
        }

        return null;
    }

    // ─────────────────────────────────────────────
    // 2. INSERT VALUATION
    // ─────────────────────────────────────────────
    @Override
    public boolean insert(Valuation v) {

        String sql = "INSERT INTO valuation " +
                "(valuation_id, property_id, estimation_fmv, confidence_lower_limit, confidence_upper_limit, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, v.getValuationId());
            stmt.setString(2, v.getPropertyId());

            stmt.setDouble(3, v.getEstimatedFmv());
            stmt.setDouble(4, v.getConfidenceIntervalLow());
            stmt.setDouble(5, v.getConfidenceIntervalHigh());

            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            stmt.setTimestamp(6, Timestamp.valueOf(v.getCreatedAt() != null ? v.getCreatedAt() : now));
            stmt.setTimestamp(7, Timestamp.valueOf(v.getUpdatedAt() != null ? v.getUpdatedAt() : now));

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("insert valuation failed", e);
        }
    }

    // ─────────────────────────────────────────────
    // 3. FIND BY DATE RANGE
    // ─────────────────────────────────────────────
    @Override
    public List<Valuation> findByDateRange(LocalDateTime start, LocalDateTime end) {

        String sql = "SELECT * FROM valuation WHERE created_at BETWEEN ? AND ?";

        List<Valuation> list = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(start));
            stmt.setTimestamp(2, Timestamp.valueOf(end));

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("findByDateRange failed", e);
        }

        return list;
    }

    // ─────────────────────────────────────────────
    // 4. MAPPER
    // ─────────────────────────────────────────────
    private Valuation mapRow(ResultSet rs) throws SQLException {

        Valuation v = new Valuation();

        v.setValuationId(rs.getString("valuation_id"));
        v.setPropertyId(rs.getString("property_id"));

        v.setEstimatedFmv(rs.getDouble("estimation_fmv"));

        v.setConfidenceIntervalLow(rs.getDouble("confidence_lower_limit"));
        v.setConfidenceIntervalHigh(rs.getDouble("confidence_upper_limit"));

        v.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        v.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        return v;
    }
}