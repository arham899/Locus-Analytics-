package com.locus.ui.controller;

import com.locus.exception.ValidationException;
import com.locus.model.PropertyAnalyst;
import com.locus.ui.BrandAssets;
import com.locus.ui.SceneManager;
import com.locus.ui.ServiceRegistry;
import com.locus.ui.controller.screen.UiAnimationHelper;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for LoginView.
 */
public class LoginController implements Initializable {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label feedbackLabel;

    @FXML
    private Button loginButton;

    @FXML
    private ImageView brandLogoView;

    private SceneManager sceneManager;
    private ServiceRegistry serviceRegistry;

    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Image logo = BrandAssets.loadLogoImage();
        if (logo != null) {
            brandLogoView.setImage(logo);
            brandLogoView.setVisible(true);
            brandLogoView.setManaged(true);
        } else {
            brandLogoView.setVisible(false);
            brandLogoView.setManaged(false);
        }
        emailField.textProperty().addListener((obs, oldV, newV) -> clearFieldFeedback());
        passwordField.textProperty().addListener((obs, oldV, newV) -> clearFieldFeedback());
        UiAnimationHelper.attachHoverScale(loginButton);

    }

    @FXML
    private void onLogin() {
        setLoadingState(true);
        try {
            // Instant access bypass as requested
            PropertyAnalyst user = new PropertyAnalyst();
            user.setEmail("analyst@locus.com");
            user.setName("Locus Analyst");
            
            feedbackLabel.setText("");
            sceneManager.showMain(user);
        } catch (Exception ex) {
            feedbackLabel.setText("System error during bypass.");
        } finally {
            setLoadingState(false);
        }
    }


    @FXML
    private void onForgotPassword() {
        feedbackLabel.setText("Password reset is not available in stub mode.");
        UiAnimationHelper.showToast(feedbackLabel, feedbackLabel.getText(), "status-warning");
    }

    private String buildValidationMessage(ValidationException ex) {
        if (!ex.hasFieldErrors()) {
            return ex.getMessage();
        }
        StringBuilder builder = new StringBuilder(ex.getMessage()).append(": ");
        boolean first = true;
        for (Map.Entry<String, String> entry : ex.getFieldErrors().entrySet()) {
            if (!first) {
                builder.append(" | ");
            }
            builder.append(entry.getKey()).append(" -> ").append(entry.getValue());
            first = false;
        }
        return builder.toString();
    }

    private void setLoadingState(boolean loading) {
        loginButton.setDisable(loading);
        loginButton.setText(loading ? "Signing in..." : "Sign In  →");
    }

    private void markInvalid(javafx.scene.control.Control control) {
        if (control != null && !control.getStyleClass().contains("field-invalid")) {
            control.getStyleClass().add("field-invalid");
        }
        UiAnimationHelper.shakeInvalid(control);
    }

    private void clearFieldFeedback() {
        emailField.getStyleClass().remove("field-invalid");
        passwordField.getStyleClass().remove("field-invalid");
    }
}
