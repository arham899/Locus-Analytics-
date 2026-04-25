package com.locus.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Abstract base class representing a user in the LOCUS Analytics system.
 * Maps to the {@code app_user} table in the database.
 *
 * <p>Uses single-table inheritance: the {@code role} column discriminates between
 * {@link PropertyAnalyst} (role='analyst') and {@link SystemAdministrator} (role='admin').</p>
 */
public abstract class User {

    private String userId;
    private String name;
    private String email;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Constructors ──────────────────────────────────────────────────

    /** Protected constructor for subclasses. */
    protected User() {
    }

    /** Protected constructor with common fields. */
    protected User(String userId, String name, String email, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ── Abstract Methods ──────────────────────────────────────────────

    /**
     * Returns a human-readable description of the user's role-specific capabilities.
     */
    public abstract String getRoleDescription();

    // ── equals / hashCode / toString ──────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
