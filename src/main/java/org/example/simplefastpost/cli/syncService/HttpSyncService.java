package org.example.simplefastpost.cli.syncService;

import org.example.simplefastpost.cli.commandService.CLICommandService;
import org.example.simplefastpost.cli.commandService.HeaderService;
import org.example.simplefastpost.cli.folderService.CLIFolderService;
import org.example.simplefastpost.model.Command;
import org.example.simplefastpost.model.Folder;
import org.example.simplefastpost.model.Headers;
import org.example.simplefastpost.repository.CommandRepository;
import org.example.simplefastpost.repository.FolderRepository;
import org.example.simplefastpost.service.CommandService;
import org.example.simplefastpost.service.FolderService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class HttpSyncService {

    private final CLICommandService cliCommandService;
    private final CLIFolderService cliFolderService;
    private final HeaderService headerService;

    public HttpSyncService(CLICommandService commandService,
                           CLIFolderService folderService,
                           HeaderService headerService) {
        this.cliCommandService = commandService;
        this.cliFolderService = folderService;
        this.headerService = headerService;
    }

    public int syncFile(Path file) throws IOException {

        List<String> lines = Files.readAllLines(file);
        int counter = 0;

        // Clear existing data
        try {
            cliFolderService.deleteAllFolders();
            cliCommandService.deleteAllCommands();
            headerService.deleteAllHeadersInDB();
        } catch (Exception e) {
            e.printStackTrace();
        }

        int total = lines.size();
        int lastPercent = -1;

        for (int i = 0; i < lines.size(); i++) {

            String line = lines.get(i).trim();

            // ---------------- MKDIR ----------------
            if (line.startsWith("#mkdir")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 4) {
                    String folderName = parts[3].replace(";", "");
                    try {
                        cliFolderService.createFolder(folderName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                counter++;
            }

            // ---------------- PLACE BLOCK ----------------
            if (line.startsWith("### place ")) {

                String[] placeParts = line.split("\\s+");
                String targetFolder;

                String placeArg = placeParts[2].replace(";", "");

                if (placeArg.equals("root")) {
                    targetFolder = "root";
                } else if (placeArg.equals("folder")) {
                    targetFolder = placeParts[3].replace(";", "");
                } else {
                    throw new IllegalArgumentException("Invalid place syntax: " + line);
                }

                // ---------------- COMMAND NAME ----------------
                i++;
                if (i >= lines.size()) break;

                String commandLine = lines.get(i).trim();
                String[] commandParts = commandLine.split("\\s+");
                String commandName = commandParts[3].replace(";", "");

                // ---------------- METHOD + URL ----------------
                i++;
                if (i >= lines.size()) break;

                String requestLine = lines.get(i).trim();
                String[] requestParts = requestLine.split("\\s+");
                String method = requestParts[0].trim();
                String url = requestParts.length > 1 ? requestParts[1].trim() : "";

                if (url.isEmpty()) {
                    System.out.println("Skipping command '" + commandName + "' due to missing URL.");
                    continue;
                }

                // ---------------- HEADERS ----------------
                List<Headers> headersList = new ArrayList<>();
                i++;

                while (i < lines.size()) {
                    String headerLine = lines.get(i).trim();

                    if (headerLine.isEmpty()) break;
                    if (headerLine.startsWith("###")) {
                        i--;
                        break;
                    }

                    int sep = headerLine.indexOf(':');
                    if (sep != -1) {
                        String key = headerLine.substring(0, sep).trim();
                        String value = headerLine.substring(sep + 1).trim();

                        if (!key.isEmpty()) {
                            Headers h = new Headers();
                            h.setKey(key);
                            h.setValue(value);
                            headersList.add(h);
                        }
                    }
                    i++;
                }

                // ---------------- BODY ----------------
                StringBuilder bodyBuilder = new StringBuilder();
                i++;
                while (i < lines.size()) {
                    String bodyLine = lines.get(i).trim();
                    if (bodyLine.startsWith("###")) {
                        i--;
                        break;
                    }
                    bodyBuilder.append(bodyLine).append("\n");
                    i++;
                }
                String body = bodyBuilder.toString().trim();

                // ---------------- EXPECTED ----------------
                // ---------------- EXPECTED ----------------
                Integer expectedStatus = 0;   // default if no expected block
                String expectedBody = null;

                if (i + 1 < lines.size() && lines.get(i + 1).trim().startsWith("### expected:")) {

                    i++; // move to ### expected:
                    i++; // move to first expected line

                    boolean isFirstLine = true;
                    StringBuilder expectedBodyBuilder = new StringBuilder();

                    while (i < lines.size()) {
                        String rawLine = lines.get(i).trim();

                        if (rawLine.startsWith("###")) {
                            i--;
                            break;
                        }

                        if (rawLine.startsWith("#")) {
                            String cleaned = rawLine.substring(1).trim();

                            if (isFirstLine) {
                                try {
                                    expectedStatus = Integer.parseInt(cleaned);
                                } catch (NumberFormatException e) {
                                    System.out.println("Invalid expected status: " + cleaned);
                                    expectedStatus = 0; // fallback safety
                                }
                                isFirstLine = false;
                            } else {
                                expectedBodyBuilder.append(cleaned).append("\n");
                            }
                        }

                        i++;
                    }

                    String parsedBody = expectedBodyBuilder.toString().trim();
                    expectedBody = parsedBody.isEmpty() ? null : parsedBody;
                }

                // ---------------- FIND FOLDER ----------------
                Long folderId = null;
                if (!targetFolder.equals("root")) {
                    try {
                        List<Folder> folders = cliFolderService.getAllFolders();
                        for (Folder f : folders) {
                            if (f.getProjectName().equals(targetFolder)) {
                                folderId = f.getId();
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // ---------------- SAVE COMMAND ----------------
                String commandStr = "-m " + method + " -u " + url;
                Command createdCommand = null;

                try {
                    createdCommand = cliCommandService.createCommand(
                            commandName,
                            commandStr,
                            body,
                            folderId,
                            null,
                            0,
                            expectedBody,
                            expectedStatus
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }

                // ---------------- SAVE HEADERS ----------------
                if (createdCommand != null && !headersList.isEmpty()) {
                    for (Headers h : headersList) {
                        h.setCommandId(createdCommand.getId());
                        try {
                            headerService.createHeader(h);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                counter++;
            }

            // ---------------- PROGRESS BAR ----------------
            int percent = (int) ((i / (double) total) * 100);
            if (percent > 100) percent = 100;

            if (percent != lastPercent) {
                printProgressBar(percent);
                lastPercent = percent;
            }
        }

        printProgressBar(100);
        System.out.println();

        return counter;
    }

    private void printProgressBar(int percent) {
        int barLength = 30;
        int filled = (percent * barLength) / 100;

        String bar = "[" + "=".repeat(filled) + " ".repeat(barLength - filled) + "] " + percent + "%";
        System.out.print("\r" + bar);
        System.out.flush();
    }
}