package org.example.simplefastpost.setup;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import java.io.File;


public class ApplicationSetup {

    public ApplicationSetup() {}

    public void initialize(String DB_URL) {
//        String DB_URL = "";
        String path = DB_URL.replace("jdbc:sqlite:", "");
        File dbFile = new File(path);

        if (dbFile.exists()) {
            return;
        } else {
            System.out.println("DB file not found, producing new...");
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                System.out.println("No db found, creating new");
                try (Statement stmt = conn.createStatement()) {

                    // Folder table
                    stmt.execute("""
                    CREATE TABLE IF NOT EXISTS folder (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        project_name TEXT NOT NULL
                    )
                """);
                    System.out.println("Table created");

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
                    System.out.println("Table created");
                    stmt.execute("""
                    CREATE TABLE IF NOT EXISTS apiInstance (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        pid INTEGER NOT NULL,
                        created TEXT DEFAULT (datetime('now','localtime'))
                    )
                """);
                    System.out.println("Table created");
                    stmt.execute("""
                    CREATE TABLE IF NOT EXISTS headers (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        command_id INTEGER NOT NULL,
                        key TEXT NOT NULL,
                        value TEXT,
                        FOREIGN KEY(command_id) REFERENCES command(id) ON DELETE CASCADE
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
