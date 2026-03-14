package org.example.simplefastpost.cli.commandRepo;

import org.example.simplefastpost.model.Headers;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HeaderRepository {

    private final String DB_URL;


    public HeaderRepository(String DB_URL) {
        this.DB_URL = DB_URL;
    }

    public void createHeader(Headers header) throws SQLException {
        System.out.println(DB_URL);
        String sql = """
            INSERT INTO headers (command_id, key, value)
            VALUES (?, ?, ?)
            """;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, header.getCommandId());
            stmt.setString(2, header.getKey());
            stmt.setString(3, header.getValue());

            // THIS LINE IS MISSING
            stmt.executeUpdate();
        }
        System.out.println("saved header " + header.getKey());
    }
    public Headers getHeader(Long headerId) throws SQLException {
        String sql = "SELECT * FROM headers WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, headerId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Headers header = new Headers();
                    header.setId(rs.getLong("id"));
                    header.setCommandId(rs.getLong("command_id"));
                    header.setKey(rs.getString("key"));
                    header.setValue(rs.getString("value"));

                    return header;
                } else {

                    return null;
                }
            }
        }
    }

    public List<Headers> getAllHeadersFromCommandId(Long commandId) throws SQLException {
        List<Headers> headers = new ArrayList<>();

        String sql = "SELECT * FROM headers WHERE command_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, commandId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Headers header = new Headers();
                    header.setId(rs.getLong("id"));
                    header.setCommandId(rs.getLong("command_id"));
                    header.setKey(rs.getString("key"));
                    header.setValue(rs.getString("value"));

                    headers.add(header);
                }
            }
        }

        return headers;
    }


    public Headers updateHeader(Headers header) throws SQLException {
        String sql = "UPDATE headers SET key = ?, value = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, header.getKey());
            stmt.setString(2, header.getValue());
            stmt.setLong(3, header.getId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Updating header failed, no rows affected.");
            }

            // Return the same header object after update
            return header;
        }
    }
    public void deleteHeader(Headers header) throws SQLException {
        String sql = "DELETE FROM headers WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, header.getId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Deleting header failed, no rows affected.");
            }
        }
    }
}
