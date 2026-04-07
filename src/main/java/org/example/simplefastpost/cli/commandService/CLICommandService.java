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
    public Command createCommand(
            String name,
            String command,
            String body,
            Long folderId
    ) {
        try {
            return cliCommandRepository.createCommand(name, command, body, folderId);

        } catch (Exception e) {
            System.err.println("Failed to create command: " + e.getMessage());
        }
        return null;
    }

    public Command createCommandWithExpected(String name, String command, String body, Long folderId, String expected_response, String expected_status) {
        try {
            return cliCommandRepository.createCommandWithExpected(name, command, body, folderId, expected_response, expected_status);
        }catch (Exception e) {
            System.err.println("Failed to create command: " + e.getMessage());
        }
        return null;
    }

    public List<Command> getAllCommands() {
        try {
            return cliCommandRepository.getAllCommands();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public void listCommands() {
        try {
            cliCommandRepository.listCommands();
        } catch (Exception e) {
            System.err.println("Failed to list commands: " + e.getMessage());
        }
    }

    public void searchCommandByName(String name) {
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

        } catch (Exception e) {
            System.err.println("Failed to update command: " + e.getMessage());
        }
    }

    public Command getCommandByName(String commandName) throws SQLException{
        return cliCommandRepository.getCommandByName(commandName);
    }

    public void deleteCommand(long id) {
        try {
            cliCommandRepository.deleteCommand(id);

        } catch (Exception e) {
            System.err.println("Failed to delete command: " + e.getMessage());
        }
    }
    public List<Command> getRootCommands() throws SQLException {
        return cliCommandRepository.getRootCommands();
    }

    public void deleteAllCommands() throws SQLException {
        cliCommandRepository.deleteAllCommands();
    }
}
