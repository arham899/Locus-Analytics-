package com.locus;

import com.locus.config.DBConnection;

public class TestConnection {
    public static void main(String[] args) throws Exception {
        var conn = DBConnection.getDataSource().getConnection();
        System.out.println("DB Connected Successfully!");
        conn.close();
    }
}