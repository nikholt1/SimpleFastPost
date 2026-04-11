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
        expected_response TEXT,
        expected_status VARCHAR(255),
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
                id INTEGER,
                action VARCHAR(255),
                command_id BIGINT,
                created TIMESTAMP,
                response VARCHAR(255),
                status VARCHAR(255),
                url VARCHAR(255),
                PRIMARY KEY (id)
            );
        """);

            stmt.execute("""
CREATE TABLE IF NOT EXISTS crawler_session (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title VARCHAR(255),
    created TIMESTAMP
);
""");

            stmt.execute("""
CREATE TABLE IF NOT EXISTS crawler_entity (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title VARCHAR(255),

    parent_id BIGINT,
    session_id BIGINT NOT NULL,

    type VARCHAR(50),
    url VARCHAR(500),

    method VARCHAR(50),
    response TEXT,

    process_time INT,

    FOREIGN KEY (session_id)
        REFERENCES crawler_session(id)
        ON DELETE CASCADE,

    FOREIGN KEY (parent_id)
        REFERENCES crawler_entity(id)
        ON DELETE CASCADE
);
""");

            stmt.execute("""
CREATE TABLE IF NOT EXISTS crawler_connections (
    entity_id INTEGER,
    connection VARCHAR(500),

    PRIMARY KEY (entity_id, connection),

    FOREIGN KEY (entity_id)
        REFERENCES crawler_entity(id)
        ON DELETE CASCADE
);
""");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }


}
