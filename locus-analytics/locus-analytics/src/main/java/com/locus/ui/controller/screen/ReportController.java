package com.locus.ui.controller.screen;

import com.locus.model.ValuationReport;
import com.locus.service.ValuationReportService;
import com.locus.ui.ServiceRegistry;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ReportController implements Initializable {

    @FXML
    private TextField propertyIdField;
    @FXML
    private CheckBox fmvCheckBox;
    @FXML
    private CheckBox comparablesCheckBox;
    @FXML
    private CheckBox rentalCheckBox;
    @FXML
    private CheckBox roiCheckBox;
    @FXML
    private CheckBox trendCheckBox;
    @FXML
    private CheckBox heatmapCheckBox;
    @FXML
    private TextArea analystNotesArea;
    @FXML
    private Label reportSummaryLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private TableView<ValuationReport> reportsTable;
    @FXML
    private TableColumn<ValuationReport, String> propertyColumn;
    @FXML
    private TableColumn<ValuationReport, String> clientColumn;
    @FXML
    private TableColumn<ValuationReport, String> generationDateColumn;
    @FXML
    private TableColumn<ValuationReport, String> confidenceColumn;
    @FXML
    private TableColumn<ValuationReport, String> statusColumn;

    private ValuationReportService valuationReportService;
    private final ObservableList<ValuationReport> generatedReports = FXCollections.observableArrayList();

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.valuationReportService = serviceRegistry.valuationReportService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (propertyIdField != null) {
            propertyIdField.setText("p-001");
        }
        if (fmvCheckBox != null) {
            fmvCheckBox.setSelected(true);
        }
        if (comparablesCheckBox != null) {
            comparablesCheckBox.setSelected(true);
        }
        if (propertyColumn != null) {
            propertyColumn.setCellValueFactory(new PropertyValueFactory<>("propertyId"));
        }
        if (clientColumn != null) {
            clientColumn.setCellValueFactory(cell ->
                    new SimpleStringProperty(cell.getValue().getAnalystId() == null ? "Nexus Capital" : cell.getValue().getAnalystId()));
        }
        if (generationDateColumn != null) {
            generationDateColumn.setCellValueFactory(cell ->
                    new SimpleStringProperty(cell.getValue().getGenerationDate() == null ? "-" : cell.getValue().getGenerationDate().toString()));
        }
        if (confidenceColumn != null) {
            confidenceColumn.setCellValueFactory(cell -> new SimpleStringProperty("95.0%"));
        }
        if (statusColumn != null) {
            statusColumn.setCellValueFactory(cell -> new SimpleStringProperty("READY"));
        }
        if (reportsTable != null) {
            reportsTable.setItems(generatedReports);
        }
    }

    @FXML
    private void onGenerateReport() {
        if (valuationReportService == null) {
            statusLabel.setText("Report service not available.");
            return;
        }
        try {
            if (propertyIdField == null || reportSummaryLabel == null) {
                if (statusLabel != null) {
                    statusLabel.setText("Report dashboard refreshed.");
                }
                return;
            }
            List<String> sections = new ArrayList<>();
            if (fmvCheckBox != null && fmvCheckBox.isSelected()) sections.add("fmv");
            if (comparablesCheckBox != null && comparablesCheckBox.isSelected()) sections.add("comparables");
            if (rentalCheckBox != null && rentalCheckBox.isSelected()) sections.add("rental_yield");
            if (roiCheckBox != null && roiCheckBox.isSelected()) sections.add("roi");
            if (trendCheckBox != null && trendCheckBox.isSelected()) sections.add("price_trend");
            if (heatmapCheckBox != null && heatmapCheckBox.isSelected()) sections.add("heatmap");

            ValuationReport report = valuationReportService.assembleReport(
                    propertyIdField.getText().trim(),
                    sections,
                    analystNotesArea == null ? "" : analystNotesArea.getText()
            );
            reportSummaryLabel.setText("Report ID: " + report.getReportId()
                    + " | Sections: " + report.getIncludedSections().size());
            generatedReports.add(0, report);
            if (reportsTable != null) {
                reportsTable.getSelectionModel().select(0);
            }
            statusLabel.setText("Report assembled using stub service.");
        } catch (Exception ex) {
            statusLabel.setText("Could not generate report: " + ex.getMessage());
        }
    }
}
