package com.locus.ui.controller.screen;

import com.locus.model.dto.TimeRange;
import com.locus.model.dto.TrendPoint;
import com.locus.model.dto.TrendStatistics;
import com.locus.service.PriceTrendService;
import com.locus.ui.ServiceRegistry;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import javafx.util.Duration;

public class PriceTrendController implements Initializable {

    @FXML
    private ComboBox<String> cityComboBox;
    @FXML
    private ComboBox<String> localityComboBox;
    @FXML
    private ComboBox<String> propertyTypeComboBox;
    @FXML
    private RadioButton oneYearRadio;
    @FXML
    private RadioButton threeYearRadio;
    @FXML
    private RadioButton fiveYearRadio;
    @FXML
    private RadioButton customRangeRadio;
    @FXML
    private DatePicker customFromDatePicker;
    @FXML
    private DatePicker customToDatePicker;
    @FXML
    private CheckBox overlaySeriesCheck;
    @FXML
    private LineChart<String, Number> trendChart;
    @FXML
    private Button refreshTrendButton;
    @FXML
    private Label annualAppreciationLabel;
    @FXML
    private Label highestPriceLabel;
    @FXML
    private Label lowestPriceLabel;
    @FXML
    private Label currentAverageLabel;
    @FXML
    private Label fallbackLabel;
    @FXML
    private Label statusLabel;

    private PriceTrendService priceTrendService;
    private List<TrendPoint> latestPoints = List.of();
    private final DoubleProperty annualAnimated = new SimpleDoubleProperty(0);
    private final DoubleProperty highestAnimated = new SimpleDoubleProperty(0);
    private final DoubleProperty lowestAnimated = new SimpleDoubleProperty(0);
    private final DoubleProperty averageAnimated = new SimpleDoubleProperty(0);
    private String highestDateSuffix = "";
    private String lowestDateSuffix = "";

    private static final java.util.Map<String, java.util.List<String>> CURATED_LOCALITIES_BY_CITY = java.util.Map.of(
            "Karachi", java.util.List.of(
                    "DHA Phase 1", "DHA Phase 2", "DHA Phase 4", "DHA Phase 5", "DHA Phase 6",
                    "DHA Phase 7", "DHA Phase 8", "Clifton", "Bahria Town Karachi",
                    "Gulshan-e-Iqbal", "Gulistan-e-Johar", "Malir Cantt", "Askari 5",
                    "PECHS", "North Nazimabad", "Federal B Area", "Saddar",
                    "Korangi", "Nazimabad", "Scheme 33", "Saima Residency"
            ),
            "Islamabad", java.util.List.of(
                    "F-6", "F-7", "F-8", "F-10", "F-11",
                    "G-9", "G-10", "G-11", "G-13", "G-15",
                    "E-7", "E-11", "I-8", "I-10",
                    "DHA Phase 1 Islamabad", "DHA Phase 2 Islamabad",
                    "Bahria Town Islamabad", "Bahria Enclave",
                    "Gulberg Islamabad", "B-17", "PWD", "Park View City"
            ),
            "Lahore", java.util.List.of(
                    "DHA Phase 1", "DHA Phase 2", "DHA Phase 3", "DHA Phase 4",
                    "DHA Phase 5", "DHA Phase 6", "DHA Phase 7", "DHA Phase 8",
                    "Gulberg", "Gulberg III", "Model Town", "Johar Town",
                    "Bahria Town Lahore", "Bahria Orchard",
                    "Wapda Town", "Garden Town", "Iqbal Town",
                    "Cantt", "Askari", "EME Society", "Valencia Town",
                    "Faisal Town", "PIA Housing Society"
            )
    );

