package com.locus.ui;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX entry point for LOCUS Analytics.
 */
public class LocusAnalyticsApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        ServiceRegistry serviceRegistry = new ServiceRegistry();
        SceneManager sceneManager = new SceneManager(primaryStage, serviceRegistry);
        sceneManager.showLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
