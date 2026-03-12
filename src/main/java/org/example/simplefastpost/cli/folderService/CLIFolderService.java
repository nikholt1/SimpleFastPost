package org.example.simplefastpost.cli.folderService;

import org.example.simplefastpost.cli.folderRepo.CLIFolderRepository;
import org.example.simplefastpost.model.Command;
import org.example.simplefastpost.model.Folder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CLIFolderService {

    public CLIFolderRepository cliFolderRepository;

    public CLIFolderService(CLIFolderRepository cliFolderRepository) {
        this.cliFolderRepository = cliFolderRepository;
    }

    public void createFolder(String projectName) {
        try {
            cliFolderRepository.createFolder(projectName);
            System.out.println("Folder created: " + projectName);
        } catch (Exception e) {
            System.err.println("Failed to create folder: " + e.getMessage());
        }
    }

    public void listFolders() {
        try {
            cliFolderRepository.listFolders();
        } catch (Exception e) {
            System.err.println("Failed to list folders: " + e.getMessage());
        }
    }

    public Folder fetchFolderById(Long id) {
        try {
            return cliFolderRepository.fetchFolderById(id);
        }catch (Exception e) {
            System.err.println("Failed to fetch folder: " + e.getMessage());
        }
        return null;
    }

    public void getFolderById(long id) {
        try {
            cliFolderRepository.getFolderById(id);
        } catch (Exception e) {
            System.err.println("Failed to fetch folder: " + e.getMessage());
        }
    }

    public void updateFolder(long id, String newName) {
        try {
            cliFolderRepository.updateFolder(id, newName);
            System.out.println("Folder updated: " + id);
        } catch (Exception e) {
            System.err.println("Failed to update folder: " + e.getMessage());
        }
    }

    public void deleteFolder(long id) {
        try {
            cliFolderRepository.deleteFolder(id);
            System.out.println("Folder deleted: " + id);
        } catch (Exception e) {
            System.err.println("Failed to delete folder: " + e.getMessage());
        }
    }

    public List<Folder> getAllFolders() throws SQLException {
        return cliFolderRepository.getAllFolders();
    }
    public List<Command> getCommandsInFolder(String folderName) {
        List<Command> commands = new ArrayList<>();
        try {
            return cliFolderRepository.getCommandsInFolder(folderName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteAllFolders() throws SQLException {
        cliFolderRepository.deleteAllFolders();
    }


}
