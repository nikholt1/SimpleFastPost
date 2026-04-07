package org.example.simplefastpost.cli.commandRepo;

import org.example.simplefastpost.model.Command;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CLICommandRepository {

    private final String DB_URL;

    public CLICommandRepository(String DB_URL) {
        this.DB_URL = DB_URL;
    }

    public Command createCommand(
            String name,
            String command,
            String body,
            Long folderId
    ) throws SQLException {

        if (commandNameExists(name)) {
            System.out.println("Command name already exists: " + name);
            return null;
        }

        String sql = """
        INSERT INTO command (name, command, body, folder_id, expected_response, expected_status)
        VALUES (?, ?, ?, ?, ?, ?)
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, name);
            stmt.setString(2, command);
            stmt.setString(3, body);

            if (folderId != null) {
                stmt.setLong(4, folderId);
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            stmt.setString(5, null);
            stmt.setString(6, null);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating command failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);

                    Command createdCommand = new Command();
                    createdCommand.setId(id);
                    createdCommand.setName(name);
                    createdCommand.setCommand(command);
                    createdCommand.setBody(body);
                    createdCommand.setFolderId(folderId);
                    createdCommand.setExpected_response(null);
                    createdCommand.setExpected_status(null);

                    return createdCommand;
                } else {
                    throw new SQLException("Creating command failed, no ID obtained.");
                }
            }
        }
    }
    public Command createCommandWithExpected(
            String name,
            String command,
            String body,
            Long folderId,
            String expectedResponse,
            String expectedStatus
    ) throws SQLException {

        if (commandNameExists(name)) {
            System.out.println("Command name already exists: " + name);
            return null;
        }

        String sql = """
    INSERT INTO command (name, command, body, folder_id, expected_response, expected_status)
    VALUES (?, ?, ?, ?, ?, ?)
""";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, name);
            stmt.setString(2, command);
            stmt.setString(3, body);

            if (folderId != null) {
                stmt.setLong(4, folderId);
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            stmt.setString(5, expectedResponse);
            stmt.setString(6, expectedStatus);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating command failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);

                    Command createdCommand = new Command();
                    createdCommand.setId(id);
                    createdCommand.setName(name);
                    createdCommand.setCommand(command);
                    createdCommand.setBody(body);
                    createdCommand.setFolderId(folderId);
                    createdCommand.setExpected_response(expectedResponse);
                    createdCommand.setExpected_status(expectedStatus);

                    return createdCommand;
                } else {
                    throw new SQLException("Creating command failed, no ID obtained.");
                }
            }
        }
    }
    public List<Command> getAllCommands() throws SQLException {
        List<Command> commands = new ArrayList<>();

        String sql = """
        SELECT id, name, command, body, folder_id, expected_response, expected_status
        FROM command
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

                long id = rs.getLong("id");
                String name = rs.getString("name");
                String command = rs.getString("command");
                String body = rs.getString("body");

                Object folderObj = rs.getObject("folder_id");
                Long folderId = folderObj == null ? null : ((Number) folderObj).longValue();

                Command cmd = new Command();
                cmd.setId(id);
                cmd.setName(name);
                cmd.setCommand(command);
                cmd.setBody(body);
                cmd.setFolderId(folderId);
                cmd.setExpected_response(rs.getString("expected_response"));
                cmd.setExpected_status(rs.getString("expected_status"));

                commands.add(cmd);
            }
        }

        return commands;
    }

    public void listCommands() throws SQLException {
        String sql = """
        SELECT id, name, command, body, folder_id, expected_response, expected_status
        FROM command
    """;
        boolean found = false;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                found = true;

                String name = rs.getString("name");
                String command = rs.getString("command");
                String body = rs.getString("body");
                Object folderId = rs.getObject("folder_id");

                System.out.printf("Name: %s | Command: %s | Folder: %s%n", name, command, folderId);

                if (body != null && !body.isBlank()) {
                    String[] lines = body.split("\\R");
                    for (String line : lines) {
                        System.out.println("---> " + line);
                    }
                } else {
                    System.out.println();
                }
            }
        }

        if (!found) {
            System.out.println("No commands found.");
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
            SET name = ?, command = ?, body = ?, folder_id = ?, expected_response = ?, expected_status = ?
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

            stmt.setString(5, null);
            stmt.setString(6, null);
            stmt.setLong(7, id);

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

    public Command getCommandByName(String commandName) throws SQLException {

        String sql = """
    SELECT 
        c.id,
        c.name,
        c.command,
        c.body,
        c.folder_id,
        c.expected_response,
        c.expected_status,
        f.project_name AS folder_name
    FROM command c
    LEFT JOIN folder f ON c.folder_id = f.id
    WHERE c.name = ?
""";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, commandName);

            try (ResultSet rs = stmt.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                Command command = new Command();
                command.setId(rs.getLong("id"));
                command.setName(rs.getString("name"));
                command.setCommand(rs.getString("command"));
                command.setBody(rs.getString("body"));
                command.setFolderId(rs.getLong("folder_id"));
                command.setExpected_response(rs.getString("expected_response"));
                command.setExpected_status(rs.getString("expected_status"));

                return command;
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

    public List<Command> getRootCommands() throws SQLException {
        String sql = """
        SELECT id, name, command, body
        FROM command
        WHERE folder_id IS NULL
        ORDER BY name
    """;

        List<Command> commands = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Command cmd = new Command();
                cmd.setId(rs.getLong("id"));
                cmd.setName(rs.getString("name"));
                cmd.setCommand(rs.getString("command"));
                cmd.setBody(rs.getString("body"));

                commands.add(cmd);
            }
        }

        return commands;
    }

    public void deleteAllCommands() throws SQLException {

        String sql = "DELETE FROM command";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int deleted = stmt.executeUpdate();
            System.out.println("Deleted commands: " + deleted);
        }
    }
}