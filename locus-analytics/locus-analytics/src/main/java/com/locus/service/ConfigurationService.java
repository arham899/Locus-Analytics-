package com.locus.service;

import com.locus.model.SystemConfiguration;

/**
 * Service for managing application configuration settings (UC-12).
 *
 * <p>Provides access to system settings (DB host, API keys, scrape intervals, model path)
 * with validation and audit logging on changes.</p>
 */
public interface ConfigurationService {

    /**
     * Retrieves the current system configuration.
     *
     * @return the active SystemConfiguration, or a default if none exists
     */
    SystemConfiguration getConfig();

    /**
     * Updates system configuration with validation.
     * Validates API key format, interval &gt; 0, and DB host reachability.
     * Logs changes to audit trail.
     *
     * @param config the updated configuration
     * @return the saved configuration
     * @throws com.locus.exception.ValidationException if validation fails
     */
    SystemConfiguration updateConfig(SystemConfiguration config);

    /**
     * Logs a configuration change to the audit trail for UC-12 compliance.
     *
     * @param adminId   the admin who made the change
     * @param fieldName the field that was changed
     * @param oldValue  previous value
     * @param newValue  new value
     */
    void logAuditEntry(String adminId, String fieldName, String oldValue, String newValue);
}
