package org.example.simplefastpost.dto;

public record CreateHeaderRequest(
        Long commandId,
        String key,
        String value

) {
}
