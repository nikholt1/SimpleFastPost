package org.example.simplefastpost.controller;

import org.example.simplefastpost.dto.CommandDTO;
import org.example.simplefastpost.dto.CreateCommandRequest;
import org.example.simplefastpost.dto.FolderDTO;
import org.example.simplefastpost.model.Folder;
import org.example.simplefastpost.service.CommandService;
import org.example.simplefastpost.service.FolderService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class APIController {

    private final CommandService commandService;
    private final FolderService folderService;

    public APIController(CommandService commandService, FolderService folderService) {
        this.commandService = commandService;
        this.folderService = folderService;
    }

    @GetMapping("/folders")
    public List<FolderDTO> getAllFolders() {
        return folderService.getAllFolders();
    }

    @GetMapping("/folders/{id}")
    public ResponseEntity<FolderDTO> getFolderById(@PathVariable Long id) {
        Optional<FolderDTO> folder =  folderService.getFolderById(id);
        if (folder.isPresent()) {
            return ResponseEntity.ok(folder.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/folder")
    public ResponseEntity<FolderDTO> createFolder(@RequestBody FolderDTO folderDTO) {
        folderService.createFolder(
                folderDTO.projectName(),
                folderDTO.cmds()
        );
        return ResponseEntity.ok(folderDTO);
    }





    @GetMapping("/commands")
    public List<CommandDTO> getAllCommands() {
        return commandService.getAllCommands();
    }

    @GetMapping("/commands/{id}")
    public ResponseEntity<CommandDTO> getCommandById(@PathVariable Long id) {
        Optional<CommandDTO> command = commandService.getCommandById(id);
        if (command.isPresent()) {
            return ResponseEntity.ok(command.get());
        } else {
            return ResponseEntity.notFound().build();
        }

    }

    @PostMapping("/command")
    public ResponseEntity<CommandDTO> createCommand(
            @RequestBody CreateCommandRequest request
    ) {
        CommandDTO saved = commandService.createCommand(
                request.command(),
                request.folderId()
        );
        return ResponseEntity.ok(saved);
    }



}
