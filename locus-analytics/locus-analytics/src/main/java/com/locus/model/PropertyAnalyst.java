package com.locus.model;

import java.util.UUID;

/**
 * Represents a Property Analyst user in the LOCUS Analytics system.
 * Extends {@link User} with the 'analyst' role.
 *
 * <p>Property Analysts can perform valuations, rental yield calculations, ROI analysis,
 * search/compare properties, view trends, and generate reports.</p>
 */
public class PropertyAnalyst extends User {

    private String certificationLevel;

    // ── Constructors ──────────────────────────────────────────────────

    /** Default no-arg constructor. */
    public PropertyAnalyst() {
        super();
        setUserId(UUID.randomUUID().toString());
        setRole("analyst");
    }

    /** Full constructor. */
    public PropertyAnalyst(String userId, String name, String email,
                           String certificationLevel) {
        super(userId, name, email, "analyst");
        this.certificationLevel = certificationLevel;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public String getCertificationLevel() {
        return certificationLevel;
    }

    public void setCertificationLevel(String certificationLevel) {
        this.certificationLevel = certificationLevel;
    }

    // ── Abstract Method Implementation ────────────────────────────────

    @Override
    public String getRoleDescription() {
        return "Property Analyst — can perform valuations, rental analysis, " +
                "ROI calculations, and generate reports.";
    }

    @Override
    public String toString() {
        return "PropertyAnalyst{" +
                "userId='" + getUserId() + '\'' +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", certificationLevel='" + certificationLevel + '\'' +
                '}';
    }
}
