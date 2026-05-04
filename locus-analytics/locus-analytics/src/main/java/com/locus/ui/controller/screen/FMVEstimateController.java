package com.locus.ui.controller.screen;

import com.locus.exception.ValidationException;
import com.locus.model.Property;
import com.locus.model.Valuation;
import com.locus.service.ValuationService;
import com.locus.ui.controller.UiNavigationBridge;
import com.locus.ui.ServiceRegistry;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.util.Duration;

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
    private Spinner<Integer> bedroomsSpinner;
    @FXML
    private Spinner<Integer> bathroomsSpinner;
    @FXML
    private CheckBox parkingAmenityCheck;
    @FXML
    private CheckBox furnishedAmenityCheck;
    @FXML
    private CheckBox securityAmenityCheck;
    @FXML
    private CheckBox liftAmenityCheck;
    @FXML
    private Label estimatedFmvLabel;
    @FXML
    private Label confidenceLabel;
    @FXML
    private Label factorsLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private ProgressIndicator loadingIndicator;
    @FXML
    private Button runEstimateButton;
    @FXML
    private Button generateReportButton;
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
    private final DoubleProperty estimatedFmvAnimated = new SimpleDoubleProperty(0);
    private final DoubleProperty confidenceLowAnimated = new SimpleDoubleProperty(0);
    private final DoubleProperty confidenceHighAnimated = new SimpleDoubleProperty(0);
    private static final Map<String, List<String>> CURATED_LOCALITIES_BY_CITY = Map.of(
            "Karachi", List.of(
                    "DHA Phase 1", "DHA Phase 2", "DHA Phase 4", "DHA Phase 5", "DHA Phase 6",
                    "DHA Phase 7", "DHA Phase 8", "Clifton", "Bahria Town Karachi",
                    "Gulshan-e-Iqbal", "Gulistan-e-Johar", "Malir Cantt", "Askari 5",
                    "PECHS", "North Nazimabad", "Federal B Area", "Saddar",
                    "Korangi", "Nazimabad", "Scheme 33", "Saima Residency"
            ),
            "Islamabad", List.of(
                    "F-6", "F-7", "F-8", "F-10", "F-11",
                    "G-9", "G-10", "G-11", "G-13", "G-15",
                    "E-7", "E-11", "I-8", "I-10",
                    "DHA Phase 1 Islamabad", "DHA Phase 2 Islamabad",
                    "Bahria Town Islamabad", "Bahria Enclave",
                    "Gulberg Islamabad", "B-17", "PWD", "Park View City"
            ),
            "Lahore", List.of(
                    "DHA Phase 1", "DHA Phase 2", "DHA Phase 3", "DHA Phase 4",
                    "DHA Phase 5", "DHA Phase 6", "DHA Phase 7", "DHA Phase 8",
                    "Gulberg", "Gulberg III", "Model Town", "Johar Town",
                    "Bahria Town Lahore", "Bahria Orchard",
                    "Wapda Town", "Garden Town", "Iqbal Town",
                    "Cantt", "Askari", "EME Society", "Valencia Town",
                    "Faisal Town", "PIA Housing Society"
            )
    );
    private Valuation lastValuation;
    private Property lastInputProperty;

    private ServiceRegistry serviceRegistry;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.valuationService = serviceRegistry.valuationService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (cityComboBox != null) {
            cityComboBox.setItems(FXCollections.observableArrayList("Karachi", "Islamabad", "Lahore"));
            cityComboBox.setValue("Karachi");
            cityComboBox.valueProperty().addListener((obs, oldCity, newCity) -> updateLocalities(newCity));
        }
        if (propertyTypeComboBox != null) {
            propertyTypeComboBox.setItems(FXCollections.observableArrayList("house", "apartment", "plot", "commercial"));
            propertyTypeComboBox.setValue("house");
        }
        if (localityComboBox != null) {
            updateLocalities("Karachi");
        }
        if (areaField != null && areaField.getText().isBlank()) {
            areaField.setText("2250");
        }
        if (bedroomsSpinner != null) {
            bedroomsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, 4));
            bedroomsSpinner.getValueFactory().setValue(4);
        }
        if (bathroomsSpinner != null) {
            bathroomsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, 3));
            bathroomsSpinner.getValueFactory().setValue(3);
        }

        if (comparableLocalityColumn != null) {
            comparableLocalityColumn.setCellValueFactory(new PropertyValueFactory<>("locality"));
        }
        if (comparableTypeColumn != null) {
            comparableTypeColumn.setCellValueFactory(new PropertyValueFactory<>("propertyType"));
        }
        if (comparableAreaColumn != null) {
            comparableAreaColumn.setCellValueFactory(new PropertyValueFactory<>("area"));
            comparableAreaColumn.setCellFactory(tc -> new javafx.scene.control.TableCell<Property, Double>() {
                @Override
                protected void updateItem(Double area, boolean empty) {
                    super.updateItem(area, empty);
                    if (empty || area == null) {
                        setText(null);
                    } else {
                        setText(String.format("%,.0f", area));
                    }
                }
            });
        }
        if (comparablePriceColumn != null) {
            comparablePriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
            comparablePriceColumn.setCellFactory(tc -> new javafx.scene.control.TableCell<Property, Double>() {
                @Override
                protected void updateItem(Double price, boolean empty) {
                    super.updateItem(price, empty);
                    if (empty || price == null) {
                        setText(null);
                    } else {
                        setText(UiFormatters.currency(price));
                    }
                }
            });
        }

        // Dashboard mode fallback defaults
        if (estimatedFmvLabel != null && "-".equals(estimatedFmvLabel.getText())) {
            estimatedFmvLabel.setText("-");
        }
        if (confidenceLabel != null && "-".equals(confidenceLabel.getText())) {
            confidenceLabel.setText("-");
        }
        if (factorsLabel != null && "-".equals(factorsLabel.getText())) {
            factorsLabel.setText("-");
        }
        estimatedFmvAnimated.addListener((obs, oldValue, newValue) ->
                estimatedFmvLabel.setText(UiFormatters.currency(newValue.doubleValue())));
        confidenceLowAnimated.addListener((obs, oldValue, newValue) ->
                confidenceLabel.setText(UiFormatters.currency(newValue.doubleValue()) + " - " + UiFormatters.currency(confidenceHighAnimated.get())));
        confidenceHighAnimated.addListener((obs, oldValue, newValue) ->
                confidenceLabel.setText(UiFormatters.currency(confidenceLowAnimated.get()) + " - " + UiFormatters.currency(newValue.doubleValue())));
        UiAnimationHelper.playStaggeredReveal(List.of(
                estimatedFmvLabel, confidenceLabel, factorsLabel, comparablesTable
        ));
        UiAnimationHelper.attachHoverScale(runEstimateButton);
        UiAnimationHelper.attachHoverScale(generateReportButton);
    }

    @FXML
    private void onEstimateFmv() {
        if (valuationService == null) {
            UiFeedbackHelper.setStatus(statusLabel, "Valuation service not available.", "status-error");
            return;
        }
        UiFeedbackHelper.clearValidation(areaField, bedroomsSpinner, bathroomsSpinner);
        try {
            // If form fields are missing, treat this page as dashboard refresh mode.
            if (cityComboBox == null || areaField == null) {
                estimatedFmvLabel.setText("-");
                confidenceLabel.setText("-");
                factorsLabel.setText("-");
                UiFeedbackHelper.setStatus(statusLabel, "Market intelligence metrics refreshed.", "status-success");
                return;
            }

            Property input = buildInputProperty();
            Task<ValuationResultBundle> task = new Task<>() {
                @Override
                protected ValuationResultBundle call() {
                    Valuation valuation = valuationService.estimateFMV(input);
                    List<Property> comparables = valuationService.findComparables(input);
                    return new ValuationResultBundle(valuation, comparables);
                }
            };
            task.setOnRunning(event -> setLoadingState(true));
            task.setOnSucceeded(event -> {
                setLoadingState(false);
                ValuationResultBundle result = task.getValue();
                UiAnimationHelper.animateNumber(
                        estimatedFmvAnimated,
                        0,
                        result.valuation.getEstimatedFmv(),
                        Duration.millis(860)
                );
                UiAnimationHelper.pulseMetric(estimatedFmvLabel, UiAnimationHelper.SignalType.POSITIVE);
                UiAnimationHelper.animateNumber(confidenceLowAnimated, confidenceLowAnimated.get(),
                        result.valuation.getConfidenceIntervalLow(), Duration.millis(620));
                UiAnimationHelper.animateNumber(confidenceHighAnimated, confidenceHighAnimated.get(),
                        result.valuation.getConfidenceIntervalHigh(), Duration.millis(680));
                UiAnimationHelper.playScanline(confidenceLabel);
                factorsLabel.setText(String.join(", ", result.valuation.getKeyFactors()));
                if (comparablesTable != null) {
                    List<Property> topFive = result.comparables.size() > 5 ? result.comparables.subList(0, 5) : result.comparables;
                    comparablesTable.setItems(FXCollections.observableArrayList(topFive));
                }
                lastValuation = result.valuation;
                lastInputProperty = input;
                UiFeedbackHelper.setStatus(statusLabel, "FMV estimate generated successfully.", "status-success");
            });
            task.setOnFailed(event -> {
                setLoadingState(false);
                Throwable ex = task.getException();
                UiFeedbackHelper.setStatus(statusLabel, "Could not estimate FMV: " + (ex == null ? "Unknown error" : ex.getMessage()), "status-error");
                UiFeedbackHelper.showErrorDialog("FMV Estimation Failed", ex == null ? "Unknown error" : ex.getMessage());
            });
            Thread thread = new Thread(task, "fmv-estimate-task");
            thread.setDaemon(true);
            thread.start();
        } catch (ValidationException ex) {
            Map<String, Control> fields = new HashMap<>();
            fields.put("area", areaField);
            fields.put("bedrooms", bedroomsSpinner);
            fields.put("bathrooms", bathroomsSpinner);
            fields.put("city", cityComboBox);
            fields.put("locality", localityComboBox);
            fields.put("propertyType", propertyTypeComboBox);
            UiFeedbackHelper.markFromFieldErrors(ex.getFieldErrors(), fields);
            UiFeedbackHelper.setStatus(statusLabel, "Validation error. Please fix highlighted fields.", "status-error");
            UiFeedbackHelper.showErrorDialog("Invalid FMV Input", UiFeedbackHelper.formatValidationMessage(ex));
        } catch (IllegalArgumentException ex) {
            UiFeedbackHelper.setStatus(statusLabel, ex.getMessage(), "status-error");
            UiFeedbackHelper.showErrorDialog("Invalid FMV Input", ex.getMessage());
        } catch (Exception ex) {
            UiFeedbackHelper.setStatus(statusLabel, "Could not estimate FMV: " + ex.getMessage(), "status-error");
            UiFeedbackHelper.showErrorDialog("FMV Estimation Failed", ex.getMessage());
        }
    }

    @FXML
    private void onClear() {
        if (cityComboBox != null) cityComboBox.setValue("Karachi");
        if (localityComboBox != null) localityComboBox.setValue("DHA Phase 6");
        if (propertyTypeComboBox != null) propertyTypeComboBox.setValue("house");
        if (areaField != null) areaField.setText("2250");
        if (bedroomsSpinner != null) bedroomsSpinner.getValueFactory().setValue(4);
        if (bathroomsSpinner != null) bathroomsSpinner.getValueFactory().setValue(3);
        if (parkingAmenityCheck != null) parkingAmenityCheck.setSelected(false);
        if (furnishedAmenityCheck != null) furnishedAmenityCheck.setSelected(false);
        if (securityAmenityCheck != null) securityAmenityCheck.setSelected(false);
        if (liftAmenityCheck != null) liftAmenityCheck.setSelected(false);
        if (estimatedFmvLabel != null) estimatedFmvLabel.setText("-");
        if (confidenceLabel != null) confidenceLabel.setText("-");
        if (factorsLabel != null) factorsLabel.setText("-");
        if (statusLabel != null) UiFeedbackHelper.setStatus(statusLabel, "Inputs reset to defaults.", "status-success");
        if (comparablesTable != null) comparablesTable.getItems().clear();
        UiFeedbackHelper.clearValidation(areaField, bedroomsSpinner, bathroomsSpinner);
        setLoadingState(false);
    }

    @FXML
    private void onGenerateReport() {
        if (lastValuation == null || lastInputProperty == null) {
            UiFeedbackHelper.showInfoDialog("No FMV Yet", "Run an FMV estimate first, then generate a report.");
            return;
        }
        UiNavigationBridge.setReportPropertyId(lastInputProperty.getPropertyId());
        UiNavigationBridge.openScreen("REPORT");
    }

    private Property buildInputProperty() {
        Property property = new Property();
        property.setPropertyId("ui-" + UUID.randomUUID());
        property.setCity(cityComboBox.getValue());
        property.setLocality(localityComboBox.getValue());
        property.setPropertyType(propertyTypeComboBox.getValue());
        property.setArea(parseDouble(areaField.getText(), "area"));
        property.setBedrooms(bedroomsSpinner.getValue());
        property.setBathrooms(bathroomsSpinner.getValue());
        property.setListingDate(LocalDate.now());
        property.setLatitude(24.8607);
        property.setLongitude(67.0011);
        property.setPrice(1);
        property.setUrlHash(UUID.randomUUID().toString());
        property.setAmenities(selectedAmenities());
        return property;
    }

    private List<String> selectedAmenities() {
        java.util.ArrayList<String> amenities = new java.util.ArrayList<>();
        if (parkingAmenityCheck != null && parkingAmenityCheck.isSelected()) amenities.add("parking");
        if (furnishedAmenityCheck != null && furnishedAmenityCheck.isSelected()) amenities.add("furnished");
        if (securityAmenityCheck != null && securityAmenityCheck.isSelected()) amenities.add("security");
        if (liftAmenityCheck != null && liftAmenityCheck.isSelected()) amenities.add("lift");
        return amenities;
    }

    private void updateLocalities(String city) {
        if (localityComboBox == null) {
            return;
        }
        List<String> localities = CURATED_LOCALITIES_BY_CITY.getOrDefault(city, List.of());
        localityComboBox.setItems(FXCollections.observableArrayList(localities));
        if (!localities.isEmpty()) {
            localityComboBox.setValue(localities.get(0));
        } else {
            localityComboBox.getSelectionModel().clearSelection();
        }
    }

    private double parseDouble(String raw, String field) {
        try {
            return Double.parseDouble(raw.trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid " + field + " value.");
        }
    }

    private void setLoadingState(boolean loading) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(loading);
            loadingIndicator.setManaged(loading);
        }
        UiAnimationHelper.setSkeletonVisible(comparablesTable, loading);
        if (runEstimateButton != null) {
            runEstimateButton.setDisable(loading);
            runEstimateButton.setText(loading ? "Thinking..." : "Run Market Intelligence");
        }
    }

    private static class ValuationResultBundle {
        private final Valuation valuation;
        private final List<Property> comparables;

        private ValuationResultBundle(Valuation valuation, List<Property> comparables) {
            this.valuation = valuation;
            this.comparables = comparables;
        }
    }
}