    private ServiceRegistry serviceRegistry;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.priceTrendService = serviceRegistry.priceTrendService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (cityComboBox != null) {
            cityComboBox.setItems(FXCollections.observableArrayList("Karachi", "Islamabad", "Lahore"));
            cityComboBox.valueProperty().addListener((obs, oldCity, newCity) -> updateLocalities(newCity));
            cityComboBox.setValue("Karachi");
        }
        if (localityComboBox != null) {
            updateLocalities("Karachi");
        }
        if (propertyTypeComboBox != null) {
            propertyTypeComboBox.setItems(FXCollections.observableArrayList("house", "apartment", "plot", "commercial"));
            propertyTypeComboBox.setValue("house");
        }
        ToggleGroup group = new ToggleGroup();
        oneYearRadio.setToggleGroup(group);
        threeYearRadio.setToggleGroup(group);
        fiveYearRadio.setToggleGroup(group);
        customRangeRadio.setToggleGroup(group);
        oneYearRadio.setSelected(true);
        customFromDatePicker.setDisable(true);
        customToDatePicker.setDisable(true);
        group.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            boolean custom = customRangeRadio.isSelected();
            customFromDatePicker.setDisable(!custom);
            customToDatePicker.setDisable(!custom);
        });
        if (fallbackLabel != null) {
            fallbackLabel.setManaged(false);
            fallbackLabel.setVisible(false);
        }
        if (trendChart != null) {
            trendChart.setAnimated(false);
        }
        annualAnimated.addListener((obs, oldValue, newValue) ->
                annualAppreciationLabel.setText(UiFormatters.number(newValue.doubleValue()) + "%"));
        highestAnimated.addListener((obs, oldValue, newValue) ->
                highestPriceLabel.setText(UiFormatters.currency(newValue.doubleValue()) + highestDateSuffix));
        lowestAnimated.addListener((obs, oldValue, newValue) ->
                lowestPriceLabel.setText(UiFormatters.currency(newValue.doubleValue()) + lowestDateSuffix));
        averageAnimated.addListener((obs, oldValue, newValue) ->
                currentAverageLabel.setText(UiFormatters.currency(newValue.doubleValue())));
        UiAnimationHelper.playStaggeredReveal(List.of(
                annualAppreciationLabel, highestPriceLabel, lowestPriceLabel, currentAverageLabel, trendChart
        ));
        UiAnimationHelper.attachHoverScale(refreshTrendButton);
    }

    @FXML
    private void onLoadTrend() {
        if (priceTrendService == null) {
            UiFeedbackHelper.setStatus(statusLabel, "Price trend service not available.", "status-error");
            return;
        }
        try {
            if (trendChart == null || cityComboBox == null || propertyTypeComboBox == null) {
                if (statusLabel != null) {
                    statusLabel.setText("Trend dashboard loaded.");
                }
                return;
            }
            TimeRange range = resolveTimeRange();
            String city = cityComboBox.getValue();
            String locality = blankToNull(localityComboBox.getValue());
            String selectedType = propertyTypeComboBox.getValue();
            Task<TrendLoadBundle> task = new Task<>() {
                @Override
                protected TrendLoadBundle call() {
                    boolean fallbackUsed = false;
                    List<TrendPoint> points = priceTrendService.getTrend(city, locality, selectedType, range);
                    if (locality != null && (points == null || points.isEmpty())) {
                        points = priceTrendService.getTrend(city, null, selectedType, range);
                        fallbackUsed = true;
                    }
                    TrendStatistics stats = priceTrendService.computeStatistics(points);
                    List<TrendPoint> overlayPoints = List.of();
                    if (overlaySeriesCheck != null && overlaySeriesCheck.isSelected()) {
                        overlayPoints = priceTrendService.getTrend(city, locality, "commercial", range);
                    }
                    return new TrendLoadBundle(points, overlayPoints, stats, fallbackUsed);
                }
            };
            task.setOnRunning(event -> {
                if (refreshTrendButton != null) {
                    refreshTrendButton.setDisable(true);
                    refreshTrendButton.setText("Loading...");
                }
                UiAnimationHelper.setSkeletonVisible(trendChart, true);
            });
            task.setOnSucceeded(event -> {
                if (refreshTrendButton != null) {
                    refreshTrendButton.setDisable(false);
                    refreshTrendButton.setText("Refresh Trend");
                }
                UiAnimationHelper.setSkeletonVisible(trendChart, false);
                TrendLoadBundle bundle = task.getValue();
                latestPoints = bundle.points;
                renderSeries(bundle.points, bundle.overlayPoints, city + " - " + selectedType);
                renderStats(bundle.statistics);
                UiAnimationHelper.pulseMetric(currentAverageLabel, UiAnimationHelper.SignalType.POSITIVE);
                setFallbackVisible(bundle.fallbackUsed);
                UiFeedbackHelper.setStatus(statusLabel, "Trend loaded with " + bundle.points.size() + " points.", "status-success");
            });
            task.setOnFailed(event -> {
                if (refreshTrendButton != null) {
                    refreshTrendButton.setDisable(false);
                    refreshTrendButton.setText("Refresh Trend");
                }
                UiAnimationHelper.setSkeletonVisible(trendChart, false);
                Throwable ex = task.getException();
                UiFeedbackHelper.setStatus(statusLabel, "Could not load trend.", "status-error");
                UiFeedbackHelper.showErrorDialog("Price Trend Error", ex == null ? "Unknown error" : ex.getMessage());
            });
            Thread thread = new Thread(task, "price-trend-task");
            thread.setDaemon(true);
            thread.start();
        } catch (Exception ex) {
            UiFeedbackHelper.setStatus(statusLabel, "Could not load trend: " + ex.getMessage(), "status-error");
        }
    }

    @FXML
    private void onResetZoom() {
        if (!latestPoints.isEmpty()) {
            renderSeries(latestPoints, List.of(), "Trend");
            UiFeedbackHelper.setStatus(statusLabel, "Zoom reset.", "status-success");
        }
    }

    private void renderSeries(List<TrendPoint> points, List<TrendPoint> overlay, String name) {
        XYChart.Series<String, Number> main = new XYChart.Series<>();
        main.setName(name);
        for (TrendPoint point : points) {
            main.getData().add(new XYChart.Data<>(point.getPeriod(), point.getAveragePrice()));
        }
        if (overlay != null && !overlay.isEmpty()) {
            XYChart.Series<String, Number> over = new XYChart.Series<>();
            over.setName("Commercial Overlay");
            for (TrendPoint point : overlay) {
                over.getData().add(new XYChart.Data<>(point.getPeriod(), point.getAveragePrice()));
            }
            UiAnimationHelper.revealLineSeries(trendChart, main, over);
        } else {
            UiAnimationHelper.revealLineSeries(trendChart, main);
        }
        installPointLabelsAndZoom(main, points);
    }

    private void installPointLabelsAndZoom(XYChart.Series<String, Number> series, List<TrendPoint> points) {
        for (int i = 0; i < series.getData().size(); i++) {
            Data<String, Number> data = series.getData().get(i);
            int idx = i;
            data.nodeProperty().addListener((obs, oldNode, node) -> {
                if (node != null) {
                    Tooltip.install(node, new Tooltip(UiFormatters.currency(data.getYValue().doubleValue())));
                    node.setOnMouseClicked(event -> zoomAround(points, idx));
                }
            });
        }
    }

    private void zoomAround(List<TrendPoint> points, int centerIndex) {
        if (points == null || points.isEmpty()) {
            return;
        }
        int from = Math.max(0, centerIndex - 3);
        int to = Math.min(points.size(), centerIndex + 4);
        List<TrendPoint> window = points.subList(from, to);
        XYChart.Series<String, Number> zoomSeries = new XYChart.Series<>();
        zoomSeries.setName("Zoomed (Monthly)");
        for (TrendPoint point : window) {
            zoomSeries.getData().add(new XYChart.Data<>(point.getPeriod(), point.getAveragePrice()));
        }
        trendChart.getData().setAll(zoomSeries);
        UiFeedbackHelper.setStatus(statusLabel, "Zoomed to monthly segment around " + points.get(centerIndex).getPeriod(), "status-warning");
    }

    private void renderStats(TrendStatistics stats) {
        highestDateSuffix = " (" + stats.getHighestDate() + ")";
        lowestDateSuffix = " (" + stats.getLowestDate() + ")";
        UiAnimationHelper.animateNumber(annualAnimated, 0, stats.getAnnualAppreciationRate(), Duration.millis(700));
        UiAnimationHelper.animateNumber(highestAnimated, 0, stats.getHighestPrice(), Duration.millis(780));
        UiAnimationHelper.animateNumber(lowestAnimated, 0, stats.getLowestPrice(), Duration.millis(780));
        UiAnimationHelper.animateNumber(averageAnimated, 0, stats.getCurrentAverage(), Duration.millis(820));
    }

    private void setFallbackVisible(boolean visible) {
        if (fallbackLabel != null) {
            fallbackLabel.setManaged(visible);
            fallbackLabel.setVisible(visible);
        }
    }

    private TimeRange resolveTimeRange() {
        if (oneYearRadio != null && oneYearRadio.isSelected()) {
            return TimeRange.ONE_YEAR;
        }
        if (threeYearRadio != null && threeYearRadio.isSelected()) {
            return TimeRange.THREE_YEARS;
        }
        if (fiveYearRadio != null && fiveYearRadio.isSelected()) {
            return TimeRange.FIVE_YEARS;
        }
        TimeRange custom = TimeRange.CUSTOM;
        custom.setCustomFrom(customFromDatePicker == null ? LocalDate.now().minusYears(1) : customFromDatePicker.getValue());
        custom.setCustomTo(customToDatePicker == null ? LocalDate.now() : customToDatePicker.getValue());
        return custom;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private record TrendLoadBundle(java.util.List<TrendPoint> points, java.util.List<TrendPoint> overlayPoints, com.locus.model.dto.TrendStatistics statistics,
                                   boolean fallbackUsed) {
    }

    private void setLoadingState(boolean loading) {
        if (refreshTrendButton != null) {
            refreshTrendButton.setDisable(loading);
            refreshTrendButton.setText(loading ? "Plotting..." : "Refresh Trends");
        }
    }

    private void updateLocalities(String city) {
        if (localityComboBox == null) return;
        java.util.List<String> localities = new java.util.ArrayList<>();
        localities.add(""); // Optional for trends
        localities.addAll(CURATED_LOCALITIES_BY_CITY.getOrDefault(city, java.util.List.of()));
        localityComboBox.setItems(FXCollections.observableArrayList(localities));
        if (localityComboBox.getItems().size() > 0) {
            localityComboBox.setValue("");
        }
    }
}
