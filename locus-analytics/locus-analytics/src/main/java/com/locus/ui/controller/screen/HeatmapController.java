package com.locus.ui.controller.screen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.locus.model.dto.HeatmapPoint;
import com.locus.service.HeatmapService;
import com.locus.ui.controller.UiNavigationBridge;
import com.locus.ui.ServiceRegistry;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

public class HeatmapController implements Initializable {

    @FXML
    private ComboBox<String> cityComboBox;
    @FXML
    private ComboBox<String> metricComboBox;
    @FXML
    private ComboBox<String> propertyTypeComboBox;
    @FXML
    private WebView heatmapWebView;
    @FXML
    private TableView<HeatmapPoint> heatmapTable;
    @FXML
    private TableColumn<HeatmapPoint, String> localityColumn;
    @FXML
    private TableColumn<HeatmapPoint, Double> latitudeColumn;
    @FXML
    private TableColumn<HeatmapPoint, Double> longitudeColumn;
    @FXML
    private TableColumn<HeatmapPoint, Double> weightColumn;
    @FXML
    private Label quotaLabel;
    @FXML
    private Label statusLabel;

    private HeatmapService heatmapService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String googleMapsApiKey = "";

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.heatmapService = serviceRegistry.heatmapService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (cityComboBox != null) {
            cityComboBox.setItems(FXCollections.observableArrayList("Karachi", "Islamabad", "Lahore"));
            cityComboBox.setValue("Karachi");
        }
        if (metricComboBox != null) {
            metricComboBox.setItems(FXCollections.observableArrayList("price_per_sqft", "listing_density", "rental_demand"));
            metricComboBox.setValue("price_per_sqft");
        }
        if (propertyTypeComboBox != null) {
            propertyTypeComboBox.setItems(FXCollections.observableArrayList("", "house", "apartment", "plot", "commercial"));
            propertyTypeComboBox.setValue("");
        }

        if (localityColumn != null) {
            localityColumn.setCellValueFactory(new PropertyValueFactory<>("locality"));
        }
        if (latitudeColumn != null) {
            latitudeColumn.setCellValueFactory(new PropertyValueFactory<>("latitude"));
        }
        if (longitudeColumn != null) {
            longitudeColumn.setCellValueFactory(new PropertyValueFactory<>("longitude"));
        }
        if (weightColumn != null) {
            weightColumn.setCellValueFactory(new PropertyValueFactory<>("weight"));
        }

