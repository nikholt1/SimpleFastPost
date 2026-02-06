package org.example.simplefastpost.service;

import org.example.simplefastpost.model.Folder;
import org.example.simplefastpost.repository.FolderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FolderService {

    private FolderRepository folderRepository;

    public FolderService(FolderRepository folderRepository) {
        this.folderRepository = folderRepository;
    }

    public List<Folder> getFolders() {
        return folderRepository.findAll();
    }

    public Folder findFolderById(Long id) {
        return folderRepository.findById(id).get();
    }

    public Folder createFolder(Folder folder) {
        return folderRepository.save(folder);
    }
    public Folder updateFolder(Folder folder) {
        return folderRepository.save(folder);
    }
}
