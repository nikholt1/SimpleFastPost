package org.example.simplefastpost.controller;

import org.example.simplefastpost.dto.CommandDTO;
import org.example.simplefastpost.dto.CreateCommandRequest;
import org.example.simplefastpost.dto.FolderDTO;
import org.example.simplefastpost.dto.UpdateCommandRequest;
import org.example.simplefastpost.model.Folder;
import org.example.simplefastpost.model.Headers;
import org.example.simplefastpost.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
public class APIController {

    private final CommandService commandService;
    private final FolderService folderService;
    private final HttpSyncService  httpSyncService;
    private final PushService pushService;
    private final HeaderService headerService;

    public APIController(CommandService commandService, FolderService folderService, HttpSyncService httpSyncService, PushService pushService, HeaderService headerService) {
        this.commandService = commandService;
        this.folderService = folderService;
        this.httpSyncService = httpSyncService;
        this.pushService = pushService;
        this.headerService = headerService;
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
    public ResponseEntity<String> sync() {
        CompletableFuture.runAsync(() -> {
            Path file = Path.of(System.getProperty("user.dir"), "sfp.http");
            try {
                httpSyncService.syncFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return ResponseEntity.ok("Sync started"); // immediate response
    }

    @PostMapping("/push")
    public ResponseEntity<String> push() {
        CompletableFuture.runAsync(() -> {
            Path file = Path.of(System.getProperty("user.dir"), "sfp.http");

            System.out.println("push to " + file);
            try {
                pushService.pushFile(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
//            try {
//                System.out.println("push");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        });

        return ResponseEntity.ok("Sync started");
    }


    @GetMapping("/headers")
    public ResponseEntity<List<Headers>> getAllHeaders() {
        List<Headers> headers = headerService.getAllHeaders();
        return ResponseEntity.ok(headers);
    }

    @GetMapping("/headers/{id}")
    public ResponseEntity<Headers> getHeaderById(@PathVariable Long id) {
        Headers header = headerService.getHeaderById(id);
        return ResponseEntity.ok(header);
    }
    @GetMapping("/headers/byCommand/{commandId}")
    public ResponseEntity<List<Headers>> getHeadersByCommandId(@PathVariable Long commandId) {
        List<Headers> headers = headerService.getHeadersByCommandId(commandId);
        return ResponseEntity.ok(headers);
    }

    @PostMapping("/headers")
    public ResponseEntity<Headers> createHeader(@RequestBody Headers header) {
        Headers saved = headerService.createNewHeader(header);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/headers")
    public ResponseEntity<Void> deleteHeaderById(@PathVariable Long id) {
        headerService.deleteHeaderById(id);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/headers/deleteAll")
    public ResponseEntity<Void> deleteAllHeaders() {
        headerService.deleteAllHeaders();
        return ResponseEntity.noContent().build();
    }



}
