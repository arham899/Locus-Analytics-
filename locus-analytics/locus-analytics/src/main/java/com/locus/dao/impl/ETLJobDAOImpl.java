package com.locus.dao.impl;

import com.locus.config.DBConnection;
import com.locus.dao.ETLJobDAO;
import com.locus.model.ETLJob;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ETLJobDAOImpl implements ETLJobDAO {

    private final DataSource dataSource = DBConnection.getDataSource();

    // ─────────────────────────────────────────────
    // 1. FIND ALL
    // ─────────────────────────────────────────────
    @Override
    public List<ETLJob> findAll() {

        String sql = "SELECT * FROM etl_job ORDER BY created_at DESC";

        List<ETLJob> list = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("findAll ETLJob failed", e);
        }

        return list;
    }

    // ─────────────────────────────────────────────
    // 2. FIND LATEST
    // ─────────────────────────────────────────────
    @Override
    public ETLJob findLatest() {

        String sql = """
            SELECT *
            FROM etl_job
            ORDER BY run_date DESC, created_at DESC
            LIMIT 1
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("findLatest ETLJob failed", e);
        }

        return null;
    }

    // ─────────────────────────────────────────────
    // 3. INSERT
    // ─────────────────────────────────────────────
    @Override
    public boolean insert(ETLJob job) {

        String sql = """
            INSERT INTO etl_job (
                job_id,
                admin_id,
                run_date,
                records_extracted,
                records_cleaned,
                records_loaded,
                errors,
                status,
                current_stage,
                progress_percent,
                created_at,
                updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, job.getJobId());
            stmt.setString(2, job.getAdminId());

            stmt.setDate(3, Date.valueOf(job.getRunDate()));

            stmt.setInt(4, job.getRecordsExtracted());
            stmt.setInt(5, job.getRecordsCleaned());
            stmt.setInt(6, job.getRecordsLoaded());
            stmt.setInt(7, job.getErrors());

            stmt.setString(8, job.getStatus());
            stmt.setString(9, job.getCurrentStage());
            stmt.setInt(10, job.getProgressPercent());

            stmt.setTimestamp(11,
                    job.getCreatedAt() != null
                            ? Timestamp.valueOf(job.getCreatedAt())
                            : new Timestamp(System.currentTimeMillis())
            );

            stmt.setTimestamp(12,
                    job.getUpdatedAt() != null
                            ? Timestamp.valueOf(job.getUpdatedAt())
                            : new Timestamp(System.currentTimeMillis())
            );

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("insert ETLJob failed", e);
        }
    }

    // ─────────────────────────────────────────────
    // MAPPER
    // ─────────────────────────────────────────────
    private ETLJob mapRow(ResultSet rs) throws SQLException {

        ETLJob job = new ETLJob();

        job.setJobId(rs.getString("job_id"));
        job.setAdminId(rs.getString("admin_id"));

        Date runDate = rs.getDate("run_date");
        if (runDate != null) {
            job.setRunDate(runDate.toLocalDate());
        }

        job.setRecordsExtracted(rs.getInt("records_extracted"));
        job.setRecordsCleaned(rs.getInt("records_cleaned"));
        job.setRecordsLoaded(rs.getInt("records_loaded"));
        job.setErrors(rs.getInt("errors"));

        job.setStatus(rs.getString("status"));
        job.setCurrentStage(rs.getString("current_stage"));
        job.setProgressPercent(rs.getInt("progress_percent"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            job.setCreatedAt(created.toLocalDateTime());
        }

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            job.setUpdatedAt(updated.toLocalDateTime());
        }

        return job;
    }
}