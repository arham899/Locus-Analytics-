package com.locus.ui.controller.screen;

import com.locus.model.Property;
import com.locus.model.dto.PagedResult;
import com.locus.model.dto.SearchFilter;
import com.locus.service.SearchService;
import com.locus.ui.controller.UiNavigationBridge;
import com.locus.ui.ServiceRegistry;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.control.Alert;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ResourceBundle;

public class SearchController implements Initializable {

    @FXML
    private ComboBox<String> cityComboBox;
    @FXML
    private ComboBox<String> localityComboBox;
    @FXML
    private ComboBox<String> propertyTypeComboBox;
    @FXML
    private TextField minPriceField;
    @FXML
    private TextField maxPriceField;
    @FXML
    private TextField minAreaField;
    @FXML
    private TextField maxAreaField;
    @FXML
    private ComboBox<Integer> bedroomsComboBox;
    @FXML
    private ComboBox<Integer> bathroomsComboBox;
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
    private TableColumn<Property, Double> areaColumn;
    @FXML
    private TableColumn<Property, Integer> bedroomsColumn;
    @FXML
    private TableColumn<Property, Integer> bathroomsColumn;
    @FXML
    private TableColumn<Property, java.time.LocalDate> listingDateColumn;
    @FXML
    private Label resultSummaryLabel;
    @FXML
    private Label emptyStateLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private ProgressIndicator loadingIndicator;
    @FXML
    private Button searchButton;
    @FXML
    private Button previousPageButton;
    @FXML
    private Button nextPageButton;
    @FXML
    private Label currentPageLabel;
    @FXML
    private ComboBox<Integer> pageSizeComboBox;
    @FXML
    private Button addToCompareButton;

    private SearchService searchService;
    private int currentPage = 1;
    private int totalPages = 1;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.searchService = serviceRegistry.searchService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cityComboBox.setItems(FXCollections.observableArrayList("Karachi", "Islamabad", "Lahore"));
        localityComboBox.setItems(FXCollections.observableArrayList("DHA", "Clifton", "Gulberg", "F-7"));
        propertyTypeComboBox.setItems(FXCollections.observableArrayList("", "house", "apartment", "plot", "commercial"));
        bedroomsComboBox.setItems(FXCollections.observableArrayList(null, 1, 2, 3, 4, 5, 6));
        bathroomsComboBox.setItems(FXCollections.observableArrayList(null, 1, 2, 3, 4, 5, 6));
        if (pageSizeComboBox != null) {
            pageSizeComboBox.setItems(FXCollections.observableArrayList(10, 20, 50));
            pageSizeComboBox.setValue(20);
        }
        idColumn.setCellValueFactory(new PropertyValueFactory<>("propertyId"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        localityColumn.setCellValueFactory(new PropertyValueFactory<>("locality"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("propertyType"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        if (areaColumn != null) areaColumn.setCellValueFactory(new PropertyValueFactory<>("area"));
        if (bedroomsColumn != null) bedroomsColumn.setCellValueFactory(new PropertyValueFactory<>("bedrooms"));
        if (bathroomsColumn != null) bathroomsColumn.setCellValueFactory(new PropertyValueFactory<>("bathrooms"));
        if (listingDateColumn != null) listingDateColumn.setCellValueFactory(new PropertyValueFactory<>("listingDate"));
        if (resultsTable != null) {
            resultsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            resultsTable.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    Property selected = resultsTable.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        showDetailsDialog(selected);
                    }
                }
            });
        }
        configureSorting();
        updatePaginationControls();
        UiAnimationHelper.attachSpringPress(searchButton);
        UiAnimationHelper.attachSpringPress(addToCompareButton);
    }

