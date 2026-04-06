package org.example.simplefastpost.service;


import org.example.simplefastpost.dto.CommandDTO;
import org.example.simplefastpost.dto.FolderDTO;
import org.example.simplefastpost.model.Command;
import org.example.simplefastpost.model.Folder;
import org.example.simplefastpost.model.Headers;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Service
public class PushService {
    private final FolderService folderService;
    private final CommandService commandService;
    private final HeaderService headerService;
    public PushService(FolderService folderService, CommandService commandService, HeaderService headerService) {
        this.folderService = folderService;
        this.commandService = commandService;
        this.headerService = headerService;
    }


    public int pushFile(Path file) throws IOException {
        System.out.println("starting push from pushFile to " + file.toString());
        int counter = 0;


        try (FileWriter writer = new FileWriter(file.toString(), false)) {
            writer.write("");
        }
        try {


            try (FileWriter writer = new FileWriter(file.toString(), true)) {
                List<Folder> folders = folderService.getAllFolderEntitiesForPush();
                List<Command> commands = commandService.getCommandEntitiesForPush();

                for (Folder folder : folders) {
                    writer.write("#mkdir folder name " + folder.getProjectName() + ";\n");
                    writer.write("\n \n");
                    counter++;
                }
                for (Command command : commands) {
                    Folder folder = null;
                    if (command.getFolderId() != null) {
                        Optional<FolderDTO> folderOpt = folderService.getFolderById(command.getFolderId());

                        if (folderOpt.isPresent()) {
                            folder = FolderMapper.fromDTO(folderOpt.get());
                            folder.getProjectName();
                        }
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

                    List<Headers> headers = headerService.getHeadersByCommandId(command.getId());
                    for (Headers header : headers) {
                        writer.write(header.getKey() + ": " + header.getValue() + "\n" );
                    }
                    writer.write("\n");
                    ObjectMapper map = new ObjectMapper();

                    ObjectMapper mapper = new ObjectMapper();

                    String body = command.getBody();
                    if (body != null && !body.isBlank()) {
                        Object json = mapper.readValue(body, Object.class);
                        writer.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));

                    }

                    String expectedResponse = command.getExpected_response();
                    int expectedStatus = command.getExpected_status();
                    if (expectedStatus != 0) {
                        writer.write("#" + expectedStatus);

                    }
                    if (expectedResponse != null) {
                        Object json = mapper.readValue(expectedResponse, Object.class);

                        String pretty = mapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(json);

                        String commented = pretty
                                .lines()
                                .map(line -> "# " + line)
                                .reduce((a, b) -> a + "\n" + b)
                                .orElse("");

                        writer.write(commented);
                        writer.write("\n\n");
                    } else {
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



