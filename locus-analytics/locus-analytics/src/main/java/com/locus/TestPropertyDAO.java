package com.locus;

import com.locus.dao.PropertyDAO;
import com.locus.dao.impl.PropertyDAOImpl;
import com.locus.model.Property;
import com.locus.model.dto.SearchFilter;

import java.util.List;

public class TestPropertyDAO {

    public static void main(String[] args) {

        // CREATE DAO
        PropertyDAO dao = new PropertyDAOImpl();

        // ─────────────────────────────
        // TEST INSERT
        // ─────────────────────────────
        Property p = new Property();
        p.setPropertyId("P2001");
        p.setCity("Lahore");
        p.setLocality("DHA");
        p.setPropertyType("House");
        p.setArea(10);
        p.setPrice(10000000);
        p.setBedrooms(4);
        p.setBathrooms(3);

        boolean inserted = dao.insert(p);
        System.out.println("Inserted: " + inserted);

        // ─────────────────────────────
        // TEST UPDATE
        // ─────────────────────────────
        p.setPrice(12000000); // updating price

        boolean updated = dao.update(p);
        System.out.println("Updated: " + updated);

        // ─────────────────────────────
        // TEST SEARCH
        // ─────────────────────────────
        SearchFilter f = new SearchFilter();
        f.setCity("Lahore");
        f.setPageNumber(1);
        f.setPageSize(5);

        List<Property> results = dao.search(f);

        System.out.println("\nSearch Results:");
        for (Property prop : results) {
            System.out.println(prop.getPropertyId() + " | " + prop.getPrice());
        }

        // ─────────────────────────────
        // TEST DELETE
        // ─────────────────────────────
        boolean deleted = dao.delete("P2001");
        System.out.println("\nDeleted: " + deleted);

        // ─────────────────────────────
// TEST AGGREGATION (STEP 4)
// ─────────────────────────────
        System.out.println("\n--- Aggregation Test ---");

        List<com.locus.model.dto.TrendPoint> trends =
                dao.findAggregatedByMonth(
                        "Lahore",      // city
                        "DHA",         // locality
                        "House",       // propertyType
                        java.time.LocalDate.of(2024, 1, 1), // startDate
                        java.time.LocalDate.of(2026, 12, 31) // endDate
                );

        for (com.locus.model.dto.TrendPoint t : trends) {
            System.out.println(
                    t.getPeriod()
                            + " | avgPrice=" + t.getAveragePrice()
                            + " | count=" + t.getListingCount()
            );
        }
    }
}