package org.example.simplefastpost;

import java.sql.*;
import java.util.Scanner;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.File;

public class MiniPostmanDB {

    private static String DB_FILE = null;

    public static void main(String[] args) throws Exception {
        // Determine save folder if -sv is provided
        String saveFolder = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-sc") && i + 1 < args.length) {
                saveFolder = args[i + 1];
                File folder = new File(saveFolder);
                if (!folder.exists()) folder.mkdirs();
                DB_FILE = saveFolder + File.separator + "commands.db";
            }
        }

        // Connect to SQLite DB if -sv is used
//        Connection conn = null;
//        if (DB_FILE != null) {
//            conn = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
//            createTableIfNotExists(conn);
//        }

        if (args.length == 0) {
            System.out.println("Usage: <program> -m METHOD -u URL OR saved commands");
            return;
        }

        if (args[0].equalsIgnoreCase("saved") && args[1].equalsIgnoreCase("commands")) {
            if (args.length == 3 && args[2].equalsIgnoreCase("prune")) {
//                pruneCommands(conn);
            } else {
//                listCommands(conn);
            }
//            if (conn != null) conn.close();
            return;
        }

        if (args[0].equalsIgnoreCase("-qc")) {
            int id = Integer.parseInt(args[1]);
//            runSavedCommand(conn, id);
//            if (conn != null) conn.close();
            return;
        }

        // Parse HTTP request arguments
        String method = "GET";
        String url = "";
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-m") && i + 1 < args.length) method = args[i + 1].toUpperCase();
            if (args[i].equalsIgnoreCase("-u") && i + 1 < args.length) url = args[i + 1];
        }

        String body = null;
        if (method.equals("POST") || method.equals("PUT") || method.equals("PATCH") || method.equals("DELETE")) {
            System.out.println("Enter JSON line by line. Type '-s' to send:");
            Scanner scanner = new Scanner(System.in);
            StringBuilder sb = new StringBuilder();
            while (true) {
                String line = scanner.nextLine();
                if (line.trim().equals("-s")) break;
                sb.append(line).append("\n");
            }
            body = sb.toString().trim();
        }

        sendRequest(method, url, body);

        // Automatically save if -sv <folder> was provided
        if (DB_FILE != null) {
//            saveCommand(conn, method, url, body);
        }

//        if (conn != null) conn.close();
    }

    private static void createTableIfNotExists(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS commands (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "method TEXT NOT NULL," +
                "url TEXT NOT NULL," +
                "json TEXT" +
                ")";
        Statement stmt = conn.createStatement();
        stmt.execute(sql);
    }

    private static void saveCommand(Connection conn, String method, String url, String json) throws SQLException {
        String sql = "INSERT INTO commands(method, url, json) VALUES (?, ?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, method);
        pstmt.setString(2, url);
        pstmt.setString(3, json);
        pstmt.executeUpdate();
        System.out.println("Command saved.");
    }

    private static void listCommands(Connection conn) throws SQLException {
        String sql = "SELECT * FROM commands";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        System.out.println("Saved commands:");
        while (rs.next()) {
            System.out.printf("ID: %d | Method: %s | URL: %s\n", rs.getInt("id"), rs.getString("method"), rs.getString("url"));
        }
    }

    private static void pruneCommands(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("DELETE FROM commands");
        stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name='commands'");
        System.out.println("All saved commands deleted and IDs reset.");
    }

    private static void runSavedCommand(Connection conn, int id) throws Exception {
        String sql = "SELECT * FROM commands WHERE id=?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, id);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            String method = rs.getString("method");
            String url = rs.getString("url");
            String json = rs.getString("json");
            sendRequest(method, url, json);
        } else {
            System.out.println("No command found with ID " + id);
        }
    }

    private static void sendRequest(String method, String url, String body) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));
        switch (method) {
            case "POST":
            case "PUT":
            case "PATCH":
            case "DELETE":
                if (body != null && !body.isEmpty())
                    builder.method(method, HttpRequest.BodyPublishers.ofString(body))
                            .header("Content-Type", "application/json");
                else
                    builder.method(method, HttpRequest.BodyPublishers.noBody());
                break;
            default:
                builder.GET();
        }
        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        System.out.println("\n--- RESPONSE ---");
        System.out.println("Status: " + response.statusCode());
        System.out.println(response.body());
    }
}
