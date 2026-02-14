package org.example.simplefastpost.dto;

import org.example.simplefastpost.model.Folder;

public record CommandDTO(
        Long id,
        String cmd,
        Folder folder
) {
}
