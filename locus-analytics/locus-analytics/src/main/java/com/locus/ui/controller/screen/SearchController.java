package com.locus.ui.controller.screen;

import com.locus.model.Property;
import com.locus.model.dto.PagedResult;
import com.locus.model.dto.SearchFilter;
import com.locus.service.SearchService;
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
import java.util.ResourceBundle;

public class SearchController implements Initializable {

    @FXML
    private ComboBox<String> cityComboBox;
    @FXML
    private ComboBox<String> propertyTypeComboBox;
    @FXML
    private TextField minPriceField;
    @FXML
    private TextField maxPriceField;
    @FXML
    private TableView<Property> resultsTable;
    @FXML
    private TableColumn<Property, String> idColumn;
    @FXML
    private TableColumn<Property, String> cityColumn;
    @FXML
    private TableColumn<Property, String> localityColumn;
    @FXML
    private TableColumn<Property, String> typeColumn;
    @FXML
    private TableColumn<Property, Double> priceColumn;
    @FXML
    private Label resultSummaryLabel;
    @FXML
    private Label statusLabel;

    private SearchService searchService;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.searchService = serviceRegistry.searchService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cityComboBox.setItems(FXCollections.observableArrayList("Karachi", "Islamabad", "Lahore"));
        propertyTypeComboBox.setItems(FXCollections.observableArrayList("", "house", "apartment", "plot", "commercial"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("propertyId"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        localityColumn.setCellValueFactory(new PropertyValueFactory<>("locality"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("propertyType"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
    }

    @FXML
    private void onSearch() {
        if (searchService == null) {
            statusLabel.setText("Search service not available.");
            return;
        }
        try {
            SearchFilter filter = new SearchFilter();
            filter.setCity(cityComboBox.getValue());
            filter.setPropertyType(blankToNull(propertyTypeComboBox.getValue()));
            if (!minPriceField.getText().isBlank()) {
                filter.setMinPrice(Double.parseDouble(minPriceField.getText().trim()));
            }
            if (!maxPriceField.getText().isBlank()) {
                filter.setMaxPrice(Double.parseDouble(maxPriceField.getText().trim()));
            }
            PagedResult<Property> result = searchService.search(filter);
            resultsTable.setItems(FXCollections.observableArrayList(result.getItems()));
            resultSummaryLabel.setText("Results: " + result.getTotalCount() + ", page " + result.getPageNumber());
            statusLabel.setText("Search executed with stub results.");
        } catch (Exception ex) {
            statusLabel.setText("Could not search: " + ex.getMessage());
        }
    }

    @FXML
    private void onClearFilters() {
        cityComboBox.getSelectionModel().clearSelection();
        propertyTypeComboBox.getSelectionModel().clearSelection();
        minPriceField.clear();
        maxPriceField.clear();
        resultsTable.getItems().clear();
        resultSummaryLabel.setText("Results: 0");
        statusLabel.setText("Filters cleared.");
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
