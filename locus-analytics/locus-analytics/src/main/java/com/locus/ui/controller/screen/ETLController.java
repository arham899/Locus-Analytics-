package com.locus.ui.controller.screen;

import com.locus.model.ETLJob;
import com.locus.service.ETLService;
import com.locus.ui.ServiceRegistry;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.net.URL;
import java.util.ResourceBundle;

public class ETLController implements Initializable {

    @FXML
    private Label lastRunSummaryLabel;
    @FXML
    private Label lastRunTimeLabel;
    @FXML
    private Label successCountLabel;
    @FXML
    private Label failedCountLabel;
    @FXML
    private Label healthLabel;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label stageLabel;
    @FXML
    private Label statusLabel;

    private ETLService etlService;
    private String currentJobId;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.etlService = serviceRegistry.etlService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        progressBar.setProgress(0);
        lastRunTimeLabel.setText("-");
        successCountLabel.setText("0");
        failedCountLabel.setText("0");
        healthLabel.setText("98.5%");
    }

    @FXML
    private void onLoadLastRun() {
        if (etlService == null) {
            statusLabel.setText("ETL service not available.");
            return;
        }
        ETLJob job = etlService.getLastRunSummary();
        if (job == null) {
            lastRunSummaryLabel.setText("No previous ETL runs.");
            lastRunTimeLabel.setText("-");
        } else {
            lastRunTimeLabel.setText(String.valueOf(job.getRunDate()));
            lastRunSummaryLabel.setText("Loaded " + job.getRecordsLoaded() + " records, errors " + job.getErrors());
            successCountLabel.setText(String.valueOf(job.getRecordsLoaded()));
            failedCountLabel.setText(String.valueOf(job.getErrors()));
            healthLabel.setText(job.getErrors() == 0 ? "100%" : "96.8%");
        }
    }

    @FXML
    private void onStartPipeline() {
        if (etlService == null) {
            statusLabel.setText("ETL service not available.");
            return;
        }
        ETLJob started = etlService.triggerPipeline();
        currentJobId = started.getJobId();
        stageLabel.setText(started.getCurrentStage());
        progressBar.setProgress(started.getProgressPercent() / 100.0);
        statusLabel.setText("ETL pipeline started in stub mode.");
        successCountLabel.setText("0");
        failedCountLabel.setText("0");
    }

    @FXML
    private void onRefreshStatus() {
        if (etlService == null || currentJobId == null) {
            statusLabel.setText("Start a job first.");
            return;
        }
        ETLJob current = etlService.getCurrentStatus(currentJobId);
        stageLabel.setText(current.getCurrentStage() + " (" + current.getStatus() + ")");
        progressBar.setProgress(current.getProgressPercent() / 100.0);
        statusLabel.setText("Loaded " + current.getRecordsLoaded() + " records.");
        successCountLabel.setText(String.valueOf(current.getRecordsLoaded()));
        failedCountLabel.setText(String.valueOf(current.getErrors()));
        healthLabel.setText(current.getErrors() == 0 ? "100%" : "98.0%");
    }
}
