package com.locus.ui.controller.screen;

import com.locus.model.ROIAnalysis;
import com.locus.model.dto.ROIInput;
import com.locus.service.ROIService;
import com.locus.ui.ServiceRegistry;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

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

    private ROIService roiService;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.roiService = serviceRegistry.roiService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (purchaseDatePicker != null) {
            purchaseDatePicker.setValue(LocalDate.now().minusYears(3));
        }
    }

    @FXML
    private void onCalculateRoi() {
        if (roiService == null) {
            statusLabel.setText("ROI service not available.");
            return;
        }
        try {
            if (purchasePriceField == null || purchaseDatePicker == null) {
                if (statusLabel != null) {
                    statusLabel.setText("Dashboard mode loaded.");
                }
                return;
            }
            ROIInput input = new ROIInput();
            input.setPurchasePrice(Double.parseDouble(purchasePriceField.getText().trim()));
            input.setPurchaseDate(purchaseDatePicker.getValue());
            input.setCurrentValue(Double.parseDouble(currentValueField.getText().trim()));
            input.setCumulativeRentalIncome(Double.parseDouble(cumulativeRentalIncomeField.getText().trim()));
            input.setTotalExpenses(Double.parseDouble(totalExpensesField.getText().trim()));

            ROIAnalysis result = roiService.calculate(input);
            roiLabel.setText(UiFormatters.number(result.getRoiPercentage()) + "%");
            annualizedRoiLabel.setText(UiFormatters.number(result.getAnnualizedROI()) + "%");
            totalReturnLabel.setText(UiFormatters.currency(result.getTotalReturn()));
            statusLabel.setText("ROI calculated from stub service.");
        } catch (Exception ex) {
            statusLabel.setText("Invalid input: " + ex.getMessage());
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
        if (statusLabel != null) statusLabel.setText("Form reset.");
    }
}
