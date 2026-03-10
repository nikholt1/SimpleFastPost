package org.example.simplefastpost.cli.syncService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class HttpSyncService {

    public int syncFile(Path file) throws IOException {

        List<String> lines = Files.readAllLines(file);
        String currentFolder = null;

        // Count total request blocks
        int totalBlocks = (int) lines.stream().filter(l -> l.trim().startsWith("### place")).count();
        int processedBlocks = 0;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            // --- Detect folder definition ---
            if (line.startsWith("#mkdir folder")) {
                if (i + 2 < lines.size() && lines.get(i + 1).trim().equals("#name;")) {
                    currentFolder = lines.get(i + 2).trim().replace("\"", "").replace("#", "");
                    Path folderPath = Paths.get(System.getProperty("user.dir"), currentFolder);
                    Files.createDirectories(folderPath); // create folder
                    System.out.println("Created folder: " + currentFolder);
                }
            }

            // --- Detect HTTP request block ---
            if (line.startsWith("### place")) {

                // Extract location and folder
                String[] parts = line.trim().split("\\s+");
                String location = parts[2]; // folder or root
                String folderName = (parts.length > 3) ? parts[3].replace("#","") : null;

                String targetFolder = "root";
                if ("folder".equals(location) && folderName != null) {
                    targetFolder = folderName;
                }

                // Initialize command info
                String commandName = null;
                String httpMethod = null;
                String url = null;
                StringBuilder headers = new StringBuilder();
                StringBuilder body = new StringBuilder();

                // Collect the request lines
                i++; // move past "### place" line
                boolean inBody = false;
                while (i < lines.size() && !lines.get(i).trim().startsWith("### place")) {
                    String l = lines.get(i).trim();

                    // Command name
                    if (l.startsWith("### command name")) {
                        String[] cmdParts = l.split("\\s+");
                        if (cmdParts.length >= 4) {
                            commandName = cmdParts[3].replace("#", "");
                        }
                    }
                    // HTTP method + URL
                    else if (l.matches("^(GET|POST|PUT|DELETE).*")) {
                        httpMethod = l.split("\\s+")[0];
                        url = l.substring(httpMethod.length()).trim();
                    }
                    // Body detection
                    else if (l.isEmpty()) {
                        inBody = true;
                    } else {
                        if (inBody) {
                            body.append(l).append("\n");
                        } else {
                            headers.append(l).append("\n");
                        }
                    }

                    i++;
                }
                i--; // adjust index

                // Ensure folder exists
                Path folderPath;
                if ("root".equals(targetFolder)) {
                    folderPath = Paths.get(System.getProperty("user.dir"));
                } else {
                    folderPath = Paths.get(System.getProperty("user.dir"), targetFolder);
                    Files.createDirectories(folderPath);
                }

                // Save command as a text file
                String fileName = (commandName != null ? commandName : "unnamed") + ".txt";
                Path commandFile = folderPath.resolve(fileName);
                StringBuilder content = new StringBuilder();
                content.append("Method: ").append(httpMethod).append("\n");
                content.append("URL: ").append(url).append("\n");
                if (headers.length() > 0) content.append("Headers:\n").append(headers);
                if (body.length() > 0) content.append("Body:\n").append(body);

                Files.writeString(commandFile, content.toString(),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                // Minimal logging including URL + headers + authorization if present
                String authLine = "";
                for (String h : headers.toString().split("\n")) {
                    if (h.toLowerCase().startsWith("authorization")) {
                        authLine = " | Auth: " + h.split(":",2)[1].trim();
                        break;
                    }
                }

                System.out.println(String.format(
                        " %-21s | Command: %-15s | Method: %-6s | URL: %s%s",
                        targetFolder,
                        (commandName != null ? commandName : "N/A"),
                        (httpMethod != null ? httpMethod : "N/A"),
                        (url != null ? url : "N/A"),
                        authLine
                ));

                // Simulate execution
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                processedBlocks++;
                printProgressBar(processedBlocks, totalBlocks);
            }
        }

        System.out.println();
        return 100;
    }

    private void printProgressBar(int current, int total) {
        int barLength = 30;
        int filled = (int) ((current / (double) total) * barLength);

        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < filled; i++) bar.append("=");
        for (int i = filled; i < barLength; i++) bar.append(" ");
        bar.append("] ");

        int percent = (int) ((current / (double) total) * 100);
        System.out.print("\r" + bar + percent + "%");
    }
}