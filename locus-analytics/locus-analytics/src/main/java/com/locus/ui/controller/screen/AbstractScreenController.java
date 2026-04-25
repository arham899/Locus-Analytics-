package com.locus.ui.controller.screen;

import com.locus.ui.ServiceRegistry;
import com.locus.ui.controller.ServiceAwareController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Base class for placeholder screens in Phase 1 scaffolding.
 */
public abstract class AbstractScreenController implements Initializable, ServiceAwareController {

    @FXML
    private Label screenTitleLabel;

    @FXML
    private Label screenDescriptionLabel;

    protected ServiceRegistry serviceRegistry;

    @Override
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        screenTitleLabel.setText(getScreenTitle());
        screenDescriptionLabel.setText(getScreenDescription());
    }

    protected abstract String getScreenTitle();

    protected abstract String getScreenDescription();
}
