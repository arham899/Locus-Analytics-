package com.locus.model.dto;

import java.util.Collections;
import java.util.List;

/**
 * Generic wrapper for paginated query results.
 *
 * <p>Used by {@code SearchService} and any DAO method that returns paged data.
 * Computes {@code totalPages} from {@code totalCount} and {@code pageSize}.</p>
 *
 * @param <T> the type of items in this page (e.g. Property, Valuation)
 */
public class PagedResult<T> {

    private List<T> items;
    private long totalCount;
    private int pageNumber;
    private int pageSize;

    // ── Constructors ──────────────────────────────────────────────────

    public PagedResult() {
        this.items = Collections.emptyList();
    }

    public PagedResult(List<T> items, long totalCount, int pageNumber, int pageSize) {
        this.items = items;
        this.totalCount = totalCount;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Computes total number of pages from totalCount and pageSize.
     */
    public int getTotalPages() {
        if (pageSize <= 0) return 0;
        return (int) Math.ceil((double) totalCount / pageSize);
    }

    /**
     * Returns true if there is a next page.
     */
    public boolean hasNextPage() {
        return pageNumber < getTotalPages();
    }

    /**
     * Returns true if there is a previous page.
     */
    public boolean hasPreviousPage() {
        return pageNumber > 1;
    }

    @Override
    public String toString() {
        return "PagedResult{" +
                "page=" + pageNumber + "/" + getTotalPages() +
                ", items=" + (items != null ? items.size() : 0) +
                ", total=" + totalCount +
                '}';
    }
}
