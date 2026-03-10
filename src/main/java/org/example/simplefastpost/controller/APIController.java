package org.example.simplefastpost.controller;

import org.example.simplefastpost.dto.CommandDTO;
import org.example.simplefastpost.dto.CreateCommandRequest;
import org.example.simplefastpost.dto.FolderDTO;
import org.example.simplefastpost.dto.UpdateCommandRequest;
import org.example.simplefastpost.model.Folder;
import org.example.simplefastpost.service.CommandService;
import org.example.simplefastpost.service.FolderService;
import org.example.simplefastpost.service.HttpSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class APIController {

    private final CommandService commandService;
    private final FolderService folderService;
    private final HttpSyncService  httpSyncService;

    public APIController(CommandService commandService, FolderService folderService, HttpSyncService httpSyncService) {
        this.commandService = commandService;
        this.folderService = folderService;
        this.httpSyncService = httpSyncService;
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

    @PutMapping("/folders/{id}")
    public ResponseEntity<FolderDTO> updateFolder(
            @PathVariable Long id,
            @RequestBody FolderDTO folderDTO
    ) {
        FolderDTO updated = folderService.updateFolder(
                id,
                folderDTO.projectName(),
                folderDTO.cmds()
        );
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/folders/{id}")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long id) {
        folderService.deleteFolder(id);
        return ResponseEntity.noContent().build();
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
                request.name(),
                request.command(),
                request.body(),
                request.folderId()
        );
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/commands/{id}")
    public ResponseEntity<CommandDTO> updateCommand(
            @PathVariable Long id,
            @RequestBody UpdateCommandRequest request
    ) {
        CommandDTO updated = commandService.updateCommand(
                id,
                request.name(),
                request.command(),
                request.body(),
                request.folderId()
        );
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/commands/{id}")
    public ResponseEntity<Void> deleteCommand(@PathVariable Long id) {
        commandService.deleteCommand(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sync")
    public ResponseEntity<String> sync() throws IOException {

        String root = System.getProperty("user.dir");
        Path file = Path.of(root, "sfp.http");

        if (!Files.exists(file)) {
            return ResponseEntity
                    .status(404)
                    .body("No sfp.http file found in: " + root);
        }

        boolean run = httpSyncService.syncFile(file);
        if (run){
            return ResponseEntity.ok("Sync completed");
        } else
            return ResponseEntity.notFound().build();
    }

}
