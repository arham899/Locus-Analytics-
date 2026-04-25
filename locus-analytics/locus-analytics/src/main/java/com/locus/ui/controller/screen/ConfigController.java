package com.locus.ui.controller.screen;

import com.locus.model.SystemConfiguration;
import com.locus.service.ConfigurationService;
import com.locus.ui.ServiceRegistry;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
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
    private Label statusLabel;

    private ConfigurationService configurationService;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.configurationService = serviceRegistry.configurationService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (statusLabel != null) {
            statusLabel.setText("Load current configuration.");
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
        statusLabel.setText("Configuration loaded.");
    }

    @FXML
    private void onSaveConfig() {
        if (configurationService == null) {
            statusLabel.setText("Configuration service not available.");
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
            configurationService.updateConfig(config);
            statusLabel.setText("Configuration saved in stub mode.");
        } catch (Exception ex) {
            statusLabel.setText("Could not save config: " + ex.getMessage());
        }
    }
}
