package org.example.simplefastpost.service;

import org.example.simplefastpost.dto.FolderDTO;
import org.example.simplefastpost.model.Command;
import org.example.simplefastpost.model.Folder;
import org.example.simplefastpost.repository.FolderRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FolderServiceImpl implements FolderService {
    private final FolderRepository folderRepository;
    public FolderServiceImpl(FolderRepository folderRepository) {
        this.folderRepository = folderRepository;
    }
    @Override
    public List<FolderDTO> getAllFolders() {
        List<Folder> folders = folderRepository.findAll();
        List<FolderDTO> folderDTOs = new ArrayList<>();
        for (Folder folder : folders) {
            folderDTOs.add(FolderMapper.toDTO(folder));
        }
        return folderDTOs;

    }
    @Override
    public Optional<FolderDTO> getFolderById(Long id) {
        Optional<Folder> folder = folderRepository.findById(id);
        if (folder.isPresent()) {
            return Optional.of(FolderMapper.toDTO(folder.get()));
        }
        return Optional.empty();
    }

    @Override
    public FolderDTO createFolder(String projectName, List<Command> commands) {
        Folder folder = new Folder();
        folder.setProjectName(projectName);
        folder.setCommands(commands);
        folderRepository.save(folder);
        return FolderMapper.toDTO(folder);
    }
}
