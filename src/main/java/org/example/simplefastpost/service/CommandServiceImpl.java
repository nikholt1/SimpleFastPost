package org.example.simplefastpost.service;


import org.example.simplefastpost.dto.CommandDTO;
import org.example.simplefastpost.model.Command;
import org.example.simplefastpost.model.Folder;
import org.example.simplefastpost.repository.CommandRepository;
import org.example.simplefastpost.repository.FolderRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CommandServiceImpl implements CommandService {


    private final CommandRepository commandRepository;
    private final FolderRepository folderRepository;
    public CommandServiceImpl(CommandRepository commandRepository, FolderRepository folderRepository) {
        this.commandRepository = commandRepository;
        this.folderRepository = folderRepository;
    }

    @Override
    public List<CommandDTO> getAllCommands() {
        List<Command> commands = commandRepository.findAll();
        List<CommandDTO> commandDTOs = new ArrayList<>();
        for (Command command : commands) {
            commandDTOs.add(CommandMapper.toDto(command));
        }
        return commandDTOs;
    }

    @Override
    public Optional<CommandDTO> getCommandById(Long id) {
        Optional<Command> command = commandRepository.findById(id);
        if (command.isPresent()) {
            return Optional.of(CommandMapper.toDto(command.get()));
        }
        return Optional.empty();
    }

    @Override
    public CommandDTO createCommand(String command, Long folderId) {

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        Command cmd = new Command();
        cmd.setCommand(command);
        cmd.setFolder(folder); // sets the owning side

        Command saved = commandRepository.save(cmd);

        return CommandMapper.toDto(saved);
    }
    @Override
    public void deleteCommand(Long commandId) {

        if (!commandRepository.existsById(commandId)) {
            throw new RuntimeException("Command not found");
        }

        commandRepository.deleteById(commandId);
    }
    @Override
    public CommandDTO updateCommand(Long commandId, String newCommand, Long folderId) {

        Command existing = commandRepository.findById(commandId)
                .orElseThrow(() -> new RuntimeException("Command not found"));

        // update command text
        existing.setCommand(newCommand);

        // update folder if provided
        if (folderId != null) {
            Folder folder = folderRepository.findById(folderId)
                    .orElseThrow(() -> new RuntimeException("Folder not found"));
            existing.setFolder(folder);
        }

        Command saved = commandRepository.save(existing);
        return CommandMapper.toDto(saved);
    }
}
