package com.locus.dao.impl;

import com.locus.config.DBConnection;
import com.locus.dao.AuditLogDAO;
import com.locus.model.AuditLog;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuditLogDAOImpl implements AuditLogDAO {

    private final DataSource dataSource = DBConnection.getDataSource();

    @Override
    public boolean insert(AuditLog log) {
        String sql = """
            INSERT INTO audit_log (audit_id, admin_id, table_name, field_name, old_value, new_value, changed_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, UUID.randomUUID().toString().substring(0, 8));
            stmt.setString(2, log.getAdminId());
            stmt.setString(3, log.getTableName());
            stmt.setString(4, log.getFieldName());
            stmt.setString(5, log.getOldValue());
            stmt.setString(6, log.getNewValue());
            stmt.setTimestamp(7, Timestamp.valueOf(log.getChangedAt()));

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Audit log insertion failed", e);
        }
    }

    @Override
    public List<AuditLog> findByAdmin(String adminId) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_log WHERE admin_id = ? ORDER BY changed_at DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, adminId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return logs;
    }

    @Override
    public List<AuditLog> findAll() {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_log ORDER BY changed_at DESC";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                logs.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return logs;
    }

    private AuditLog mapRow(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setAuditId(rs.getString("audit_id"));
        log.setAdminId(rs.getString("admin_id"));
        log.setTableName(rs.getString("table_name"));
        log.setFieldName(rs.getString("field_name"));
        log.setOldValue(rs.getString("old_value"));
        log.setNewValue(rs.getString("new_value"));
        log.setChangedAt(rs.getTimestamp("changed_at").toLocalDateTime());
        return log;
    }
}
