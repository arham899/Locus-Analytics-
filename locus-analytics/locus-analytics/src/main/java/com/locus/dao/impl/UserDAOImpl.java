package com.locus.dao.impl;
import org.mindrot.jbcrypt.BCrypt;

import com.locus.config.DBConnection;
import com.locus.dao.UserDAO;
import com.locus.model.PropertyAnalyst;
import com.locus.model.SystemAdministrator;
import com.locus.model.User;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {

    private final DataSource dataSource = DBConnection.getDataSource();

    // ─────────────────────────────────────────────
    // 1. FIND BY ID
    // ─────────────────────────────────────────────
    @Override
    public User findById(String userId) {

        String sql = "SELECT * FROM app_user WHERE user_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("findById failed", e);
        }

        return null;
    }

    // ─────────────────────────────────────────────
    // 2. FIND BY EMAIL
    // ─────────────────────────────────────────────
    @Override
    public User findByEmail(String email) {

        String sql = "SELECT * FROM app_user WHERE email = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("findByEmail failed", e);
        }

        return null;
    }

    // ─────────────────────────────────────────────
    // 3. INSERT
    // ─────────────────────────────────────────────
    @Override
    public boolean insert(User user) {

        String sql = """
            INSERT INTO app_user (
                user_id, name, email, role, password_hash
            )
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUserId());
            stmt.setString(2, user.getName());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getRole());
            stmt.setString(5, user.getPasswordHash());

            int rows = stmt.executeUpdate();
            System.out.println("ROWS INSERTED = " + rows);
            return rows > 0;

        } catch (SQLException e) {
            throw new RuntimeException("insert user failed", e);
        }
    }

    // ─────────────────────────────────────────────
    // 4. UPDATE
    // ─────────────────────────────────────────────
    @Override
    public boolean update(User user) {

        String sql = """
            UPDATE app_user
            SET name = ?,
                email = ?,
                role = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE user_id = ?
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getUserId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("update user failed", e);
        }
    }

    // ─────────────────────────────────────────────
    // 5. DELETE
    // ─────────────────────────────────────────────
    @Override
    public boolean delete(String userId) {

        String sql = "DELETE FROM app_user WHERE user_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("delete user failed", e);
        }
    }

    // ─────────────────────────────────────────────
    // 6. FIND ALL
    // ─────────────────────────────────────────────
    @Override
    public List<User> findAll() {

        List<User> list = new ArrayList<>();

        String sql = "SELECT * FROM app_user ORDER BY created_at DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("findAll failed", e);
        }

        return list;
    }

    // ─────────────────────────────────────────────
    // MAPPER (ROLE-BASED OBJECT CREATION)
    // ─────────────────────────────────────────────
    private User mapRow(ResultSet rs) throws SQLException {

        String role = rs.getString("role");

        User user;

        if ("analyst".equalsIgnoreCase(role)) {
            user = new PropertyAnalyst();
        } else if ("admin".equalsIgnoreCase(role)) {
            user = new SystemAdministrator();
        } else {
            throw new RuntimeException("Unknown role: " + role);
        }

        user.setUserId(rs.getString("user_id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setRole(role);

        // password (needed later for auth)
        user.setPasswordHash(rs.getString("password_hash"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            user.setCreatedAt(created.toLocalDateTime());
        }

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            user.setUpdatedAt(updated.toLocalDateTime());
        }

        return user;
    }
}