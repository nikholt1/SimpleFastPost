package org.example.simplefastpost.service;

import org.example.simplefastpost.dto.CommandDTO;
import org.example.simplefastpost.model.Command;
import org.springframework.stereotype.Service;

@Service
public class CommandMapper {

    public static CommandDTO toDto(Command command) {
        return new CommandDTO(
                command.getId(),
                command.getCommand(),
                command.getFolder() != null ? command.getFolder().getId() : null
        );
    }
}
