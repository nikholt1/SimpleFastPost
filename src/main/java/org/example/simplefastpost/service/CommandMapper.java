package org.example.simplefastpost.service;

import org.example.simplefastpost.dto.CommandDTO;
import org.example.simplefastpost.model.Command;
import org.example.simplefastpost.model.Folder;
import org.springframework.stereotype.Service;

@Service
public class CommandMapper {

    public static CommandDTO toDto(Command command) {
        return new CommandDTO(
                command.getId(),
                command.getCommand(),
                command.getName(),
                command.getBody(),
                command.getFolder() != null ? command.getFolder().getId() : null
        );
    }


    public static Command fromDTO(CommandDTO dto, Folder folder) {
        Command command = new Command();
        command.setId(dto.id());
        command.setCommand(dto.command());
        command.setName(dto.name());
        command.setBody(dto.body());
        command.setFolder(folder);
        return command;
    }
}
