package com.locus.ui.controller.screen;

import com.locus.model.ETLJob;
import com.locus.service.ETLService;
import com.locus.ui.ServiceRegistry;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;

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
    private Label failedSubLabel;
    @FXML
    private Label healthLabel;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label stageLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private ProgressIndicator loadingIndicator;
    @FXML
    private Button loadLastRunButton;
    @FXML
    private Button refreshStatusButton;
    @FXML
    private Button startPipelineButton;

    private ETLService etlService;
    private String currentJobId;
    private Timeline pollTimeline;
    private boolean pipelineRunning;
    private final DoubleProperty successAnimated = new SimpleDoubleProperty(0);
    private final DoubleProperty failedAnimated = new SimpleDoubleProperty(0);
    private final DoubleProperty progressAnimated = new SimpleDoubleProperty(0);

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
        successAnimated.addListener((obs, oldValue, newValue) ->
                successCountLabel.setText(String.valueOf(newValue.intValue())));
        failedAnimated.addListener((obs, oldValue, newValue) -> {
            int count = newValue.intValue();
            failedCountLabel.setText(String.valueOf(count));
            if (failedSubLabel != null) {
                failedSubLabel.setText(count > 0 ? "Needs operator attention" : "All tasks completed");
            }
        });
        progressAnimated.addListener((obs, oldValue, newValue) ->
                progressBar.setProgress(Math.max(0, Math.min(1, newValue.doubleValue()))));
        UiAnimationHelper.playStaggeredReveal(List.of(
                lastRunSummaryLabel, progressBar, successCountLabel, failedCountLabel, stageLabel
        ));
        UiAnimationHelper.attachHoverScale(loadLastRunButton);
        UiAnimationHelper.attachHoverScale(refreshStatusButton);
        UiAnimationHelper.attachHoverScale(startPipelineButton);
    }

    @FXML
    private void onLoadLastRun() {
        if (etlService == null) {
            UiFeedbackHelper.setStatus(statusLabel, "ETL service not available.", "status-error");
            return;
        }
        Task<ETLJob> lastRunTask = new Task<>() {
            @Override
            protected ETLJob call() {
                return etlService.getLastRunSummary();
            }
        };
        lastRunTask.setOnRunning(event -> setLoadingState(true));
        lastRunTask.setOnSucceeded(event -> {
            setLoadingState(false);
            ETLJob job = lastRunTask.getValue();
            if (job == null) {
                lastRunSummaryLabel.setText("No previous ETL runs.");
                lastRunTimeLabel.setText("-");
                return;
            }
            lastRunTimeLabel.setText(String.valueOf(job.getRunDate()));
            lastRunSummaryLabel.setText("Loaded " + job.getRecordsLoaded() + " records, errors " + job.getErrors());
            successCountLabel.setText(String.valueOf(job.getRecordsLoaded()));
            failedCountLabel.setText(String.valueOf(job.getErrors()));
            healthLabel.setText(job.getErrors() == 0 ? "100%" : "96.8%");
            UiFeedbackHelper.setStatus(statusLabel, "Loaded latest ETL run summary.", "status-success");
        });
        lastRunTask.setOnFailed(event -> {
            setLoadingState(false);
            Throwable ex = lastRunTask.getException();
            UiFeedbackHelper.setStatus(statusLabel, "Could not load last run summary.", "status-error");
            UiFeedbackHelper.showErrorDialog("ETL Error", ex == null ? "Unknown error" : ex.getMessage());
        });
        Thread thread = new Thread(lastRunTask, "etl-last-run-task");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void onStartPipeline() {
        if (etlService == null) {
            UiFeedbackHelper.setStatus(statusLabel, "ETL service not available.", "status-error");
            return;
        }
        Task<ETLJob> triggerTask = new Task<>() {
            @Override
            protected ETLJob call() {
                return etlService.triggerPipeline();
            }
        };
        triggerTask.setOnRunning(event -> setLoadingState(true));
        triggerTask.setOnSucceeded(event -> {
            setLoadingState(false);
            ETLJob started = triggerTask.getValue();
            currentJobId = started.getJobId();
            stageLabel.setText(started.getCurrentStage());
            animateProgress(started.getProgressPercent() / 100.0);
            UiFeedbackHelper.setStatus(statusLabel, "ETL pipeline started.", "status-success");
            UiAnimationHelper.animateNumber(successAnimated, successAnimated.get(), 0, Duration.millis(500));
            UiAnimationHelper.animateNumber(failedAnimated, failedAnimated.get(), 0, Duration.millis(500));
            pipelineRunning = true;
            startPolling();
        });
        triggerTask.setOnFailed(event -> {
            setLoadingState(false);
            Throwable ex = triggerTask.getException();
            UiFeedbackHelper.setStatus(statusLabel, "Could not start ETL pipeline.", "status-error");
            UiFeedbackHelper.showErrorDialog("ETL Start Failed", ex == null ? "Unknown error" : ex.getMessage());
        });
        Thread thread = new Thread(triggerTask, "etl-start-task");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void onRefreshStatus() {
        if (etlService == null || currentJobId == null) {
            UiFeedbackHelper.setStatus(statusLabel, "Start a job first.", "status-warning");
            return;
        }
        Task<ETLJob> refreshTask = new Task<>() {
            @Override
            protected ETLJob call() {
                return etlService.getCurrentStatus(currentJobId);
            }
        };
        refreshTask.setOnRunning(event -> setLoadingState(true));
        refreshTask.setOnSucceeded(event -> {
            setLoadingState(false);
            ETLJob current = refreshTask.getValue();
            stageLabel.setText(current.getCurrentStage() + " (" + current.getStatus() + ")");
            animateProgress(current.getProgressPercent() / 100.0);
            UiFeedbackHelper.setStatus(statusLabel, "Loaded " + current.getRecordsLoaded() + " records.", "status-success");
            UiAnimationHelper.animateNumber(successAnimated, successAnimated.get(), current.getRecordsLoaded(), Duration.millis(640));
            UiAnimationHelper.animateNumber(failedAnimated, failedAnimated.get(), current.getErrors(), Duration.millis(640));
            UiAnimationHelper.pulseMetric(successCountLabel, UiAnimationHelper.SignalType.POSITIVE);
            healthLabel.setText(current.getErrors() == 0 ? "100%" : "98.0%");
        });
        refreshTask.setOnFailed(event -> {
            setLoadingState(false);
            Throwable ex = refreshTask.getException();
            UiFeedbackHelper.setStatus(statusLabel, "Could not refresh ETL status.", "status-error");
            UiFeedbackHelper.showErrorDialog("ETL Refresh Failed", ex == null ? "Unknown error" : ex.getMessage());
        });
        Thread thread = new Thread(refreshTask, "etl-refresh-task");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void onSearchPipelines() {
        UiFeedbackHelper.showInfoDialog("Search", "Pipeline search dialog opened.");
    }

    @FXML
    private void onPausePipeline() {
        if (pipelineRunning) {
            pipelineRunning = false;
            stopPolling();
            UiFeedbackHelper.setStatus(statusLabel, "Pipeline paused.", "status-warning");
        }
    }

    @FXML
    private void onViewLogs() {
        UiFeedbackHelper.showInfoDialog("Logs", "Fetching latest execution logs...");
    }

    private void setLoadingState(boolean loading) {
        boolean disableActions = loading || pipelineRunning;
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(loading);
            loadingIndicator.setManaged(loading);
        }
        UiAnimationHelper.setSkeletonVisible(progressBar, loading);
        if (loadLastRunButton != null) {
            loadLastRunButton.setDisable(disableActions);
        }
        if (refreshStatusButton != null) {
            refreshStatusButton.setDisable(disableActions);
        }
        if (startPipelineButton != null) {
            startPipelineButton.setDisable(disableActions);
            startPipelineButton.setText(disableActions ? "Running..." : "Start Pipeline");
        }
    }

    private void startPolling() {
        stopPolling();
        pollTimeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> pollCurrentJob()));
        pollTimeline.setCycleCount(Timeline.INDEFINITE);
        pollTimeline.play();
    }

    private void stopPolling() {
        if (pollTimeline != null) {
            pollTimeline.stop();
            pollTimeline = null;
        }
    }

    private void pollCurrentJob() {
        if (etlService == null || currentJobId == null) {
            return;
        }
        Task<ETLJob> task = new Task<>() {
            @Override
            protected ETLJob call() {
                return etlService.getCurrentStatus(currentJobId);
            }
        };
        task.setOnSucceeded(event -> {
            ETLJob current = task.getValue();
            stageLabel.setText(normalizeStage(current.getCurrentStage()) + " (" + current.getStatus() + ")");
            animateProgress(current.getProgressPercent() / 100.0);
            UiAnimationHelper.animateNumber(successAnimated, successAnimated.get(), current.getRecordsLoaded(), Duration.millis(520));
            UiAnimationHelper.animateNumber(failedAnimated, failedAnimated.get(), current.getErrors(), Duration.millis(520));
            healthLabel.setText(current.getErrors() == 0 ? "100%" : "98.0%");
            if ("success".equalsIgnoreCase(current.getStatus()) || "failed".equalsIgnoreCase(current.getStatus())
                    || current.getProgressPercent() >= 100) {
                pipelineRunning = false;
                stopPolling();
                setLoadingState(false);
                showCompletionDialog(current);
            }
        });
        task.setOnFailed(event -> {
            pipelineRunning = false;
            stopPolling();
            setLoadingState(false);
            Throwable ex = task.getException();
            UiFeedbackHelper.setStatus(statusLabel, "ETL polling failed.", "status-error");
            UiFeedbackHelper.showErrorDialog("ETL Polling Error", ex == null ? "Unknown error" : ex.getMessage());
        });
        Thread thread = new Thread(task, "etl-poll-task");
        thread.setDaemon(true);
        thread.start();
    }

    private void showCompletionDialog(ETLJob job) {
        String summary = "Extracted: " + job.getRecordsExtracted()
                + "\nCleaned: " + job.getRecordsCleaned()
                + "\nNew/Loaded: " + job.getRecordsLoaded()
                + "\nSkipped: " + Math.max(0, job.getRecordsExtracted() - job.getRecordsCleaned())
                + "\nErrors: " + job.getErrors();
        UiFeedbackHelper.showInfoDialog("ETL Completed", summary + "\n\nClick OK to close.");
        UiFeedbackHelper.setStatus(statusLabel, "ETL completed. Summary confirmed.", "status-success");
    }

    private String normalizeStage(String stage) {
        if (stage == null) {
            return "Extracting";
        }
        String s = stage.toLowerCase();
        if (s.contains("extract")) return "Extracting";
        if (s.contains("transform")) return "Transforming";
        if (s.contains("load")) return "Loading";
        return stage;
    }

    private void animateProgress(double target) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(progressAnimated, progressAnimated.get())),
                new KeyFrame(Duration.millis(360), new KeyValue(progressAnimated, target))
        );
        timeline.play();
    }

    public void dispose() {
        stopPolling();
        pipelineRunning = false;
        setLoadingState(false);
        currentJobId = null;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            dispose();
        } finally {
            super.finalize();
        }
    }
}
