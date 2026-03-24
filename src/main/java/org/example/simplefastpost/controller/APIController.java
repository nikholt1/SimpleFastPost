package org.example.simplefastpost.controller;

import org.example.simplefastpost.dto.*;
import org.example.simplefastpost.model.Folder;
import org.example.simplefastpost.model.Headers;
import org.example.simplefastpost.model.History;
import org.example.simplefastpost.service.*;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private final RestTemplate restTemplate = new RestTemplate();
    private final HistoryService historyService;
    public APIController(CommandService commandService, FolderService folderService, HttpSyncService httpSyncService, PushService pushService, HeaderService headerService, HistoryService historyService) {
        this.commandService = commandService;
        this.folderService = folderService;
        this.httpSyncService = httpSyncService;
        this.pushService = pushService;
        this.headerService = headerService;
        this.historyService = historyService;
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

    @DeleteMapping("/headers/{id}")
    public ResponseEntity<Void> deleteHeaderById(@PathVariable Long id) {
        headerService.deleteHeaderById(id);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/headers/deleteAll")
    public ResponseEntity<Void> deleteAllHeaders() {
        headerService.deleteAllHeaders();
        return ResponseEntity.noContent().build();
    }



    @PostMapping("/executeCommand")
    public ResponseEntity<String> executeCommand(@RequestBody CommandRequest commandRequest) {
        try {
            String url = commandRequest.getUrl();
            // If no scheme, default to http://
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }

            // Build HTTP headers
            HttpHeaders headers = new HttpHeaders();
            if (commandRequest.getHeaders() != null) {
                commandRequest.getHeaders().forEach(headers::add);
            }

            HttpEntity<String> entity = new HttpEntity<>(commandRequest.getBody(), headers);

            HttpMethod httpMethod = HttpMethod.valueOf(commandRequest.getMethod().toUpperCase());

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    httpMethod,
                    entity,
                    String.class
            );

            return ResponseEntity
                    .status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Network error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    @GetMapping("/currentDir")
    public Map<String, String> getCurrentDirectory() {
        String currentDir = System.getProperty("user.dir");
        return Collections.singletonMap("currentDirectory", currentDir);
    }


    @PostMapping("/history")
    public History createNewHistoryEntity(@RequestBody History history) {
        return historyService.createNewHistoryEntity(history);
    }

    @GetMapping("/history/{id}")
    public History getHistoryById(@PathVariable Long id) {
        return historyService.getHistoryById(id);
    }

    @GetMapping("/history")
    public List<History> getAllHistory() {
        return historyService.getAllHistoryEntities();
    }

    @GetMapping("/history/comamnd/{commandId}")
    public List<History> getHistoryByCommandId(@PathVariable Long commandId) {
        return historyService.getHistoryByCommandId(commandId);
    }

    @DeleteMapping("/history/{id}")
    public ResponseEntity<String> deleteHistoryById(@PathVariable Long id) {
        historyService.deleteHistoryEntityById(id);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/history")
    public ResponseEntity<String> deleteHistoryById() {
        historyService.deleteAllHistory();
        return ResponseEntity.noContent().build();
    }
}
