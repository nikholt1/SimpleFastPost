package org.example.simplefastpost.dto;

import org.example.simplefastpost.model.Command;
import org.example.simplefastpost.model.Folder;

public record CommandDTO(
        Long id,
        String name,
        String command,
        String body,
        Long folderId
) {}

