package org.example.simplefastpost.cli.syncService;

import org.example.simplefastpost.cli.commandService.CLICommandService;
import org.example.simplefastpost.cli.folderService.CLIFolderService;
import org.example.simplefastpost.model.Command;
import org.example.simplefastpost.model.Folder;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PushService {

    private final CLIFolderService folderService;
    private final CLICommandService commandService;

    public PushService(CLIFolderService folderService, CLICommandService commandService) {
        this.folderService = folderService;
        this.commandService = commandService;
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
                    writer.write("#mkdir "folder.getProjectName() + "\n");
                }
            }

            for (Command command : commands) {
                System.out.println(command);
            }



        } catch (Exception e) {
            e.printStackTrace();
        }

        return counter;
    }
}
