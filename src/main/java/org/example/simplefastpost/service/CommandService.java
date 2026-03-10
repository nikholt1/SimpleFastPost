package org.example.simplefastpost.service;

import org.example.simplefastpost.dto.CommandDTO;
import org.example.simplefastpost.model.Command;
import org.example.simplefastpost.model.Folder;
import org.example.simplefastpost.repository.CommandRepository;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;



public interface CommandService {
    List<CommandDTO> getAllCommands();
    Optional<CommandDTO> getCommandById(Long id);

    CommandDTO createCommand(
            String name,
            String command,
            String body,
            Long folderId
    );

    CommandDTO updateCommand(
            Long commandId,
            String name,
            String command,
            String body,
            Long folderId
    );

    void deleteCommand(Long commandId);
    void deleteAllCommands();
}