    @FXML
    private void onSearch() {
        if (searchService == null) {
            UiFeedbackHelper.setStatus(statusLabel, "Search service not available.", "status-error");
            return;
        }
        UiFeedbackHelper.clearValidation(minPriceField, maxPriceField);
        try {
            SearchFilter filter = new SearchFilter();
            filter.setCity(cityComboBox.getValue());
            filter.setLocality(blankToNull(localityComboBox.getValue()));
            filter.setPropertyType(blankToNull(propertyTypeComboBox.getValue()));
            if (!minPriceField.getText().isBlank()) {
                filter.setMinPrice(Double.parseDouble(minPriceField.getText().trim()));
            }
            if (!maxPriceField.getText().isBlank()) {
                filter.setMaxPrice(Double.parseDouble(maxPriceField.getText().trim()));
            }
            if (minAreaField != null && !minAreaField.getText().isBlank()) {
                filter.setMinArea(Double.parseDouble(minAreaField.getText().trim()));
            }
            if (maxAreaField != null && !maxAreaField.getText().isBlank()) {
                filter.setMaxArea(Double.parseDouble(maxAreaField.getText().trim()));
            }
            if (bedroomsComboBox != null && bedroomsComboBox.getValue() != null) {
                filter.setBedrooms(bedroomsComboBox.getValue());
            }
            if (bathroomsComboBox != null && bathroomsComboBox.getValue() != null) {
                filter.setBathrooms(bathroomsComboBox.getValue());
            }
            filter.setPageNumber(currentPage);
            filter.setPageSize(pageSizeComboBox != null && pageSizeComboBox.getValue() != null ? pageSizeComboBox.getValue() : 20);
            if (filter.getMinPrice() != null && filter.getMaxPrice() != null
                    && filter.getMinPrice() > filter.getMaxPrice()) {
                UiFeedbackHelper.markInvalid(minPriceField);
                UiFeedbackHelper.markInvalid(maxPriceField);
                throw new IllegalArgumentException("Min price cannot exceed max price.");
            }
            if (filter.getMinArea() != null && filter.getMaxArea() != null
                    && filter.getMinArea() > filter.getMaxArea()) {
                UiFeedbackHelper.markInvalid(minAreaField);
                UiFeedbackHelper.markInvalid(maxAreaField);
                throw new IllegalArgumentException("Min area cannot exceed max area.");
            }
            Task<PagedResult<Property>> searchTask = new Task<>() {
                @Override
                protected PagedResult<Property> call() {
                    return searchService.search(filter);
                }
            };
            searchTask.setOnRunning(event -> setLoadingState(true));
            searchTask.setOnSucceeded(event -> {
                setLoadingState(false);
                PagedResult<Property> result = searchTask.getValue();
                List<Property> previous = List.copyOf(resultsTable.getItems());
                resultsTable.setItems(FXCollections.observableArrayList(result.getItems()));
                UiAnimationHelper.highlightTableDiff(resultsTable, previous, result.getItems());
                resultSummaryLabel.setText("Results: " + result.getTotalCount() + ", page " + result.getPageNumber());
                totalPages = Math.max(1, result.getTotalPages());
                currentPage = Math.max(1, result.getPageNumber());
                if (currentPageLabel != null) {
                    currentPageLabel.setText("Page " + currentPage + " / " + totalPages);
                }
                updatePaginationControls();
                boolean empty = result.getItems() == null || result.getItems().isEmpty();
                if (emptyStateLabel != null) {
                    emptyStateLabel.setManaged(empty);
                    emptyStateLabel.setVisible(empty);
                }
                UiFeedbackHelper.setStatus(statusLabel, "Search executed successfully.", "status-success");
            });
            searchTask.setOnFailed(event -> {
                setLoadingState(false);
                Throwable ex = searchTask.getException();
                UiFeedbackHelper.setStatus(statusLabel, "Could not search: " + (ex == null ? "Unknown error" : ex.getMessage()), "status-error");
                UiFeedbackHelper.showErrorDialog("Search Failed", ex == null ? "Unknown error" : ex.getMessage());
            });
            Thread thread = new Thread(searchTask, "search-task");
            thread.setDaemon(true);
            thread.start();
        } catch (com.locus.exception.ValidationException ex) {
            UiFeedbackHelper.setStatus(statusLabel, "Validation error in search filters.", "status-error");
            UiFeedbackHelper.showErrorDialog("Search Validation Error", UiFeedbackHelper.formatValidationMessage(ex));
        } catch (NumberFormatException ex) {
            UiFeedbackHelper.markInvalid(minPriceField);
            UiFeedbackHelper.markInvalid(maxPriceField);
            UiFeedbackHelper.setStatus(statusLabel, "Price filters must be numeric.", "status-error");
            UiFeedbackHelper.showErrorDialog("Invalid Price Range", "Enter valid numeric values for min and max price.");
        } catch (Exception ex) {
            UiFeedbackHelper.setStatus(statusLabel, "Could not search: " + ex.getMessage(), "status-error");
            UiFeedbackHelper.showErrorDialog("Search Failed", ex.getMessage());
        }
    }

