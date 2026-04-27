package com.locus;

import com.locus.dao.UserDAO;
import com.locus.dao.impl.UserDAOImpl;
import com.locus.model.PropertyAnalyst;
import com.locus.model.User;

import java.util.List;
import java.util.UUID;

public class UserDAOTest {

    public static void main(String[] args) {

        UserDAO dao = new UserDAOImpl();

        // UNIQUE ID EVERY RUN (fixes duplicate error permanently)
        String testId = "AL_" + UUID.randomUUID().toString().substring(0, 5);

        System.out.println("===== INSERT TEST =====");

        PropertyAnalyst u = new PropertyAnalyst();
        u.setUserId(testId);
        u.setName("Test Analyst");
        u.setEmail(testId + "@locus.com"); // unique email too
        u.setRole("analyst");
        u.setPasswordHash("12345");

        boolean inserted = dao.insert(u);
        System.out.println("Inserted: " + inserted);

        // ─────────────────────────────
        System.out.println("\n===== FIND BY EMAIL =====");

        User found = dao.findByEmail(u.getEmail());

        if (found != null) {
            System.out.println("FOUND: " + found.getUserId() + " | " + found.getRole());
        } else {
            System.out.println("User NOT found");
        }

        // ─────────────────────────────
        System.out.println("\n===== UPDATE TEST =====");

        u.setName("Updated Analyst");
        boolean updated = dao.update(u);
        System.out.println("Updated: " + updated);

        // Verify update
        User updatedUser = dao.findById(testId);
        System.out.println("Updated Name: " + updatedUser.getName());

        // ─────────────────────────────
        System.out.println("\n===== FIND ALL =====");

        List<User> users = dao.findAll();
        for (User x : users) {
            System.out.println(x.getUserId() + " | " + x.getEmail());
        }

        // ─────────────────────────────
        System.out.println("\n===== DELETE TEST =====");

        boolean deleted = dao.delete(testId);
        System.out.println("Deleted: " + deleted);

        // Verify delete
        User deletedCheck = dao.findById(testId);
        System.out.println("Exists after delete: " + (deletedCheck != null));

        System.out.println("\n===== TEST COMPLETE =====");
    }
}