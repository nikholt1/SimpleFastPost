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

                i++;
                if (i >= lines.size()) break;

                String commandLine = lines.get(i).trim();
                String[] commandParts = commandLine.split("\\s+");
                String commandName = commandParts[3].replace(";", "");

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
                String body = bodyBuilder.toString();

                String expectedStatus = null;
                StringBuilder expectedResponseBuilder = new StringBuilder();

                if (i + 1 < lines.size() && lines.get(i + 1).trim().startsWith("###expected")) {
                    i += 2;

                    while (i < lines.size()) {
                        String expectedLine = lines.get(i).trim();

                        if (expectedLine.startsWith("###")) {
                            i--;
                            break;
                        }

                        if (expectedLine.startsWith("#")) {
                            String clean = expectedLine.substring(1).trim();

                            if (expectedStatus == null && clean.matches("\\d+")) {
                                expectedStatus = clean;
                            } else {
                                expectedResponseBuilder.append(clean).append("\n");
                            }
                        }

                        i++;
                    }
                }

                String expectedResponse = expectedResponseBuilder.toString().trim();

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

                String commandStr = "-m " + method + " -u " + url;
                Command createdCommand = null;

                try {
                    if (expectedStatus != null || !expectedResponse.isEmpty()) {
                        createdCommand = cliCommandService.createCommandWithExpected(
                                commandName,
                                commandStr,
                                body,
                                folderId,
                                expectedResponse.isEmpty() ? null : expectedResponse,
                                expectedStatus
                        );
                    } else {
                        createdCommand = cliCommandService.createCommand(
                                commandName,
                                commandStr,
                                body,
                                folderId
                        );
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }

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

            int percent = (int) ((i / (double) total) * 100);
            if (percent > 100) percent = 100;

            if (percent != lastPercent) {
                printProgressBar(percent);
                lastPercent = percent;
            }
        }

        printProgressBar(100);
        System.out.println();
        System.out.print("Commands build: ");
        for (int i = 0; i < counter; i++) {
            System.out.print("\u001B[32m" + "+" + "\u001B[0m");
        }
        System.out.println();
        return counter;

    }

    private void printProgressBar(int percent) {
        int barLength = 30;
        int filled = (percent * barLength) / 100;

        String bar = "[" + "\u001B[32m" +  "=".repeat(filled) + " ".repeat(barLength - filled) + "\u001B[0m" +  "] " + percent + "%";
        System.out.print("\r" + bar);
        System.out.flush();
    }
}