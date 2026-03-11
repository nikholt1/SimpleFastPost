package org.example.simplefastpost.service;

import org.example.simplefastpost.cli.commandService.CLICommandService;
import org.example.simplefastpost.cli.folderService.CLIFolderService;
import org.example.simplefastpost.dto.FolderDTO;
import org.example.simplefastpost.model.Command;
import org.example.simplefastpost.model.Folder;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
public class HttpSyncService {

    private final CommandService cliCommandService;
    private final FolderService cliFolderService;

    public HttpSyncService(CommandService commandService, FolderService folderService) {
        this.cliCommandService = commandService;
        this.cliFolderService = folderService;
    }

    public int syncFile(Path file) throws IOException {

        cliCommandService.deleteAllCommands();
        cliFolderService.deleteAllFolders();

        List<String> lines = Files.readAllLines(file);
        int counter = 0;

        for (int i = 0; i < lines.size(); i++) {

            String line = lines.get(i).trim();


            if (line.startsWith("#mkdir")) {

                String[] parts = line.split("\\s+");

                if (parts.length >= 4) {

                    String folderName = parts[3].replace(";", "");

                    try {
                        List<Command> commands = new ArrayList<>();
                        cliFolderService.createFolder(folderName, commands);

                        System.out.println("Created folder: " + folderName);
                        counter++;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }


            if (line.startsWith("### place")) {

                String[] placeParts = line.split("\\s+");

                String target;

                if (placeParts[2].equalsIgnoreCase("root")) {
                    target = "root";
                }
                else if (placeParts[2].equalsIgnoreCase("folder")) {
                    target = placeParts[3].replace(";", "");
                }
                else {
                    target = "unknown";
                }

                System.out.println("Place detected: " + target);



                i++;
                String commandLine = lines.get(i).trim();
                String[] commandParts = commandLine.split("\\s+");
                String commandName = commandParts[3].replace(";", "");



                i++;
                String requestLine = lines.get(i).trim();
                String[] requestParts = requestLine.split("\\s+");

                String method = requestParts[0];
                String url = requestParts[1];


                StringBuilder headersBuilder = new StringBuilder();

                i++;

                while (i < lines.size()) {

                    String headerLine = lines.get(i).trim();

                    if (headerLine.isEmpty()) {
                        break;
                    }

                    if (headerLine.startsWith("###")) {
                        i--;
                        break;
                    }

                    headersBuilder.append(headerLine).append("\n");

                    i++;
                }

                String headers = headersBuilder.toString();



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



                Long folderId = null;

                if (!target.equals("root")) {

                    try {

                        List<Folder> folders = new ArrayList<>();
                        List<FolderDTO> dtos = cliFolderService.getAllFolders();

                        for (FolderDTO folderDTO : dtos) {
                            folders.add(FolderMapper.fromDTO(folderDTO));
                        }
                        for (Folder f : folders) {

                            if (f.getProjectName().equals(target)) {

                                folderId = f.getId();
                                break;
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }



                String command = "-m " + method + " -u " + url;

                try {

                    cliCommandService.createCommand(
                            commandName,
                            command,
                            body,
                            folderId
                    );

                } catch (Exception e) {
                    e.printStackTrace();
                }


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

}