package com.locus.ui.controller.screen;

import com.locus.model.ValuationReport;
import com.locus.service.ReportPdfService;
import com.locus.service.ValuationReportService;
import com.locus.ui.controller.UiNavigationBridge;
import com.locus.ui.ServiceRegistry;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

public class ReportController implements Initializable {

    @FXML
    private TextField propertyIdField;
    @FXML
    private ComboBox<String> valuationSelectorComboBox;
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
    private ProgressIndicator loadingIndicator;
    @FXML
    private Button generateReportButton;
    @FXML
    private Button downloadPdfButton;
    @FXML
    private Button savePdfButton;
    @FXML
    private Button printPdfButton;
    @FXML
    private Button redirectToFmvButton;
    @FXML
    private ImageView pdfPreviewImageView;
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
    private ReportPdfService reportPdfService;
    private final ObservableList<ValuationReport> generatedReports = FXCollections.observableArrayList();
    private ValuationReport latestGeneratedReport;
    private final ObservableList<String> previousValuations = FXCollections.observableArrayList();

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.valuationReportService = serviceRegistry.valuationReportService();
        this.reportPdfService = serviceRegistry.reportPdfService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (propertyIdField != null) {
            propertyIdField.setText("p-001");
        }
        seedValuationSelector();
        String prefill = UiNavigationBridge.consumeReportPropertyId();
        if (propertyIdField != null && prefill != null && !prefill.isBlank()) {
            propertyIdField.setText(prefill);
            if (!previousValuations.contains(prefill)) {
                previousValuations.add(prefill);
            }
            if (valuationSelectorComboBox != null) {
                valuationSelectorComboBox.setValue(prefill);
            }
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
        UiAnimationHelper.attachSpringPress(generateReportButton);
        UiAnimationHelper.attachSpringPress(downloadPdfButton);
        UiAnimationHelper.attachSpringPress(savePdfButton);
        UiAnimationHelper.attachSpringPress(printPdfButton);
        updateNoValuationState();
    }

