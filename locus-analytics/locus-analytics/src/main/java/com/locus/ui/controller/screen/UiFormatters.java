package com.locus.ui.controller.screen;

import java.text.NumberFormat;
import java.util.Locale;

public final class UiFormatters {

    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(new Locale("en", "PK"));

    private UiFormatters() {
    }

    public static String currency(double value) {
        return CURRENCY.format(value);
    }

    public static String number(double value) {
        return String.format(Locale.US, "%.2f", value);
    }
}
