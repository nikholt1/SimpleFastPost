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

    public PushService(CLIFolderService folderService, CLICommandService commandService,  HeaderService headerService) {
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
            List<Folder>  folders = folderService.getAllFolders();
            try (FileWriter writer = new FileWriter(file.toString(), true)) {


                for (Folder folder : folders) {
                    writer.write("#mkdir folder name " + folder.getProjectName() + ";\n");
                    writer.write("\n \n");
                    counter++;
                }
                for (Command command : commands) {
                    Folder folder = null;
                    if (command.getFolderId() != null) {
                        folder = folderService.fetchFolderById(command.getFolderId());
                    }
                    if (folder == null) {
                        writer.write("### place root" + ";\n");
                        counter++;
                    } else {
                        writer.write("### place folder " + folder.getProjectName() + ";\n");
                        counter++;

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
                        writer.write(method + " " + url + "\n" );

                    } else {
                        finalCommand = commandString;
                        writer.write(finalCommand);

                    }
                    List<Headers> headers = headerService.getAllHeadersFromCommandId(command.getId());
                    for (Headers header : headers) {
                        writer.write(header.getKey() + ": " + header.getValue() + "\n" );
                    }
                    writer.write("\n");
                    ObjectMapper  mapper = new ObjectMapper();

                    String body = command.getBody();
                    if (body != null && !body.isBlank()) {
                        Object json = mapper.readValue(body, Object.class);
                        writer.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
                        writer.write("\n");
                        writer.write("\n \n");
                    }




                }
            }





        } catch (Exception e) {
            e.printStackTrace();
        }

        return counter;
    }
}
