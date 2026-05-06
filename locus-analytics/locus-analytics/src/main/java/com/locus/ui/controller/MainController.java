package com.locus.ui.controller;

import com.locus.model.User;
import com.locus.ui.BrandAssets;
import com.locus.ui.SceneManager;
import com.locus.ui.ServiceRegistry;
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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private Label userLabel;
    @FXML private Label roleLabel;
    @FXML private Label statusLabel;
    @FXML private VBox adminSection;
    @FXML private VBox contentPane;
    @FXML private ImageView topbarLogoView;

    // Nav buttons for active-state tracking
    @FXML private Button navDashboard;
    @FXML private Button navFmv;
    @FXML private Button navSearch;
    @FXML private Button navCompare;
    @FXML private Button navRental;
    @FXML private Button navRoi;
    @FXML private Button navReport;
    @FXML private Button navTrends;
    @FXML private Button navHeatmap;
    @FXML private Button navClusters;
    @FXML private Button navEtl;
    @FXML private Button navListings;
    @FXML private Button navConfig;

    private SceneManager sceneManager;
    private ServiceRegistry serviceRegistry;
    private User currentUser;
    private Button activeNavButton;
    private List<Button> allNavButtons;

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
        if (currentUser == null) return;

        String name = currentUser.getName() != null ? currentUser.getName() : currentUser.getEmail();
        userLabel.setText(name);
        roleLabel.setText(currentUser.getRole() != null ? currentUser.getRole().toUpperCase() : "");

        boolean isAdmin = "admin".equalsIgnoreCase(currentUser.getRole());
        adminSection.setManaged(isAdmin);
        adminSection.setVisible(isAdmin);

        allNavButtons = new ArrayList<>(List.of(
                navDashboard, navFmv, navSearch, navCompare,
                navRental, navRoi, navReport,
                navTrends, navHeatmap, navClusters
        ));
        if (isAdmin) {
            allNavButtons.addAll(List.of(navEtl, navListings, navConfig));
        }
        allNavButtons.forEach(btn -> {
            if (btn != null) {
                UiAnimationHelper.attachHoverScale(btn);
                UiAnimationHelper.attachSpringPress(btn);
            }
        });

        UiNavigationBridge.registerScreenOpener(this::openByKey);
        loadContent("/fxml/screens/FMVEstimateView.fxml", "FMV ESTIMATE", navFmv);
    }

    private void openByKey(String key) {
        if (key == null) return;
        switch (key) {
            case "REPORT"  -> onOpenReport();
            case "COMPARE" -> onOpenCompare();
            case "SEARCH"  -> onOpenSearch();
            case "FMV"     -> onOpenFmv();
        }
    }

    @FXML private void onOpenDashboard()   { loadContent("/fxml/screens/FMVEstimateView.fxml",  "DASHBOARD",             navDashboard); }
    @FXML private void onOpenFmv()         { loadContent("/fxml/screens/FMVEstimateView.fxml",  "FMV ESTIMATE",          navFmv); }
    @FXML private void onOpenRentalYield() { loadContent("/fxml/screens/RentalYieldView.fxml",  "RENTAL YIELD",          navRental); }
    @FXML private void onOpenRoi()         { loadContent("/fxml/screens/ROIView.fxml",           "ROI ANALYSIS",          navRoi); }
    @FXML private void onOpenPriceTrends() { loadContent("/fxml/screens/PriceTrendView.fxml",   "PRICE TRENDS",          navTrends); }
    @FXML private void onOpenClusters()    { loadContent("/fxml/screens/ClusterView.fxml",       "INVESTMENT CLUSTERS",   navClusters); }
    @FXML private void onOpenSearch()      { loadContent("/fxml/screens/SearchView.fxml",        "SEARCH PROPERTIES",     navSearch); }
    @FXML private void onOpenCompare()     { loadContent("/fxml/screens/CompareView.fxml",       "COMPARE PROPERTIES",    navCompare); }
    @FXML private void onOpenHeatmap()     { loadContent("/fxml/screens/HeatmapView.fxml",       "HEATMAP",               navHeatmap); }
    @FXML private void onOpenReport()      { loadContent("/fxml/screens/ReportView.fxml",        "REPORTING",             navReport); }
    @FXML private void onOpenEtl()         { loadContent("/fxml/screens/ETLView.fxml",           "ETL PIPELINE",          navEtl); }
    @FXML private void onOpenListings()    { loadContent("/fxml/screens/ListingsView.fxml",      "MANAGE LISTINGS",       navListings); }
    @FXML private void onOpenConfig()      { loadContent("/fxml/screens/ConfigView.fxml",        "CONFIGURATION",         navConfig); }

    private void loadContent(String fxmlPath, String screenTitle, Button sourceButton) {
        setActiveNav(sourceButton);
        statusLabel.setText("Loading " + screenTitle + "...");
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
            List<Node> revealTargets = collectRevealTargets(content);
            UiAnimationHelper.playStaggeredReveal(revealTargets);
            UiAnimationHelper.installScrollReveal(scrollWrapper, revealTargets);
            if (intent == UiAnimationHelper.RouteIntent.ANALYSIS && !revealTargets.isEmpty()) {
                UiAnimationHelper.playScanline(revealTargets.get(0));
            }
            statusLabel.setText(screenTitle);
        } catch (IOException ex) {
            ex.printStackTrace();
            Label heading = new Label(screenTitle);
            heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");
            String rootMsg = ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage();
            Label details = new Label("Unable to load: " + fxmlPath
                    + (rootMsg == null ? "" : "\n" + rootMsg));
            details.setStyle("-fx-text-fill: #f87171;");
            details.setWrapText(true);
            contentPane.getChildren().setAll(heading, details);
            statusLabel.setText("Error loading " + screenTitle);
        }
    }

    private void setActiveNav(Button button) {
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove("terminal-nav-button-active");
        }
        activeNavButton = button;
        if (button != null && !button.getStyleClass().contains("terminal-nav-button-active")) {
            button.getStyleClass().add("terminal-nav-button-active");
        }
    }

    private void injectServices(Object controller) {
        if (controller == null) return;
        if (controller instanceof ServiceAwareController sac) {
            sac.setServiceRegistry(serviceRegistry);
            return;
        }
        try {
            Method m = controller.getClass().getMethod("setServiceRegistry", ServiceRegistry.class);
            m.invoke(controller, serviceRegistry);
        } catch (Exception ignored) {}
        try {
            Method m = controller.getClass().getMethod("setCurrentUser", User.class);
            m.invoke(controller, currentUser);
        } catch (Exception ignored) {}
    }

    @FXML
    private void onLogout() {
        serviceRegistry.authenticationService().logout();
        sceneManager.showLogin();
    }

    private List<Node> collectRevealTargets(Node content) {
        if (content == null) return List.of();
        if (!(content instanceof VBox root)) return List.of(content);
        return root.getChildren().stream()
                .filter(n -> n != null && n.isManaged())
                .limit(6)
                .toList();
    }

    private UiAnimationHelper.RouteIntent resolveIntent(String title) {
        if (title == null) return UiAnimationHelper.RouteIntent.GENERAL;
        String t = title.toLowerCase();
        if (t.contains("report"))                                   return UiAnimationHelper.RouteIntent.REPORT;
        if (t.contains("config") || t.contains("listings") || t.contains("etl")) return UiAnimationHelper.RouteIntent.ADMIN;
        if (t.contains("yield") || t.contains("roi") || t.contains("trend")
                || t.contains("cluster") || t.contains("heatmap") || t.contains("fmv")
                || t.contains("search") || t.contains("compare"))  return UiAnimationHelper.RouteIntent.ANALYSIS;
        return UiAnimationHelper.RouteIntent.GENERAL;
    }
}