        googleMapsApiKey = loadGoogleMapsApiKey();
        if (heatmapWebView != null) {
            configureWebView();
        }
        if (quotaLabel != null) {
            quotaLabel.setManaged(false);
            quotaLabel.setVisible(false);
        }
    }

    @FXML
    private void onLoadHeatmapData() {
        if (heatmapService == null) {
            UiFeedbackHelper.setStatus(statusLabel, "Heatmap service not available.", "status-error");
            return;
        }
        try {
            if (cityComboBox == null || metricComboBox == null || heatmapTable == null) {
                if (statusLabel != null) {
                    statusLabel.setText("Spatial dashboard refreshed.");
                }
                return;
            }
            Task<List<HeatmapPoint>> task = new Task<>() {
                @Override
                protected List<HeatmapPoint> call() {
                    return heatmapService.getHeatmapData(
                            cityComboBox.getValue(),
                            metricComboBox.getValue(),
                            blankToNull(propertyTypeComboBox.getValue())
                    );
                }
            };
            task.setOnSucceeded(event -> {
                List<HeatmapPoint> points = task.getValue();
                List<HeatmapPoint> previous = List.copyOf(heatmapTable.getItems());
                heatmapTable.setItems(FXCollections.observableArrayList(points));
                UiAnimationHelper.highlightTableDiff(heatmapTable, previous, points);
                boolean canUseMap = googleMapsApiKey != null && !googleMapsApiKey.isBlank() && !googleMapsApiKey.startsWith("REPLACE_");
                if (canUseMap) {
                    sendHeatmapToWebView(points);
                    showWebMap(true);
                    showQuotaWarning(false);
                    UiFeedbackHelper.setStatus(statusLabel, "Loaded " + points.size() + " heatmap points on map.", "status-success");
                } else {
                    showWebMap(false);
                    showQuotaWarning(true);
                    UiFeedbackHelper.setStatus(statusLabel, "API key missing/quota issue. Showing fallback table.", "status-warning");
                }
            });
            task.setOnFailed(event -> {
                showWebMap(false);
                Throwable ex = task.getException();
                UiFeedbackHelper.setStatus(statusLabel, "Could not load heatmap data.", "status-error");
                UiFeedbackHelper.showErrorDialog("Heatmap Error", ex == null ? "Unknown error" : ex.getMessage());
            });
            Thread thread = new Thread(task, "heatmap-load-task");
            thread.setDaemon(true);
            thread.start();
        } catch (Exception ex) {
            UiFeedbackHelper.setStatus(statusLabel, "Could not load heatmap data: " + ex.getMessage(), "status-error");
        }
    }

    private void configureWebView() {
        WebEngine engine = heatmapWebView.getEngine();
        URL heatmapPage = getClass().getResource("/web/heatmap.html");
        if (heatmapPage != null) {
            engine.load(heatmapPage.toExternalForm());
            engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    JSObject window = (JSObject) engine.executeScript("window");
                    window.setMember("javaHeatmapBridge", new JavaHeatmapBridge());
                    engine.executeScript("setConfig(" + toJsString(googleMapsApiKey) + ");");
                }
            });
        } else {
            engine.loadContent("<html><body><h3>heatmap.html not found</h3></body></html>");
        }
    }

    private void sendHeatmapToWebView(List<HeatmapPoint> points) {
        try {
            List<Map<String, Object>> payload = points.stream().map(point -> {
                Map<String, Object> row = new HashMap<>();
                row.put("latitude", point.getLatitude());
                row.put("longitude", point.getLongitude());
                row.put("weight", point.getWeight());
                row.put("locality", point.getLocality());
                row.put("avgPrice", Math.round(point.getWeight() * 100_000_000));
                row.put("listingCount", Math.max(1, (int) Math.round(point.getWeight() * 100)));
                return row;
            }).toList();
            String json = objectMapper.writeValueAsString(payload);
            heatmapWebView.getEngine().executeScript("loadHeatmap(" + json + ");");
        } catch (Exception ex) {
            showWebMap(false);
            UiFeedbackHelper.setStatus(statusLabel, "Map rendering failed; fallback table active.", "status-warning");
        }
    }

    private void showWebMap(boolean visible) {
        if (heatmapWebView != null) {
            heatmapWebView.setManaged(visible);
            heatmapWebView.setVisible(visible);
        }
    }

    private void showQuotaWarning(boolean visible) {
        if (quotaLabel != null) {
            quotaLabel.setManaged(visible);
            quotaLabel.setVisible(visible);
            if (visible) {
                UiAnimationHelper.pulseMetric(quotaLabel, UiAnimationHelper.SignalType.WARNING);
            }
        }
    }

    private String loadGoogleMapsApiKey() {
        Properties properties = new Properties();
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (stream != null) {
                properties.load(stream);
                String key = properties.getProperty("google.maps.api.key", "");
                if ("REPLACE_WITH_API_KEY".equals(key)) {
                    showQuotaWarning(true);
                    if (quotaLabel != null) quotaLabel.setText("Map Setup Required: Please provide a Google Maps API Key in config.properties.");
                    return "";
                }
                return key;
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String toJsString(String value) {
        if (value == null) {
            return "''";
        }
        return "'" + value.replace("\\", "\\\\").replace("'", "\\'") + "'";
    }

    public static class JavaHeatmapBridge {
        public void onSearchHere(String locality) {
            UiNavigationBridge.openScreen("SEARCH");
        }
    }
}
