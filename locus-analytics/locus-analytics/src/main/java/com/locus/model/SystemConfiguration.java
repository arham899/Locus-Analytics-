package com.locus.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents the system configuration settings for the LOCUS Analytics application.
 * Maps to the {@code system_configuration} table in the database.
 *
 * <p>Stores administrative settings including database host, API keys,
 * scraping intervals, and ML model file paths.</p>
 */
public class SystemConfiguration {

    private String configId;
    private String adminId;
    private String dbHost;
    private String googleMapsApiKey;
    private String zameenScrapeInterval;
    private String modelFilePath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Constructors ──────────────────────────────────────────────────

    /** Default no-arg constructor. Generates a random UUID for configId. */
    public SystemConfiguration() {
        this.configId = UUID.randomUUID().toString();
    }

    /** Full constructor for database retrieval. */
    public SystemConfiguration(String configId, String adminId, String dbHost,
                               String googleMapsApiKey, String zameenScrapeInterval,
                               String modelFilePath) {
        this.configId = configId;
        this.adminId = adminId;
        this.dbHost = dbHost;
        this.googleMapsApiKey = googleMapsApiKey;
        this.zameenScrapeInterval = zameenScrapeInterval;
        this.modelFilePath = modelFilePath;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getDbHost() {
        return dbHost;
    }

    public void setDbHost(String dbHost) {
        this.dbHost = dbHost;
    }

    public String getGoogleMapsApiKey() {
        return googleMapsApiKey;
    }

    public void setGoogleMapsApiKey(String googleMapsApiKey) {
        this.googleMapsApiKey = googleMapsApiKey;
    }

    public String getZameenScrapeInterval() {
        return zameenScrapeInterval;
    }

    public void setZameenScrapeInterval(String zameenScrapeInterval) {
        this.zameenScrapeInterval = zameenScrapeInterval;
    }

    public String getModelFilePath() {
        return modelFilePath;
    }

    public void setModelFilePath(String modelFilePath) {
        this.modelFilePath = modelFilePath;
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

    // ── equals / hashCode / toString ──────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SystemConfiguration that = (SystemConfiguration) o;
        return Objects.equals(configId, that.configId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configId);
    }

    @Override
    public String toString() {
        return "SystemConfiguration{" +
                "configId='" + configId + '\'' +
                ", dbHost='" + dbHost + '\'' +
                ", scrapeInterval='" + zameenScrapeInterval + '\'' +
                ", modelPath='" + modelFilePath + '\'' +
                '}';
    }
}