package org.example.simplefastpost.service;

import org.example.simplefastpost.dto.FolderDTO;
import org.example.simplefastpost.model.Command;
import org.example.simplefastpost.model.Folder;
import org.example.simplefastpost.repository.FolderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


public interface FolderService {
    List<FolderDTO> getAllFolders();
    Optional<FolderDTO> getFolderById(Long id);
    FolderDTO createFolder(String projectName, List<Command> commands);
    FolderDTO updateFolder(Long folderId, String projectName, List<Command> commands);
    void deleteFolder(Long folderId);
    void deleteAllFolders();
}
