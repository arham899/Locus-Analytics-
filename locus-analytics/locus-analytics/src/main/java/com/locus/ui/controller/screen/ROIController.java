package com.locus.ui.controller.screen;

import com.locus.model.ROIAnalysis;
import com.locus.model.Property;
import com.locus.model.Valuation;
import com.locus.model.dto.ROIInput;
import com.locus.service.ROIService;
import com.locus.service.ValuationService;
import com.locus.ui.ServiceRegistry;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.util.Duration;

public class ROIController implements Initializable {

    @FXML
    private TextField purchasePriceField;
    @FXML
    private DatePicker purchaseDatePicker;
    @FXML
    private TextField currentValueField;
    @FXML
    private TextField cumulativeRentalIncomeField;
    @FXML
    private TextField totalExpensesField;
    @FXML
    private Label roiLabel;
    @FXML
    private Label annualizedRoiLabel;
    @FXML
    private Label totalReturnLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label warningBannerLabel;
    @FXML
    private PieChart roiBreakdownPieChart;
    @FXML
    private LineChart<String, Number> valueGrowthLineChart;
    @FXML
    private Button autoEstimateButton;
    @FXML
    private Button calculateRoiButton;
    @FXML
    private ProgressIndicator loadingIndicator;

    private ROIService roiService;
    private ValuationService valuationService;
    private final DoubleProperty roiAnimated = new SimpleDoubleProperty(0);
    private final DoubleProperty annualizedAnimated = new SimpleDoubleProperty(0);
    private final DoubleProperty totalReturnAnimated = new SimpleDoubleProperty(0);

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.roiService = serviceRegistry.roiService();
        this.valuationService = serviceRegistry.valuationService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (purchaseDatePicker != null) {
            purchaseDatePicker.setValue(LocalDate.now().minusYears(3));
        }
        if (warningBannerLabel != null) {
            warningBannerLabel.setVisible(false);
            warningBannerLabel.setManaged(false);
        }
        roiAnimated.addListener((obs, oldValue, newValue) ->
                roiLabel.setText(UiFormatters.number(newValue.doubleValue()) + "%"));
        annualizedAnimated.addListener((obs, oldValue, newValue) ->
                annualizedRoiLabel.setText(UiFormatters.number(newValue.doubleValue()) + "%"));
        totalReturnAnimated.addListener((obs, oldValue, newValue) ->
                totalReturnLabel.setText(UiFormatters.currency(newValue.doubleValue())));
        UiAnimationHelper.playStaggeredReveal(List.of(
                roiLabel, annualizedRoiLabel, totalReturnLabel, warningBannerLabel, roiBreakdownPieChart, valueGrowthLineChart
        ));
        UiAnimationHelper.attachHoverScale(calculateRoiButton);
        UiAnimationHelper.attachHoverScale(autoEstimateButton);
    }

    @FXML
    private void onCalculateRoi() {
        if (roiService == null) {
            UiFeedbackHelper.setStatus(statusLabel, "ROI service not available.", "status-error");
            return;
        }
        UiFeedbackHelper.clearValidation(
                purchasePriceField, purchaseDatePicker, currentValueField, cumulativeRentalIncomeField, totalExpensesField
        );
        try {
            if (purchasePriceField == null || purchaseDatePicker == null) {
                UiFeedbackHelper.setStatus(statusLabel, "Dashboard mode loaded.", "status-success");
                return;
            }
            ROIInput input = new ROIInput();
            input.setPurchasePrice(Double.parseDouble(purchasePriceField.getText().trim()));
            input.setPurchaseDate(purchaseDatePicker.getValue());
            input.setCurrentValue(Double.parseDouble(currentValueField.getText().trim()));
            input.setCumulativeRentalIncome(Double.parseDouble(cumulativeRentalIncomeField.getText().trim()));
            input.setTotalExpenses(Double.parseDouble(totalExpensesField.getText().trim()));

            Task<ROIAnalysis> task = new Task<>() {
                @Override
                protected ROIAnalysis call() {
                    return roiService.calculate(input);
                }
            };
            task.setOnRunning(event -> setLoadingState(true));
            task.setOnSucceeded(event -> {
                setLoadingState(false);
                ROIAnalysis result = task.getValue();
                UiAnimationHelper.animateNumber(roiAnimated, 0, result.getRoiPercentage(), Duration.millis(760));
                UiAnimationHelper.animateNumber(annualizedAnimated, 0, result.getAnnualizedROI(), Duration.millis(760));
                UiAnimationHelper.animateNumber(totalReturnAnimated, 0, result.getTotalReturn(), Duration.millis(860));
                UiAnimationHelper.pulseMetric(roiLabel, UiAnimationHelper.SignalType.POSITIVE);
                UiAnimationHelper.pulseMetric(annualizedRoiLabel, UiAnimationHelper.SignalType.POSITIVE);
                renderCharts(result);
                renderHoldingPeriodWarning(input.getPurchaseDate());
                UiAnimationHelper.playScanline(valueGrowthLineChart);
                UiFeedbackHelper.setStatus(statusLabel, "ROI calculated successfully.", "status-success");
            });
            task.setOnFailed(event -> {
                setLoadingState(false);
                Throwable ex = task.getException();
                UiFeedbackHelper.setStatus(statusLabel, "Could not calculate ROI.", "status-error");
                UiFeedbackHelper.showErrorDialog("ROI Calculation Failed", ex == null ? "Unknown error" : ex.getMessage());
            });
            Thread thread = new Thread(task, "roi-calc-task");
            thread.setDaemon(true);
            thread.start();
        } catch (com.locus.exception.ValidationException ex) {
            Map<String, Control> controls = new HashMap<>();
            controls.put("purchasePrice", purchasePriceField);
            controls.put("purchaseDate", purchaseDatePicker);
            controls.put("currentValue", currentValueField);
            controls.put("cumulativeRentalIncome", cumulativeRentalIncomeField);
            controls.put("totalExpenses", totalExpensesField);
            UiFeedbackHelper.markFromFieldErrors(ex.getFieldErrors(), controls);
            UiFeedbackHelper.setStatus(statusLabel, "Validation error. Check highlighted fields.", "status-error");
            UiFeedbackHelper.showErrorDialog("Invalid ROI Input", UiFeedbackHelper.formatValidationMessage(ex));
        } catch (NumberFormatException ex) {
            UiFeedbackHelper.markInvalid(purchasePriceField);
            UiFeedbackHelper.markInvalid(currentValueField);
            UiFeedbackHelper.markInvalid(cumulativeRentalIncomeField);
            UiFeedbackHelper.markInvalid(totalExpensesField);
            UiFeedbackHelper.setStatus(statusLabel, "Values must be numeric.", "status-error");
            UiFeedbackHelper.showErrorDialog("Invalid ROI Input", "Numeric fields require valid numbers.");
        } catch (Exception ex) {
            UiFeedbackHelper.setStatus(statusLabel, "Invalid input: " + ex.getMessage(), "status-error");
            UiFeedbackHelper.showErrorDialog("ROI Calculation Failed", ex.getMessage());
        }
    }

    @FXML
    private void onAutoEstimateCurrentValue() {
        if (valuationService == null) {
            UiFeedbackHelper.setStatus(statusLabel, "Valuation service not available.", "status-error");
            return;
        }
        try {
            Property property = new Property();
            property.setPropertyId("roi-auto-" + UUID.randomUUID());
            property.setCity("Karachi");
            property.setLocality("DHA Phase 6");
            property.setPropertyType("house");
            property.setArea(2000);
            property.setBedrooms(4);
            property.setBathrooms(3);
            property.setListingDate(LocalDate.now());
            property.setPrice(Double.parseDouble(purchasePriceField.getText().trim()));
            property.setAmenities(List.of("parking", "security"));

            Task<Valuation> task = new Task<>() {
                @Override
                protected Valuation call() {
                    return valuationService.estimateFMV(property);
                }
            };
            task.setOnRunning(event -> setLoadingState(true));
            task.setOnSucceeded(event -> {
                setLoadingState(false);
                currentValueField.setText(UiFormatters.number(task.getValue().getEstimatedFmv()).replace(",", ""));
                UiFeedbackHelper.setStatus(statusLabel, "Current value auto-estimated.", "status-success");
            });
            task.setOnFailed(event -> {
                setLoadingState(false);
                Throwable ex = task.getException();
                UiFeedbackHelper.setStatus(statusLabel, "Auto-estimate failed.", "status-error");
                UiFeedbackHelper.showErrorDialog("Auto-estimate Failed", ex == null ? "Unknown error" : ex.getMessage());
            });
            Thread thread = new Thread(task, "roi-auto-estimate-task");
            thread.setDaemon(true);
            thread.start();
        } catch (Exception ex) {
            UiFeedbackHelper.markInvalid(purchasePriceField);
            UiFeedbackHelper.showErrorDialog("Invalid Input", "Enter a valid purchase price before auto-estimation.");
        }
    }

    @FXML
    private void onReset() {
        if (purchasePriceField != null) purchasePriceField.clear();
        if (currentValueField != null) currentValueField.clear();
        if (cumulativeRentalIncomeField != null) cumulativeRentalIncomeField.clear();
        if (totalExpensesField != null) totalExpensesField.clear();
        if (purchaseDatePicker != null) purchaseDatePicker.setValue(LocalDate.now().minusYears(3));
        if (roiLabel != null) roiLabel.setText("-");
        if (annualizedRoiLabel != null) annualizedRoiLabel.setText("-");
        if (totalReturnLabel != null) totalReturnLabel.setText("-");
        if (statusLabel != null) UiFeedbackHelper.setStatus(statusLabel, "Form reset.", "status-success");
        if (warningBannerLabel != null) {
            warningBannerLabel.setVisible(false);
            warningBannerLabel.setManaged(false);
        }
        if (roiBreakdownPieChart != null) {
            roiBreakdownPieChart.getData().clear();
        }
        if (valueGrowthLineChart != null) {
            valueGrowthLineChart.getData().clear();
        }
        UiFeedbackHelper.clearValidation(
                purchasePriceField, purchaseDatePicker, currentValueField, cumulativeRentalIncomeField, totalExpensesField
        );
        setLoadingState(false);
    }

    private void renderCharts(ROIAnalysis result) {
        if (roiBreakdownPieChart != null) {
            double capitalAppreciation = Math.max(0, result.getCurrentValue() - result.getPurchasePrice());
            double rentalIncome = Math.max(0, result.getCumulativeRentalIncome());
            roiBreakdownPieChart.getData().setAll(
                    new PieChart.Data("Capital Appreciation", capitalAppreciation),
                    new PieChart.Data("Rental Income", rentalIncome)
            );
        }
        if (valueGrowthLineChart != null) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Value Growth");
            series.getData().add(new XYChart.Data<>("Purchase", result.getPurchasePrice()));
            series.getData().add(new XYChart.Data<>("Current", result.getCurrentValue()));
            UiAnimationHelper.revealLineSeries(valueGrowthLineChart, series);
        }
    }

    private void renderHoldingPeriodWarning(LocalDate purchaseDate) {
        if (warningBannerLabel == null || purchaseDate == null) {
            return;
        }
        boolean warn = purchaseDate.isAfter(LocalDate.now().minusYears(1));
        warningBannerLabel.setManaged(warn);
        warningBannerLabel.setVisible(warn);
    }

    private void setLoadingState(boolean loading) {
        if (loadingIndicator != null) {
            loadingIndicator.setManaged(loading);
            loadingIndicator.setVisible(loading);
        }
        UiAnimationHelper.setSkeletonVisible(roiBreakdownPieChart, loading);
        UiAnimationHelper.setSkeletonVisible(valueGrowthLineChart, loading);
        if (calculateRoiButton != null) {
            calculateRoiButton.setDisable(loading);
            calculateRoiButton.setText(loading ? "Calculating..." : "Recalculate");
        }
        if (autoEstimateButton != null) {
            autoEstimateButton.setDisable(loading);
        }
    }
}
