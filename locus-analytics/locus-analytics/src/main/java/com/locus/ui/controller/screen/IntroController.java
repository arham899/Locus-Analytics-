package com.locus.ui.controller.screen;

import com.locus.ui.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.net.URL;
import java.util.ResourceBundle;

public class IntroController implements Initializable {

    @FXML
    private MediaView mediaView;

    private SceneManager sceneManager;
    private MediaPlayer mediaPlayer;

    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            URL mediaUrl = getClass().getResource("/media/intro.mp4");
            if (mediaUrl != null) {
                Media media = new Media(mediaUrl.toExternalForm());
                mediaPlayer = new MediaPlayer(media);
                mediaView.setMediaPlayer(mediaPlayer);

                // Wait for media to be ready to auto-play and resize
                mediaPlayer.setOnReady(() -> {
                    // Resize video to fit the window dynamically while preserving aspect ratio
                    mediaView.fitWidthProperty().bind(mediaView.sceneProperty().flatMap(scene -> scene.widthProperty()));
                    mediaView.fitHeightProperty().bind(mediaView.sceneProperty().flatMap(scene -> scene.heightProperty()));
                    mediaView.setPreserveRatio(true);
                });

                mediaPlayer.setOnEndOfMedia(this::transitionToLogin);
            } else {
                System.err.println("Could not find intro.mp4. Skipping intro.");
            }
        } catch (Exception e) {
            System.err.println("Error loading media: " + e.getMessage());
        }
    }

    public void play() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        } else {
            // If media failed to load, skip straight to login
            Platform.runLater(this::transitionToLogin);
        }
    }

    @FXML
    private void onSkip() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        transitionToLogin();
    }

    private void transitionToLogin() {
        if (sceneManager != null) {
            sceneManager.showLogin();
        }
    }
}
