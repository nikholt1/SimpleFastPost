package org.example.simplefastpost.cli.commandRepo;

import java.sql.*;

public class CLICommandRepository {


    private final String DB_URL;

    public CLICommandRepository(String DB_URL) {
        this.DB_URL = DB_URL;
        System.out.println(DB_URL + " in CommandRepository");

    }


    public void createCommand(
            String name,
            String command,
            String body,
            Long folderId
    ) throws SQLException {

        if (commandNameExists(name)) {
            System.out.println("Command name already exists: " + name);
            return;
        }

        String sql = """
        INSERT INTO command (name, command, body, folder_id)
        VALUES (?, ?, ?, ?)
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, command);
            stmt.setString(3, body);

            if (folderId != null) {
                stmt.setLong(4, folderId);
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            stmt.executeUpdate();
            System.out.println("Command created: " + name);
        }
    }

    public void listCommands() throws SQLException {
        String sql = """
            SELECT id, name, command, body, folder_id
            FROM command
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String name = rs.getString("name");
                String command = rs.getString("command");
                String body = rs.getString("body");
                Object folderId = rs.getObject("folder_id");

                System.out.printf("Name: %s | Command: %s | Folder: %s%n", name, command, folderId);

                if (body != null && !body.isBlank()) {
                    String[] lines = body.split("\\R"); // splits on any newline (\r, \n, \r\n)
                    for (String line : lines) {
                        System.out.println("---> " + line); // indented for readability
                    }
                } else {
                    System.out.println();
                }
            }
        }
    }

    public void deleteCommand(long id) throws SQLException {
        String sql = "DELETE FROM command WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int rows = stmt.executeUpdate();

            if (rows == 0) {
                System.out.println("No command found with id " + id);
            } else {
                System.out.println("Command deleted: " + id);
            }
        }
    }

    public void updateCommand(
            long id,
            String name,
            String command,
            String body,
            Long folderId
    ) throws SQLException {

        String sql = """
            UPDATE command
            SET name = ?, command = ?, body = ?, folder_id = ?
            WHERE id = ?
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, command);
            stmt.setString(3, body);

            if (folderId != null) {
                stmt.setLong(4, folderId);
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            stmt.setLong(5, id);

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                System.out.println("No command found with id " + id);
            } else {
                System.out.println("Command updated: " + id);
            }
        }
    }
    private boolean commandNameExists(String name) throws SQLException {
        String sql = "SELECT 1 FROM command WHERE name = ? LIMIT 1";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
    public void searchCommandsByName(String searchTerm) throws SQLException {

        String sql = """
        SELECT 
            c.id,
            c.name,
            c.command,
            c.body,
            f.project_name AS folder_name
        FROM command c
        LEFT JOIN folder f ON c.folder_id = f.id
        WHERE c.name LIKE ?
        ORDER BY c.name
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + searchTerm + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                boolean found = false;

                while (rs.next()) {
                    found = true;
                    System.out.printf(
                            "ID: %d | Name: %s | Cmd: %s | Folder: %s%n",
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("command"),
                            rs.getString("folder_name") != null
                                    ? rs.getString("folder_name")
                                    : "none"
                    );
                }

                if (!found) {
                    System.out.println("No commands found matching: " + searchTerm);
                }
            }
        }
    }
}
