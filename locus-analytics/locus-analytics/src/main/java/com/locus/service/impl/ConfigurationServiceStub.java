package com.locus.service.impl;

import com.locus.model.SystemConfiguration;
import com.locus.service.ConfigurationService;

/**
 * Stub implementation of {@link ConfigurationService}.
 */
public class ConfigurationServiceStub implements ConfigurationService {

    @Override
    public SystemConfiguration getConfig() {
        return new SystemConfiguration(
            "config-001", "admin-001",
            "localhost:5432",
            "STUB_GOOGLE_MAPS_API_KEY",
            "weekly",
            "/models/locus_model_v1.json"
        );
    }

    @Override
    public SystemConfiguration updateConfig(SystemConfiguration config) {
        System.out.println("[STUB] Config updated: " + config);
        return config;
    }

    @Override
    public void logAuditEntry(String adminId, String field, String oldVal, String newVal) {
        System.out.printf("[STUB AUDIT] %s changed '%s': '%s' -> '%s'%n",
                adminId, field, oldVal, newVal);
    }
}
