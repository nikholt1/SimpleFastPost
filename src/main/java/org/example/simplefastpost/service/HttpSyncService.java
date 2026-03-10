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
                        List<Command> commands = new ArrayList<>();
                        cliFolderService.createFolder(currentFolder, commands);
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
                    List<FolderDTO> foldersDTO = cliFolderService.getAllFolders();
                    List<Folder> folders = new ArrayList<>();
                    for (FolderDTO folderDTO : foldersDTO) {
                        folders.add(FolderMapper.fromDTO(folderDTO));
                    }
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
}