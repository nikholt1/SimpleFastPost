package org.example.simplefastpost.dto;

import org.example.simplefastpost.model.Command;

import java.util.List;

public record CreateFolderRequest(
        String projectName,
        List<Command> commands
) {
}
