package org.example.simplefastpost.crawler.cli;

import org.example.simplefastpost.cli.commandService.CLICommandService;
import org.example.simplefastpost.cli.commandService.HeaderService;
import org.example.simplefastpost.cli.folderRepo.CLIFolderRepository;
import org.example.simplefastpost.cli.folderService.CLIFolderService;
import org.example.simplefastpost.model.CrawlerEntity;
import org.example.simplefastpost.model.Folder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CrawlerToCommandConverter {
    public CLIFolderService folderService;
    public CLICommandService commandService;
    public HeaderService headerService;
    public CLICrawlerService crawlerService;

    public CrawlerToCommandConverter(CLIFolderService folderService, CLICommandService commandService, HeaderService headerService, CLICrawlerService crawlerService) {
        this.folderService = folderService;
        this.commandService = commandService;
        this.headerService = headerService;
        this.crawlerService = crawlerService;
    }

    public void convert(Long id, String folderName) throws SQLException {

        System.out.println("\u001B[48;5;129m" +
                "Starting build in folder: " + folderName +
                " From session ID: " + id +
                "\u001B[0m");

        // 1️⃣ Check folder existence
        List<Folder> folders = folderService.getAllFolders();
        Folder folderToBuildIn = null;
        boolean folderExists = false;
        for (Folder folder : folders) {
            if (folder.getProjectName().equals(folderName)) {
                System.out.println("Folder with name: " + folderName + " already exists, building into existing folder");
                folderExists = true;
                folderToBuildIn = folder;
            }
        }

        if (!folderExists) {
            folderToBuildIn = folderService.createFolder(folderName);
        }

        // 2️⃣ Create folder

        Long folderId = folderToBuildIn.getId();

        System.out.println("Created folder ID: " + folderId);

        // 3️⃣ Load entities
        List<CrawlerEntity> crawlerEntities =
                crawlerService.getAllEntitiesBySessionId(id);

        if (crawlerEntities.isEmpty()) {
            System.out.println("Session: " + id + " is empty or does not exist.");
            return;
        }

        // 4️⃣ Filter APIs + REMOVE DUPLICATES (LAST WINS)
        Map<String, CrawlerEntity> uniqueMap = new LinkedHashMap<>();

        for (CrawlerEntity e : crawlerEntities) {
            if (!"api".equalsIgnoreCase(e.getType())) continue;

            String key = e.getMethod().toUpperCase().trim()
                    + ":" + e.getUrl().trim();

            uniqueMap.put(key, e); // last occurrence wins
        }

        List<CrawlerEntity> apiEntities = new ArrayList<>(uniqueMap.values());

        // 5️⃣ Debug print (optional)
        for (CrawlerEntity e : apiEntities) {
            System.out.println(e.getMethod() + " " + e.getUrl());
        }

        // 6️⃣ Progress setup
        int total = apiEntities.size();
        if (total == 0) {
            System.out.println("No API entities found.");
            printProgressBar(100);
            System.out.println();
            return;
        }

        int processed = 0;

        // 7️⃣ Build commands
        for (CrawlerEntity e : apiEntities) {

            String commandName = e.getMethod().toUpperCase()
                    + ":" + e.getUrl();

            String command = "-m " + e.getMethod()
                    + " -u " + e.getUrl();

            commandService.createCommand(
                    commandName,
                    command,
                    null,
                    folderId
            );

            // 8️⃣ progress bar
            processed++;
            int percent = (int) ((processed * 100.0) / total);
            printProgressBar(percent);
        }

        System.out.println(); // newline after progress bar
    }

    private void printProgressBar(int percent) {
        int barLength = 30;
        int filled = (percent * barLength) / 100;

        String bar = "[" + "\u001B[32m" + "=".repeat(filled) + " ".repeat(barLength - filled) + "\u001B[0m" + "] " + percent + "%";
        System.out.print("\r" + bar);
        System.out.flush();
    }
}
