package org.example.simplefastpost.dto;

import org.example.simplefastpost.model.Command;
import org.example.simplefastpost.model.Folder;

public record CommandDTO(
        Long id,
        String command,
        Long folderId
) {
    public CommandDTO(Command command) {
        this(command.getId(), command.getCommand(), command.getFolder().getId());
    }
}

