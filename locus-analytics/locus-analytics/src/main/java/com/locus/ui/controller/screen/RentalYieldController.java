package com.locus.ui.controller.screen;

import com.locus.model.RentalAnalysis;
import com.locus.service.RentalYieldService;
import com.locus.ui.ServiceRegistry;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class RentalYieldController implements Initializable {

    @FXML
    private TextField propertyValueField;
    @FXML
    private TextField monthlyRentField;
    @FXML
    private TextField annualExpensesField;
    @FXML
    private Label grossYieldLabel;
    @FXML
    private Label netYieldLabel;
    @FXML
    private Label cityAverageLabel;
    @FXML
    private Label statusLabel;

    private RentalYieldService rentalYieldService;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.rentalYieldService = serviceRegistry.rentalYieldService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (statusLabel != null) {
            statusLabel.setText("Enter values and click Calculate.");
        }
    }

    @FXML
    private void onCalculateRentalYield() {
        if (rentalYieldService == null) {
            statusLabel.setText("Rental service not available.");
            return;
        }
        try {
            if (propertyValueField == null || monthlyRentField == null || annualExpensesField == null) {
                if (statusLabel != null) {
                    statusLabel.setText("Rental yield dashboard refreshed.");
                }
                return;
            }
            double propertyValue = Double.parseDouble(propertyValueField.getText().trim());
            double monthlyRent = Double.parseDouble(monthlyRentField.getText().trim());
            double annualExpenses = Double.parseDouble(annualExpensesField.getText().trim());
            RentalAnalysis result = rentalYieldService.calculate(propertyValue, monthlyRent, annualExpenses);

            grossYieldLabel.setText(UiFormatters.number(result.getGrossYield()) + "%");
            netYieldLabel.setText(UiFormatters.number(result.getNetYield()) + "%");
            cityAverageLabel.setText(UiFormatters.number(result.getCityAverage()) + "%");
            statusLabel.setText("Rental yield calculated from stub service.");
        } catch (Exception ex) {
            statusLabel.setText("Invalid input: " + ex.getMessage());
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
        if (statusLabel != null) statusLabel.setText("Form reset.");
    }
}
