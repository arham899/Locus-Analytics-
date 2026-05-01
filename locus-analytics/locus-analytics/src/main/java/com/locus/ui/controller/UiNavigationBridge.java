package com.locus.ui.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Small in-memory bridge for cross-screen navigation triggers and prefill payloads.
 */
public final class UiNavigationBridge {

    private static Consumer<String> screenOpener;
    private static String reportPropertyId;
    private static List<String> comparePropertyIds = new ArrayList<>();

    private UiNavigationBridge() {
    }

    public static void registerScreenOpener(Consumer<String> opener) {
        screenOpener = opener;
    }

    public static void openScreen(String screenKey) {
        if (screenOpener != null) {
            screenOpener.accept(screenKey);
        }
    }

    public static void setReportPropertyId(String propertyId) {
        reportPropertyId = propertyId;
    }

    public static String consumeReportPropertyId() {
        String value = reportPropertyId;
        reportPropertyId = null;
        return value;
    }

    public static void setComparePropertyIds(List<String> ids) {
        comparePropertyIds = ids == null ? new ArrayList<>() : new ArrayList<>(ids);
    }

    public static List<String> consumeComparePropertyIds() {
        List<String> values = new ArrayList<>(comparePropertyIds);
        comparePropertyIds.clear();
        return values;
    }
}
