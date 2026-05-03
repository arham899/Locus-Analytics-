package com.locus.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.InputStream;
import java.util.Properties;

/**
 * Manages two HikariCP connection pools (PostgreSQL + Oracle) with the
 * ability to switch the active database at runtime.
 *
 * Both databases share the same schema and store the same data.
 * The application can toggle between them at any time via
 * {@link #switchTo(DatabaseType)}.
 *
 * All DAOs call {@link #getDataSource()} which always returns
 * whichever database is currently active.
 */
public class DBConnection {

    public enum DatabaseType {
        POSTGRESQL, ORACLE
    }

    private static HikariDataSource pgDataSource;
    private static HikariDataSource oracleDataSource;

    /** The currently active database — all DAOs read from this. */
    private static volatile DatabaseType activeDatabase = DatabaseType.POSTGRESQL;

    static {
        Properties props = new Properties();
        try {
            InputStream input = DBConnection.class
                    .getClassLoader()
                    .getResourceAsStream("config.properties");

            if (input == null) {
                System.err.println("[DBConnection] CRITICAL: config.properties not found!");
            } else {
                props.load(input);
            }
        } catch (Exception e) {
            System.err.println("[DBConnection] Error loading properties: " + e.getMessage());
        }

        // ── PostgreSQL Pool ──────────────────────────
        try {
            HikariConfig pgConfig = new HikariConfig();
            pgConfig.setPoolName("PostgreSQL-Pool");
            pgConfig.setJdbcUrl(props.getProperty("pg.db.url"));
            pgConfig.setUsername(props.getProperty("pg.db.username"));
            pgConfig.setPassword(props.getProperty("pg.db.password"));
            pgConfig.setMaximumPoolSize(
                    Integer.parseInt(props.getProperty("db.pool.maximumPoolSize"))
            );

            pgConfig.setConnectionTimeout(
                    Long.parseLong(props.getProperty("db.pool.connectionTimeout"))
            );

            pgConfig.setIdleTimeout(
                    Long.parseLong(props.getProperty("db.pool.idleTimeout"))
            );

// keep this if you want (optional)
            pgConfig.setMinimumIdle(2);

            pgDataSource = new HikariDataSource(pgConfig);
            System.out.println("[DBConnection] PostgreSQL Pool initialized successfully.");
        } catch (Exception e) {
            System.err.println("[DBConnection] WARNING: PostgreSQL initialization failed: " + e.getMessage());
        }

        // ── Oracle Pool ──────────────────────────────
        try {
            HikariConfig oracleConfig = new HikariConfig();
            oracleConfig.setPoolName("Oracle-Pool");
            oracleConfig.setJdbcUrl(props.getProperty("oracle.db.url"));
            oracleConfig.setUsername(props.getProperty("oracle.db.username"));
            oracleConfig.setPassword(props.getProperty("oracle.db.password"));
            oracleConfig.setMaximumPoolSize(
                    Integer.parseInt(props.getProperty("db.pool.maximumPoolSize"))
            );

            oracleConfig.setConnectionTimeout(
                    Long.parseLong(props.getProperty("db.pool.connectionTimeout"))
            );

            oracleConfig.setIdleTimeout(
                    Long.parseLong(props.getProperty("db.pool.idleTimeout"))
            );

// keep this
            oracleConfig.setMinimumIdle(2);

            oracleDataSource = new HikariDataSource(oracleConfig);
            System.out.println("[DBConnection] Oracle Pool initialized successfully.");
        } catch (Exception e) {
            System.err.println("[DBConnection] WARNING: Oracle initialization failed: " + e.getMessage());
            System.err.println("[DBConnection] Continuing without Oracle support...");
        }

        // Set default active database
        if (pgDataSource != null) {
            activeDatabase = DatabaseType.POSTGRESQL;
        } else if (oracleDataSource != null) {
            activeDatabase = DatabaseType.ORACLE;
        }
    }

    private DBConnection() {
        // prevent instantiation
    }

    // ─────────────────────────────────────────────
    //  Switch active database
    // ─────────────────────────────────────────────

    /**
     * Switches the active database that all DAOs will use.
     * This can be called at any time — the next DAO call will
     * use the new database.
     *
     * @param type the database to switch to
     */
    public static void switchTo(DatabaseType type) {
        activeDatabase = type;
        System.out.println("[DBConnection] Switched active database to: " + type);
    }

    /**
     * Returns the currently active database type.
     */
    public static DatabaseType getActiveDatabaseType() {
        return activeDatabase;
    }

    // ─────────────────────────────────────────────
    //  Data source accessors
    // ─────────────────────────────────────────────

    /**
     * Returns the currently active DataSource.
     * All DAOs should use this method.
     */
    public static DataSource getDataSource() {
        return switch (activeDatabase) {
            case POSTGRESQL -> pgDataSource;
            case ORACLE     -> oracleDataSource;
        };
    }

    /**
     * Returns the PostgreSQL DataSource directly (bypass active toggle).
     */
    public static DataSource getPostgresDataSource() {
        return pgDataSource;
    }

    /**
     * Returns the Oracle DataSource directly (bypass active toggle).
     */
    public static DataSource getOracleDataSource() {
        return oracleDataSource;
    }

    /**
     * Gracefully shuts down both connection pools.
     * Call this on application exit.
     */
    public static void shutdown() {
        if (pgDataSource != null && !pgDataSource.isClosed()) {
            pgDataSource.close();
        }
        if (oracleDataSource != null && !oracleDataSource.isClosed()) {
            oracleDataSource.close();
        }
    }
}