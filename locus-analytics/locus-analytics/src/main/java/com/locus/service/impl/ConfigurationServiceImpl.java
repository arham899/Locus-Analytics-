package com.locus.service.impl;

import com.locus.dao.SystemConfigurationDAO;
import com.locus.model.SystemConfiguration;
import com.locus.service.ConfigurationService;
import com.locus.service.validation.InputValidator;
import java.util.Objects;

/**
 * Real implementation of {@link ConfigurationService}.
 */
public class ConfigurationServiceImpl implements ConfigurationService {

    private final SystemConfigurationDAO configDAO;
    private static final String DEFAULT_CONFIG_ID = "config-default";

    public ConfigurationServiceImpl(SystemConfigurationDAO configDAO) {
        this.configDAO = configDAO;
    }

    @Override
    public SystemConfiguration getConfig() {
        SystemConfiguration config = configDAO.getConfig(DEFAULT_CONFIG_ID);
        if (config == null) {
            // Create default config if missing
            config = new SystemConfiguration(
                DEFAULT_CONFIG_ID,
                "system",
                "localhost:5432",
                "STUB_GOOGLE_MAPS_API_KEY",
                "weekly",
                "/models/model.json"
            );
            boolean success = configDAO.update(config);
            if (!success) {
                System.out.println("[WARNING] Failed to save default system configuration to DB. Using in-memory defaults.");
            }
        }
        return config;
    }

    @Override
    public SystemConfiguration updateConfig(SystemConfiguration config) {
        new InputValidator()
                .validateNotNull("configId", config.getConfigId())
                .throwIfInvalid();

        SystemConfiguration oldConfig = getConfig();

        boolean success = configDAO.update(config);
        if (!success) {
            throw new RuntimeException("Failed to update system configuration.");
        }

        // Log audit entries for changes
        if (!Objects.equals(oldConfig.getDbHost(), config.getDbHost())) {
            logAuditEntry(config.getAdminId(), "dbHost",
                Objects.toString(oldConfig.getDbHost(), null),
                Objects.toString(config.getDbHost(), null));
        }
        if (!Objects.equals(oldConfig.getGoogleMapsApiKey(), config.getGoogleMapsApiKey())) {
            logAuditEntry(config.getAdminId(), "googleMapsApiKey", "HIDDEN", "HIDDEN");
        }

        return config;
    }

    @Override
    public void logAuditEntry(String adminId, String field, String oldVal, String newVal) {
        // Since there is no AuditDAO, we log to console.
        // In a real system, this would insert into an audit_logs table.
        String safeAdminId = Objects.toString(adminId, "UNKNOWN");
        System.out.printf("[AUDIT] User %s updated %s: '%s' -> '%s'%n",
                safeAdminId, field, oldVal, newVal);
    }
}
