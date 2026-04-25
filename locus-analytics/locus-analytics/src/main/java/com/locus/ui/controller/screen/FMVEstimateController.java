package com.locus.ui.controller.screen;

import com.locus.exception.ValidationException;
import com.locus.model.Property;
import com.locus.model.Valuation;
import com.locus.service.ValuationService;
import com.locus.ui.ServiceRegistry;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class FMVEstimateController implements Initializable {

    @FXML
    private ComboBox<String> cityComboBox;
    @FXML
    private ComboBox<String> localityComboBox;
    @FXML
    private ComboBox<String> propertyTypeComboBox;
    @FXML
    private TextField areaField;
    @FXML
    private TextField bedroomsField;
    @FXML
    private TextField bathroomsField;
    @FXML
    private Label estimatedFmvLabel;
    @FXML
    private Label confidenceLabel;
    @FXML
    private Label factorsLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private TableView<Property> comparablesTable;
    @FXML
    private TableColumn<Property, String> comparableLocalityColumn;
    @FXML
    private TableColumn<Property, String> comparableTypeColumn;
    @FXML
    private TableColumn<Property, Double> comparableAreaColumn;
    @FXML
    private TableColumn<Property, Double> comparablePriceColumn;

    private ValuationService valuationService;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.valuationService = serviceRegistry.valuationService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (cityComboBox != null) {
            cityComboBox.setItems(FXCollections.observableArrayList("Karachi", "Islamabad", "Lahore"));
            cityComboBox.setValue("Karachi");
        }
        if (propertyTypeComboBox != null) {
            propertyTypeComboBox.setItems(FXCollections.observableArrayList("house", "apartment", "plot", "commercial"));
            propertyTypeComboBox.setValue("house");
        }
        if (localityComboBox != null) {
            localityComboBox.setItems(FXCollections.observableArrayList("DHA Phase 6", "Clifton", "Bahria Town", "F-7", "Gulberg"));
            localityComboBox.setValue("DHA Phase 6");
        }
        if (areaField != null && areaField.getText().isBlank()) {
            areaField.setText("2250");
        }
        if (bedroomsField != null && bedroomsField.getText().isBlank()) {
            bedroomsField.setText("4");
        }
        if (bathroomsField != null && bathroomsField.getText().isBlank()) {
            bathroomsField.setText("3");
        }

        if (comparableLocalityColumn != null) {
            comparableLocalityColumn.setCellValueFactory(new PropertyValueFactory<>("locality"));
        }
        if (comparableTypeColumn != null) {
            comparableTypeColumn.setCellValueFactory(new PropertyValueFactory<>("propertyType"));
        }
        if (comparableAreaColumn != null) {
            comparableAreaColumn.setCellValueFactory(new PropertyValueFactory<>("area"));
        }
        if (comparablePriceColumn != null) {
            comparablePriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        }

        // Dashboard mode fallback defaults
        if (estimatedFmvLabel != null && "-".equals(estimatedFmvLabel.getText())) {
            estimatedFmvLabel.setText("42.5B PKR");
        }
        if (confidenceLabel != null && "-".equals(confidenceLabel.getText())) {
            confidenceLabel.setText("4.2%");
        }
        if (factorsLabel != null && "-".equals(factorsLabel.getText())) {
            factorsLabel.setText("12,450");
        }
    }

    @FXML
    private void onEstimateFmv() {
        if (valuationService == null) {
            statusLabel.setText("Valuation service not available.");
            return;
        }
        try {
            // If form fields are missing, treat this page as dashboard refresh mode.
            if (cityComboBox == null || areaField == null) {
                estimatedFmvLabel.setText("42.5B PKR");
                confidenceLabel.setText("4.2%");
                factorsLabel.setText("12,450");
                statusLabel.setText("Market intelligence metrics refreshed.");
                return;
            }

            Property input = buildInputProperty();
            Valuation valuation = valuationService.estimateFMV(input);
            List<Property> comparables = valuationService.findComparables(input);

            estimatedFmvLabel.setText(UiFormatters.currency(valuation.getEstimatedFmv()));
            confidenceLabel.setText(UiFormatters.currency(valuation.getConfidenceIntervalLow()) + " - "
                    + UiFormatters.currency(valuation.getConfidenceIntervalHigh()));
            factorsLabel.setText(String.join(", ", valuation.getKeyFactors()));
            if (comparablesTable != null) {
                comparablesTable.setItems(FXCollections.observableArrayList(comparables));
            }
            statusLabel.setText("FMV estimate generated from stub service.");
        } catch (ValidationException ex) {
            statusLabel.setText("Validation error: " + ex.getMessage());
        } catch (Exception ex) {
            statusLabel.setText("Could not estimate FMV: " + ex.getMessage());
        }
    }

    @FXML
    private void onClear() {
        if (cityComboBox != null) cityComboBox.setValue("Karachi");
        if (localityComboBox != null) localityComboBox.setValue("DHA Phase 6");
        if (propertyTypeComboBox != null) propertyTypeComboBox.setValue("house");
        if (areaField != null) areaField.setText("2250");
        if (bedroomsField != null) bedroomsField.setText("4");
        if (bathroomsField != null) bathroomsField.setText("3");
        if (estimatedFmvLabel != null) estimatedFmvLabel.setText("-");
        if (confidenceLabel != null) confidenceLabel.setText("-");
        if (factorsLabel != null) factorsLabel.setText("-");
        if (statusLabel != null) statusLabel.setText("Inputs reset to defaults.");
        if (comparablesTable != null) comparablesTable.getItems().clear();
    }

    private Property buildInputProperty() {
        Property property = new Property();
        property.setPropertyId("ui-" + UUID.randomUUID());
        property.setCity(cityComboBox.getValue());
        property.setLocality(localityComboBox.getValue());
        property.setPropertyType(propertyTypeComboBox.getValue());
        property.setArea(parseDouble(areaField.getText(), "area"));
        property.setBedrooms((int) parseDouble(bedroomsField.getText(), "bedrooms"));
        property.setBathrooms((int) parseDouble(bathroomsField.getText(), "bathrooms"));
        property.setListingDate(LocalDate.now());
        property.setLatitude(24.8607);
        property.setLongitude(67.0011);
        property.setPrice(1);
        property.setUrlHash(UUID.randomUUID().toString());
        return property;
    }

    private double parseDouble(String raw, String field) {
        try {
            return Double.parseDouble(raw.trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid " + field + " value.");
        }
    }
}
