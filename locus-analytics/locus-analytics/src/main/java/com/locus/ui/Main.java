package com.locus.ui;

/**
 * A workaround launcher class to fix the "JavaFX runtime components are missing" error
 * when running JavaFX 11+ from an IDE like Eclipse without module-path VM arguments.
 */
public class Main {
    public static void main(String[] args) {
        LocusAnalyticsApp.main(args);
    }
}
