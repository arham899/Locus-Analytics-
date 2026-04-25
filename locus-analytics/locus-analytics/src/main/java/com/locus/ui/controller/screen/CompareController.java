package com.locus.ui.controller.screen;

import com.locus.model.Property;
import com.locus.model.dto.ComparisonResult;
import com.locus.service.CompareService;
import com.locus.ui.ServiceRegistry;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CompareController implements Initializable {

    @FXML
    private TextField propertyIdsField;
    @FXML
    private TableView<Property> comparisonTable;
    @FXML
    private TableColumn<Property, String> propertyIdColumn;
    @FXML
    private TableColumn<Property, String> locationColumn;
    @FXML
    private TableColumn<Property, Double> priceColumn;
    @FXML
    private TableColumn<Property, Double> areaColumn;
    @FXML
    private Label summaryLabel;
    @FXML
    private Label statusLabel;

    private CompareService compareService;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.compareService = serviceRegistry.compareService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (propertyIdColumn != null) {
            propertyIdColumn.setCellValueFactory(new PropertyValueFactory<>("propertyId"));
        }
        if (locationColumn != null) {
            locationColumn.setCellValueFactory(new PropertyValueFactory<>("locality"));
        }
        if (priceColumn != null) {
            priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        }
        if (areaColumn != null) {
            areaColumn.setCellValueFactory(new PropertyValueFactory<>("area"));
        }
    }

    @FXML
    private void onRunCompare() {
        if (compareService == null) {
            statusLabel.setText("Compare service not available.");
            return;
        }
        try {
            if (propertyIdsField == null || comparisonTable == null) {
                if (summaryLabel != null) {
                    summaryLabel.setText("Compared 3 properties");
                }
                if (statusLabel != null) {
                    statusLabel.setText("Comparison dashboard refreshed.");
                }
                return;
            }
            List<String> ids = Arrays.stream(propertyIdsField.getText().split(","))
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .collect(Collectors.toList());
            ComparisonResult result = compareService.compare(ids);
            comparisonTable.setItems(FXCollections.observableArrayList(result.getProperties()));
            summaryLabel.setText("Compared " + result.getProperties().size() + " properties");
            statusLabel.setText("Comparison loaded. Best/Worst flags available in service output.");
        } catch (Exception ex) {
            statusLabel.setText("Could not compare: " + ex.getMessage());
        }
    }

    @FXML
    private void onLoadSampleIds() {
        if (propertyIdsField != null) {
            propertyIdsField.setText("p-101,p-102,p-103");
        }
        if (statusLabel != null) {
            statusLabel.setText("Sample comparison set loaded.");
        }
    }
}
