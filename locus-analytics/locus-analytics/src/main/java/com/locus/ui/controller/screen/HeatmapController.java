package com.locus.ui.controller.screen;

import com.locus.model.dto.HeatmapPoint;
import com.locus.service.HeatmapService;
import com.locus.ui.ServiceRegistry;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class HeatmapController implements Initializable {

    @FXML
    private ComboBox<String> cityComboBox;
    @FXML
    private ComboBox<String> metricComboBox;
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
    private Label statusLabel;

    private HeatmapService heatmapService;

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

        if (heatmapWebView != null) {
            WebEngine engine = heatmapWebView.getEngine();
            engine.loadContent("<html><body style='font-family:sans-serif;'><h3>Heatmap Placeholder</h3>"
                    + "<p>Google Maps integration will be connected in Phase 2.</p></body></html>");
        }
    }

    @FXML
    private void onLoadHeatmapData() {
        if (heatmapService == null) {
            statusLabel.setText("Heatmap service not available.");
            return;
        }
        try {
            if (cityComboBox == null || metricComboBox == null || heatmapTable == null) {
                if (statusLabel != null) {
                    statusLabel.setText("Spatial dashboard refreshed.");
                }
                return;
            }
            List<HeatmapPoint> points = heatmapService.getHeatmapData(
                    cityComboBox.getValue(),
                    metricComboBox.getValue(),
                    null
            );
            heatmapTable.setItems(FXCollections.observableArrayList(points));
            statusLabel.setText("Loaded " + points.size() + " heatmap points.");
        } catch (Exception ex) {
            statusLabel.setText("Could not load heatmap data: " + ex.getMessage());
        }
    }
}
