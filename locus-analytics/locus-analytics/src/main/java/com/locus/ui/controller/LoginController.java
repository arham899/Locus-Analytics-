package com.locus.ui.controller;

import com.locus.exception.ValidationException;
import com.locus.model.User;
import com.locus.ui.BrandAssets;
import com.locus.ui.SceneManager;
import com.locus.ui.ServiceRegistry;
import com.locus.ui.controller.screen.UiAnimationHelper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label feedbackLabel;
    @FXML private Button loginButton;
    @FXML private ImageView brandLogoView;

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
        emailField.textProperty().addListener((obs, o, n) -> clearFieldFeedback());
        passwordField.textProperty().addListener((obs, o, n) -> clearFieldFeedback());
        UiAnimationHelper.attachHoverScale(loginButton);

        emailField.setOnAction(e -> onLogin());
        passwordField.setOnAction(e -> onLogin());
    }

    @FXML
    private void onLogin() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        boolean hasError = false;
        if (email.isEmpty()) {
            markInvalid(emailField);
            hasError = true;
        }
        if (password.isEmpty()) {
            markInvalid(passwordField);
            hasError = true;
        }
        if (hasError) {
            showFeedback("Email and password are required.", "status-error");
            return;
        }

        setLoadingState(true);

        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() {
                return serviceRegistry.authenticationService().login(email, password);
            }
        };

        loginTask.setOnSucceeded(e -> {
            User user = loginTask.getValue();
            feedbackLabel.setText("");
            setLoadingState(false);
            sceneManager.showMain(user);
        });

        loginTask.setOnFailed(e -> {
            setLoadingState(false);
            Throwable ex = loginTask.getException();
            if (ex instanceof ValidationException ve) {
                applyValidationErrors(ve);
            } else {
                showFeedback("Login failed. Please try again.", "status-error");
            }
        });

        Thread thread = new Thread(loginTask);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void onForgotPassword() {
        showFeedback("Contact your system administrator to reset your password.", "status-warning");
    }

    private void applyValidationErrors(ValidationException ex) {
        Platform.runLater(() -> {
            if (ex.hasFieldErrors()) {
                Map<String, String> errors = ex.getFieldErrors();
                if (errors.containsKey("email")) {
                    markInvalid(emailField);
                }
                if (errors.containsKey("password")) {
                    markInvalid(passwordField);
                }
                String first = errors.values().iterator().next();
                showFeedback(first, "status-error");
            } else {
                showFeedback(ex.getMessage(), "status-error");
            }
        });
    }

    private void showFeedback(String message, String styleClass) {
        feedbackLabel.getStyleClass().removeAll("status-error", "status-success", "status-warning");
        feedbackLabel.getStyleClass().add(styleClass);
        feedbackLabel.setText(message);
        UiAnimationHelper.showToast(feedbackLabel, message, styleClass);
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
        feedbackLabel.setText("");
        feedbackLabel.getStyleClass().removeAll("status-error", "status-success", "status-warning");
    }
}
