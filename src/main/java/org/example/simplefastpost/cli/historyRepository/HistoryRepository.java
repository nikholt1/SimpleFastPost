package org.example.simplefastpost.cli.historyRepository;

import org.example.simplefastpost.model.History;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class HistoryRepository {

    private final String DB_URL;

    public HistoryRepository(String DB_URL) throws SQLException {

        this.DB_URL = DB_URL;
    }

    /* CREATE */
    public History createNewHistoryEntity(History history) throws SQLException {
        String sql = "INSERT INTO history(command_id, created, response, url, status, action) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, history.getCommandId());
            stmt.setObject(2, history.getCreated());  // LocalDateTime directly
            stmt.setString(3, history.getResponse());
            stmt.setString(4, history.getUrl());
            stmt.setString(5, history.getStatus());
            stmt.setString(6, history.getAction());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    history.setId(rs.getLong(1));
                }
            }
        }

        return history;
    }

    /* READ ONE */
    public History getHistoryById(Long id) throws SQLException {
        String sql = "SELECT * FROM history WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    History h = new History();
                    h.setId(rs.getLong("id"));
                    h.setCommandId(rs.getLong("command_id"));
                    h.setCreated(rs.getObject("created", LocalDateTime.class)); // <-- LocalDateTime
                    h.setResponse(rs.getString("response"));
                    h.setUrl(rs.getString("url"));
                    h.setStatus(rs.getString("status"));
                    h.setAction(rs.getString("action"));
                    return h;
                }
                return null;
            }
        }
    }

    /* READ ALL */
    public List<History> getAllHistoryEntities() throws SQLException {
        String sql = "SELECT * FROM history";
        List<History> list = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                History h = new History();
                h.setId(rs.getLong("id"));
                h.setCommandId(rs.getLong("command_id"));
                h.setCreated(rs.getObject("created", LocalDateTime.class));
                h.setResponse(rs.getString("response"));
                h.setUrl(rs.getString("url"));
                h.setStatus(rs.getString("status"));
                h.setAction(rs.getString("action"));
                list.add(h);
            }
        }

        return list;
    }

    /* READ BY command_id */
    public List<History> getHistoryByCommandId(Long commandId) throws SQLException {
        String sql = "SELECT * FROM history WHERE command_id = ?";
        List<History> list = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, commandId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    History h = new History();
                    h.setId(rs.getLong("id"));
                    h.setCommandId(rs.getLong("command_id"));
                    h.setCreated(rs.getObject("created", LocalDateTime.class));
                    h.setResponse(rs.getString("response"));
                    h.setUrl(rs.getString("url"));
                    h.setStatus(rs.getString("status"));
                    h.setAction(rs.getString("action"));
                    list.add(h);
                }
            }
        }

        return list;
    }

    /* UPDATE */
    public History updateHistoryEntity(History history, Long id) throws SQLException {
        String sql = "UPDATE history SET command_id = ?, created = ?, response = ?, url = ?, status = ?, action = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, history.getCommandId());
            stmt.setObject(2, history.getCreated()); // <-- LocalDateTime
            stmt.setString(3, history.getResponse());
            stmt.setString(4, history.getUrl());
            stmt.setString(5, history.getStatus());
            stmt.setString(6, history.getAction());
            stmt.setLong(7, id);

            stmt.executeUpdate();
        }

        history.setId(id);
        return history;
    }

    /* DELETE ALL */
    public void deleteAllHistory() throws SQLException {
        String sql = "DELETE FROM history";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    /* DELETE ONE */
    public void deleteHistoryEntityById(Long id) throws SQLException {
        String sql = "DELETE FROM history WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
}