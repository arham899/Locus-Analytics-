package com.locus.ui.controller.screen;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

public final class UiAnimationHelper {

    public enum SignalType { POSITIVE, WARNING, RISK, NEUTRAL }

    public enum RouteIntent { ANALYSIS, ADMIN, REPORT, GENERAL }
    private static final Map<Window, Popup> FLOATING_TOAST_POPUPS = new WeakHashMap<>();
    private static final Map<Window, VBox> FLOATING_TOAST_STACKS = new WeakHashMap<>();

    private UiAnimationHelper() {
    }

    public static void playPageEnter(Node node) {
        playPageEnter(node, RouteIntent.GENERAL);
    }

    public static void playPageEnter(Node node, RouteIntent intent) {
        if (node == null) {
            return;
        }
        runOnFxThread(() -> {
            stopStoredAnimation(node, "anim-page-enter");
            double fromY = (intent == RouteIntent.ADMIN ? 6 : 10) * UiMotionProfile.movementScale();
            Duration duration = intent == RouteIntent.REPORT ? UiMotionProfile.slow() : UiMotionProfile.medium();
            node.setOpacity(0.0);
            node.setTranslateY(fromY);
            FadeTransition fade = new FadeTransition(duration, node);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.setInterpolator(UiMotionProfile.ENTER);
            TranslateTransition translate = new TranslateTransition(duration, node);
            translate.setFromY(fromY);
            translate.setToY(0);
            translate.setInterpolator(UiMotionProfile.ENTER);
            ParallelTransition transition = new ParallelTransition(fade, translate);
            transition.setOnFinished(event -> node.setTranslateY(0));
            storeAndPlay(node, "anim-page-enter", transition);
        });
    }

    public static void playStaggeredReveal(List<? extends Node> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        runOnFxThread(() -> {
            int visibleIndex = 0;
            for (Node node : nodes) {
                if (node == null) {
                    continue;
                }
                stopStoredAnimation(node, "anim-reveal");
                node.setOpacity(0.0);
                double offset = 12 * UiMotionProfile.movementScale();
                node.setTranslateY(offset);
                FadeTransition fade = new FadeTransition(UiMotionProfile.medium(), node);
                fade.setFromValue(0.0);
                fade.setToValue(1.0);
                fade.setDelay(UiMotionProfile.stagger().multiply(visibleIndex));
                TranslateTransition slide = new TranslateTransition(UiMotionProfile.medium(), node);
                slide.setFromY(offset);
                slide.setToY(0);
                slide.setDelay(UiMotionProfile.stagger().multiply(visibleIndex));
                slide.setInterpolator(UiMotionProfile.ENTER);
                ParallelTransition transition = new ParallelTransition(fade, slide);
                transition.setOnFinished(event -> node.setTranslateY(0));
                storeAndPlay(node, "anim-reveal", transition);
                visibleIndex++;
            }
        });
    }

