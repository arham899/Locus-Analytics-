package com.locus.ui.controller.screen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.locus.model.InvestmentCluster;
import com.locus.model.dto.ClusterParams;
import com.locus.service.InvestmentClusterService;
import com.locus.ui.ServiceRegistry;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ClusterController implements Initializable {

    @FXML
    private ComboBox<String> cityComboBox;
    @FXML
    private ComboBox<String> propertyTypeComboBox;
    @FXML
    private ComboBox<String> analysisPeriodComboBox;
    @FXML
    private TextField minListingsField;
    @FXML
    private Button identifyButton;
    @FXML
    private ProgressIndicator loadingIndicator;
    @FXML
    private TableView<InvestmentCluster> clusterTable;
    @FXML
    private TableColumn<InvestmentCluster, Integer> rankColumn;
    @FXML
    private TableColumn<InvestmentCluster, String> localityColumn;
    @FXML
    private TableColumn<InvestmentCluster, Double> scoreColumn;
    @FXML
    private TableColumn<InvestmentCluster, Double> appreciationColumn;
    @FXML
    private TableColumn<InvestmentCluster, Double> volumeGrowthColumn;
    @FXML
    private TableColumn<InvestmentCluster, Double> rentalTrendColumn;
    @FXML
    private WebView clusterMapWebView;
    @FXML
    private Label clusterDetailsLabel;
    @FXML
    private Label drilldownPriceLabel;
    @FXML
    private Label drilldownCagrLabel;
    @FXML
    private Label offlineNoticeLabel;
    @FXML
    private Label statusLabel;

    private InvestmentClusterService investmentClusterService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<InvestmentCluster> latestClusters = List.of();
    private String googleMapsApiKey = "";

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.investmentClusterService = serviceRegistry.investmentClusterService();
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
        if (analysisPeriodComboBox != null) {
            analysisPeriodComboBox.setItems(FXCollections.observableArrayList("1Y", "3Y", "5Y"));
            analysisPeriodComboBox.setValue("3Y");
        }
        if (minListingsField != null) {
            minListingsField.setText("10");
        }

        if (rankColumn != null) {
            rankColumn.setCellValueFactory(cell ->
                    new javafx.beans.property.SimpleIntegerProperty(clusterTable.getItems().indexOf(cell.getValue()) + 1).asObject());
        }
        if (localityColumn != null) {
            localityColumn.setCellValueFactory(new PropertyValueFactory<>("locality"));
        }
        if (scoreColumn != null) {
            scoreColumn.setCellValueFactory(new PropertyValueFactory<>("investmentScore"));
        }
        if (appreciationColumn != null) {
            appreciationColumn.setCellValueFactory(new PropertyValueFactory<>("priceAppreciation"));
        }
        if (volumeGrowthColumn != null) {
            volumeGrowthColumn.setCellValueFactory(new PropertyValueFactory<>("listingVolumeGrowth"));
        }
        if (rentalTrendColumn != null) {
            rentalTrendColumn.setCellValueFactory(new PropertyValueFactory<>("rentalTrend"));
        }
        if (clusterTable != null) {
            clusterTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, cluster) -> {
                if (cluster != null) {
                    showDrilldown(cluster);
                    highlightMarker(cluster.getLocality());
                }
            });
        }
        configureMapWebView();
        if (offlineNoticeLabel != null) {
            offlineNoticeLabel.setManaged(false);
            offlineNoticeLabel.setVisible(false);
        }
        UiAnimationHelper.attachSpringPress(identifyButton);
        googleMapsApiKey = loadGoogleMapsApiKey();
    }

    @FXML
    private void onIdentifyClusters() {
        if (investmentClusterService == null) {
            UiFeedbackHelper.setStatus(statusLabel, "Cluster service not available.", "status-error");
            return;
        }
        try {
            if (cityComboBox == null || propertyTypeComboBox == null || minListingsField == null || clusterTable == null) {
                if (statusLabel != null) {
                    statusLabel.setText("Cluster dashboard refreshed.");
                }
                return;
            }
            ClusterParams params = new ClusterParams();
            params.setCity(cityComboBox.getValue());
            params.setPropertyType(propertyTypeComboBox.getValue());
            params.setMinListingCount(Integer.parseInt(minListingsField.getText().trim()));
            params.setAnalysisPeriodYears(parsePeriodYears());

            Task<List<InvestmentCluster>> task = new Task<>() {
                @Override
                protected List<InvestmentCluster> call() {
                    return investmentClusterService.identifyClusters(params);
                }
            };
            task.setOnRunning(event -> setLoadingState(true));
            task.setOnSucceeded(event -> {
                setLoadingState(false);
                List<InvestmentCluster> clusters = task.getValue();
                List<InvestmentCluster> previous = List.copyOf(clusterTable.getItems());
                latestClusters = clusters;
                clusterTable.setItems(FXCollections.observableArrayList(clusters));
                UiAnimationHelper.highlightTableDiff(clusterTable, previous, clusters);
                pushClustersToMap(clusters);
                if (!clusters.isEmpty()) {
                    clusterTable.getSelectionModel().select(0);
                }
                UiAnimationHelper.pulseMetric(clusterDetailsLabel, UiAnimationHelper.SignalType.POSITIVE);
                UiFeedbackHelper.setStatus(statusLabel, "Loaded " + clusters.size() + " clusters.", "status-success");
            });
            task.setOnFailed(event -> {
                setLoadingState(false);
                showMap(false);
                Throwable ex = task.getException();
                UiFeedbackHelper.setStatus(statusLabel, "Could not identify clusters.", "status-error");
                UiFeedbackHelper.showErrorDialog("Cluster Analysis Error", ex == null ? "Unknown error" : ex.getMessage());
            });
            Thread thread = new Thread(task, "cluster-identify-task");
            thread.setDaemon(true);
            thread.start();
        } catch (Exception ex) {
            UiFeedbackHelper.setStatus(statusLabel, "Could not identify clusters: " + ex.getMessage(), "status-error");
        }
    }

    private int parsePeriodYears() {
        if (analysisPeriodComboBox == null || analysisPeriodComboBox.getValue() == null) {
            return 3;
        }
        String value = analysisPeriodComboBox.getValue();
        if ("1Y".equals(value)) return 1;
        if ("5Y".equals(value)) return 5;
        return 3;
    }

    private void configureMapWebView() {
        if (clusterMapWebView == null) {
            return;
        }
        URL mapPage = getClass().getResource("/web/clusters-map.html");
        if (mapPage != null) {
            clusterMapWebView.getEngine().load(mapPage.toExternalForm());
            clusterMapWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    JSObject window = (JSObject) clusterMapWebView.getEngine().executeScript("window");
                    window.setMember("javaClusterBridge", new JavaClusterBridge());
                    clusterMapWebView.getEngine().executeScript("setConfig(" + toJsString(googleMapsApiKey) + ");");
                }
            });
        } else {
            showMap(false);
        }
    }

    private void pushClustersToMap(List<InvestmentCluster> clusters) {
        if (clusterMapWebView == null) {
            return;
        }
        try {
            List<Map<String, Object>> payload = clusters.stream().map(cluster -> {
                Map<String, Object> row = new HashMap<>();
                row.put("locality", cluster.getLocality());
                row.put("score", cluster.getInvestmentScore());
                row.put("appreciation", cluster.getPriceAppreciation());
                row.put("volumeGrowth", cluster.getListingVolumeGrowth());
                row.put("rentalTrend", cluster.getRentalTrend());
                row.put("lat", 24.8607 + (Math.random() - 0.5) * 0.25);
                row.put("lng", 67.0011 + (Math.random() - 0.5) * 0.25);
                return row;
            }).toList();
            String json = objectMapper.writeValueAsString(payload);
            clusterMapWebView.getEngine().executeScript("loadClusters(" + json + ");");
            showMap(true);
        } catch (Exception ex) {
            showMap(false);
        }
    }

    private void highlightMarker(String locality) {
        if (clusterMapWebView == null || locality == null) {
            return;
        }
        try {
            clusterMapWebView.getEngine().executeScript("highlightCluster(" + toJsString(locality) + ");");
        } catch (Exception ignored) {
            // no-op
        }
    }

    private void showDrilldown(InvestmentCluster cluster) {
        if (clusterDetailsLabel != null) {
            clusterDetailsLabel.setText(cluster.getLocality() + ": score " + UiFormatters.number(cluster.getInvestmentScore())
                    + ", appreciation " + UiFormatters.number(cluster.getPriceAppreciation()) + "%, volume growth "
                    + UiFormatters.number(cluster.getListingVolumeGrowth()) + "%.");
        }
        if (drilldownPriceLabel != null) {
            drilldownPriceLabel.setText(UiFormatters.number(cluster.getInvestmentScore()));
        }
        if (drilldownCagrLabel != null) {
            drilldownCagrLabel.setText(UiFormatters.number(cluster.getPriceAppreciation()) + "%");
        }
    }

    private void showMap(boolean visible) {
        if (clusterMapWebView != null) {
            clusterMapWebView.setManaged(visible);
            clusterMapWebView.setVisible(visible);
        }
        if (offlineNoticeLabel != null) {
            offlineNoticeLabel.setManaged(!visible);
            offlineNoticeLabel.setVisible(!visible);
            if (!visible) {
                UiAnimationHelper.pulseMetric(offlineNoticeLabel, UiAnimationHelper.SignalType.WARNING);
            }
        }
    }

    private void setLoadingState(boolean loading) {
        if (loadingIndicator != null) {
            loadingIndicator.setManaged(loading);
            loadingIndicator.setVisible(loading);
        }
        if (identifyButton != null) {
            identifyButton.setDisable(loading);
            identifyButton.setText(loading ? "Identifying..." : "Identify");
        }
    }

    private String toJsString(String value) {
        if (value == null) return "''";
        return "'" + value.replace("\\", "\\\\").replace("'", "\\'") + "'";
    }

    private String loadGoogleMapsApiKey() {
        java.util.Properties properties = new java.util.Properties();
        try (java.io.InputStream stream = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (stream != null) {
                properties.load(stream);
                return properties.getProperty("google.maps.api.key", "");
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    public class JavaClusterBridge {
        public void onClusterMarkerClick(String locality) {
            if (latestClusters == null) {
                return;
            }
            for (InvestmentCluster cluster : latestClusters) {
                if (cluster.getLocality().equalsIgnoreCase(locality)) {
                    clusterTable.getSelectionModel().select(cluster);
                    clusterTable.scrollTo(cluster);
                    showDrilldown(cluster);
                    break;
                }
            }
        }
    }
}
