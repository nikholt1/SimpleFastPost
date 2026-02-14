package org.example.simplefastpost.dto;

import org.example.simplefastpost.model.Command;

import java.util.List;

public record FolderDTO(
        Long id,
        String projectName,
        List<Command> cmds
) {
}
