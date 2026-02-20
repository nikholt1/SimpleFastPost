package org.example.simplefastpost.cli.folderRepo;

import java.sql.*;

public class CLIFolderRepository {

    private final String DB_URL;

    public CLIFolderRepository(String DB_URL) {
        this.DB_URL = DB_URL;
    }

    public void createFolder(String projectName) throws SQLException {
        String sql = "INSERT INTO folder(project_name) VALUES (?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, projectName);
            stmt.executeUpdate();
            System.out.println("Folder created: " + projectName);
        }
    }

    /* READ ALL */
    public void listFolders() throws SQLException {
        String sql = "SELECT id, project_name FROM folder";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                System.out.printf(
                        "ID: %d | Name: %s%n",
                        rs.getLong("id"),
                        rs.getString("project_name")
                );
            }
        }
    }

    /* READ ONE */
    public void getFolderById(long id) throws SQLException {
        String sql = "SELECT id, project_name FROM folder WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.printf(
                            "ID: %d | Name: %s%n",
                            rs.getLong("id"),
                            rs.getString("project_name")
                    );
                } else {
                    System.out.println("Folder not found: " + id);
                }
            }
        }
    }

    /* UPDATE */
    public void updateFolder(long id, String newName) throws SQLException {
        String sql = "UPDATE folder SET project_name = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newName);
            stmt.setLong(2, id);

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                System.out.println("Folder not found: " + id);
            } else {
                System.out.println("Folder updated: " + id);
            }
        }
    }

    /* DELETE */
    public void deleteFolder(long id) throws SQLException {
        String sql = "DELETE FROM folder WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                System.out.println("Folder not found: " + id);
            } else {
                System.out.println("Folder deleted: " + id);
            }
        }
    }
}
