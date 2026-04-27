package com.locus.dao.impl;

import com.locus.config.DBConnection;
import com.locus.dao.ROIAnalysisDAO;
import com.locus.model.ROIAnalysis;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ROIAnalysisDAOImpl implements ROIAnalysisDAO {

    private final DataSource dataSource = DBConnection.getDataSource();

    // ─────────────────────────────────────────────
    // 1. FIND BY PROPERTY
    // ─────────────────────────────────────────────
    @Override
    public ROIAnalysis findByProperty(String propertyId) {

        String sql = "SELECT * FROM roi_analysis WHERE property_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, propertyId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("ROI findByProperty failed", e);
        }

        return null;
    }

    // ─────────────────────────────────────────────
    // 2. INSERT
    // ─────────────────────────────────────────────
    @Override
    public boolean insert(ROIAnalysis r) {

        String sql = """
            INSERT INTO roi_analysis (
                analysis_id,
                property_id,
                analysis_date,
                purchase_price,
                purchase_date,
                current_value,
                cumulative_rental_income,
                total_expenses,
                total_return,
                roi_percentage,
                annualized_roi,
                created_at,
                updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, r.getAnalysisId());
            stmt.setString(2, r.getPropertyId());
            stmt.setDate(3, Date.valueOf(r.getAnalysisDate()));

            stmt.setDouble(4, r.getPurchasePrice());
            stmt.setDate(5, Date.valueOf(r.getPurchaseDate()));

            stmt.setDouble(6, r.getCurrentValue());
            stmt.setDouble(7, r.getCumulativeRentalIncome());
            stmt.setDouble(8, r.getTotalExpenses());
            stmt.setDouble(9, r.getTotalReturn());
            stmt.setDouble(10, r.getRoiPercentage());
            stmt.setDouble(11, r.getAnnualizedROI());

            LocalDateTime now = LocalDateTime.now();

            stmt.setTimestamp(12,
                    r.getCreatedAt() != null
                            ? Timestamp.valueOf(r.getCreatedAt())
                            : Timestamp.valueOf(now)
            );

            stmt.setTimestamp(13,
                    r.getUpdatedAt() != null
                            ? Timestamp.valueOf(r.getUpdatedAt())
                            : Timestamp.valueOf(now)
            );

            int rows = stmt.executeUpdate();
            System.out.println("ROWS INSERTED = " + rows);
            return rows > 0;

        } catch (SQLException e) {
            throw new RuntimeException("ROI insert failed", e);
        }
    }

    // ─────────────────────────────────────────────
    // 3. MAPPER
    // ─────────────────────────────────────────────
    private ROIAnalysis mapRow(ResultSet rs) throws SQLException {

        ROIAnalysis r = new ROIAnalysis();

        r.setAnalysisId(rs.getString("analysis_id"));
        r.setPropertyId(rs.getString("property_id"));

        r.setAnalysisDate(rs.getDate("analysis_date").toLocalDate());
        r.setPurchasePrice(rs.getDouble("purchase_price"));
        r.setPurchaseDate(rs.getDate("purchase_date").toLocalDate());

        r.setCurrentValue(rs.getDouble("current_value"));
        r.setCumulativeRentalIncome(rs.getDouble("cumulative_rental_income"));
        r.setTotalExpenses(rs.getDouble("total_expenses"));
        r.setTotalReturn(rs.getDouble("total_return"));

        r.setRoiPercentage(rs.getDouble("roi_percentage"));
        r.setAnnualizedROI(rs.getDouble("annualized_roi"));

        r.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        r.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        return r;
    }
}