    @FXML
    private void onGenerateReport() {
        if (valuationReportService == null || reportPdfService == null) {
            UiFeedbackHelper.setStatus(statusLabel, "Report service not available.", "status-error");
            return;
        }
        if (propertyIdField == null || reportSummaryLabel == null) {
            UiFeedbackHelper.setStatus(statusLabel, "Report dashboard refreshed.", "status-success");
            return;
        }

        String propertyId = propertyIdField.getText() == null ? "" : propertyIdField.getText().trim();
        List<String> sections = selectedSections();
        UiFeedbackHelper.clearValidation(propertyIdField);
        if (propertyId.isBlank()) {
            UiFeedbackHelper.markInvalid(propertyIdField);
            UiFeedbackHelper.setStatus(statusLabel, "Property ID is required.", "status-error");
            UiFeedbackHelper.showErrorDialog("Validation Error", "Property ID is required before generating a report.");
            return;
        }
        if (sections.isEmpty()) {
            UiFeedbackHelper.setStatus(statusLabel, "Select at least one report section.", "status-error");
            UiFeedbackHelper.showErrorDialog("Validation Error", "Select at least one section to include in the report.");
            return;
        }

        Task<ValuationReport> generateTask = new Task<>() {
            @Override
            protected ValuationReport call() throws Exception {
                ValuationReport report = valuationReportService.assembleReport(
                        propertyId,
                        sections,
                        analystNotesArea == null ? "" : analystNotesArea.getText()
                );
                Path generatedPath = reportPdfService.generatePdf(report);
                report.setPdfFilePath(generatedPath.toString());
                return report;
            }
        };

        generateTask.setOnRunning(event -> setLoadingState(true));
        generateTask.setOnSucceeded(event -> {
            setLoadingState(false);
            ValuationReport report = generateTask.getValue();
            latestGeneratedReport = report;
            reportSummaryLabel.setText("Report ID: " + report.getReportId()
                    + " | Sections: " + report.getIncludedSections().size());
            generatedReports.add(0, report);
            if (reportsTable != null) {
                reportsTable.getSelectionModel().select(0);
                UiAnimationHelper.highlightTableDiff(reportsTable, List.of(), generatedReports);
            }
            previousValuations.add(report.getPropertyId());
            updateNoValuationState();
            renderPdfPreview(report);
            UiAnimationHelper.playScanline(reportSummaryLabel);
            UiFeedbackHelper.setStatus(statusLabel, "Report generated: " + report.getPdfFilePath(), "status-success");
        });
        generateTask.setOnFailed(event -> {
            setLoadingState(false);
            Throwable ex = generateTask.getException();
            String message = ex == null ? "Unknown error" : ex.getMessage();
            UiFeedbackHelper.setStatus(statusLabel, "Could not generate report: " + message, "status-error");
            UiFeedbackHelper.showErrorDialog("Report Generation Failed", message);
        });
        Thread thread = new Thread(generateTask, "report-generate-task");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void onDownloadPdf() {
        ValuationReport selected = reportsTable == null ? null : reportsTable.getSelectionModel().getSelectedItem();
        ValuationReport report = selected != null ? selected : latestGeneratedReport;
        if (report == null || report.getPdfFilePath() == null || report.getPdfFilePath().isBlank()) {
            UiFeedbackHelper.showErrorDialog("No PDF Available", "Generate a report first, then try downloading/opening it.");
            return;
        }

        try {
            File pdf = new File(report.getPdfFilePath());
            if (!pdf.exists()) {
                UiFeedbackHelper.showErrorDialog("File Not Found", "The generated PDF file could not be found:\n" + report.getPdfFilePath());
                return;
            }
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdf);
                UiFeedbackHelper.setStatus(statusLabel, "Opened PDF: " + pdf.getAbsolutePath(), "status-success");
            } else {
                UiFeedbackHelper.showInfoDialog("PDF Location", "PDF generated at:\n" + pdf.getAbsolutePath());
            }
        } catch (Exception ex) {
            UiFeedbackHelper.showErrorDialog("Open PDF Failed", ex.getMessage());
        }
    }

    @FXML
    private void onSavePdfAs() {
        ValuationReport report = currentReport();
        if (report == null || report.getPdfFilePath() == null) {
            UiFeedbackHelper.showErrorDialog("No PDF", "Generate report before saving.");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Valuation Report");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        chooser.setInitialFileName("valuation-report-" + report.getPropertyId() + ".pdf");
        File target = chooser.showSaveDialog(null);
        if (target == null) {
            return;
        }
        try {
            Files.copy(Path.of(report.getPdfFilePath()), target.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            UiFeedbackHelper.showInfoDialog("Saved", "Report saved to:\n" + target.getAbsolutePath());
        } catch (Exception ex) {
            UiFeedbackHelper.showErrorDialog("Save Failed", "Could not save PDF (possible disk space issue): " + ex.getMessage());
        }
    }

    @FXML
    private void onPrintPdf() {
        ValuationReport report = currentReport();
        if (report == null || report.getPdfFilePath() == null) {
            UiFeedbackHelper.showErrorDialog("No PDF", "Generate report before printing.");
            return;
        }
        try (FileInputStream stream = new FileInputStream(report.getPdfFilePath())) {
            PrintService service = PrintServiceLookup.lookupDefaultPrintService();
            if (service == null) {
                UiFeedbackHelper.showErrorDialog("Print Unavailable", "No printer service found.");
                return;
            }
            DocPrintJob job = service.createPrintJob();
            Doc doc = new SimpleDoc(stream, DocFlavor.INPUT_STREAM.AUTOSENSE, null);
            job.print(doc, null);
            UiFeedbackHelper.setStatus(statusLabel, "Print job sent.", "status-success");
        } catch (Exception ex) {
            UiFeedbackHelper.showErrorDialog("Print Failed", ex.getMessage());
        }
    }

    @FXML
    private void onRedirectToFmv() {
        UiNavigationBridge.openScreen("FMV");
    }

    private List<String> selectedSections() {
        List<String> sections = new ArrayList<>();
        if (fmvCheckBox != null && fmvCheckBox.isSelected()) sections.add("fmv");
        if (comparablesCheckBox != null && comparablesCheckBox.isSelected()) sections.add("comparables");
        if (rentalCheckBox != null && rentalCheckBox.isSelected()) sections.add("rental_yield");
        if (roiCheckBox != null && roiCheckBox.isSelected()) sections.add("roi");
        if (trendCheckBox != null && trendCheckBox.isSelected()) sections.add("price_trend");
        if (heatmapCheckBox != null && heatmapCheckBox.isSelected()) sections.add("heatmap");
        if (!sections.contains("fmv") || !sections.contains("comparables") || !sections.contains("rental_yield")
                || !sections.contains("roi") || !sections.contains("price_trend") || !sections.contains("heatmap")) {
            sections.add("not_calculated_note");
        }
        return sections;
    }

    private void setLoadingState(boolean loading) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(loading);
            loadingIndicator.setManaged(loading);
        }
        UiAnimationHelper.setSkeletonVisible(reportsTable, loading);
        UiAnimationHelper.setSkeletonVisible(pdfPreviewImageView, loading);
        if (generateReportButton != null) {
            generateReportButton.setDisable(loading);
        }
        if (downloadPdfButton != null) {
            downloadPdfButton.setDisable(loading);
        }
        if (savePdfButton != null) {
            savePdfButton.setDisable(loading);
        }
        if (printPdfButton != null) {
            printPdfButton.setDisable(loading);
        }
    }

    private void seedValuationSelector() {
        if (valuationSelectorComboBox == null) {
            return;
        }
        valuationSelectorComboBox.setItems(previousValuations);
        if (!previousValuations.isEmpty()) {
            valuationSelectorComboBox.setValue(previousValuations.get(0));
        }
        valuationSelectorComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && propertyIdField != null) {
                propertyIdField.setText(newValue);
            }
        });
    }

    private void updateNoValuationState() {
        boolean empty = previousValuations.isEmpty();
        if (redirectToFmvButton != null) {
            redirectToFmvButton.setManaged(empty);
            redirectToFmvButton.setVisible(empty);
        }
    }

    private void renderPdfPreview(ValuationReport report) {
        if (pdfPreviewImageView == null || report == null || report.getPdfFilePath() == null) {
            return;
        }
        try (PDDocument document = PDDocument.load(new File(report.getPdfFilePath()))) {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(0, 120);
            File temp = File.createTempFile("report-preview-", ".png");
            ImageIO.write(image, "png", temp);
            temp.deleteOnExit();
            Image fxImage = new Image(temp.toURI().toString());
            pdfPreviewImageView.setImage(fxImage);
        } catch (Exception ex) {
            UiFeedbackHelper.setStatus(statusLabel, "Preview unavailable.", "status-warning");
        }
    }

    private ValuationReport currentReport() {
        ValuationReport selected = reportsTable == null ? null : reportsTable.getSelectionModel().getSelectedItem();
        return selected != null ? selected : latestGeneratedReport;
    }

}
