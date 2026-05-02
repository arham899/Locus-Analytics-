package com.locus;

import com.locus.dao.SystemConfigurationDAO;
import com.locus.dao.impl.SystemConfigurationDAOImpl;
import com.locus.model.SystemConfiguration;

import java.time.LocalDateTime;
import java.util.List;

public class TestSystemConfigurationDAO {

    public static void main(String[] args) {

        SystemConfigurationDAO dao = new SystemConfigurationDAOImpl();

        // ─────────────────────────────
        // 1. GET CONFIG TEST
        // ─────────────────────────────
        System.out.println("GET CONFIG TEST");

        SystemConfiguration cfg = dao.getConfig("CFG001");

        if (cfg != null) {
            System.out.println(
                    cfg.getConfigId() + " | " +
                            cfg.getDbHost() + " | " +
                            cfg.getZameenScrapeInterval()
            );
        } else {
            System.out.println("CONFIG NOT FOUND");
        }

        // ─────────────────────────────
        // 2. UPDATE TEST
        // ─────────────────────────────
        System.out.println("\nUPDATE TEST");

        if (cfg != null) {

            cfg.setDbHost("127.0.0.1:5432");
            cfg.setZameenScrapeInterval("12h");
            cfg.setModelFilePath("/models/v2.pkl");

            boolean updated = dao.update(cfg);

            System.out.println("UPDATED = " + updated);
        }

        // ─────────────────────────────
        // 3. GET ALL TEST
        // ─────────────────────────────
        System.out.println("\nGET ALL TEST");

        List<SystemConfiguration> list = dao.getAllConfigs();

        for (SystemConfiguration c : list) {
            System.out.println(
                    c.getConfigId() + " | " +
                            c.getAdminId() + " | " +
                            c.getDbHost() + " | " +
                            c.getZameenScrapeInterval()
            );
        }
    }
}