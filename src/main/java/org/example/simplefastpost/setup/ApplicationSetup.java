package org.example.simplefastpost.setup;

import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import java.io.File;

@Component
public class ApplicationSetup {

    public ApplicationSetup() {}

    public void initialize(String DB_URL) {
        String path = DB_URL.replace("jdbc:sqlite:", "");
        File dbFile = new File(path);

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // Always create tables if they do not exist
            stmt.execute("""
            CREATE TABLE IF NOT EXISTS folder (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                project_name TEXT NOT NULL
            )
        """);

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

            stmt.execute("""
            CREATE TABLE IF NOT EXISTS apiInstance (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                pid INTEGER NOT NULL,
                created TEXT DEFAULT (datetime('now','localtime'))
            )
        """);

            stmt.execute("""
            CREATE TABLE IF NOT EXISTS headers (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                command_id INTEGER NOT NULL,
                key TEXT NOT NULL,
                value TEXT,
                FOREIGN KEY(command_id) REFERENCES command(id) ON DELETE CASCADE
            )
        """);

            stmt.execute("""
            CREATE TABLE IF NOT EXISTS history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                command_id INTEGER,
                created TEXT DEFAULT (datetime('now','localtime'))
                response TEXT,
                url TEXT
            )

        """);

            System.out.println("Database initialized: " + dbFile.getAbsolutePath());

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }


}
