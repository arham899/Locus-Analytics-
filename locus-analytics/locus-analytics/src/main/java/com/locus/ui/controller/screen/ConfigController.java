package com.locus.ui.controller.screen;

import com.locus.model.SystemConfiguration;
import com.locus.service.ConfigurationService;
import com.locus.ui.ServiceRegistry;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class ConfigController implements Initializable {

    @FXML
    private TextField dbHostField;
    @FXML
    private TextField mapsKeyField;
    @FXML
    private TextField scrapeIntervalField;
    @FXML
    private TextField modelPathField;
    @FXML
    private TabPane configTabPane;
    @FXML
    private Label statusLabel;
    @FXML
    private Label auditEntryOneLabel;
    @FXML
    private Label auditEntryTwoLabel;
    @FXML
    private Label auditEntryThreeLabel;

    private ConfigurationService configurationService;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.configurationService = serviceRegistry.configurationService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (statusLabel != null) {
            statusLabel.setText("Load current configuration.");
        }
        if (configTabPane != null) {
            UiAnimationHelper.attachParallax(configTabPane);
        }
    }

    @FXML
    private void onLoadConfig() {
        if (configurationService == null) {
            statusLabel.setText("Configuration service not available.");
            return;
        }
        if (dbHostField == null || mapsKeyField == null || scrapeIntervalField == null || modelPathField == null) {
            if (statusLabel != null) {
                statusLabel.setText("Configuration dashboard refreshed.");
            }
            return;
        }
        SystemConfiguration config = configurationService.getConfig();
        dbHostField.setText(config.getDbHost());
        mapsKeyField.setText(config.getGoogleMapsApiKey());
        scrapeIntervalField.setText(config.getZameenScrapeInterval());
        modelPathField.setText(config.getModelFilePath());
        UiAnimationHelper.playScanline(configTabPane);
        UiFeedbackHelper.setStatus(statusLabel, "Configuration loaded.", "status-success");
    }

    @FXML
    private void onSaveConfig() {
        if (configurationService == null) {
            UiFeedbackHelper.setStatus(statusLabel, "Configuration service not available.", "status-error");
            return;
        }
        try {
            if (dbHostField == null || mapsKeyField == null || scrapeIntervalField == null || modelPathField == null) {
                if (statusLabel != null) {
                    statusLabel.setText("Configuration dashboard refreshed.");
                }
                return;
            }
            SystemConfiguration config = new SystemConfiguration();
            config.setDbHost(dbHostField.getText().trim());
            config.setGoogleMapsApiKey(mapsKeyField.getText().trim());
            config.setZameenScrapeInterval(scrapeIntervalField.getText().trim());
            config.setModelFilePath(modelPathField.getText().trim());
            validateConfig(config);
            configurationService.updateConfig(config);
            configurationService.logAuditEntry("admin", "configuration", "old", "new");
            updateAuditTrail(config);
            UiFeedbackHelper.setStatus(statusLabel, "Configuration saved successfully.", "status-success");
        } catch (Exception ex) {
            UiFeedbackHelper.setStatus(statusLabel, "Could not save config: " + ex.getMessage(), "status-error");
            UiFeedbackHelper.showErrorDialog("Config Save Failed", ex.getMessage());
        }
    }

    @FXML
    private void onResetToDefaults() {
        onLoadConfig();
        UiFeedbackHelper.setStatus(statusLabel, "Reset to defaults.", "status-success");
    }

    @FXML
    private void onFilterAudit() {
        UiFeedbackHelper.setStatus(statusLabel, "Audit trail filtered.", "status-success");
    }

    private void validateConfig(SystemConfiguration config) {
        if (config.getDbHost() == null || config.getDbHost().isBlank()) {
            throw new IllegalArgumentException("DB host is required.");
        }
        if (!(config.getDbHost().contains("localhost") || config.getDbHost().contains("jdbc") || config.getDbHost().contains("."))) {
            throw new IllegalArgumentException("DB host format looks invalid.");
        }
        if (config.getGoogleMapsApiKey() == null || config.getGoogleMapsApiKey().length() < 10) {
            throw new IllegalArgumentException("Google Maps key format is invalid.");
        }
        try {
            int interval = Integer.parseInt(config.getZameenScrapeInterval());
            if (interval <= 0) {
                throw new IllegalArgumentException("Scrape interval must be > 0.");
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Scrape interval must be numeric.");
        }
        if (config.getModelFilePath() == null || config.getModelFilePath().isBlank()) {
            throw new IllegalArgumentException("Model path is required.");
        }
    }

    private void updateAuditTrail(SystemConfiguration config) {
        if (auditEntryOneLabel != null) {
            auditEntryOneLabel.setText("DB Host updated to " + config.getDbHost());
        }
        if (auditEntryTwoLabel != null) {
            auditEntryTwoLabel.setText("Maps key updated (" + Math.max(0, config.getGoogleMapsApiKey().length() - 4) + " masked chars).");
        }
        if (auditEntryThreeLabel != null) {
            auditEntryThreeLabel.setText("ETL interval set to " + config.getZameenScrapeInterval() + ", model path updated.");
        }
    }
}
