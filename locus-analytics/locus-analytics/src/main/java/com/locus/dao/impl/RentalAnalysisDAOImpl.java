package com.locus.dao.impl;

import com.locus.config.DBConnection;
import com.locus.dao.RentalAnalysisDAO;
import com.locus.model.RentalAnalysis;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RentalAnalysisDAOImpl implements RentalAnalysisDAO {

    private final DataSource dataSource = DBConnection.getDataSource();

    // ─────────────────────────────────────────────
    // 1. FIND LATEST BY PROPERTY
    // ─────────────────────────────────────────────
    @Override
    public RentalAnalysis findLatestByProperty(String propertyId) {

        String sql = """
            SELECT *
            FROM rental_analysis
            WHERE property_id = ?
            ORDER BY analysis_date DESC
            LIMIT 1
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, propertyId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    // ─────────────────────────────────────────────
    // 2. FIND BY PROPERTY
    // ─────────────────────────────────────────────
    @Override
    public List<RentalAnalysis> findByProperty(String propertyId) {

        List<RentalAnalysis> list = new ArrayList<>();

        String sql = """
            SELECT *
            FROM rental_analysis
            WHERE property_id = ?
            ORDER BY analysis_date ASC
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, propertyId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    // ─────────────────────────────────────────────
    // 3. INSERT (MATCHING YOUR TABLE)
    // ─────────────────────────────────────────────
    @Override
    public boolean insert(RentalAnalysis r) {

        String sql = """
            INSERT INTO rental_analysis (
                analysis_id,
                property_id,
                annual_rent,
                annual_expenses,
                gross_yield,
                net_yield,
                analyst_id,
                analysis_date,
                created_at,
                updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, r.getAnalysisId());
            stmt.setString(2, r.getPropertyId());

            stmt.setDouble(3, r.getExpectedRent());   // mapped to annual_rent
            stmt.setDouble(4, r.getAnnualExpenses());

            stmt.setDouble(5, r.getGrossYield());
            stmt.setDouble(6, r.getNetYield());

            stmt.setString(7, r.getAnalystId());
            stmt.setDate(8, Date.valueOf(r.getAnalysisDate()));

            stmt.setTimestamp(9,
                    r.getCreatedAt() != null
                            ? Timestamp.valueOf(r.getCreatedAt())
                            : Timestamp.valueOf(LocalDateTime.now())
            );

            stmt.setTimestamp(10,
                    r.getUpdatedAt() != null
                            ? Timestamp.valueOf(r.getUpdatedAt())
                            : Timestamp.valueOf(LocalDateTime.now())
            );

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ─────────────────────────────────────────────
    // 4. MAPPER (FIXED)
    // ─────────────────────────────────────────────
    private RentalAnalysis mapRow(ResultSet rs) throws SQLException {

        RentalAnalysis r = new RentalAnalysis();

        r.setAnalysisId(rs.getString("analysis_id"));
        r.setPropertyId(rs.getString("property_id"));

        r.setExpectedRent(rs.getDouble("annual_rent"));
        r.setAnnualExpenses(rs.getDouble("annual_expenses"));

        r.setGrossYield(rs.getDouble("gross_yield"));
        r.setNetYield(rs.getDouble("net_yield"));

        r.setAnalystId(rs.getString("analyst_id"));

        r.setAnalysisDate(rs.getDate("analysis_date").toLocalDate());

        r.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        r.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        return r;
    }
}