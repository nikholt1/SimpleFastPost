package org.example.simplefastpost.service;

import org.springframework.stereotype.Service;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
public class HttpSyncService {

    public boolean syncFile(Path file) throws IOException {

        List<String> lines = Files.readAllLines(file);
        String currentFolder = null;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            // Detect folder definition
            if (line.startsWith("#mkdir folder")) {
                // Look ahead for "#name;" line
                if (i + 2 < lines.size() && lines.get(i + 1).trim().equals("#name;")) {
                    currentFolder = lines.get(i + 2).trim().replace("\"", "");
                    System.out.println("Detected folder to create: " + currentFolder);
                }
            }

            // Detect HTTP request blocks
            if (line.startsWith("### place")) {
                String[] parts = line.split(" ");
                String location = parts[2]; // folder or root
                String folderName = (parts.length > 3) ? parts[3] : null;

                // Determine where to place this request
                String targetFolder = "root";
                if ("folder".equals(location) && folderName != null) {
                    targetFolder = folderName;
                }

                System.out.println("Processing request to place: " + targetFolder);

                // Collect the request lines
                StringBuilder requestBuilder = new StringBuilder();
                i++; // move past the "###" line
                while (i < lines.size() && !lines.get(i).trim().startsWith("###")) {
                    requestBuilder.append(lines.get(i)).append("\n");
                    i++;
                }
                i--; // adjust index to not skip next block

                String requestContent = requestBuilder.toString();
                System.out.println("Request content:\n" + requestContent);

                // TODO: execute the request here or queue it for later
            }
        }
        return true;
    }
}