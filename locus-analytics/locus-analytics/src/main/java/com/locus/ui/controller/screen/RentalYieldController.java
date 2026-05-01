package com.locus.ui.controller.screen;

import com.locus.model.RentalAnalysis;
import com.locus.service.RentalYieldService;
import com.locus.ui.ServiceRegistry;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.List;
import javafx.util.Duration;

public class RentalYieldController implements Initializable {

    @FXML
    private TextField propertyValueField;
    @FXML
    private TextField monthlyRentField;
    @FXML
    private TextField annualExpensesField;
    @FXML
    private ComboBox<String> previousValuationComboBox;
    @FXML
    private ComboBox<String> cityComboBox;
    @FXML
    private ComboBox<String> localityComboBox;
    @FXML
    private Label grossYieldLabel;
    @FXML
    private Label netYieldLabel;
    @FXML
    private Label cityAverageLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label fallbackLabel;
    @FXML
    private ProgressIndicator loadingIndicator;
    @FXML
    private Button recalculateButton;
    @FXML
    private BarChart<String, Number> yieldBarChart;

    private RentalYieldService rentalYieldService;
    private final DoubleProperty grossAnimated = new SimpleDoubleProperty(0);
    private final DoubleProperty netAnimated = new SimpleDoubleProperty(0);
    private final DoubleProperty cityAvgAnimated = new SimpleDoubleProperty(0);

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.rentalYieldService = serviceRegistry.rentalYieldService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (statusLabel != null) {
            statusLabel.setText("Enter values and click Calculate.");
        }
        if (cityComboBox != null) {
            cityComboBox.setItems(FXCollections.observableArrayList("Karachi", "Islamabad", "Lahore"));
            cityComboBox.setValue("Karachi");
        }
        if (localityComboBox != null) {
            localityComboBox.setItems(FXCollections.observableArrayList("DHA", "Clifton", "F-7", "Gulberg"));
            localityComboBox.setValue("DHA");
        }
        if (previousValuationComboBox != null) {
            previousValuationComboBox.setItems(FXCollections.observableArrayList(
                    "None",
                    "Valuation p-001 (25,000,000)",
                    "Valuation p-002 (31,500,000)",
                    "Valuation p-003 (42,000,000)"
            ));
            previousValuationComboBox.setValue("None");
            previousValuationComboBox.valueProperty().addListener((obs, oldValue, newValue) -> applyPreviousValuation(newValue));
        }
        if (fallbackLabel != null) {
            fallbackLabel.setVisible(false);
            fallbackLabel.setManaged(false);
        }
        grossAnimated.addListener((obs, oldValue, newValue) ->
                grossYieldLabel.setText(UiFormatters.number(newValue.doubleValue()) + "%"));
        netAnimated.addListener((obs, oldValue, newValue) ->
                netYieldLabel.setText(UiFormatters.number(newValue.doubleValue()) + "%"));
        cityAvgAnimated.addListener((obs, oldValue, newValue) ->
                cityAverageLabel.setText(UiFormatters.number(newValue.doubleValue()) + "%"));
        UiAnimationHelper.playStaggeredReveal(List.of(
                grossYieldLabel, netYieldLabel, cityAverageLabel, yieldBarChart, fallbackLabel
        ));
        UiAnimationHelper.attachHoverScale(recalculateButton);
    }

    @FXML
    private void onCalculateRentalYield() {
        if (rentalYieldService == null) {
            UiFeedbackHelper.setStatus(statusLabel, "Rental service not available.", "status-error");
            return;
        }
        UiFeedbackHelper.clearValidation(propertyValueField, monthlyRentField, annualExpensesField);
        try {
            if (propertyValueField == null || monthlyRentField == null || annualExpensesField == null) {
                UiFeedbackHelper.setStatus(statusLabel, "Rental yield dashboard refreshed.", "status-success");
                return;
            }
            double propertyValue = Double.parseDouble(propertyValueField.getText().trim());
            double monthlyRent = Double.parseDouble(monthlyRentField.getText().trim());
            double annualExpenses = Double.parseDouble(annualExpensesField.getText().trim());
            String city = cityComboBox == null ? "Karachi" : cityComboBox.getValue();
            String locality = localityComboBox == null ? null : localityComboBox.getValue();

            Task<RentalAnalysis> task = new Task<>() {
                @Override
                protected RentalAnalysis call() {
                    RentalAnalysis result = rentalYieldService.calculate(propertyValue, monthlyRent, annualExpenses);
                    double cityAverage = rentalYieldService.getCityAverageYield(city, locality);
                    result.setCityAverage(cityAverage);
                    return result;
                }
            };
            task.setOnRunning(event -> setLoadingState(true));
            task.setOnSucceeded(event -> {
                setLoadingState(false);
                RentalAnalysis result = task.getValue();
                UiAnimationHelper.animateNumber(grossAnimated, 0, result.getGrossYield(), Duration.millis(700));
                UiAnimationHelper.animateNumber(netAnimated, 0, result.getNetYield(), Duration.millis(760));
                UiAnimationHelper.animateNumber(cityAvgAnimated, 0, result.getCityAverage(), Duration.millis(820));
                UiAnimationHelper.pulseMetric(netYieldLabel, UiAnimationHelper.SignalType.POSITIVE);
                renderYieldChart(result.getNetYield(), result.getCityAverage());
                boolean usedFallback = locality == null || locality.isBlank();
                setFallbackMessage(usedFallback);
                UiAnimationHelper.playScanline(yieldBarChart);
                UiFeedbackHelper.setStatus(statusLabel, "Rental yield calculated successfully.", "status-success");
            });
            task.setOnFailed(event -> {
                setLoadingState(false);
                Throwable ex = task.getException();
                UiFeedbackHelper.setStatus(statusLabel, "Could not calculate rental yield.", "status-error");
                UiFeedbackHelper.showErrorDialog("Rental Yield Error", ex == null ? "Unknown error" : ex.getMessage());
            });
            Thread thread = new Thread(task, "rental-yield-task");
            thread.setDaemon(true);
            thread.start();
        } catch (com.locus.exception.ValidationException ex) {
            UiFeedbackHelper.markFromFieldErrors(
                    ex.getFieldErrors(),
                    Map.of(
                            "propertyValue", (Control) propertyValueField,
                            "monthlyRent", monthlyRentField,
                            "annualExpenses", annualExpensesField,
                            "rent", monthlyRentField
                    )
            );
            UiFeedbackHelper.setStatus(statusLabel, "Validation error. Check highlighted fields.", "status-error");
            UiFeedbackHelper.showErrorDialog("Invalid Rental Input", UiFeedbackHelper.formatValidationMessage(ex));
        } catch (NumberFormatException ex) {
            UiFeedbackHelper.markInvalid(propertyValueField);
            UiFeedbackHelper.markInvalid(monthlyRentField);
            UiFeedbackHelper.markInvalid(annualExpensesField);
            UiFeedbackHelper.setStatus(statusLabel, "Values must be numeric.", "status-error");
            UiFeedbackHelper.showErrorDialog("Invalid Rental Input", "All input values must be valid numbers.");
        } catch (Exception ex) {
            UiFeedbackHelper.setStatus(statusLabel, "Invalid input: " + ex.getMessage(), "status-error");
            UiFeedbackHelper.showErrorDialog("Rental Yield Error", ex.getMessage());
        }
    }

    @FXML
    private void onReset() {
        if (propertyValueField != null) propertyValueField.clear();
        if (monthlyRentField != null) monthlyRentField.clear();
        if (annualExpensesField != null) annualExpensesField.clear();
        if (grossYieldLabel != null) grossYieldLabel.setText("-");
        if (netYieldLabel != null) netYieldLabel.setText("-");
        if (cityAverageLabel != null) cityAverageLabel.setText("-");
        if (statusLabel != null) UiFeedbackHelper.setStatus(statusLabel, "Form reset.", "status-success");
        UiFeedbackHelper.clearValidation(propertyValueField, monthlyRentField, annualExpensesField);
        setFallbackMessage(false);
        if (yieldBarChart != null) {
            yieldBarChart.getData().clear();
        }
        setLoadingState(false);
    }

    private void applyPreviousValuation(String option) {
        if (option == null || option.equals("None") || propertyValueField == null) {
            return;
        }
        int start = option.lastIndexOf('(');
        int end = option.lastIndexOf(')');
        if (start > -1 && end > start) {
            String value = option.substring(start + 1, end).replace(",", "");
            propertyValueField.setText(value);
        }
    }

    private void renderYieldChart(double propertyYield, double cityAverage) {
        if (yieldBarChart == null) {
            return;
        }
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Yield Comparison");
        series.getData().add(new XYChart.Data<>("Property", propertyYield));
        series.getData().add(new XYChart.Data<>("City Average", cityAverage));
        UiAnimationHelper.revealBarSeries(yieldBarChart, series);
    }

    private void setFallbackMessage(boolean fallbackUsed) {
        if (fallbackLabel == null) {
            return;
        }
        fallbackLabel.setManaged(fallbackUsed);
        fallbackLabel.setVisible(fallbackUsed);
        fallbackLabel.setText(fallbackUsed
                ? "Using city-average fallback because locality-level data is unavailable."
                : "Using locality-level benchmark.");
    }

    private void setLoadingState(boolean loading) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(loading);
            loadingIndicator.setManaged(loading);
        }
        UiAnimationHelper.setSkeletonVisible(yieldBarChart, loading);
        if (recalculateButton != null) {
            recalculateButton.setDisable(loading);
            recalculateButton.setText(loading ? "Calculating..." : "Recalculate");
        }
    }
}
