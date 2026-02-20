package org.example.simplefastpost.setup;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import java.io.File;


public class ApplicationSetup {

    public ApplicationSetup() {}

    public static void initialize(String DB_URL) {
        String path = DB_URL.replace("jdbc:sqlite:", "");
        File dbFile = new File(path);

        if (dbFile.exists()) {
            return;
        } else {
            System.out.println("DB file not found, producing new...");
            try (Connection conn = DriverManager.getConnection(DB_URL)) {

                try (Statement stmt = conn.createStatement()) {

                    // Folder table
                    stmt.execute("""
                    CREATE TABLE IF NOT EXISTS folder (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        project_name TEXT NOT NULL
                    )
                """);

                    // Command table
                    stmt.execute("""
                    CREATE TABLE IF NOT EXISTS command (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL UNIQUE,
                        command TEXT NOT NULL,
                        body TEXT,
                        folder_id INTEGER,
                        FOREIGN KEY (folder_id) REFERENCES folder(id)
                    )
                """);

                    System.out.println("Database created");

                }

            } catch (SQLException e) {
                throw new RuntimeException("Failed to initialize database", e);
            }
        }

    }
}
