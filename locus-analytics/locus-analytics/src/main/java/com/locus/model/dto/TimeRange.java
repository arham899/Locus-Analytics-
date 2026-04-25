package com.locus.model.dto;

import java.time.LocalDate;

/**
 * Enum representing predefined time ranges for price trend analysis (UC-6).
 *
 * <p>Use {@link #getFromDate()} and {@link #getToDate()} to resolve the range.
 * For {@code CUSTOM}, set {@code customFrom} and {@code customTo} before calling those methods.</p>
 */
public enum TimeRange {

    ONE_YEAR(1),
    THREE_YEARS(3),
    FIVE_YEARS(5),
    CUSTOM(0);

    private final int years;
    private LocalDate customFrom;
    private LocalDate customTo;

    TimeRange(int years) {
        this.years = years;
    }

    /**
     * Resolves the start date of this time range.
     * For predefined ranges, computes from today minus N years.
     * For CUSTOM, returns the user-specified fromDate.
     */
    public LocalDate getFromDate() {
        if (this == CUSTOM) {
            return customFrom;
        }
        return LocalDate.now().minusYears(years);
    }

    /**
     * Resolves the end date of this time range.
     * For predefined ranges, returns today.
     * For CUSTOM, returns the user-specified toDate.
     */
    public LocalDate getToDate() {
        if (this == CUSTOM) {
            return customTo;
        }
        return LocalDate.now();
    }

    public LocalDate getCustomFrom() {
        return customFrom;
    }

    public void setCustomFrom(LocalDate customFrom) {
        this.customFrom = customFrom;
    }

    public LocalDate getCustomTo() {
        return customTo;
    }

    public void setCustomTo(LocalDate customTo) {
        this.customTo = customTo;
    }

    public int getYears() {
        return years;
    }
}