    @FXML
    private void onClearFilters() {
        cityComboBox.getSelectionModel().clearSelection();
        propertyTypeComboBox.getSelectionModel().clearSelection();
        minPriceField.clear();
        maxPriceField.clear();
        if (minAreaField != null) minAreaField.clear();
        if (maxAreaField != null) maxAreaField.clear();
        if (localityComboBox != null) localityComboBox.getSelectionModel().clearSelection();
        if (bedroomsComboBox != null) bedroomsComboBox.getSelectionModel().clearSelection();
        if (bathroomsComboBox != null) bathroomsComboBox.getSelectionModel().clearSelection();
        resultsTable.getItems().clear();
        currentPage = 1;
        totalPages = 1;
        updatePaginationControls();
        if (currentPageLabel != null) currentPageLabel.setText("Page 1 / 1");
        if (emptyStateLabel != null) {
            emptyStateLabel.setManaged(false);
            emptyStateLabel.setVisible(false);
        }
        resultSummaryLabel.setText("Results: 0");
        UiFeedbackHelper.clearValidation(minPriceField, maxPriceField);
        UiFeedbackHelper.setStatus(statusLabel, "Filters cleared.", "status-success");
    }

    @FXML
    private void onPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            onSearch();
        }
    }

    @FXML
    private void onNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            onSearch();
        }
    }

    @FXML
    private void onAddToCompare() {
        if (resultsTable == null) {
            return;
        }
        List<String> ids = resultsTable.getSelectionModel().getSelectedItems()
                .stream()
                .map(Property::getPropertyId)
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            UiFeedbackHelper.showInfoDialog("No Selection", "Select one or more rows before adding to compare.");
            return;
        }
        UiNavigationBridge.setComparePropertyIds(ids);
        UiNavigationBridge.openScreen("COMPARE");
    }

    private void setLoadingState(boolean loading) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(loading);
            loadingIndicator.setManaged(loading);
        }
        UiAnimationHelper.setSkeletonVisible(resultsTable, loading);
        if (searchButton != null) {
            searchButton.setDisable(loading);
            searchButton.setText(loading ? "Searching..." : "Search");
        }
        if (addToCompareButton != null) {
            addToCompareButton.setDisable(loading);
        }
    }

    private void updatePaginationControls() {
        if (previousPageButton != null) {
            previousPageButton.setDisable(currentPage <= 1);
        }
        if (nextPageButton != null) {
            nextPageButton.setDisable(currentPage >= totalPages);
        }
    }

    private void showDetailsDialog(Property property) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Property Details");
        alert.setHeaderText("Property: " + property.getPropertyId());
        alert.setContentText(
                "Location: " + property.getCity() + ", " + property.getLocality() + "\n"
                        + "Type: " + property.getPropertyType() + "\n"
                        + "Area: " + UiFormatters.number(property.getArea()) + " sq.ft.\n"
                        + "Price: " + UiFormatters.currency(property.getPrice()) + "\n"
                        + "Bedrooms/Bathrooms: " + property.getBedrooms() + "/" + property.getBathrooms() + "\n"
                        + "Listing Date: " + (property.getListingDate() == null ? "-" : property.getListingDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
        );
        alert.showAndWait();
    }

    private void configureSorting() {
        if (idColumn != null) idColumn.setSortable(true);
        if (cityColumn != null) cityColumn.setSortable(true);
        if (localityColumn != null) localityColumn.setSortable(true);
        if (typeColumn != null) typeColumn.setSortable(true);
        if (priceColumn != null) priceColumn.setSortable(true);
        if (areaColumn != null) areaColumn.setSortable(true);
        if (bedroomsColumn != null) bedroomsColumn.setSortable(true);
        if (bathroomsColumn != null) bathroomsColumn.setSortable(true);
        if (listingDateColumn != null) listingDateColumn.setSortable(true);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
