package org.example.simplefastpost.crawler.cli;
import org.example.simplefastpost.model.CrawlerEntity;
import org.example.simplefastpost.model.CrawlerSession;
import org.sqlite.core.DB;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class CLICrawlerRepository {

    private final String DB_URL;

    public CLICrawlerRepository(String DB_URL) {
        this.DB_URL = DB_URL;
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }



    public Long createSession(CrawlerSession session) {
        String sql = "INSERT INTO crawler_session (title, created) VALUES (?, ?)";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, session.getTitle());
            stmt.setTimestamp(2, Timestamp.valueOf(session.getCreated()));

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                Long id = rs.getLong(1);
                session.setId(id);
                return id;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Long createEntity(CrawlerEntity entity) {
        String sql = """
            INSERT INTO crawler_entity
            (title, session_id, parent_id, type, url, method, response, process_time)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, entity.getTitle());
            stmt.setLong(2, entity.getSession().getId());

            if (entity.getParent() != null)
                stmt.setLong(3, entity.getParent().getId());
            else
                stmt.setNull(3, Types.BIGINT);

            stmt.setString(4, entity.getType());
            stmt.setString(5, entity.getUrl());
            stmt.setString(6, entity.getMethod());
            stmt.setString(7, entity.getResponse());
            stmt.setInt(8, entity.getProcessTime());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            Long entityId = null;

            if (rs.next()) {
                entityId = rs.getLong(1);
                entity.setId(entityId);
            }

            if (entity.getConnections() != null && entityId != null) {
                insertConnections(entityId, entity.getConnections(), conn);
            }

            return entityId;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void insertConnections(Long entityId, List<String> connections, Connection conn) throws SQLException {
        String sql = "INSERT INTO crawler_connections (entity_id, connection) VALUES (?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (String c : connections) {
                stmt.setLong(1, entityId);
                stmt.setString(2, c);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }



    public List<CrawlerSession> getAllSessions() {
        System.out.println(DB_URL);
        List<CrawlerSession> sessions = new ArrayList<>();

        try (Connection conn = connect()) {

            Map<Long, CrawlerSession> sessionMap = new HashMap<>();
            Map<Long, CrawlerEntity> entityMap = new HashMap<>();
            Map<Long, Long> parentMap = new HashMap<>();

            ResultSet rsSession = conn.createStatement().executeQuery("SELECT * FROM crawler_session");

            while (rsSession.next()) {
                CrawlerSession s = new CrawlerSession();
                s.setId(rsSession.getLong("id"));
                s.setTitle(rsSession.getString("title"));
                s.setCreated(rsSession.getTimestamp("created").toLocalDateTime());
                s.setCrawlerEntities(new ArrayList<>());

                sessionMap.put(s.getId(), s);
            }

            ResultSet rsEntity = conn.createStatement().executeQuery("SELECT * FROM crawler_entity");

            while (rsEntity.next()) {
                CrawlerEntity e = new CrawlerEntity();

                Long id = rsEntity.getLong("id");
                Long sessionId = rsEntity.getLong("session_id");

                // Fix: safely cast parent_id to Long
                Object parentObj = rsEntity.getObject("parent_id");
                Long parentId = null;
                if (parentObj != null) {
                    parentId = ((Number) parentObj).longValue();
                }

                e.setId(id);
                e.setTitle(rsEntity.getString("title"));
                e.setType(rsEntity.getString("type"));
                e.setUrl(rsEntity.getString("url"));
                e.setMethod(rsEntity.getString("method"));
                e.setResponse(rsEntity.getString("response"));
                e.setProcessTime(rsEntity.getInt("process_time"));

                e.setConnections(new ArrayList<>());
                e.setChildren(new ArrayList<>());

                CrawlerSession session = sessionMap.get(sessionId);
                e.setSession(session);
                session.getCrawlerEntities().add(e);

                entityMap.put(id, e);
                parentMap.put(id, parentId);
            }

            ResultSet rsConn = conn.createStatement().executeQuery("SELECT * FROM crawler_connections");

            while (rsConn.next()) {
                Long entityId = rsConn.getLong("entity_id");
                String connection = rsConn.getString("connection");

                entityMap.get(entityId).getConnections().add(connection);
            }

            // 🟢 Build tree
            for (Map.Entry<Long, Long> entry : parentMap.entrySet()) {
                Long childId = entry.getKey();
                Long parentId = entry.getValue();

                if (parentId != null) {
                    CrawlerEntity child = entityMap.get(childId);
                    CrawlerEntity parent = entityMap.get(parentId);

                    child.setParent(parent);
                    parent.getChildren().add(child);
                }
            }

            sessions.addAll(sessionMap.values());

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sessions;
    }



    public void updateEntity(CrawlerEntity entity) {
        String sql = """
            UPDATE crawler_entity
            SET title=?, type=?, url=?, method=?, response=?, process_time=?
            WHERE id=?
        """;

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getTitle());
            stmt.setString(2, entity.getType());
            stmt.setString(3, entity.getUrl());
            stmt.setString(4, entity.getMethod());
            stmt.setString(5, entity.getResponse());
            stmt.setInt(6, entity.getProcessTime());
            stmt.setLong(7, entity.getId());

            stmt.executeUpdate();

            deleteConnections(entity.getId(), conn);
            insertConnections(entity.getId(), entity.getConnections(), conn);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteConnections(Long entityId, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM crawler_connections WHERE entity_id=?")) {
            stmt.setLong(1, entityId);
            stmt.executeUpdate();
        }
    }

    public void deleteSession(Long sessionId) {
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM crawler_session WHERE id=?")) {

            stmt.setLong(1, sessionId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteEntity(Long entityId) {
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM crawler_entity WHERE id=?")) {

            stmt.setLong(1, entityId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public int deleteAllConnections() {
        String sql = "DELETE FROM crawler_connections";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int deleted = stmt.executeUpdate(); // ✅ correct

            System.out.print("Deleted crawler connections: " + deleted + " ");
            for (int i = 0; i < deleted; i++) {
                System.out.print("\u001B[31m-\u001B[0m");
            }
            System.out.println();

            return deleted; // ✅ return correct value

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting all connections", e);
        }
    }
    public int deleteAllEntities() {
        String sql = "DELETE FROM crawler_entity";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int deleted = stmt.executeUpdate();

            System.out.print("Deleted crawler entities: " + deleted + " ");
            for (int i = 0; i < deleted; i++) {
                System.out.print("\u001B[31m-\u001B[0m");
            }
            System.out.println();

            return deleted;

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting all entities", e);
        }
    }
    public int deleteAllSessions() {
        String sql = "DELETE FROM crawler_session";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int deleted = stmt.executeUpdate();

            System.out.print("Deleted crawler sessions: " + deleted + " ");
            for (int i = 0; i < deleted; i++) {
                System.out.print("\u001B[31m-\u001B[0m");
            }
            System.out.println();

            return deleted;

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting all sessions", e);
        }
    }
}