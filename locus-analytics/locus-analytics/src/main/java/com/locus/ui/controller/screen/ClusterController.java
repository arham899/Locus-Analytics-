package com.locus.ui.controller.screen;

import com.locus.model.InvestmentCluster;
import com.locus.model.dto.ClusterParams;
import com.locus.service.InvestmentClusterService;
import com.locus.ui.ServiceRegistry;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ClusterController implements Initializable {

    @FXML
    private ComboBox<String> cityComboBox;
    @FXML
    private ComboBox<String> propertyTypeComboBox;
    @FXML
    private TextField minListingsField;
    @FXML
    private TableView<InvestmentCluster> clusterTable;
    @FXML
    private TableColumn<InvestmentCluster, String> localityColumn;
    @FXML
    private TableColumn<InvestmentCluster, Double> scoreColumn;
    @FXML
    private TableColumn<InvestmentCluster, Double> appreciationColumn;
    @FXML
    private Label statusLabel;

    private InvestmentClusterService investmentClusterService;

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
        if (minListingsField != null) {
            minListingsField.setText("10");
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
    }

    @FXML
    private void onIdentifyClusters() {
        if (investmentClusterService == null) {
            statusLabel.setText("Cluster service not available.");
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
            params.setAnalysisPeriodYears(3);
            List<InvestmentCluster> clusters = investmentClusterService.identifyClusters(params);
            clusterTable.setItems(FXCollections.observableArrayList(clusters));
            statusLabel.setText("Loaded " + clusters.size() + " clusters.");
        } catch (Exception ex) {
            statusLabel.setText("Could not identify clusters: " + ex.getMessage());
        }
    }
}
