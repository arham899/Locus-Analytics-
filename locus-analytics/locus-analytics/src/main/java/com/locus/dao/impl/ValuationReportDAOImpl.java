package com.locus.dao.impl;

import com.locus.config.DBConnection;
import com.locus.dao.ValuationReportDAO;
import com.locus.model.ValuationReport;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * PostgreSQL implementation of {@link ValuationReportDAO}.
 *
 * <p>Maps to the {@code valuation_report} table.</p>
 *
 * @author Fasih Ul Mubashir (24i-0517)
 */
public class ValuationReportDAOImpl implements ValuationReportDAO {

    private final DataSource dataSource = DBConnection.getDataSource();

    // ─────────────────────────────────────────────
    // INSERT
    // ─────────────────────────────────────────────

    @Override
    public boolean insert(ValuationReport report) {
        String sql = """
            INSERT INTO valuation_report (
                report_id,
                property_id,
                analyst_id,
                generation_date,
                included_sections,
                analyst_notes,
                pdf_file_path,
                created_at,
                updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (report_id) DO NOTHING
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, report.getReportId());
            stmt.setString(2, report.getPropertyId());
            stmt.setString(3, report.getAnalystId());
            stmt.setDate(4, Date.valueOf(
                    report.getGenerationDate() != null ? report.getGenerationDate() : LocalDate.now()
            ));
            stmt.setString(5, report.getIncludedSectionsAsString());
            stmt.setString(6, report.getAnalystNotes());
            stmt.setString(7, report.getPdfFilePath());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("ValuationReport insert failed", e);
        }
    }

    // ─────────────────────────────────────────────
    // FIND BY ID
    // ─────────────────────────────────────────────

    @Override
    public Optional<ValuationReport> findById(String reportId) {
        String sql = "SELECT * FROM valuation_report WHERE report_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reportId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("ValuationReport findById failed", e);
        }
        return Optional.empty();
    }

    // ─────────────────────────────────────────────
    // FIND BY PROPERTY
    // ─────────────────────────────────────────────

    @Override
    public List<ValuationReport> findByProperty(String propertyId) {
        String sql = """
            SELECT * FROM valuation_report
            WHERE property_id = ?
            ORDER BY generation_date DESC, created_at DESC
        """;

        return query(sql, propertyId);
    }

    // ─────────────────────────────────────────────
    // FIND BY ANALYST
    // ─────────────────────────────────────────────

    @Override
    public List<ValuationReport> findByAnalyst(String analystId) {
        String sql = """
            SELECT * FROM valuation_report
            WHERE analyst_id = ?
            ORDER BY generation_date DESC, created_at DESC
        """;

        return query(sql, analystId);
    }

    // ─────────────────────────────────────────────
    // UPDATE FILE PATH
    // ─────────────────────────────────────────────

    @Override
    public boolean updateFilePath(String reportId, String filePath) {
        String sql = """
            UPDATE valuation_report
            SET pdf_file_path = ?,
                updated_at    = CURRENT_TIMESTAMP
            WHERE report_id = ?
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, filePath);
            stmt.setString(2, reportId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("ValuationReport updateFilePath failed", e);
        }
    }

    // ─────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────

    private List<ValuationReport> query(String sql, String param) {
        List<ValuationReport> list = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, param);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("ValuationReport query failed", e);
        }
        return list;
    }

    private ValuationReport mapRow(ResultSet rs) throws SQLException {
        ValuationReport r = new ValuationReport();
        r.setReportId(rs.getString("report_id"));
        r.setPropertyId(rs.getString("property_id"));
        r.setAnalystId(rs.getString("analyst_id"));

        Date genDate = rs.getDate("generation_date");
        if (genDate != null) r.setGenerationDate(genDate.toLocalDate());

        r.setIncludedSectionsFromString(rs.getString("included_sections"));
        r.setAnalystNotes(rs.getString("analyst_notes"));
        r.setPdfFilePath(rs.getString("pdf_file_path"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) r.setCreatedAt(created.toLocalDateTime());

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) r.setUpdatedAt(updated.toLocalDateTime());

        return r;
    }
}
