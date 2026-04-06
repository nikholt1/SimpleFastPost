package org.example.simplefastpost.dto;

import org.example.simplefastpost.model.Folder;

public record CreateCommandRequest(
        String name,
        String command,
        String body,
        Long folderId,
        String response,
        int status,
        String expected_response,
        int expected_status
) {}
