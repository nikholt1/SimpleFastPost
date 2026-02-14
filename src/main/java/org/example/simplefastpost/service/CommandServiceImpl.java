package org.example.simplefastpost.service;


import org.example.simplefastpost.dto.CommandDTO;
import org.example.simplefastpost.model.Command;
import org.example.simplefastpost.repository.CommandRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CommandServiceImpl implements CommandService {

    CommandRepository commandRepository;

    public CommandServiceImpl(CommandRepository commandRepository) {
        this.commandRepository = commandRepository;
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

}
