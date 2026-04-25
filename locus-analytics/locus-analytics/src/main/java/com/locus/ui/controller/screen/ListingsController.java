package com.locus.ui.controller.screen;

import com.locus.model.Property;
import com.locus.model.dto.PagedResult;
import com.locus.model.dto.SearchFilter;
import com.locus.service.ListingManagementService;
import com.locus.ui.ServiceRegistry;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class ListingsController implements Initializable {

    @FXML
    private TextField cityFilterField;
    @FXML
    private TableView<Property> listingsTable;
    @FXML
    private TableColumn<Property, String> idColumn;
    @FXML
    private TableColumn<Property, String> cityColumn;
    @FXML
    private TableColumn<Property, String> localityColumn;
    @FXML
    private TableColumn<Property, Double> priceColumn;
    @FXML
    private Label statusLabel;

    private ListingManagementService listingManagementService;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.listingManagementService = serviceRegistry.listingManagementService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (idColumn != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("propertyId"));
        }
        if (cityColumn != null) {
            cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        }
        if (localityColumn != null) {
            localityColumn.setCellValueFactory(new PropertyValueFactory<>("locality"));
        }
        if (priceColumn != null) {
            priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        }
    }

    @FXML
    private void onLoadListings() {
        if (listingManagementService == null) {
            statusLabel.setText("Listing service not available.");
            return;
        }
        if (cityFilterField == null || listingsTable == null) {
            if (statusLabel != null) {
                statusLabel.setText("Listings dashboard refreshed.");
            }
            return;
        }
        SearchFilter filter = new SearchFilter();
        if (!cityFilterField.getText().isBlank()) {
            filter.setCity(cityFilterField.getText().trim());
        }
        PagedResult<Property> paged = listingManagementService.searchListings(filter);
        listingsTable.setItems(FXCollections.observableArrayList(paged.getItems()));
        statusLabel.setText("Loaded " + paged.getItems().size() + " listings.");
    }

    @FXML
    private void onAddListing() {
        if (statusLabel != null) {
            statusLabel.setText("Add Listing dialog placeholder.");
        }
    }

    @FXML
    private void onEditListing() {
        if (statusLabel != null) {
            statusLabel.setText("Edit Listing dialog placeholder.");
        }
    }

    @FXML
    private void onDeleteListing() {
        if (listingsTable == null) {
            if (statusLabel != null) {
                statusLabel.setText("No list selected in dashboard mode.");
            }
            return;
        }
        Property selected = listingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a listing first.");
            return;
        }
        listingManagementService.deleteListing(selected.getPropertyId());
        listingsTable.getItems().remove(selected);
        statusLabel.setText("Deleted listing " + selected.getPropertyId() + " in stub mode.");
    }
}
