package com.locus.ui.controller.screen;

import com.locus.model.Property;
import com.locus.model.dto.PagedResult;
import com.locus.model.dto.SearchFilter;
import com.locus.service.ListingManagementService;
import com.locus.ui.ServiceRegistry;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
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
    @FXML
    private Button editListingButton;
    @FXML
    private Button deleteListingButton;
    @FXML
    private Button addListingButton;
    @FXML
    private Label paginationSummaryLabel;
    @FXML
    private Button prevPageButton;
    @FXML
    private Button nextPageButton;
    @FXML
    private Button page1Button;
    @FXML
    private Button page2Button;
    @FXML
    private Button page3Button;

    private ListingManagementService listingManagementService;
    private int currentPage = 1;
    private int totalPages = 1;

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
            priceColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Property, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%,.0f", item));
                    }
                }
            });
        }
        if (listingsTable != null) {
            listingsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, selected) -> {
                boolean hasSelection = selected != null;
                if (editListingButton != null) editListingButton.setDisable(!hasSelection);
                if (deleteListingButton != null) deleteListingButton.setDisable(!hasSelection);
            });
        }
        UiAnimationHelper.attachSpringPress(addListingButton);
        UiAnimationHelper.attachSpringPress(editListingButton);
        UiAnimationHelper.attachSpringPress(deleteListingButton);
    }

    @FXML
    private void onLoadListings() {
        currentPage = 1;
        loadCurrentPage();
    }

    private void loadCurrentPage() {
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
        filter.setPageNumber(currentPage);
        filter.setPageSize(15); 

        UiAnimationHelper.setSkeletonVisible(listingsTable, true);
        PagedResult<Property> paged = listingManagementService.searchListings(filter);
        
        java.util.List<Property> previous = java.util.List.copyOf(listingsTable.getItems());
        listingsTable.setItems(FXCollections.observableArrayList(paged.getItems()));
        UiAnimationHelper.highlightTableDiff(listingsTable, previous, paged.getItems());
        UiAnimationHelper.setSkeletonVisible(listingsTable, false);
        
        this.totalPages = paged.getTotalPages();
        updatePaginationUI(paged);
        
        UiFeedbackHelper.setStatus(statusLabel, "Loaded page " + currentPage + " of " + totalPages, "status-success");
    }

    private void updatePaginationUI(PagedResult<Property> paged) {
        if (paginationSummaryLabel != null) {
            int start = (paged.getPageNumber() - 1) * paged.getPageSize() + 1;
            int end = start + paged.getItems().size() - 1;
            if (paged.getItems().isEmpty()) start = 0;
            paginationSummaryLabel.setText(String.format("Showing %d to %d of %d entries", start, end, paged.getTotalCount()));
        }

        if (prevPageButton != null) prevPageButton.setDisable(!paged.hasPreviousPage());
        if (nextPageButton != null) nextPageButton.setDisable(!paged.hasNextPage());

        updatePageButtonStyle(page1Button, 1);
        updatePageButtonStyle(page2Button, 2);
        updatePageButtonStyle(page3Button, 3);
        
        if (page2Button != null) page2Button.setDisable(totalPages < 2);
        if (page3Button != null) page3Button.setDisable(totalPages < 3);
    }

    private void updatePageButtonStyle(Button btn, int pageNum) {
        if (btn == null) return;
        btn.getStyleClass().remove("terminal-primary-button");
        btn.getStyleClass().remove("terminal-secondary-button");
        
        if (currentPage == pageNum) {
            btn.getStyleClass().add("terminal-primary-button");
        } else {
            btn.getStyleClass().add("terminal-secondary-button");
        }
    }

    @FXML
    private void onAddListing() {
        Property property = openPropertyDialog("Add Listing", null);
        if (property == null) {
            return;
        }
        listingManagementService.addListing(property);
        loadCurrentPage();
        UiFeedbackHelper.showInfoDialog("Added", "Listing added successfully.");
    }

    @FXML
    private void onEditListing() {
        if (listingsTable == null) {
            return;
        }
        Property selected = listingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiFeedbackHelper.setStatus(statusLabel, "Select a listing to edit.", "status-warning");
            return;
        }
        Property updated = openPropertyDialog("Edit Listing", selected);
        if (updated == null) {
            return;
        }
        updated.setPropertyId(selected.getPropertyId());
        listingManagementService.updateListing(updated);
        loadCurrentPage();
        UiFeedbackHelper.showInfoDialog("Updated", "Listing updated successfully.");
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
            UiFeedbackHelper.setStatus(statusLabel, "Select a listing first.", "status-warning");
            return;
        }
        boolean confirmed = confirmDelete(selected.getPropertyId());
        if (!confirmed) {
            return;
        }
        listingManagementService.deleteListing(selected.getPropertyId());
        loadCurrentPage();
        UiFeedbackHelper.showInfoDialog("Deleted", "Listing " + selected.getPropertyId() + " deleted.");
    }

    @FXML
    private void onPrevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadCurrentPage();
        }
    }

    @FXML
    private void onGoToPage1() {
        currentPage = 1;
        loadCurrentPage();
    }

    @FXML
    private void onGoToPage2() {
        if (totalPages >= 2) {
            currentPage = 2;
            loadCurrentPage();
        }
    }

    @FXML
    private void onGoToPage3() {
        if (totalPages >= 3) {
            currentPage = 3;
            loadCurrentPage();
        }
    }

    @FXML
    private void onNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadCurrentPage();
        }
    }

    @FXML
    private void onEditMetadata() {
        UiFeedbackHelper.showInfoDialog("Metadata", "Bulk metadata editor opened.");
    }

    @FXML
    private void onForceSync() {
        UiFeedbackHelper.setStatus(statusLabel, "Force sync started...", "status-success");
    }

    private Property openPropertyDialog(String title, Property existing) {
        Dialog<Property> dialog = new Dialog<>();
        dialog.setTitle(title);
        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField cityField = new TextField(existing == null ? "" : existing.getCity());
        TextField localityField = new TextField(existing == null ? "" : existing.getLocality());
        TextField typeField = new TextField(existing == null ? "" : existing.getPropertyType());
        TextField areaField = new TextField(existing == null ? "" : String.valueOf(existing.getArea()));
        TextField priceField = new TextField(existing == null ? "" : String.valueOf(existing.getPrice()));
        TextField bedsField = new TextField(existing == null ? "" : String.valueOf(existing.getBedrooms()));
        TextField bathsField = new TextField(existing == null ? "" : String.valueOf(existing.getBathrooms()));
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("City"), cityField);
        grid.addRow(1, new Label("Locality"), localityField);
        grid.addRow(2, new Label("Type"), typeField);
        grid.addRow(3, new Label("Area"), areaField);
        grid.addRow(4, new Label("Price"), priceField);
        grid.addRow(5, new Label("Bedrooms"), bedsField);
        grid.addRow(6, new Label("Bathrooms"), bathsField);
        pane.setContent(grid);
        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) {
                return null;
            }
            try {
                if (cityField.getText().isBlank() || localityField.getText().isBlank() || typeField.getText().isBlank()) {
                    throw new IllegalArgumentException("City, locality and type are required.");
                }
                Property property = new Property();
                property.setCity(cityField.getText().trim());
                property.setLocality(localityField.getText().trim());
                property.setPropertyType(typeField.getText().trim());
                property.setArea(Double.parseDouble(areaField.getText().trim()));
                property.setPrice(Double.parseDouble(priceField.getText().trim()));
                property.setBedrooms(Integer.parseInt(bedsField.getText().trim()));
                property.setBathrooms(Integer.parseInt(bathsField.getText().trim()));
                property.setListingDate(java.time.LocalDate.now());
                property.setLatitude(existing == null ? 24.8607 : existing.getLatitude());
                property.setLongitude(existing == null ? 67.0011 : existing.getLongitude());
                property.setUrlHash(existing == null ? java.util.UUID.randomUUID().toString() : existing.getUrlHash());
                return property;
            } catch (Exception ex) {
                UiFeedbackHelper.showErrorDialog("Invalid Listing", ex.getMessage());
                return null;
            }
        });
        return dialog.showAndWait().orElse(null);
    }

    private boolean confirmDelete(String propertyId) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Listing");
        alert.setContentText("Are you sure you want to delete listing " + propertyId + "?");
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}
