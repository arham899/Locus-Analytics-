package com.locus.ui.controller.screen;

import javafx.animation.Interpolator;
import javafx.util.Duration;

public final class UiMotionProfile {

    public enum Preset {
        MINIMAL(0.72, 0.5),
        BALANCED(1.0, 1.0),
        RICH(1.15, 1.2);

        private final double durationScale;
        private final double movementScale;

        Preset(double durationScale, double movementScale) {
            this.durationScale = durationScale;
            this.movementScale = movementScale;
        }
    }

    public static final Duration FAST = Duration.millis(170);
    public static final Duration MEDIUM = Duration.millis(250);
    public static final Duration SLOW = Duration.millis(360);
    public static final Duration STAGGER = Duration.millis(70);
    public static final Duration TOAST_VISIBLE = Duration.millis(1200);

    public static final Interpolator EMPHASIS = Interpolator.EASE_BOTH;
    public static final Interpolator EXIT = Interpolator.EASE_IN;
    public static final Interpolator ENTER = Interpolator.EASE_OUT;

    public static final int MAX_ANIMATION_KEYS_PER_NODE = 10;
    private static volatile Preset preset = Preset.BALANCED;
    private static volatile boolean reducedMotion;

    private UiMotionProfile() {
    }

    public static void setPreset(Preset value) {
        if (value != null) {
            preset = value;
        }
    }

    public static Preset getPreset() {
        return preset;
    }

    public static void setReducedMotion(boolean reduced) {
        reducedMotion = reduced;
    }

    public static boolean isReducedMotion() {
        return reducedMotion;
    }

    public static Duration fast() {
        return scale(FAST);
    }

    public static Duration medium() {
        return scale(MEDIUM);
    }

    public static Duration slow() {
        return scale(SLOW);
    }

    public static Duration stagger() {
        return scale(STAGGER);
    }

    public static Duration toastVisible() {
        return scale(TOAST_VISIBLE);
    }

    public static double movementScale() {
        return reducedMotion ? 0.35 : preset.movementScale;
    }

    public static double durationScale() {
        return reducedMotion ? 0.58 : preset.durationScale;
    }

    private static Duration scale(Duration base) {
        return base.multiply(durationScale());
    }
}
