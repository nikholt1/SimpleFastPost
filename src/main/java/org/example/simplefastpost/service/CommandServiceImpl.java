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
    public void deleteCommand(Long commandId) {

        if (!commandRepository.existsById(commandId)) {
            throw new RuntimeException("Command not found");
        }

        commandRepository.deleteById(commandId);
    }
    @Override
    public CommandDTO createCommand(String name, String command, String body, Long folderId) {

        Command cmd = new Command();
        cmd.setName(name);
        cmd.setCommand(command);
        cmd.setBody(body);

        if (folderId != null) {
            Folder folder = folderRepository.findById(folderId)
                    .orElseThrow(() -> new RuntimeException("Folder not found"));
            cmd.setFolder(folder);
        }

        Command saved = commandRepository.save(cmd);
        return CommandMapper.toDto(saved);
    }
    @Override
    public CommandDTO updateCommand(
            Long commandId,
            String name,
            String command,
            String body,
            Long folderId
    ) {

        Command existing = commandRepository.findById(commandId)
                .orElseThrow(() -> new RuntimeException("Command not found"));

        if (name != null) existing.setName(name);
        if (command != null) existing.setCommand(command);
        if (body != null) existing.setBody(body);

        if (folderId != null) {
            Folder folder = folderRepository.findById(folderId)
                    .orElseThrow(() -> new RuntimeException("Folder not found"));
            existing.setFolder(folder);
        } else {
            existing.setFolder(null); // allow un-assigning
        }

        return CommandMapper.toDto(commandRepository.save(existing));
    }
}
