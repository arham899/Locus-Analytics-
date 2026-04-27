package com.locus.dao.impl;

import com.locus.config.DBConnection;
import com.locus.dao.SystemConfigurationDAO;
import com.locus.model.SystemConfiguration;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SystemConfigurationDAOImpl implements SystemConfigurationDAO {

    private final DataSource dataSource = DBConnection.getDataSource();

    // ─────────────────────────────────────────────
    // GET CONFIG BY ID
    // ─────────────────────────────────────────────
    @Override
    public SystemConfiguration getConfig(String configId) {

        String sql = "SELECT * FROM system_configuration WHERE config_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, configId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("getConfig failed", e);
        }

        return null;
    }

    // ─────────────────────────────────────────────
    // UPDATE CONFIG
    // ─────────────────────────────────────────────
    @Override
    public boolean update(SystemConfiguration c) {

        String sql = """
            UPDATE system_configuration
            SET admin_id = ?,
                db_host = ?,
                google_maps_api_key = ?,
                zameen_scrape_interval = ?,
                model_file_path = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE config_id = ?
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, c.getAdminId());
            stmt.setString(2, c.getDbHost());
            stmt.setString(3, c.getGoogleMapsApiKey());
            stmt.setString(4, c.getZameenScrapeInterval());
            stmt.setString(5, c.getModelFilePath());
            stmt.setString(6, c.getConfigId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("update config failed", e);
        }
    }

    // ─────────────────────────────────────────────
    // GET ALL CONFIGS
    // ─────────────────────────────────────────────
    @Override
    public List<SystemConfiguration> getAllConfigs() {

        List<SystemConfiguration> list = new ArrayList<>();

        String sql = "SELECT * FROM system_configuration ORDER BY created_at DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("getAllConfigs failed", e);
        }

        return list;
    }

    // ─────────────────────────────────────────────
    // MAPPER
    // ─────────────────────────────────────────────
    private SystemConfiguration mapRow(ResultSet rs) throws SQLException {

        SystemConfiguration c = new SystemConfiguration();

        c.setConfigId(rs.getString("config_id"));
        c.setAdminId(rs.getString("admin_id"));
        c.setDbHost(rs.getString("db_host"));
        c.setGoogleMapsApiKey(rs.getString("google_maps_api_key"));
        c.setZameenScrapeInterval(rs.getString("zameen_scrape_interval"));
        c.setModelFilePath(rs.getString("model_file_path"));

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