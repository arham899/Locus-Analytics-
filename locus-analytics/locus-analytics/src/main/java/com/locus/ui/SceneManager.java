package com.locus.ui;

import com.locus.model.User;
import com.locus.ui.controller.LoginController;
import com.locus.ui.controller.MainController;
import com.locus.ui.controller.screen.UiAnimationHelper;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Handles scene/view transitions for the JavaFX application.
 */
public class SceneManager {

    private final Stage stage;
    private final ServiceRegistry serviceRegistry;

    public SceneManager(Stage stage, ServiceRegistry serviceRegistry) {
        this.stage = stage;
        this.serviceRegistry = serviceRegistry;
    }

    public void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();

            LoginController controller = loader.getController();
            controller.setSceneManager(this);
            controller.setServiceRegistry(serviceRegistry);

            stage.setTitle("LOCUS Analytics - Login");
            Scene scene = new Scene(root, 1180, 720);
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            UiAnimationHelper.playSceneCrossfade(stage, scene);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load LoginView.fxml", e);
        }
    }

    public void showMain(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            Parent root = loader.load();

            MainController controller = loader.getController();
            controller.setSceneManager(this);
            controller.setServiceRegistry(serviceRegistry);
            controller.setCurrentUser(user);
            controller.initializeShell();

            stage.setTitle("LOCUS Analytics");
            Scene scene = new Scene(root, 1260, 800);
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            UiAnimationHelper.playSceneCrossfade(stage, scene, true);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load MainView.fxml", e);
        }
    }
}
