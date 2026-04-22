package com.locus.dao;

/**
 * Custom exception for all data access layer failures.
 * Wraps lower-level exceptions (e.g., SQL exceptions)
 * to keep DAO contracts clean and consistent.
 */
public class DataAccessException extends RuntimeException {

    /**
     * Constructs a new DataAccessException.
     *
     * @param message explanation of the error
     * @param cause underlying exception (e.g., SQLException)
     */
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}