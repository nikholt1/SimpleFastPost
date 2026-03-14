package org.example.simplefastpost.cli.syncService;

import org.example.simplefastpost.cli.commandService.CLICommandService;
import org.example.simplefastpost.cli.folderService.CLIFolderService;
import org.example.simplefastpost.model.Folder;
import org.example.simplefastpost.repository.CommandRepository;
import org.example.simplefastpost.repository.FolderRepository;
import org.example.simplefastpost.service.CommandService;
import org.example.simplefastpost.service.FolderService;

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

    private final CLICommandService cliCommandService;
    private final CLIFolderService cliFolderService;

    public HttpSyncService(CLICommandService commandService, CLIFolderService folderService) {
        this.cliCommandService = commandService;
        this.cliFolderService = folderService;
    }

    public int syncFile(Path file) throws IOException {

        List<String> lines = Files.readAllLines(file);
        int counter = 0;

        try {
            cliFolderService.deleteAllFolders();
            cliCommandService.deleteAllCommands();
        } catch (Exception e) {
            e.printStackTrace();
        }

        int total = lines.size();
        int lastPercent = -1;

        for (int i = 0; i < lines.size(); i++) {

            String line = lines.get(i).trim();

        /* -------------------------
           MKDIR
        ------------------------- */
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
            }

        /* -------------------------
           PLACE BLOCK
        ------------------------- */
            if (line.startsWith("### place ")) {

                String[] placeParts = line.split("\\s+");
                String targetFolder;

                String placeArg = placeParts[2].replace(";", ""); // remove semicolon

                if (placeArg.equals("root")) {
                    targetFolder = "root";
                } else if (placeArg.equals("folder")) {
                    targetFolder = placeParts[3].replace(";", "");
                } else {
                    throw new IllegalArgumentException("Invalid place syntax: " + line);
                }

                /* COMMAND NAME */
                i++;
                if (i >= lines.size()) break;

                String commandLine = lines.get(i).trim();
                String[] commandParts = commandLine.split("\\s+");
                String commandName = commandParts[3].replace(";", "");

                /* METHOD + URL */
                i++;
                if (i >= lines.size()) break;

                String requestLine = lines.get(i).trim();
                String[] requestParts = requestLine.split("\\s+");
                String method = requestParts[0];
                String url = requestParts[1];

                /* HEADERS */
                StringBuilder headersBuilder = new StringBuilder();
                i++;

                while (i < lines.size()) {
                    String headerLine = lines.get(i).trim();

                    if (headerLine.isEmpty()) break;
                    if (headerLine.startsWith("###")) {
                        i--;
                        break;
                    }

                    headersBuilder.append(headerLine).append("\n");
                    i++;
                }

                /* BODY */
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

                /* FIND FOLDER */
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

                /* SAVE COMMAND */
                String command = "-m " + method + " -u " + url;

                try {
                    cliCommandService.createCommand(commandName, command, body, folderId);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                counter++;
            }

            /* PROGRESS BAR UPDATE */

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

        String bar =
                "[" +
                        "=".repeat(filled) +
                        " ".repeat(barLength - filled) +
                        "] " +
                        percent + "%";

        System.out.print("\r" + bar);
        System.out.flush();
    }
}