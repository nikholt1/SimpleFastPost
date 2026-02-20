package org.example.simplefastpost.dto;

public record UpdateCommandRequest(
        String name,
        String command,
        String body,
        Long folderId
) {}