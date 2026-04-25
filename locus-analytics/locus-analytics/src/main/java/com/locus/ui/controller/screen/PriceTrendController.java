package com.locus.ui.controller.screen;

import com.locus.model.dto.TimeRange;
import com.locus.model.dto.TrendPoint;
import com.locus.model.dto.TrendStatistics;
import com.locus.service.PriceTrendService;
import com.locus.ui.ServiceRegistry;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PriceTrendController implements Initializable {

    @FXML
    private ComboBox<String> cityComboBox;
    @FXML
    private ComboBox<String> propertyTypeComboBox;
    @FXML
    private LineChart<String, Number> trendChart;
    @FXML
    private Label annualAppreciationLabel;
    @FXML
    private Label highestPriceLabel;
    @FXML
    private Label lowestPriceLabel;
    @FXML
    private Label statusLabel;

    private PriceTrendService priceTrendService;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.priceTrendService = serviceRegistry.priceTrendService();
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
        if (trendChart != null) {
            trendChart.setAnimated(false);
        }
    }

    @FXML
    private void onLoadTrend() {
        if (priceTrendService == null) {
            statusLabel.setText("Price trend service not available.");
            return;
        }
        try {
            if (trendChart == null || cityComboBox == null || propertyTypeComboBox == null) {
                if (statusLabel != null) {
                    statusLabel.setText("Trend dashboard loaded.");
                }
                return;
            }
            List<TrendPoint> points = priceTrendService.getTrend(
                    cityComboBox.getValue(),
                    null,
                    propertyTypeComboBox.getValue(),
                    TimeRange.ONE_YEAR
            );
            TrendStatistics stats = priceTrendService.computeStatistics(points);
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(cityComboBox.getValue() + " - " + propertyTypeComboBox.getValue());
            for (TrendPoint point : points) {
                series.getData().add(new XYChart.Data<>(point.getPeriod(), point.getAveragePrice()));
            }
            trendChart.getData().setAll(series);
            annualAppreciationLabel.setText(UiFormatters.number(stats.getAnnualAppreciationRate()) + "%");
            highestPriceLabel.setText(UiFormatters.currency(stats.getHighestPrice()));
            lowestPriceLabel.setText(UiFormatters.currency(stats.getLowestPrice()));
            statusLabel.setText("Trend loaded with " + points.size() + " points.");
        } catch (Exception ex) {
            statusLabel.setText("Could not load trend: " + ex.getMessage());
        }
    }
}
