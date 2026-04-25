package com.locus.model;

import java.util.UUID;

/**
 * Represents a System Administrator user in the LOCUS Analytics system.
 * Extends {@link User} with the 'admin' role.
 *
 * <p>System Administrators can manage ETL pipelines, CRUD property listings,
 * configure system settings, and access audit logs.</p>
 */
public class SystemAdministrator extends User {

    private String accessLevel;

    // ── Constructors ──────────────────────────────────────────────────

    /** Default no-arg constructor. */
    public SystemAdministrator() {
        super();
        setUserId(UUID.randomUUID().toString());
        setRole("admin");
    }

    /** Full constructor. */
    public SystemAdministrator(String userId, String name, String email,
                               String accessLevel) {
        super(userId, name, email, "admin");
        this.accessLevel = accessLevel;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }

    // ── Abstract Method Implementation ────────────────────────────────

    @Override
    public String getRoleDescription() {
        return "System Administrator — can manage ETL pipelines, listings, " +
                "and system configuration.";
    }

    @Override
    public String toString() {
        return "SystemAdministrator{" +
                "userId='" + getUserId() + '\'' +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", accessLevel='" + accessLevel + '\'' +
                '}';
    }
}
