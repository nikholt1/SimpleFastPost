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

        String currentFolder = null;
        int counter = 0;

        try {
            cliFolderService.deleteAllFolders();
            cliCommandService.deleteAllCommands();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        for (int i = 0; i < lines.size(); i++) {

            String rawLine = lines.get(i);
            String line = rawLine.trim();

        /* -------------------------
           MKDIR PARSING
        ------------------------- */

            boolean containsMkdir = line.contains("#mkdir");

            if (containsMkdir) {

                String mkdirLine = line;
                String[] mkdirParts = mkdirLine.split("\\s+");

                int partsLength = mkdirParts.length;

                boolean validFormat = partsLength >= 4;

                if (validFormat) {

                    String rawFolderName = mkdirParts[3];
                    String cleanedFolderName = rawFolderName.replace(";", "");

                    currentFolder = cleanedFolderName;
                    try {
                        cliFolderService.createFolder(currentFolder);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    System.out.println("Detected folder name: " + currentFolder);
                }
            }

        /* -------------------------
           PLACE BLOCK PARSING
        ------------------------- */

            boolean isPlaceLine = line.startsWith("### place folder");

            if (isPlaceLine) {

                /* PLACE LINE */

                String placeLine = line;
                String[] placeParts = placeLine.split("\\s+");

                String rawFolderName = placeParts[3];
                String targetFolder = rawFolderName.replace(";", "");

                /* COMMAND NAME */

                i++;
                String commandLineRaw = lines.get(i);
                String commandLine = commandLineRaw.trim();

                String[] commandParts = commandLine.split("\\s+");

                String rawCommandName = commandParts[3];
                String commandName = rawCommandName.replace(";", "");

                /* METHOD + URL */

                i++;
                String requestLineRaw = lines.get(i);
                String requestLine = requestLineRaw.trim();

                String[] requestParts = requestLine.split("\\s+");

                String method = requestParts[0];
                String url = requestParts[1];

                /* HEADERS */

                StringBuilder headersBuilder = new StringBuilder();

                i++;

                while (i < lines.size()) {

                    String headerRawLine = lines.get(i);
                    String headerLine = headerRawLine.trim();

                    boolean nextBlock = headerLine.startsWith("###");

                    if (nextBlock) {
                        i--;
                        break;
                    }

                    boolean emptyLine = headerLine.isEmpty();

                    if (emptyLine) {
                        break;
                    }

                    headersBuilder.append(headerLine).append("\n");

                    i++;
                }

                String headers = headersBuilder.toString();

                /* BODY */

                StringBuilder bodyBuilder = new StringBuilder();

                i++;

                while (i < lines.size()) {

                    String bodyRawLine = lines.get(i);
                    String bodyLine = bodyRawLine.trim();

                    boolean nextBlock = bodyLine.startsWith("###");

                    if (nextBlock) {
                        i--;
                        break;
                    }

                    bodyBuilder.append(bodyLine).append("\n");

                    i++;
                }

                String body = bodyBuilder.toString();

                /* DEBUG OUTPUT */
                String command = "-m " + method + " -u " + url;
                Long folderId = null;
                try {
                    List<Folder> folders = cliFolderService.getAllFolders();
                    for (Folder f : folders) {
                        if (f.getProjectName().equals(currentFolder)) {
                            System.out.println(currentFolder);
                            folderId = f.getId();
                        }
                    }
                } catch (Exception e ) {
                    e.printStackTrace();
                }

                try {
                    cliCommandService.createCommand(commandName, command, body, folderId
                            );
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.println("Folder: " + targetFolder);
                System.out.println("Command: " + commandName);
                System.out.println("Method: " + method);
                System.out.println("URL: " + url);
                System.out.println("Headers:\n" + headers);
                System.out.println("Body:\n" + body);

                counter++;
            }
        }

        return counter;
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