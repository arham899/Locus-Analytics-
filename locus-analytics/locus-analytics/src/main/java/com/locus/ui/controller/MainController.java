package com.locus.ui.controller;

import com.locus.model.User;
import com.locus.ui.BrandAssets;
import com.locus.ui.SceneManager;
import com.locus.ui.ServiceRegistry;
import com.locus.ui.controller.UiNavigationBridge;
import com.locus.ui.controller.screen.UiAnimationHelper;
import com.locus.ui.controller.screen.UiMotionProfile;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;

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
    private boolean navHoverWired;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        UiAnimationHelper.setMotionPreset(UiMotionProfile.Preset.BALANCED);
        UiAnimationHelper.setReducedMotion(false);
        Image logo = BrandAssets.loadLogoImage();
        if (logo != null) {
            topbarLogoView.setImage(logo);
            topbarLogoView.setVisible(true);
            topbarLogoView.setManaged(true);
        } else {
            topbarLogoView.setVisible(false);
            topbarLogoView.setManaged(false);
        }
        wireHoverAnimations();
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
        UiNavigationBridge.registerScreenOpener(this::openByKey);
        loadContent("/fxml/screens/FMVEstimateView.fxml", "Estimate FMV");
    }

    private void openByKey(String key) {
        if (key == null) {
            return;
        }
        switch (key) {
            case "REPORT" -> onOpenReport();
            case "COMPARE" -> onOpenCompare();
            case "SEARCH" -> onOpenSearch();
            case "FMV" -> onOpenFmv();
            default -> {
                // ignore unknown route keys
            }
        }
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

            ScrollPane scrollWrapper = new ScrollPane(content);
            scrollWrapper.setFitToWidth(true);
            scrollWrapper.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollWrapper.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollWrapper.setPannable(true);
            scrollWrapper.getStyleClass().add("terminal-screen-scroll");
            scrollWrapper.setStyle("-fx-background-color: transparent;");

            contentPane.getChildren().setAll(scrollWrapper);
            UiAnimationHelper.RouteIntent intent = resolveIntent(screenTitle);
            UiAnimationHelper.playPageEnter(scrollWrapper, intent);
            java.util.List<Node> revealTargets = collectRevealTargets(content);
            UiAnimationHelper.playStaggeredReveal(revealTargets);
            UiAnimationHelper.installScrollReveal(scrollWrapper, revealTargets);
            if (intent == UiAnimationHelper.RouteIntent.ANALYSIS) {
                // Keep scanline subtle and avoid full-page parallax jitter.
                if (!revealTargets.isEmpty()) {
                    UiAnimationHelper.playScanline(revealTargets.get(0));
                }
            }
            statusLabel.setText("Loaded: " + screenTitle);
        } catch (IOException exception) {
            exception.printStackTrace();
            Label heading = new Label(screenTitle);
            heading.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
            String rootMessage = exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage();
            Label details = new Label("Unable to load screen: " + fxmlPath
                    + (rootMessage == null ? "" : "\n" + rootMessage));
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

    private java.util.List<Node> collectRevealTargets(Node content) {
        if (content == null) {
            return java.util.List.of();
        }
        if (!(content instanceof VBox)) {
            return java.util.List.of(content);
        }
        VBox root = (VBox) content;
        return root.getChildren().stream()
                .filter(node -> node != null && node.isManaged())
                .limit(6)
                .toList();
    }

    private void wireHoverAnimations() {
        if (navHoverWired || contentPane == null) {
            return;
        }
        if (contentPane.getScene() == null) {
            contentPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null && !navHoverWired) {
                    wireHoverAnimations();
                }
            });
            return;
        }
        Set<Node> navNodes = contentPane.getScene().getRoot().lookupAll(".terminal-nav-button");
        navNodes.stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .forEach(button -> {
                    UiAnimationHelper.attachHoverScale(button);
                    UiAnimationHelper.attachSpringPress(button);
                });
        navHoverWired = true;
    }

    private UiAnimationHelper.RouteIntent resolveIntent(String screenTitle) {
        if (screenTitle == null) {
            return UiAnimationHelper.RouteIntent.GENERAL;
        }
        String normalized = screenTitle.toLowerCase();
        if (normalized.contains("report")) {
            return UiAnimationHelper.RouteIntent.REPORT;
        }
        if (normalized.contains("config") || normalized.contains("listings") || normalized.contains("etl")) {
            return UiAnimationHelper.RouteIntent.ADMIN;
        }
        if (normalized.contains("yield") || normalized.contains("roi")
                || normalized.contains("trend") || normalized.contains("cluster")
                || normalized.contains("heatmap") || normalized.contains("fmv")
                || normalized.contains("search") || normalized.contains("compare")) {
            return UiAnimationHelper.RouteIntent.ANALYSIS;
        }
        return UiAnimationHelper.RouteIntent.GENERAL;
    }
}
