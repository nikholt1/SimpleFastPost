package org.example.simplefastpost.dto;

import org.example.simplefastpost.model.Folder;

public record CreateCommandRequest(
        String name,
        String command,
        String body,
        Long folderId
) {}
