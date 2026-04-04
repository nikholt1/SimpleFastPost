package org.example.simplefastpost.service;

import org.example.simplefastpost.dto.FolderDTO;
import org.example.simplefastpost.model.Folder;
import org.springframework.stereotype.Service;

@Service
public class FolderMapper {

    public static FolderDTO toDTO(Folder folder) {
        return new FolderDTO(
                folder.getId(),
                folder.getProjectName(),
                folder.getCommands()
        );
    }

    public static Folder fromDTO(FolderDTO dto) {
        Folder folder = new Folder();
        folder.setId(dto.id());
        folder.setProjectName(dto.projectName());
        folder.setCommands(dto.cmds());
        return folder;
    }
}