    public static Timeline animateNumber(DoubleProperty property, double from, double to, Duration duration) {
        Duration safeDuration = duration == null ? UiMotionProfile.slow() : duration;
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(property, from, UiMotionProfile.ENTER)),
                new KeyFrame(safeDuration, new KeyValue(property, to, UiMotionProfile.EMPHASIS))
        );
        runOnFxThread(() -> timeline.playFromStart());
        return timeline;
    }

    public static void attachHoverScale(Node node) {
        if (node == null) {
            return;
        }
        runOnFxThread(() -> {
            if (Boolean.TRUE.equals(node.getProperties().get("anim-hover-attached"))) {
                return;
            }
            node.getProperties().put("anim-hover-attached", true);
            node.setOnMouseEntered(event -> {
                if (node instanceof Control control && control.isDisabled()) {
                    return;
                }
                playHoverScale(node, 1.03);
            });
            node.setOnMouseExited(event -> playHoverScale(node, 1.0));
        });
    }

    public static void attachSpringPress(Node node) {
        if (node == null) {
            return;
        }
        runOnFxThread(() -> {
            if (Boolean.TRUE.equals(node.getProperties().get("anim-spring-attached"))) {
                return;
            }
            node.getProperties().put("anim-spring-attached", true);
            node.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> playHoverScale(node, 0.98));
            node.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_RELEASED, event -> playHoverScale(node, 1.01));
        });
    }

    public static void setMotionPreset(UiMotionProfile.Preset preset) {
        UiMotionProfile.setPreset(preset);
    }

    public static void setReducedMotion(boolean reducedMotion) {
        UiMotionProfile.setReducedMotion(reducedMotion);
    }

    public static void showToast(Label label, String message, String toneStyleClass) {
        enqueueToast(label, message, toneStyleClass);
    }

    public static void enqueueToast(Label label, String message, String toneStyleClass) {
        if (label == null) {
            return;
        }
        runOnFxThread(() -> {
            Deque<ToastMessage> queue = getToastQueue(label);
            queue.addLast(new ToastMessage(message, toneStyleClass));
            if (Boolean.TRUE.equals(label.getProperties().get("anim-toast-running"))) {
                return;
            }
            label.getProperties().put("anim-toast-running", true);
            playNextToast(label);
        });
    }

    public static void shakeInvalid(Control control) {
        if (control == null) {
            return;
        }
        runOnFxThread(() -> {
            if (!control.getStyleClass().contains("anim-invalid")) {
                control.getStyleClass().add("anim-invalid");
            }
            stopStoredAnimation(control, "anim-invalid");
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(control.translateXProperty(), 0)),
                    new KeyFrame(Duration.millis(50), new KeyValue(control.translateXProperty(), -6)),
                    new KeyFrame(Duration.millis(100), new KeyValue(control.translateXProperty(), 6)),
                    new KeyFrame(Duration.millis(150), new KeyValue(control.translateXProperty(), -4)),
                    new KeyFrame(Duration.millis(200), new KeyValue(control.translateXProperty(), 4)),
                    new KeyFrame(Duration.millis(250), new KeyValue(control.translateXProperty(), 0))
            );
            timeline.setOnFinished(event -> control.getStyleClass().remove("anim-invalid"));
            storeAndPlay(control, "anim-invalid", timeline);
        });
    }

    public static void pulseMetric(Node node, SignalType signalType) {
        if (node == null) {
            return;
        }
        runOnFxThread(() -> {
            node.getStyleClass().removeAll("anim-signal-positive", "anim-signal-warning", "anim-signal-risk");
            switch (signalType) {
                case POSITIVE -> node.getStyleClass().add("anim-signal-positive");
                case WARNING -> node.getStyleClass().add("anim-signal-warning");
                case RISK -> node.getStyleClass().add("anim-signal-risk");
                default -> {
                }
            }
            stopStoredAnimation(node, "anim-pulse");
            ScaleTransition up = new ScaleTransition(UiMotionProfile.FAST, node);
            up.setToX(1.03);
            up.setToY(1.03);
            ScaleTransition down = new ScaleTransition(UiMotionProfile.medium(), node);
            down.setToX(1.0);
            down.setToY(1.0);
            SequentialTransition pulse = new SequentialTransition(up, down);
            pulse.setOnFinished(event -> node.getStyleClass().removeAll("anim-signal-positive", "anim-signal-warning", "anim-signal-risk"));
            storeAndPlay(node, "anim-pulse", pulse);
        });
    }

    public static void revealLineSeries(XYChart<String, Number> chart, XYChart.Series<String, Number> series) {
        if (chart == null || series == null) {
            return;
        }
        runOnFxThread(() -> {
            revealLineSeries(chart, series, null);
        });
    }

    public static void revealBarSeries(XYChart<String, Number> chart, XYChart.Series<String, Number> series) {
        revealLineSeries(chart, series, null);
    }

    public static void revealLineSeries(XYChart<String, Number> chart, XYChart.Series<String, Number> main,
                                        XYChart.Series<String, Number> overlay) {
        if (chart == null || main == null) {
            return;
        }
        runOnFxThread(() -> {
            stopStoredAnimation(chart, "anim-chart-reveal");
            if (overlay != null) {
                chart.getData().setAll(main, overlay);
            } else {
                chart.getData().setAll(main);
            }
            // For larger series, avoid incremental stepping to prevent jitter.
            if (main.getData().size() > 14) {
                playChartFade(chart, UiMotionProfile.FAST);
                return;
            }
            Timeline timeline = new Timeline();
            for (int i = 0; i < main.getData().size(); i++) {
                final int idx = i;
                timeline.getKeyFrames().add(new KeyFrame(
                        UiMotionProfile.stagger().multiply(i + 1),
                        event -> {
                            XYChart.Data<String, Number> data = main.getData().get(idx);
                            Node dataNode = data.getNode();
                            if (dataNode != null) {
                                dataNode.setOpacity(0.0);
                                FadeTransition fade = new FadeTransition(UiMotionProfile.fast(), dataNode);
                                fade.setFromValue(0.0);
                                fade.setToValue(1.0);
                                fade.playFromStart();
                            }
                        }
                ));
            }
            storeAndPlay(chart, "anim-chart-reveal", timeline);
        });
    }

    public static void setSkeletonVisible(Node node, boolean visible) {
        if (node == null) {
            return;
        }
        runOnFxThread(() -> {
            if (visible) {
                if (!node.getStyleClass().contains("anim-shimmer")) {
                    node.getStyleClass().add("anim-shimmer");
                }
                stopStoredAnimation(node, "anim-shimmer");
                FadeTransition shimmer = new FadeTransition(UiMotionProfile.SLOW, node);
                shimmer.setFromValue(0.72);
                shimmer.setToValue(1.0);
                shimmer.setAutoReverse(true);
                shimmer.setCycleCount(Animation.INDEFINITE);
                storeAndPlay(node, "anim-shimmer", shimmer);
            } else {
                node.getStyleClass().remove("anim-shimmer");
                stopStoredAnimation(node, "anim-shimmer");
                node.setOpacity(1.0);
            }
        });
    }

    public static void attachParallax(Node node) {
        if (node == null) {
            return;
        }
        runOnFxThread(() -> {
            if (Boolean.TRUE.equals(node.getProperties().get("anim-parallax-attached"))) {
                return;
            }
            node.getProperties().put("anim-parallax-attached", true);
            node.getStyleClass().add("anim-parallax-card");
            node.setOnMouseMoved(event -> {
                double amplitude = 1.8 * UiMotionProfile.movementScale();
                double x = ((event.getX() / Math.max(1, node.getBoundsInLocal().getWidth())) - 0.5) * amplitude;
                double y = ((event.getY() / Math.max(1, node.getBoundsInLocal().getHeight())) - 0.5) * amplitude;
                node.setTranslateX(x);
                node.setTranslateY(y);
            });
            node.setOnMouseExited(event -> {
                node.setTranslateX(0);
                node.setTranslateY(0);
            });
        });
    }

    public static void playScanline(Node node) {
        if (node == null) {
            return;
        }
        runOnFxThread(() -> {
            node.getStyleClass().add("anim-scanline");
            PauseTransition pause = new PauseTransition(UiMotionProfile.medium().add(UiMotionProfile.fast()));
            pause.setOnFinished(event -> node.getStyleClass().remove("anim-scanline"));
            pause.playFromStart();
        });
    }

    public static void installScrollReveal(ScrollPane scrollPane, List<? extends Node> nodes) {
        if (scrollPane == null || nodes == null || nodes.isEmpty()) {
            return;
        }
        runOnFxThread(() -> {
            if (Boolean.TRUE.equals(scrollPane.getProperties().get("anim-scroll-reveal-installed"))) {
                return;
            }
            scrollPane.getProperties().put("anim-scroll-reveal-installed", true);
            List<Node> validNodes = nodes.stream()
                    .filter(Objects::nonNull)
                    .map(node -> (Node) node)
                    .toList();
            int initialVisible = Math.min(2, validNodes.size());
            for (int i = 0; i < validNodes.size(); i++) {
                Node node = validNodes.get(i);
                boolean visibleNow = i < initialVisible;
                node.setOpacity(visibleNow ? 1.0 : 0.0);
                node.getProperties().put("anim-scroll-revealed", visibleNow);
            }
            scrollPane.vvalueProperty().addListener((obs, oldV, newV) -> {
                int targetVisible = Math.min(
                        validNodes.size(),
                        initialVisible + (int) Math.round(newV.doubleValue() * Math.max(0, validNodes.size() - initialVisible))
                );
                for (int i = 0; i < targetVisible; i++) {
                    Node node = validNodes.get(i);
                    if (!Boolean.TRUE.equals(node.getProperties().get("anim-scroll-revealed"))) {
                        node.getProperties().put("anim-scroll-revealed", true);
                        playStaggeredReveal(List.of(node));
                    }
                }
            });
        });
    }

    public static <T> void highlightTableDiff(TableView<T> table, List<T> oldItems, List<T> newItems) {
        if (table == null || newItems == null) {
            return;
        }
        runOnFxThread(() -> {
            int oldSize = oldItems == null ? 0 : oldItems.size();
            if (newItems.size() != oldSize) {
                table.getStyleClass().add("anim-diff-highlight");
                PauseTransition pause = new PauseTransition(Duration.millis(700));
                pause.setOnFinished(event -> table.getStyleClass().remove("anim-diff-highlight"));
                pause.playFromStart();
                return;
            }
            for (int i = 0; i < newItems.size(); i++) {
                if (!Objects.equals(newItems.get(i), oldItems.get(i))) {
                    table.getStyleClass().add("anim-diff-highlight");
                    PauseTransition pause = new PauseTransition(Duration.millis(700));
                    pause.setOnFinished(event -> table.getStyleClass().remove("anim-diff-highlight"));
                    pause.playFromStart();
                    break;
                }
            }
        });
    }

    public static void playSceneCrossfade(Stage stage, javafx.scene.Scene nextScene, boolean cinematic) {
        playSceneCrossfade(stage, nextScene);
        if (stage == null || !cinematic || nextScene == null || nextScene.getRoot() == null) {
            return;
        }
        runOnFxThread(() -> {
            Node logo = nextScene.getRoot().lookup("#topbarLogoView");
            if (logo != null) {
                logo.setScaleX(0.2);
                logo.setScaleY(0.2);
                logo.setOpacity(0.0);
                ScaleTransition s = new ScaleTransition(UiMotionProfile.SLOW, logo);
                s.setToX(1.0);
                s.setToY(1.0);
                FadeTransition f = new FadeTransition(UiMotionProfile.medium(), logo);
                f.setToValue(1.0);
                new ParallelTransition(s, f).playFromStart();
            }
        });
    }

    public static void playSceneCrossfade(Stage stage, javafx.scene.Scene nextScene) {
        if (stage == null || nextScene == null) {
            return;
        }
        runOnFxThread(() -> {
            javafx.scene.Parent nextRoot = nextScene.getRoot();
            stage.setScene(nextScene);
            stage.show();
            if (nextRoot != null) {
                nextRoot.setOpacity(0.0);
                FadeTransition fadeIn = new FadeTransition(UiMotionProfile.medium(), nextRoot);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
        });
    }

    private static void playHoverScale(Node node, double to) {
        stopStoredAnimation(node, "anim-hover");
        ScaleTransition scale = new ScaleTransition(UiMotionProfile.fast(), node);
        scale.setToX(to);
        scale.setToY(to);
        scale.setInterpolator(UiMotionProfile.EMPHASIS);
        storeAndPlay(node, "anim-hover", scale);
    }

    private static void storeAndPlay(Node node, String key, Animation animation) {
        trimAnimationKeys(node);
        node.getProperties().put(key, animation);
        animation.playFromStart();
    }

    private static void stopStoredAnimation(Node node, String key) {
        Object existing = node.getProperties().get(key);
        if (existing instanceof Animation animation) {
            animation.stop();
        }
    }

    private static void runOnFxThread(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }

    private static void playChartFade(Node chart, Duration duration) {
        chart.setOpacity(0.82);
        FadeTransition fade = new FadeTransition(duration == null ? UiMotionProfile.fast() : duration, chart);
        fade.setFromValue(0.82);
        fade.setToValue(1.0);
        fade.playFromStart();
    }

    private static void trimAnimationKeys(Node node) {
        List<String> keys = node.getProperties().keySet().stream()
                .filter(key -> key instanceof String && ((String) key).startsWith("anim-"))
                .map(key -> (String) key)
                .sorted(Comparator.naturalOrder())
                .toList();
        int overflow = keys.size() - UiMotionProfile.MAX_ANIMATION_KEYS_PER_NODE;
        for (int i = 0; i < overflow; i++) {
            node.getProperties().remove(keys.get(i));
        }
    }

    @SuppressWarnings("unchecked")
    private static Deque<ToastMessage> getToastQueue(Label label) {
        Object existing = label.getProperties().get("anim-toast-queue");
        if (existing instanceof Deque<?> deque) {
            return (Deque<ToastMessage>) deque;
        }
        Deque<ToastMessage> created = new ArrayDeque<>();
        label.getProperties().put("anim-toast-queue", created);
        return created;
    }

    private static void playNextToast(Label label) {
        Deque<ToastMessage> queue = getToastQueue(label);
        ToastMessage next = queue.pollFirst();
        if (next == null) {
            label.getProperties().put("anim-toast-running", false);
            return;
        }
        if (next.text != null) {
            label.setText(next.text);
        }
        showFloatingToast(label, next);
        label.getStyleClass().removeAll("status-error", "status-success", "status-warning");
        if (next.toneStyleClass != null && !next.toneStyleClass.isBlank()) {
            label.getStyleClass().add(next.toneStyleClass);
        }
        if (!label.getStyleClass().contains("anim-toast")) {
            label.getStyleClass().add("anim-toast");
        }
        stopStoredAnimation(label, "anim-toast");
        label.setOpacity(0.0);
        label.setTranslateY(6.0);
        FadeTransition fadeIn = new FadeTransition(UiMotionProfile.fast(), label);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        TranslateTransition slideIn = new TranslateTransition(UiMotionProfile.fast(), label);
        slideIn.setFromY(6.0);
        slideIn.setToY(0);
        ParallelTransition in = new ParallelTransition(fadeIn, slideIn);
        PauseTransition hold = new PauseTransition(UiMotionProfile.toastVisible());
        FadeTransition settle = new FadeTransition(UiMotionProfile.medium(), label);
        settle.setFromValue(1.0);
        settle.setToValue(0.92);
        SequentialTransition seq = new SequentialTransition(in, hold, settle);
        seq.setOnFinished(event -> playNextToast(label));
        storeAndPlay(label, "anim-toast", seq);
    }

    private record ToastMessage(String text, String toneStyleClass) {
    }

    private static void showFloatingToast(Label source, ToastMessage message) {
        if (source.getScene() == null || source.getScene().getWindow() == null || message == null || message.text == null) {
            return;
        }
        Window window = source.getScene().getWindow();
        Popup popup = FLOATING_TOAST_POPUPS.computeIfAbsent(window, w -> {
            Popup created = new Popup();
            created.setAutoFix(true);
            created.setAutoHide(false);
            created.setHideOnEscape(false);
            created.setConsumeAutoHidingEvents(false);
            return created;
        });
        VBox stack = FLOATING_TOAST_STACKS.computeIfAbsent(window, w -> {
            VBox box = new VBox(8);
            box.setPadding(new Insets(10));
            box.getStyleClass().add("floating-toast-stack");
            return box;
        });
        if (!popup.getContent().contains(stack)) {
            popup.getContent().setAll(stack);
        }
        Label toast = new Label(message.text);
        toast.getStyleClass().addAll("floating-toast", "anim-toast");
        if (message.toneStyleClass != null && !message.toneStyleClass.isBlank()) {
            toast.getStyleClass().add(message.toneStyleClass);
        }
        stack.getChildren().add(0, toast);
        while (stack.getChildren().size() > 3) {
            stack.getChildren().remove(stack.getChildren().size() - 1);
        }
        if (!popup.isShowing()) {
            popup.show(window);
        }
        popup.setX(window.getX() + window.getWidth() - 360);
        popup.setY(window.getY() + window.getHeight() - 120 - (stack.getChildren().size() * 40));
        PauseTransition remove = new PauseTransition(UiMotionProfile.toastVisible().add(UiMotionProfile.fast()));
        remove.setOnFinished(evt -> {
            stack.getChildren().remove(toast);
            if (stack.getChildren().isEmpty()) {
                popup.hide();
            }
        });
        remove.playFromStart();
    }
}
