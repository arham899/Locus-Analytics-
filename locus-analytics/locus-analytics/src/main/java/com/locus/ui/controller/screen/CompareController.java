package com.locus.ui.controller.screen;

import com.locus.model.Property;
import com.locus.model.dto.ComparisonResult;
import com.locus.service.CompareService;
import com.locus.service.ValuationService;
import com.locus.ui.controller.UiNavigationBridge;
import com.locus.ui.ServiceRegistry;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private TableView<ComparisonMetricRow> sideBySideTable;
    @FXML
    private TableColumn<ComparisonMetricRow, String> metricColumn;
    @FXML
    private TableColumn<ComparisonMetricRow, String> property1ValueColumn;
    @FXML
    private TableColumn<ComparisonMetricRow, String> property2ValueColumn;
    @FXML
    private TableColumn<ComparisonMetricRow, String> property3ValueColumn;
    @FXML
    private TableColumn<ComparisonMetricRow, String> property4ValueColumn;
    @FXML
    private Label summaryLabel;
    @FXML
    private Label statusLabel;

    private CompareService compareService;
    private ValuationService valuationService;
    private List<Property> comparedProperties = List.of();
    private ComparisonResult comparisonResult;
    private final Map<String, String> estimatedFmvByPropertyId = new HashMap<>();

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.compareService = serviceRegistry.compareService();
        this.valuationService = serviceRegistry.valuationService();
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
        initializeSideBySideTable();
        applyPrefilledIds();
        UiAnimationHelper.attachSpringPress(sideBySideTable);
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
            if (ids.size() < 2 || ids.size() > 4) {
                UiFeedbackHelper.showErrorDialog("Invalid Comparison Input", "Enter 2 to 4 property IDs.");
                return;
            }
            ComparisonResult result = compareService.compare(ids);
            this.comparisonResult = result;
            this.comparedProperties = result.getProperties();
            List<Property> previous = List.copyOf(comparisonTable.getItems());
            comparisonTable.setItems(FXCollections.observableArrayList(result.getProperties()));
            UiAnimationHelper.highlightTableDiff(comparisonTable, previous, result.getProperties());
            renderSideBySide(result.getProperties());
            summaryLabel.setText("Compared " + result.getProperties().size() + " properties");
            UiFeedbackHelper.setStatus(statusLabel, "Comparison loaded. Best/Worst flags available in service output.", "status-success");
        } catch (Exception ex) {
            UiFeedbackHelper.setStatus(statusLabel, "Could not compare: " + ex.getMessage(), "status-error");
            UiFeedbackHelper.showErrorDialog("Compare Failed", ex.getMessage());
        }
    }

    @FXML
    private void onLoadSampleIds() {
        if (propertyIdsField != null) {
            propertyIdsField.setText("p-101,p-102,p-103");
        }
        if (statusLabel != null) {
            UiFeedbackHelper.setStatus(statusLabel, "Sample comparison set loaded.", "status-success");
        }
    }

    @FXML
    private void onExportToReport() {
        if (comparedProperties == null || comparedProperties.isEmpty()) {
            UiFeedbackHelper.showInfoDialog("No Comparison Data", "Run compare first, then export to report.");
            return;
        }
        UiNavigationBridge.setReportPropertyId(comparedProperties.get(0).getPropertyId());
        UiNavigationBridge.openScreen("REPORT");
    }

    private void applyPrefilledIds() {
        List<String> prefilled = UiNavigationBridge.consumeComparePropertyIds();
        if (!prefilled.isEmpty() && propertyIdsField != null) {
            propertyIdsField.setText(String.join(",", prefilled));
            if (statusLabel != null) {
                statusLabel.setText("Loaded selected properties from Search.");
            }
        }
    }

    private void initializeSideBySideTable() {
        if (sideBySideTable == null) {
            return;
        }
        metricColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().label()));
        property1ValueColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().value(0)));
        property2ValueColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().value(1)));
        property3ValueColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().value(2)));
        property4ValueColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().value(3)));
        property1ValueColumn.setCellFactory(buildCompareCellFactory(0));
        property2ValueColumn.setCellFactory(buildCompareCellFactory(1));
        property3ValueColumn.setCellFactory(buildCompareCellFactory(2));
        property4ValueColumn.setCellFactory(buildCompareCellFactory(3));
    }

    private Callback<TableColumn<ComparisonMetricRow, String>, TableCell<ComparisonMetricRow, String>> buildCompareCellFactory(int idx) {
        return column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("compare-cell-best", "compare-cell-worst");
                setText(null);
                setGraphic(null);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    return;
                }
                ComparisonMetricRow row = (ComparisonMetricRow) getTableRow().getItem();
                String value = row.value(idx);
                if ("estimatedFmv".equals(row.metricKey()) && "N/A".equals(value) && idx < comparedProperties.size()) {
                    Hyperlink link = new Hyperlink("Run Estimate");
                    link.setOnAction(evt -> runEstimateFor(idx));
                    setGraphic(link);
                } else {
                    setText(value);
                }
                if (idx < comparedProperties.size()) {
                    String propertyId = comparedProperties.get(idx).getPropertyId();
                    ComparisonResult.BestWorstFlag flag = comparisonResult == null
                            ? ComparisonResult.BestWorstFlag.NEUTRAL
                            : comparisonResult.getFlag(row.metricKey(), propertyId);
                    if (flag == ComparisonResult.BestWorstFlag.BEST) {
                        getStyleClass().add("compare-cell-best");
                    } else if (flag == ComparisonResult.BestWorstFlag.WORST) {
                        getStyleClass().add("compare-cell-worst");
                    }
                }
            }
        };
    }

    private void runEstimateFor(int index) {
        if (valuationService == null || index >= comparedProperties.size()) {
            UiFeedbackHelper.showErrorDialog("FMV Estimate", "Valuation service is unavailable.");
            return;
        }
        try {
            Property p = comparedProperties.get(index);
            double fmv = valuationService.estimateFMV(p).getEstimatedFmv();
            estimatedFmvByPropertyId.put(p.getPropertyId(), UiFormatters.currency(fmv));
            renderSideBySide(comparedProperties);
            UiFeedbackHelper.setStatus(statusLabel, "FMV estimated for " + p.getPropertyId(), "status-success");
        } catch (Exception ex) {
            UiFeedbackHelper.showErrorDialog("FMV Estimation Failed", ex.getMessage());
        }
    }

    private void renderSideBySide(List<Property> properties) {
        if (sideBySideTable == null) {
            return;
        }
        property1ValueColumn.setText(header(properties, 0));
        property2ValueColumn.setText(header(properties, 1));
        property3ValueColumn.setText(header(properties, 2));
        property4ValueColumn.setText(header(properties, 3));
        sideBySideTable.setItems(FXCollections.observableArrayList(
                row("location", "Location", properties, p -> p.getCity() + ", " + p.getLocality()),
                row("propertyType", "Type", properties, Property::getPropertyType),
                row("area", "Area", properties, p -> UiFormatters.number(p.getArea())),
                row("price", "Price", properties, p -> UiFormatters.currency(p.getPrice())),
                row("pricePerSqft", "Price / sq.ft.", properties, p -> UiFormatters.number(p.getPricePerSqft())),
                row("bedrooms", "Bedrooms", properties, p -> String.valueOf(p.getBedrooms())),
                row("bathrooms", "Bathrooms", properties, p -> String.valueOf(p.getBathrooms())),
                row("listingDate", "Listing Date", properties, p -> p.getListingDate() == null ? "-" : p.getListingDate().format(DateTimeFormatter.ISO_DATE)),
                row("estimatedFmv", "Estimated FMV", properties, p -> estimatedFmvByPropertyId.getOrDefault(p.getPropertyId(), "N/A"))
        ));
    }

    private ComparisonMetricRow row(String key, String label, List<Property> properties, java.util.function.Function<Property, String> mapper) {
        java.util.ArrayList<String> vals = new java.util.ArrayList<>();
        for (int i = 0; i < 4; i++) {
            vals.add(i < properties.size() ? mapper.apply(properties.get(i)) : "-");
        }
        return new ComparisonMetricRow(key, label, vals);
    }

    private String header(List<Property> properties, int idx) {
        return idx < properties.size() ? properties.get(idx).getPropertyId() : "-";
    }

    private record ComparisonMetricRow(String metricKey, String label, List<String> values) {
        String value(int index) {
            return index < values.size() ? values.get(index) : "-";
        }
    }
}
