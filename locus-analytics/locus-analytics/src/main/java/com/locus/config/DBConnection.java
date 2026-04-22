package com.locus.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.InputStream;
import java.util.Properties;

public class DBConnection {

    private static HikariDataSource dataSource;

    static {
        try {
            Properties props = new Properties();

            InputStream input = DBConnection.class
                    .getClassLoader()
                    .getResourceAsStream("config.properties");

            if (input == null) {
                throw new RuntimeException("config.properties not found in resources folder");
            }

            props.load(input);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.username"));
            config.setPassword(props.getProperty("db.password"));

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);

            dataSource = new HikariDataSource(config);

        } catch (Exception e) {
            throw new RuntimeException("Database connection initialization failed", e);
        }
    }

    private DBConnection() {
        // prevent instantiation
    }

    public static HikariDataSource getDataSource() {
        return dataSource;
    }
}