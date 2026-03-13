package org.example.simplefastpost.cli.apiRepository;

import org.example.simplefastpost.model.APIEntity;
import org.hibernate.boot.model.relational.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ApiRepository {

    private String DB_URL;

    public ApiRepository(String BASE_URL) {
        this.DB_URL = BASE_URL;
    }
    private boolean apiEntityExists(Long pid) throws SQLException {
        String sql = "SELECT 1 FROM apiInstance WHERE pid = ? LIMIT 1";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, pid);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void createNewInstance(APIEntity apiEntity) throws SQLException {


        if (apiEntityExists(apiEntity.getpId())) {
            System.out.println("API instance with PID already exists: " + apiEntity.getpId());
            return;
        }

        String sql = """
        INSERT INTO apiInstance (pid, created)
        VALUES (?, ?)
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, apiEntity.getpId());


            stmt.setString(2, apiEntity.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            stmt.executeUpdate();
        }
    }
    public List<APIEntity> listAllInstances() throws SQLException {
        List<APIEntity> instances = new ArrayList<>();

        String sql = "SELECT id, pid, created FROM apiInstance";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                long id = rs.getLong("id");
                long pid = rs.getLong("pid");
                String createdStr = rs.getString("created");

                LocalDateTime created = LocalDateTime.parse(createdStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                APIEntity entity = new APIEntity(id, pid, created);
                instances.add(entity);
            }
        }

        return instances;
    }


    public void deleteByPid(Long pid) throws SQLException {
        String sql = "DELETE FROM apiInstance WHERE pid = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, pid);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Deleted API instance with PID: " + pid);
            } else {
                System.out.println("No API instance found with PID: " + pid);
            }
        }
    }

    public APIEntity getInstanceByPid(Long pid) throws SQLException {
        String sql = "SELECT id, pid, created FROM apiInstance WHERE pid = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, pid);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Long id = rs.getLong("id"); // fetch the ID from the table
                    Long foundPid = rs.getLong("pid");
                    String createdStr = rs.getString("created");

                    return new APIEntity(foundPid,
                            LocalDateTime.parse(createdStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                } else {
                    return null; // no instance with this PID
                }
            }
        }
    }

}
