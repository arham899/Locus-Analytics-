package com.locus.ui.controller;

import com.locus.model.User;
import com.locus.ui.BrandAssets;
import com.locus.ui.SceneManager;
import com.locus.ui.ServiceRegistry;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for MainView shell placeholder.
 */
public class MainController implements Initializable {

    @FXML
    private Label userLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox adminSection;

    @FXML
    private VBox contentPane;

    @FXML
    private ImageView topbarLogoView;

    private SceneManager sceneManager;
    private ServiceRegistry serviceRegistry;
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Image logo = BrandAssets.loadLogoImage();
        if (logo != null) {
            topbarLogoView.setImage(logo);
            topbarLogoView.setVisible(true);
            topbarLogoView.setManaged(true);
        } else {
            topbarLogoView.setVisible(false);
            topbarLogoView.setManaged(false);
        }
    }

    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void initializeShell() {
        if (currentUser == null) {
            return;
        }
        userLabel.setText(currentUser.getName() + " (" + currentUser.getEmail() + ")");
        roleLabel.setText("Role: " + currentUser.getRole());
        boolean isAdmin = "admin".equalsIgnoreCase(currentUser.getRole());
        adminSection.setManaged(isAdmin);
        adminSection.setVisible(isAdmin);
        loadContent("/fxml/screens/FMVEstimateView.fxml", "Estimate FMV");
    }

    @FXML
    private void onOpenDashboard() {
        loadContent("/fxml/screens/FMVEstimateView.fxml", "Market Intelligence Dashboard");
    }

    @FXML
    private void onOpenFmv() {
        loadContent("/fxml/screens/FMVEstimateView.fxml", "Estimate FMV");
    }

    @FXML
    private void onOpenRentalYield() {
        loadContent("/fxml/screens/RentalYieldView.fxml", "Calculate Rental Yield");
    }

    @FXML
    private void onOpenRoi() {
        loadContent("/fxml/screens/ROIView.fxml", "Calculate ROI");
    }

    @FXML
    private void onOpenPriceTrends() {
        loadContent("/fxml/screens/PriceTrendView.fxml", "View Price Trends");
    }

    @FXML
    private void onOpenClusters() {
        loadContent("/fxml/screens/ClusterView.fxml", "Identify Investment Clusters");
    }

    @FXML
    private void onOpenSearch() {
        loadContent("/fxml/screens/SearchView.fxml", "Search Properties");
    }

    @FXML
    private void onOpenCompare() {
        loadContent("/fxml/screens/CompareView.fxml", "Compare Properties");
    }

    @FXML
    private void onOpenHeatmap() {
        loadContent("/fxml/screens/HeatmapView.fxml", "Property Heatmap");
    }

    @FXML
    private void onOpenReport() {
        loadContent("/fxml/screens/ReportView.fxml", "Generate Valuation Report");
    }

    @FXML
    private void onOpenEtl() {
        loadContent("/fxml/screens/ETLView.fxml", "Run ETL Pipeline");
    }

    @FXML
    private void onOpenListings() {
        loadContent("/fxml/screens/ListingsView.fxml", "Manage Listings");
    }

    @FXML
    private void onOpenConfig() {
        loadContent("/fxml/screens/ConfigView.fxml", "System Configuration");
    }

    private void loadContent(String fxmlPath, String screenTitle) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();
            Object controller = loader.getController();
            injectServices(controller);
            contentPane.getChildren().setAll(content);
            FadeTransition fade = new FadeTransition(Duration.millis(220), content);
            fade.setFromValue(0.25);
            fade.setToValue(1.0);
            fade.play();
            statusLabel.setText("Loaded: " + screenTitle);
        } catch (IOException exception) {
            Label heading = new Label(screenTitle);
            heading.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
            Label details = new Label("Unable to load screen: " + fxmlPath);
            details.setStyle("-fx-text-fill: #b00020;");
            details.setWrapText(true);
            contentPane.getChildren().setAll(heading, details);
            statusLabel.setText("Failed to load: " + screenTitle);
        }
    }

    private void injectServices(Object controller) {
        if (controller == null) {
            return;
        }
        if (controller instanceof ServiceAwareController serviceAwareController) {
            serviceAwareController.setServiceRegistry(serviceRegistry);
            return;
        }
        try {
            Method method = controller.getClass().getMethod("setServiceRegistry", com.locus.ui.ServiceRegistry.class);
            method.invoke(controller, serviceRegistry);
        } catch (Exception ignored) {
            // Controller does not require service injection.
        }
    }

    @FXML
    private void onLogout() {
        serviceRegistry.authenticationService().logout();
        sceneManager.showLogin();
    }
}
