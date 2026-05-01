package com.locus.ui.controller.screen;

import com.locus.exception.ValidationException;
import javafx.scene.control.Alert;
import javafx.scene.control.Control;
import javafx.scene.control.Label;

import java.util.Map;

final class UiFeedbackHelper {

    private static final String INVALID_STYLE = "field-invalid";

    private UiFeedbackHelper() {
    }

    static void clearValidation(Control... controls) {
        for (Control control : controls) {
            if (control != null) {
                control.getStyleClass().remove(INVALID_STYLE);
            }
        }
    }

    static void markInvalid(Control control) {
        if (control != null && !control.getStyleClass().contains(INVALID_STYLE)) {
            control.getStyleClass().add(INVALID_STYLE);
        }
        UiAnimationHelper.shakeInvalid(control);
    }

    static void markFromFieldErrors(Map<String, String> fieldErrors, Map<String, Control> controlByField) {
        for (String field : fieldErrors.keySet()) {
            markInvalid(controlByField.get(field));
        }
    }

    static String formatValidationMessage(ValidationException ex) {
        if (!ex.hasFieldErrors()) {
            return ex.getMessage();
        }
        StringBuilder builder = new StringBuilder(ex.getMessage()).append('\n');
        ex.getFieldErrors().forEach((key, value) -> builder.append("- ").append(key).append(": ").append(value).append('\n'));
        return builder.toString().trim();
    }

    static void setStatus(Label statusLabel, String text, String styleClass) {
        if (statusLabel == null) {
            return;
        }
        UiAnimationHelper.showToast(statusLabel, text, styleClass);
    }

    static void showErrorDialog(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("LOCUS Analytics");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    static void showInfoDialog(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("LOCUS Analytics");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
