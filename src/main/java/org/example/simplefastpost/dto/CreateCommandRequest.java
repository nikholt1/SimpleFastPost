package org.example.simplefastpost.dto;

import org.example.simplefastpost.model.Folder;

public record CreateCommandRequest(
        String command,
        Long folderId
) {}
