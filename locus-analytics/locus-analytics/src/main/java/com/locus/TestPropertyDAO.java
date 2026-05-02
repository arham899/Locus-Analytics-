package com.locus;

import com.locus.dao.PropertyDAO;
import com.locus.dao.impl.PropertyDAOImpl;
import com.locus.model.Property;
import com.locus.model.dto.SearchFilter;

import java.time.LocalDate;
import java.util.List;

public class TestPropertyDAO {

    public static void main(String[] args) {

        PropertyDAO dao = new PropertyDAOImpl();

        System.out.println("\n--- CLEAN START (DELETE OLD DATA) ---");

        dao.delete("P3001");
        dao.delete("P3002");
        dao.delete("P3003");
        dao.delete("P3004");
        dao.delete("P3005");

        // ─────────────────────────────
        // FIXED TEST DATA (NO RANDOMNESS)
        // ─────────────────────────────
        Property p1 = create("P3001", "Lahore", "DHA", 10000000, 2022);
        Property p2 = create("P3002", "Lahore", "DHA", 12000000, 2023);
        Property p3 = create("P3003", "Lahore", "DHA", 15000000, 2024);
        Property p4 = create("P3004", "Lahore", "DHA", 18000000, 2025);
        Property p5 = create("P3005", "Lahore", "DHA", 20000000, 2025);

        dao.insert(p1);
        dao.insert(p2);
        dao.insert(p3);
        dao.insert(p4);
        dao.insert(p5);

        System.out.println("\nInserted 5 test properties\n");

        // ─────────────────────────────
        // SEARCH TEST
        // ─────────────────────────────
        SearchFilter f = new SearchFilter();
        f.setCity("Lahore");
        f.setPageNumber(1);
        f.setPageSize(5);

        List<Property> results = dao.search(f);

        System.out.println("--- SEARCH ---");
        for (Property prop : results) {
            System.out.println(prop.getPropertyId() + " | " + prop.getPrice());
        }

        System.out.println("\n--- COUNT TEST ---");

        SearchFilter countFilter = new SearchFilter();
        countFilter.setCity("Lahore");

        int total = dao.countByFilter(countFilter);

        System.out.println("Total properties in Lahore: " + total);

        // ─────────────────────────────
        // LOCALITY METRICS TEST
        // ─────────────────────────────
        System.out.println("\n--- LOCALITY METRICS ---");

        var metrics = dao.getLocalityMetrics(
                "Lahore",
                3,
                1
        );

        for (var m : metrics) {
            System.out.println(
                    m.getLocality()
                            + " | app=" + m.getPriceAppreciation()
                            + "% | vol=" + m.getVolumeGrowth() + "%"
            );
        }

        // ─────────────────────────────
        // AGGREGATION TEST
        // ─────────────────────────────
        System.out.println("\n--- AGGREGATION ---");

        var trends = dao.findAggregatedByMonth(
                "Lahore",
                "DHA",
                "House",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2026, 12, 31)
        );

        for (var t : trends) {
            System.out.println(
                    t.getPeriod()
                            + " | avg=" + t.getAveragePrice()
                            + " | count=" + t.getListingCount()
            );
        }

        // ─────────────────────────────
        // CLEANUP
        // ─────────────────────────────
        System.out.println("\n--- CLEANUP ---");

        dao.delete("P3001");
        dao.delete("P3002");
        dao.delete("P3003");
        dao.delete("P3004");
        dao.delete("P3005");

        System.out.println("Cleanup done");
        System.out.println("\n--- TEST COMPLETE ---");
    }

    // FIXED deterministic creator (NO RANDOMNESS)
    private static Property create(String id, String city, String locality, double price, int year) {
        Property p = new Property();

        p.setPropertyId(id);
        p.setCity(city);
        p.setLocality(locality);
        p.setPropertyType("House");
        p.setArea(10);
        p.setPrice(price);
        p.setBedrooms(3);
        p.setBathrooms(2);

        p.setListingDate(LocalDate.of(year, 1, 1));

        return p;
    }
}