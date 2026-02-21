package org.example.simplefastpost.cli.commandService;

import org.example.simplefastpost.cli.commandRepo.CLICommandRepository;
import org.example.simplefastpost.model.Command;

import java.sql.SQLException;
import java.util.List;

public class CLICommandService {

    public CLICommandRepository cliCommandRepository;

    public CLICommandService(CLICommandRepository cliCommandRepository) {
        this.cliCommandRepository = cliCommandRepository;
    }
    public void createCommand(
            String name,
            String command,
            String body,
            Long folderId
    ) {
        try {
            cliCommandRepository.createCommand(name, command, body, folderId);
            System.out.println("✔ Command created: " + name);
        } catch (Exception e) {
            System.err.println("✖ Failed to create command: " + e.getMessage());
        }
    }

    public void listCommands() {
        try {
            cliCommandRepository.listCommands();
        } catch (Exception e) {
            System.err.println("Failed to list commands: " + e.getMessage());
        }
    }

    public void getCommandByName(String name) {
        try {
            cliCommandRepository.searchCommandsByName(name);
        } catch (Exception e) {
            System.err.println("Failed to fetch command: " + e.getMessage());
        }
    }

    public void updateCommand(
            long id,
            String name,
            String command,
            String body,
            Long folderId
    ) {
        try {
            cliCommandRepository.updateCommand(id, name, command, body, folderId);
            System.out.println("Command updated: " + id);
        } catch (Exception e) {
            System.err.println("Failed to update command: " + e.getMessage());
        }
    }

    public void deleteCommand(long id) {
        try {
            cliCommandRepository.deleteCommand(id);
            System.out.println("Command deleted: " + id);
        } catch (Exception e) {
            System.err.println("Failed to delete command: " + e.getMessage());
        }
    }
    public List<Command> getRootCommands() throws SQLException {
        return cliCommandRepository.getRootCommands();
    }
}
