package org.example.simplefastpost.cli.syncService;

import org.example.simplefastpost.cli.commandService.CLICommandService;
import org.example.simplefastpost.cli.commandService.HeaderService;
import org.example.simplefastpost.cli.folderService.CLIFolderService;
import org.example.simplefastpost.model.Command;
import org.example.simplefastpost.model.Folder;
import org.example.simplefastpost.model.Headers;
import tools.jackson.databind.ObjectMapper;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PushService {

    private final CLIFolderService folderService;
    private final CLICommandService commandService;
    private final HeaderService headerService;

    public PushService(CLIFolderService folderService, CLICommandService commandService, HeaderService headerService) {
        this.folderService = folderService;
        this.commandService = commandService;
        this.headerService = headerService;
    }

    public int pushFile(Path file) throws IOException {
        int counter = 0;

        try (FileWriter writer = new FileWriter(file.toString(), false)) {
            writer.write("");
        }

        try {
            List<Command> commands = commandService.getAllCommands();
            List<Folder> folders = folderService.getAllFolders();

            int total = folders.size() + commands.size(); // total work
            int progress = 0;

            try (FileWriter writer = new FileWriter(file.toString(), true)) {

                // 📁 FOLDERS
                for (Folder folder : folders) {
                    writer.write("#mkdir folder name " + folder.getProjectName() + ";\n");
                    writer.write("\n \n");
                    counter++;

                    progress++;
                    printProgressBar((progress * 100) / total);
                }

                // 📄 COMMANDS
                for (Command command : commands) {
                    Folder folder = null;
                    if (command.getFolderId() != null) {
                        folder = folderService.fetchFolderById(command.getFolderId());
                    }

                    if (folder == null) {
                        writer.write("### place root;\n");
                    } else {
                        writer.write("### place folder " + folder.getProjectName() + ";\n");
                    }

                    writer.write("### command name " + command.getName() + ";\n");
                    counter++;

                    String commandString = command.getCommand();

                    String method = null;
                    String url = null;
                    String finalCommand = commandString;

                    if (commandString.contains("-m") && commandString.contains("-u")) {
                        String[] parts = commandString.split("\\s+");

                        for (int i = 0; i < parts.length; i++) {
                            if (parts[i].equals("-m") && i + 1 < parts.length) {
                                method = parts[i + 1];
                            }
                            if (parts[i].equals("-u") && i + 1 < parts.length) {
                                url = parts[i + 1];
                            }
                        }
                        writer.write(method + " " + url + "\n");
                    } else {
                        writer.write(finalCommand);
                    }

                    List<Headers> headers = headerService.getAllHeadersFromCommandId(command.getId());
                    for (Headers header : headers) {
                        writer.write(header.getKey() + ": " + header.getValue() + "\n");
                    }

                    writer.write("\n");

                    ObjectMapper mapper = new ObjectMapper();

                    String body = command.getBody();
                    if (body != null && !body.isBlank()) {
                        Object json = mapper.readValue(body, Object.class);
                        writer.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
                        writer.write("\n");
                    }

                    if (command.getExpected_status() != null ||
                            (command.getExpected_response() != null && !command.getExpected_response().isBlank())) {

                        writer.write("###expected:\n");

                        if (command.getExpected_status() != null) {
                            writer.write("#" + command.getExpected_status() + "\n");
                        }

                        if (command.getExpected_response() != null && !command.getExpected_response().isBlank()) {
                            String[] lines = command.getExpected_response().split("\\R");
                            for (String line : lines) {
                                writer.write("#" + line + "\n");
                            }
                        }
                    }

                    writer.write("\n \n");

                    // ✅ update progress AFTER each command
                    progress++;
                    printProgressBar((progress * 100) / total);

                }
                System.out.println();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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

        String bar = "[" + "\u001B[32m" + "=".repeat(filled) + " ".repeat(barLength - filled) + "\u001B[0m" + "] " + percent + "%";
        System.out.print("\r" + bar);
        System.out.flush();
    }
}