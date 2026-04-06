package org.example.simplefastpost.dto;

public record UpdateCommandRequest(
        String name,
        String command,
        String body,
        Long folderId,
        String response,
        int status,
        String expected_response,
        int expected_status
) {}