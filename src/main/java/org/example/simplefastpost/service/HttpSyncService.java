package org.example.simplefastpost.service;

import org.example.simplefastpost.cli.commandService.CLICommandService;
import org.example.simplefastpost.cli.folderService.CLIFolderService;
import org.example.simplefastpost.dto.CommandDTO;
import org.example.simplefastpost.dto.FolderDTO;
import org.example.simplefastpost.model.Command;
import org.example.simplefastpost.model.Folder;
import org.example.simplefastpost.model.Headers;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
@Service
public class HttpSyncService {

    private final CommandService cliCommandService;
    private final FolderService cliFolderService;
    private final HeaderService headerService;

    public HttpSyncService(CommandService commandService, FolderService folderService, HeaderService headerService) {
        this.cliCommandService = commandService;
        this.cliFolderService = folderService;
        this.headerService = headerService;
    }

    public int syncFile(Path file) throws IOException {

        cliCommandService.deleteAllCommands();
        cliFolderService.deleteAllFolders();
        headerService.deleteAllHeaders();

        List<String> lines = Files.readAllLines(file);
        int counter = 0;

        for (int i = 0; i < lines.size(); i++) {

            String line = lines.get(i).trim();

            // ------------------------- CREATE FOLDER -------------------------
            if (line.startsWith("#mkdir")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 4) {
                    String folderName = parts[3].replace(";", "");
                    try {
                        cliFolderService.createFolder(folderName, new ArrayList<>());
                        System.out.println("Created folder: " + folderName);
                        counter++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // ------------------------- CREATE COMMAND -------------------------
            if (line.startsWith("### place")) {

                String[] placeParts = line.split("\\s+");
                String target;

                if (placeParts[2].equalsIgnoreCase("root")) {
                    target = "root";
                } else if (placeParts[2].equalsIgnoreCase("folder")) {
                    target = placeParts[3].replace(";", "");
                } else {
                    target = "unknown";
                }


                // Command name
                i++;
                String commandLine = lines.get(i).trim();
                String[] commandParts = commandLine.split("\\s+");
                String commandName = commandParts[3].replace(";", "");

                // Method + URL
                i++;
                String requestLine = lines.get(i).trim();
                String[] requestParts = requestLine.split("\\s+");
                String method = requestParts[0];
                String url = requestParts.length > 1 ? requestParts[1] : "";

                // ------------------------- HEADERS -------------------------
                List<Headers> headersList = new ArrayList<>();
                i++;
                while (i < lines.size()) {
                    String headerLine = lines.get(i).trim();
                    if (headerLine.isEmpty()) break; // end of headers
                    if (headerLine.startsWith("###")) {
                        i--; // backtrack
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

                // ------------------------- BODY -------------------------
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

                // ------------------------- FIND FOLDER ID -------------------------
                Long folderId = null;
                if (!target.equals("root")) {
                    try {
                        List<FolderDTO> dtos = cliFolderService.getAllFolders();
                        for (FolderDTO dto : dtos) {
                            if (dto.projectName().equals(target)) {
                                folderId = dto.id();
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
                    CommandDTO commandDTO = cliCommandService.createCommand(commandName, commandStr, body, folderId, null, 1, null, 1);
                    Folder emptyFolder = new Folder();
                    createdCommand = CommandMapper.fromDTO(commandDTO, emptyFolder);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (createdCommand != null && !headersList.isEmpty()) {
                    for (Headers h : headersList) {
                        h.setCommandId(createdCommand.getId());
                        try {
                            headerService.createNewHeader(h);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                System.out.println("Command: " + commandName);
                System.out.println("Method: " + method);
                System.out.println("URL: " + url);
                System.out.println("Headers: " + headersList.size() + " saved");
                System.out.println("Body length: " + body.length());

                counter++;
            }
        }

        return counter;
    }
}