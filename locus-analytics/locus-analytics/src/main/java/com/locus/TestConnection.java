package com.locus;

import com.locus.config.DBConnection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestConnection {
    public static void main(String[] args) {
        try (var conn = DBConnection.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("DB Connected Successfully!");

            // Better Test: Check if we can read the property table
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM property");
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Total Properties in Database: " + count);
                
                if (count == 0) {
                    System.out.println("Warning: Your database is connected but the tables are EMPTY.");
                } else {
                    System.out.println("Success! Data is loaded and ready.");
                }
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
