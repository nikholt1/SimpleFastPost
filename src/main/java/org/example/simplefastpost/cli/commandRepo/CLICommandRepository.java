package org.example.simplefastpost.cli.commandRepo;

import org.example.simplefastpost.model.Command;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

        }
    }

    public List<Command> getAllCommands() throws SQLException {
        List<Command> commands = new ArrayList<>();

        String sql = """
        SELECT id, name, command, body, folder_id
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

                commands.add(cmd);
            }
        }

        return commands;
    }

    public void listCommands() throws SQLException {
        String sql = """
        SELECT id, name, command, body, folder_id
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
    public Command getCommandByName(String commandName) throws SQLException {

        String sql = """
        SELECT 
            c.id,
            c.name,
            c.command,
            c.body,
            c.folder_id,
            f.project_name AS folder_name
        FROM command c
        LEFT JOIN folder f ON c.folder_id = f.id
        WHERE c.name LIKE ?
        ORDER BY c.name
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + commandName + "%");

            try (ResultSet rs = stmt.executeQuery()) {

                Command command = null;
                boolean multiple = false;

                while (rs.next()) {
                    if (command == null) {
                        command = new Command();
                        command.setId(rs.getLong("id"));
                        command.setName(rs.getString("name"));
                        command.setCommand(rs.getString("command"));
                        command.setBody(rs.getString("body"));
                        command.setFolderId(rs.getLong("folder_id"));
                    } else {
                        multiple = true;
                    }
                }

                if (command == null) {
                    return null;
                }

                if (multiple) {
                    System.out.println(
                            "Warning: multiple commands matched '" + commandName +
                                    "', using first result: " + command.getName()
                    );
                }

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
