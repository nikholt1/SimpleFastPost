package org.example.simplefastpost.cli;

import org.example.simplefastpost.cli.OSService.OSService;
import org.example.simplefastpost.cli.RequestController.RequestController;
import org.example.simplefastpost.cli.apiRepository.ApiRepository;
import org.example.simplefastpost.cli.apiService.ApiService;
import org.example.simplefastpost.cli.commandRepo.CLICommandRepository;
import org.example.simplefastpost.cli.commandRepo.HeaderRepository;
import org.example.simplefastpost.cli.commandService.CLICommandService;
import org.example.simplefastpost.cli.commandService.HeaderService;
import org.example.simplefastpost.cli.config.CLIconfig;
import org.example.simplefastpost.cli.folderRepo.CLIFolderRepository;
import org.example.simplefastpost.cli.folderService.CLIFolderService;
import org.example.simplefastpost.cli.historyRepository.HistoryRepository;
import org.example.simplefastpost.cli.historyService.HistoryService;
import org.example.simplefastpost.cli.syncService.HttpSyncService;
import org.example.simplefastpost.cli.syncService.PushService;
import org.example.simplefastpost.crawler.cli.CLICrawler;
import org.example.simplefastpost.crawler.cli.CLICrawlerRepository;
import org.example.simplefastpost.crawler.cli.CLICrawlerService;
import org.example.simplefastpost.model.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;


public class CLIExecutor {

    private static final int MAX_RECURSION_DEPTH = 5;

    private String DB_URL;

    private final CLICommandRepository cliCommandRepository;
    private final CLIFolderRepository cliFolderRepository;
    private final CLICommandService cliCommandService;
    private final CLIFolderService cliFolderService;
    private final HttpSyncService syncService;
    private final PushService pushService;
    private boolean replayMode = false;
    private final RequestController requestController;
    private final CLIconfig clIconfig = new CLIconfig();
    private final HeaderService headerService;
    private final HeaderRepository headerRepository;
    private final HistoryService historyService;
    private final HistoryRepository historyRepository;
    private final CLICrawlerRepository cliCrawlerRepository;
    private final CLICrawlerService cliCrawlerService;
    private final CLICrawler crawler;

    public CLIExecutor() throws Exception {

        try {
            this.DB_URL = clIconfig.getDBUrl();

        } catch (Exception e) {
            e.printStackTrace();
        }
        //this.DB_URL = DB_URL;
        this.cliCommandRepository = new CLICommandRepository(DB_URL);
        this.cliFolderRepository = new CLIFolderRepository(DB_URL);
        this.cliCommandService = new CLICommandService(cliCommandRepository);
        this.cliFolderService = new CLIFolderService(cliFolderRepository);
        this.headerRepository = new HeaderRepository(DB_URL);
        this.headerService = new HeaderService(headerRepository);
        this.pushService = new PushService(cliFolderService, cliCommandService, headerService);
        this.syncService = new HttpSyncService(cliCommandService, cliFolderService, headerService);
        this.historyRepository = new HistoryRepository(DB_URL);
        this.historyService = new HistoryService(historyRepository);
        this.requestController = new RequestController(historyService, headerService);


        this.cliCrawlerRepository = new CLICrawlerRepository(DB_URL);
        this.cliCrawlerService = new CLICrawlerService(cliCrawlerRepository);
        this.crawler = new CLICrawler(cliCrawlerService, cliCrawlerRepository);
    }

    public void execute(String[] rawArgs) throws Exception {
        execute(rawArgs, 0, false);
    }

    private void execute(String[] rawArgs, int depth, boolean replayMode) throws Exception {
        CLIconfig cliConfig = new CLIconfig();
        ApiRepository apiRepository = new ApiRepository(cliConfig.getDBUrl());
        OSService osService = new OSService();
        ApiService apiService = new ApiService(cliConfig, apiRepository, osService);


        this.replayMode = replayMode;

        if (depth > MAX_RECURSION_DEPTH) {
            System.err.println("Max command recursion depth reached.");
            return;
        }

        if (rawArgs.length == 0) {
            System.out.println("Usage: <command> [options]");
            return;
        }

        String primaryCommand = null;
        int i = 0;

        if (!rawArgs[0].startsWith("-")) {
            primaryCommand = rawArgs[0].toLowerCase();
            i = 1;
        }

        boolean saveMode = false;
        boolean helpMode = false;
        boolean updateMode = false;
        String commandName = null;
        String folderName = null;
        String method = "GET";
        String url = "";
        String body = null;
        String updateTarget = null;
        String updateField = null;
        String updateValue = null;
        boolean testMode = false;

        for (; i < rawArgs.length; i++) {
            String arg = rawArgs[i];

            if (arg.equalsIgnoreCase("-m") && i + 1 < rawArgs.length) {
                method = rawArgs[++i].toUpperCase();
            } else if (arg.equalsIgnoreCase("-u") && i + 1 < rawArgs.length) {
                url = rawArgs[++i];
            } else if (arg.equalsIgnoreCase("-sv")) {
                saveMode = true;
            }else if (arg.equalsIgnoreCase("-t")) {
                testMode = true;
            } else if (arg.equalsIgnoreCase("-f") && i + 1 < rawArgs.length) {
                folderName = rawArgs[++i];
            } else if ((arg.equalsIgnoreCase("-cmd") || arg.equalsIgnoreCase("-scmd"))
                    && i + 1 < rawArgs.length) {
                commandName = rawArgs[++i];
            } else if (arg.equalsIgnoreCase("--help")) {
                helpMode = true;
            } else if (arg.equalsIgnoreCase("--version")) {
                System.out.println("""
                                   
                          =++===    
                       +===+++====+  
                     =+====++======+= 
                     =====+++++====== 
                  =++====++++=++++===+==
                   +===   +    +   +==+
                    +=    +    =    == 
                     ++=          += 
                        +++    =+=   
                """);
                System.out.println("sfp version: 0.0.1 alpha");
                return;
            } else if (arg.equalsIgnoreCase("api")) {

                if (i + 1 < rawArgs.length) {
                    String subCommand = rawArgs[++i];

                    if (subCommand.equalsIgnoreCase("ls")) {

                        apiService.listAllInstances();

                    } else if (subCommand.equalsIgnoreCase("delete") && i + 1 < rawArgs.length) {
                        try {
                            Long pid = Long.parseLong(rawArgs[++i]);
                            apiService.stopApiDaemon(pid);
                            System.out.println("Stopped API instance with PID " + pid);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid PID: " + rawArgs[i]);
                        } catch (Exception e) {
                            System.out.println("Failed to stop API instance: " + e.getMessage());
                        }

                    } else {
                        System.out.println("Unknown API subcommand. Use 'ls' or 'delete <pid>'.");
                    }
                } else {

                    System.out.println("No API subcommand provided. Use 'ls' or 'delete <pid>'.");
                }
            }


        }

        if (helpMode) {
            printHelp();
            return;
        }

        if (primaryCommand != null) {
            switch (primaryCommand) {

                case "ls":
                    if (folderName != null) {
                        List<Command> cmds =
                                cliFolderRepository.getCommandsInFolder(folderName);
                        if (cmds.isEmpty()) {
                            System.out.println("No folders or commands found in DB");
                        }
                        cmds.forEach(c ->
                                System.out.println(c.getName() + " -> " + c.getCommand()));
                    } else {
                        List<Folder> folders = cliFolderService.getAllFolders();
                        List<Command> commands = cliCommandService.getAllCommands();

                        for (Folder folder  : folders) {
                            System.out.println("[] " + folder.getProjectName());
                            for (Command command : commands) {
                                if (command.getFolderId() == folder.getId()) {
                                    System.out.printf("         Name: %s | Command: %s%n", command.getName(), command.getCommand());

                                    List<Headers> headers = headerService.getAllHeadersFromCommandId(command.getId());
                                    if (!headers.isEmpty()) {
                                        for (Headers header : headers) {
                                            System.out.println("         " + header.getKey() + ": " + header.getValue());
                                        }
                                    }
                                    String commandBody = command.getBody();
                                    if (commandBody != null && !commandBody.isBlank()) {
                                        String[] lines = commandBody.split("\\R");
                                        for (String line : lines) {
                                            System.out.println("         ---> " + line);
                                        }
                                        System.out.println();
                                    } else {
                                        System.out.println();
                                    }
                                }
                            }
                        }
                        for (Command command : commands) {
                            if (command.getFolderId() == null) {
                                System.out.println("\n{} Root Commands");
                                System.out.printf("Name: %s | Command: %s%n", command.getName(), command.getCommand());

                                List<Headers> headers = headerService.getAllHeadersFromCommandId(command.getId());
                                if (!headers.isEmpty()) {
                                    for (Headers header : headers) {
                                        System.out.println(header.getKey() + ": " + header.getValue());
                                    }
                                }

                                String commandBody = command.getBody();
                                if (commandBody != null && !commandBody.isBlank()) {
                                    String[] lines = commandBody.split("\\R");
                                    for (String line : lines) {
                                        System.out.println("---> " + line);
                                    }

                                } else {
                                    System.out.println();
                                }

                            }
                        }

                    }
                    return;
                case "crawl":
                    if (rawArgs.length < 2) {
                        System.out.println("Usage: crawl <url> | crawl history [<sessionId>]");
                        break;
                    }

                    // ===== SHOW HISTORY =====
                    // ===== SHOW HISTORY =====
                    if ("history".equalsIgnoreCase(rawArgs[1])) {

                        // 🔥 PURGE ALL SESSIONS
                        if (rawArgs.length == 3 && "purge".equalsIgnoreCase(rawArgs[2])) {
                            cliCrawlerService.deleteAllSessions();
                            System.out.println("All crawl sessions deleted.");
                            return;
                        }

                        // List all sessions
// List all sessions
                        if (rawArgs.length == 2) {
                            List<CrawlerSession> sessions = cliCrawlerService.getAllSessions();

                            if (sessions == null || sessions.isEmpty()) {
                                System.out.println("History empty.");
                                return;
                            }

                            System.out.println("===== ALL CRAWL SESSIONS =====");

                            for (CrawlerSession s : sessions) {
                                System.out.println("Session ID: " + s.getId() +
                                        ", Title: " + s.getTitle() +
                                        ", Entities: " + (s.getCrawlerEntities() != null ? s.getCrawlerEntities().size() : 0));
                            }
                        }

                        // Show detailed session
                        else if (rawArgs.length == 3) {
                            try {
                                Long sessionId = Long.parseLong(rawArgs[2]);
                                CrawlerSession session = cliCrawlerService.getSessionById(sessionId);

                                if (session == null) {
                                    System.out.println("Session not found: ID " + sessionId);
                                    return;
                                }

                                System.out.println("===== SESSION DETAIL =====");
                                System.out.println("Session ID: " + session.getId() + ", Title: " + session.getTitle());

                                if (session.getCrawlerEntities() != null) {
                                    LinkedList<CrawlerEntity> stack = new LinkedList<>(session.getCrawlerEntities());
                                    LinkedList<String> prefixStack = new LinkedList<>();
                                    for (int l = 0; l < stack.size(); l++) prefixStack.add("|_ ");

                                    while (!stack.isEmpty()) {
                                        CrawlerEntity entity = stack.pollFirst();
                                        String prefix = prefixStack.pollFirst();
                                        System.out.println(prefix + entity.getType() + ": " + entity.getTitle() + " " + entity.getMethod());

                                        if (entity.getConnections() != null) {
                                            for (String conn : entity.getConnections()) {
                                                System.out.println(prefix + "   -> Connection: " + conn);
                                            }
                                        }

                                        if (entity.getChildren() != null) {
                                            for (CrawlerEntity child : entity.getChildren()) {
                                                stack.addFirst(child);
                                                prefixStack.addFirst(prefix + "   ");
                                            }
                                        }
                                    }
                                }

                            } catch (NumberFormatException e) {
                                System.out.println("Invalid session ID: " + rawArgs[2]);
                            }
                        }

                        else {
                            System.out.println("Usage: crawl history [<sessionId>|purge]");
                        }

                        return;
                    }

                    // ===== DELETE SESSION =====
                    if ("delete".equalsIgnoreCase(rawArgs[1])) {

                        if (rawArgs.length != 3) {
                            System.out.println("Usage: crawl delete <sessionId>");
                            return;
                        }

                        try {
                            Long sessionId = Long.parseLong(rawArgs[2]);

                            CrawlerSession session = cliCrawlerService.getSessionById(sessionId);
                            if (session == null) {
                                System.out.println("Session not found: ID " + sessionId);
                                return;
                            }

                            cliCrawlerService.deleteSession(sessionId);
                            System.out.println("Deleted session ID: " + sessionId);

                        } catch (NumberFormatException e) {
                            System.out.println("Invalid session ID: " + rawArgs[2]);
                        }

                        return; // 🔥 VERY IMPORTANT
                    }
                    // ===== RUN NEW CRAWL =====
                    else {
                        String baseUrl = rawArgs[1];
                        crawler.run(baseUrl);

                        History crawlHistoryEntry = new History();
                        crawlHistoryEntry.setUrl(baseUrl);
                        crawlHistoryEntry.setAction("crawl -> " + baseUrl);
                        historyService.createHistoryEntity(crawlHistoryEntry);

                        // Optional: print persistent session immediately
                        List<CrawlerSession> sessions = cliCrawlerService.getAllSessions();
                        CrawlerSession latestSession = sessions.get(sessions.size() - 1);

                        System.out.println("\n===== NEW SESSION DETAIL =====");
                        System.out.println("Session ID: " + latestSession.getId() + ", Title: " + latestSession.getTitle());

                        if (latestSession.getCrawlerEntities() != null) {
                            LinkedList<CrawlerEntity> stack = new LinkedList<>(latestSession.getCrawlerEntities());
                            LinkedList<String> prefixStack = new LinkedList<>();
                            for (int j = 0; j < stack.size(); j++) prefixStack.add("|_ ");

                            while (!stack.isEmpty()) {
                                CrawlerEntity entity = stack.pollFirst();
                                String prefix = prefixStack.pollFirst();
                                System.out.println(prefix + entity.getType() + ": " + entity.getTitle());

                                if (entity.getConnections() != null) {
                                    for (String conn : entity.getConnections()) {
                                        System.out.println(prefix + "   -> Connection: " + conn);
                                    }
                                }

                                if (entity.getChildren() != null) {
                                    for (CrawlerEntity child : entity.getChildren()) {
                                        stack.addFirst(child);
                                        prefixStack.addFirst(prefix + "   ");
                                    }
                                }
                            }
                        }
                    }
                    return;
                case "api":
                    if (rawArgs.length < 2) {
                        System.out.println("Usage: api [ls|kill <pid>]");
                        return;
                    }

                    String subCommand = rawArgs[1];

                    if (subCommand.equals("ls")) {
                        apiService.listAllInstances();
                        return;
                    } else if (subCommand.equals("kill")) {
                        if (rawArgs.length < 3) {
                            System.out.println("Usage: api kill <pid>");
                            return;
                        }

                        try {
                            Long pid = Long.parseLong(rawArgs[2]);
                            apiService.stopApiDaemon(pid);
                            System.out.println("Stopped API instance with PID: " + pid);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid PID: " + rawArgs[2]);
                        } catch (Exception e) {
                            System.err.println("Failed to stop API: " + e.getMessage());
                        }
                        return;
                    } else {
                        System.out.println("Unknown api subcommand: " + subCommand);
                        return;
                    }

                case "history":
                    List<History> historyList = historyService.getAllHistory();

                    String subHistoryCommand = rawArgs.length > 1 ? rawArgs[1] : "";

                    if ("purge".equalsIgnoreCase(subHistoryCommand)) {
                        historyService.deleteAllHistory();
                        System.out.println("All history purged.");
                        return;
                    }

                    if ("reverse".equalsIgnoreCase(subHistoryCommand)) {
                        historyList.sort((h1, h2) -> h1.getCreated().compareTo(h2.getCreated()));
                    } else {
                        historyList.sort((h1, h2) -> h2.getCreated().compareTo(h1.getCreated()));
                    }

                    for (History historyItem : historyList) {
                        if (historyItem.getResponse() == null) {
                            System.out.println("___________________________________________");
                            System.out.println(historyItem.getAction() + " " + historyItem.getCreated());
                            System.out.println("___________________________________________");
                        } else {
                            System.out.println(historyItem.getUrl() + " " + historyItem.getCreated());
                            System.out.println(historyItem.getResponse());
                        }
                        System.out.println();
                    }
                    return;
                case "push":
                    String findRoot = System.getProperty("user.dir");
                    Path fileToFind = Path.of(findRoot, "sfp.http");

                    if (!Files.exists(fileToFind)) {
                        System.out.println("File not found in root directory");
                    }
                    int runner = pushService.pushFile(fileToFind);
                    System.out.println("Found " + runner + " commands to build in root .http file");
                    History historyPush = new History();
                    historyPush.setCreated(LocalDateTime.now());
                    historyPush.setAction("---> push");

                    historyService.createHistoryEntity(historyPush);

                    return;
                case "sync":
                    String root = System.getProperty("user.dir");
                    Path file = Path.of(root, "sfp.http");

                    if (!Files.exists(file)) {
                        System.out.println("File not found in root directory");
                    }

                    int run = syncService.syncFile(file);
                    System.out.println("Found " + run + " commands to build from .http file to root");
                    History historySync = new History();
                    historySync.setCreated(LocalDateTime.now());
                    historySync.setAction("<--- sync");

                    historyService.createHistoryEntity(historySync);

                    return;
                case "mkdir":
                    if (folderName == null) {
                        System.err.println("mkdir requires -f <folder>");
                        return;
                    }
                    cliFolderService.createFolder(folderName);
                    System.out.println("Folder created: " + folderName);
                    return;

                case "run":

                    // RUN ALL COMMANDS IN A FOLDER
                    if (folderName != null && commandName == null) {

                        List<Command> commands = cliFolderRepository.getCommandsInFolder(folderName);

                        if (commands.isEmpty()) {

                            System.err.println("No commands found in folder: " + folderName);
                            return;
                        }


                        System.out.println("Running folder: " + folderName);

                        if (testMode) {
                            try {
                                requestController.sendRequests(commands); // runs all commands with expected checks
                            } catch (Exception e) {
                                e.printStackTrace();
                                System.exit(1); // fail if something goes wrong
                            }
                        } else {
                            // run all commands without expected checks
                            for (Command command : commands) {

                                if (command.getCommand().contains("run")) {
                                    System.err.println("Skipping recursive command: " + command.getName());
                                    continue;
                                }

                                String commandStr = command.getCommand();
                                String[] parts = commandStr.split("\\s+");
//                                String method = null;
//                                String url = null;

                                for (int k = 0; k < parts.length; k++) {
                                    if (parts[k].equals("-m") && k + 1 < parts.length) {
                                        method = parts[k + 1];
                                    }
                                    if (parts[k].equals("-u") && k + 1 < parts.length) {
                                        url = parts[k + 1];
                                    }
                                }

                                try {
                                    requestController.sendRequest(
                                            method,
                                            url,
                                            command.getBody(),
                                            headerService.getAllHeadersFromCommandId(command.getId()),
                                            command.getId()
                                    );
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        return;
                    }

                    // RUN SINGLE COMMAND
                    if (commandName == null) {
                        System.err.println("run requires -scmd <commandName> OR -f <folder>");
                        return;
                    }

                    Command cmd = resolveCommand(commandName, folderName);

                    if (cmd == null) {
                        System.err.println("Command not found: " + commandName);
                        return;
                    }

                    if (cmd.getCommand().contains("run -scmd")) {
                        System.err.println("Refusing recursive command.");
                        return;
                    }

                    System.out.println("Re-running:");
                    System.out.println("> " + cmd.getCommand());

                    requestController.sendRequest(method, url, body, headerService.getAllHeadersFromCommandId(cmd.getId()), cmd.getId());
//                    execute(tokenize(cmd.getCommand()), depth + 1, true);
                    return;
                case "delete":
                    String flag = rawArgs[1].toLowerCase();

                    if (flag.equals("-cmd")) {


                        if (rawArgs.length > 3 && "header".equalsIgnoreCase(rawArgs[3])) {

                            if (rawArgs.length > 4) {
                                String headerTarget = rawArgs[4];
                                Headers header = headerService.getHeaderByName(headerTarget);
                                if (header == null) {
                                    System.out.println("Header not found in command");
                                    return;
                                }
                                System.out.println("Deleting header '" + header.getKey() + "'");
                                headerService.deleteHeader(header);

                            } else {

                                System.out.println("Please specify a header name");
                            }

                            return;
                        }


                        if (rawArgs.length > 2) {
                            String commandTarget = rawArgs[2];

                            Command command = cliCommandService.getCommandByName(commandTarget);

                            if (command == null) {
                                System.out.println("Command not found, or multiple commands found");
                            } else {
                                cliCommandService.deleteCommand(command.getId());
                            }
                            return;
                        }

                        System.out.println("Please specify a command name");
                        return;

                    } else if (flag.equals("-f")) {

                        if (rawArgs.length > 2) {
                            String folderTarget = rawArgs[2];
                            List<Folder> folders = cliFolderService.getAllFolders();

                            for (Folder folder : folders) {
                                if (folder.getProjectName().equals(folderTarget)) {
                                    cliFolderService.deleteFolder(folder.getId());
                                }
                            }
                            return;
                        }

                        System.out.println("Please specify a folder name");
                        return;
                    }

                    System.out.println("delete requires specification of -cmd or -f");
                case "update":
                    if (rawArgs.length < 3) {
                        System.err.println("Usage: update <command|folder> <header> <headerkey> <name> [options]");
                        return;
                    }

                    updateTarget = rawArgs[1].toLowerCase();
                    String targetName = rawArgs[2];

                    Long id = null;
                    Long folderId = null;


                    for (int j = 3; j < rawArgs.length; j++) {
                        if (rawArgs[j].equalsIgnoreCase("--set") && j + 1 < rawArgs.length) {
                            String first = rawArgs[++j];

                            int sep = first.indexOf(':');
                            if (sep == -1) {
                                System.err.println("Invalid --set format. Use field:value");
                                return;
                            }

                            updateField = first.substring(0, sep);
                            StringBuilder valueBuilder = new StringBuilder(first.substring(sep + 1));

                            while (j + 1 < rawArgs.length && !rawArgs[j + 1].startsWith("--")) {
                                valueBuilder.append(" ").append(rawArgs[++j]);
                            }

                            updateValue = valueBuilder.toString().trim();
                        }
                        if (rawArgs[j].equalsIgnoreCase("--name") && j + 1 < rawArgs.length) {
                            updateField = "name";
                            updateValue = rawArgs[++j];
                        }
                    }

                    switch (updateTarget) {

                        case "folder" -> {
                            Folder folder = null;
                            List<Folder> folders = cliFolderService.getAllFolders();
                            for (Folder f : folders) {
                                if (f.getProjectName().equalsIgnoreCase(targetName)) {
                                    folder = f;
                                }
                            }
                            if (folder == null) {
                                System.err.println("Folder not found: " + targetName);
                                return;
                            }
                            id = folder.getId();
                        }

                        case "command" -> {

                            Command command = cliCommandService.getCommandByName(targetName);
                            if (command == null) {
                                System.err.println("Command not found: " + targetName);
                                return;
                            }

                            id = command.getId();
                            folderId = command.getFolderId();

                            if ("folder".equalsIgnoreCase(updateField)) {

                                if ("0".equals(updateValue)) {
                                    folderId = null;
                                    command.setFolderId(null);
                                    cliCommandService.updateCommand(
                                            command.getId(),
                                            command.getName(),
                                            command.getCommand(),
                                            command.getBody(),
                                            folderId
                                    );
                                    System.out.println("Folder removed from command '" + targetName + "' (folderId set to null)");
                                    return;
                                }


                                List<Folder> folders = cliFolderService.getAllFolders();
                                Folder targetFolder = null;
                                for (Folder f : folders) {
                                    if (f.getProjectName().equalsIgnoreCase(updateValue)) {
                                        targetFolder = f;
                                        break;
                                    }
                                }

                                if (targetFolder == null) {
                                    System.err.println("Folder not found: " + updateValue);
                                    return;
                                }

                                folderId = targetFolder.getId();
                                command.setFolderId(folderId);
                                cliCommandService.updateCommand(
                                        command.getId(),
                                        command.getName(),
                                        command.getCommand(),
                                        command.getBody(),
                                        folderId
                                );

                                System.out.println("Updated folderId of command '" + targetName + "' to folder '" + updateValue + "' (id=" + folderId + ")");
                                return;
                            }
                            if (rawArgs.length >= 5 && rawArgs[3].equalsIgnoreCase("header")) {

                                String headerKey = rawArgs[4];

                                List<Headers> headers = headerService.getAllHeadersFromCommandId(command.getId());

                                Headers header = null;
                                for (Headers h : headers) {
                                    if (h.getKey().equalsIgnoreCase(headerKey)) {
                                        header = h;
                                        break;
                                    }
                                }

                                if (header == null) {
                                    System.err.println("Header not found: " + headerKey);
                                    return;
                                }

                                Scanner scanner = new Scanner(System.in);

                                System.out.print("New header key (" + header.getKey() + "): ");
                                String newKey = scanner.nextLine();

                                System.out.print("New header value (" + header.getValue() + "): ");
                                String newValue = scanner.nextLine();

                                if (!newKey.isBlank()) {
                                    header.setKey(newKey);
                                }

                                if (!newValue.isBlank()) {
                                    header.setValue(newValue);
                                }

                                headerService.updateHeader(header);

                                System.out.println("Header updated successfully.");
                                return;
                            }
                        }

                        case "header" -> {
                            System.out.println("updating header key and value");
                        }

                        default -> {
                            System.err.println("Unknown update target: " + updateTarget);
                            return;
                        }


                    }

                    handleUpdate(id, folderId, updateTarget, updateField, updateValue);
                    return;

                case "purge":
                    cliFolderService.deleteAllFolders();
                    cliCommandService.deleteAllCommands();
                    headerService.deleteAllHeadersInDB();
                    historyService.deleteAllHistory();
                    return;


            }
        }

        if (saveMode) {
            if (commandName == null || url.isBlank()) {
                System.err.println("Saving requires -cmd <name> and -u <url>");
                return;
            }

            if (List.of("POST", "PUT", "PATCH", "DELETE").contains(method)) {
                body = readJsonBody(commandName, replayMode);
            }

            Long folderId = null;
            if (folderName != null) {
                List<Folder> folders = cliFolderRepository.getAllFolders();
                for (Folder folder : folders) {
                    if (folder.getProjectName().equalsIgnoreCase(folderName)) {
                        folderId = folder.getId();
                    }
                }
            }

            String fullCommand =
                    "-m " + method + " -u " + url + " -cmd " + commandName;

            cliCommandService.createCommand(
                    commandName,
                    fullCommand,
                    body,
                    folderId
            );
            Command savedCommand = cliCommandService.getCommandByName(commandName);
            List<Headers> headersToAdd = readHeadersInteractively();


            for (Headers header : headersToAdd) {
                header.setCommandId(savedCommand.getId());
                headerService.createHeader(header);
            }

            requestController.sendRequest(method, url, body, headersToAdd, savedCommand.getId());
            return;
        }

        /* DIRECT REQUEST */
        if (url.isBlank()) {
            System.err.println("URL required (-u)");
            return;
        }

        if (List.of("POST", "PUT", "PATCH", "DELETE").contains(method)) {
            body = readJsonBody(null, replayMode);
        }
        if (replayMode) {
            List<Folder> folders =  cliFolderRepository.getAllFolders();
            List<Command>  commands = cliCommandRepository.getRootCommands();
            for (Folder f : folders) {
                List<Command> cmds = cliFolderRepository.getCommandsInFolder(f.getProjectName());
                for (Command c : cmds) {
                    if (c.getName().equalsIgnoreCase(commandName)) {
                        body = c.getBody();
                    }
                }
            }
            for (Command c : commands) {
                if (c.getName().equalsIgnoreCase(commandName)) {
                    body = c.getBody();
                }
            }
        }
        Command savedCommand = cliCommandService.getCommandByName(commandName);
        List<Headers> headersToAdd = readHeadersInteractively();


        for (Headers header : headersToAdd) {
            header.setCommandId(savedCommand.getId());
            headerService.createHeader(header);
        }
        requestController.sendRequest(method, url, body, headersToAdd, 0L);
    }

    private Command resolveCommand(String name, String folder) throws SQLException {
        if (folder != null) {
            for (Command c : cliFolderRepository.getCommandsInFolder(folder)) {
                if (c.getName().equalsIgnoreCase(name)) return c;
            }
        }

        for (Command c : cliCommandRepository.getRootCommands()) {
            if (c.getName().equalsIgnoreCase(name)) return c;
        }

        return null;
    }

    private String[] tokenize(String command) {
        return command.trim().split("\\s+");
    }

    private List<Headers> readHeadersInteractively() {
        List<Headers> headers = new ArrayList<>();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print("Do you want to add a header? (y/n): ");
            String input = sc.nextLine().trim().toLowerCase();

            if (input.equals("n")) {
                break;
            } else if (input.equals("y")) {
                System.out.print("Header key: ");
                String key = sc.nextLine().trim();

                System.out.print("Header value: ");
                String value = sc.nextLine().trim();

                headers.add(new Headers(null, null, key, value)); // id and commandId will be set later
            } else {
                System.out.println("Please enter y or n.");
            }
        }

        return headers;
    }

    private String readJsonBody(String name, boolean replayMode) {
        if (replayMode) {
            return name;
        }
        System.out.println("Enter JSON" + (name != null ? " for " + name : "") + " (-s to end):");
        Scanner sc = new Scanner(System.in);
        StringBuilder sb = new StringBuilder();
        while (true) {
            String line = sc.nextLine();
            if (line.equals("-s")) break;
            sb.append(line).append("\n");
        }
        return sb.toString().trim();
    }

    private void printHelp() {
        System.out.println("""
USAGE:
  <command> [options]

START SFP BROWSER
  --api                                 start api

  -d                                    detached mode

MANAGE SFP BROWSER
  api ls                                List all current instances
  
  api stop <pid>                        Stop instance with <pid>
  
   
COMMANDS:
  ls                                    List all folders and root commands

  ls -f <folder>                        List commands inside a folder

  mkdir -f <folder>                     Create a new folder

  run -scmd <command> [-f <folder>]     Execute a saved command
                                        Prevents recursive execution

  run -f <folder>                       Execute all commands in the folder
  
  update command <commandName> --set <field>:<value>
            Update a saved command

      Fields:
        name                            Rename the command
        body                            Edit JSON body interactively
        command                         Replace the raw command string
        header <header key>             fetches the key and prompts for new key and value
        
      Examples:
        update command getUsers --set name:newGetUsers
        update command getUsers --set body
        update command getUsers --set command:-m POST -u https://api.com

  update folder <folderName> --name <newName>   
                            Rename a folder

  purge                     Delete ALL folders and ALL commands (DANGEROUS)

HTTP REQUESTS:
  -m <METHOD>               HTTP method (GET, POST, PUT, PATCH, DELETE)

  -u <URL>                  Target URL (required for direct requests)

  -sv                       Save the request as a reusable command

  -cmd <name>               Name of the command when saving

  -f <folder>               Folder to save the command in (optional)

EXAMPLES:
  -m GET -u https://example.com

  -m POST -u https://api.com/users -cmd createUser -sv

  run -scmd createUser

  update command createUser --set command:-m POST -u https://api.com/users
  
HISTORY
  history                               Prints the full history (latest at the top)
  
  history reverse                       Prints the full history (latest at the bottom)
  
  history purge                         Deletes the full history trace  
                """);
    }

    private void handleUpdate(
            Long id,
            Long folderId,
            String target,
            String field,
            String value
    ) throws Exception {

        switch (target) {

            case "command" -> {
                Command existing = cliCommandService.getCommandByName(target);
                if (existing == null) {
                    System.err.println("Command not found");
                    return;
                }

                String name = existing.getName();
                String commandStr = existing.getCommand();
                String body = existing.getBody();
                Long newFolderId = existing.getFolderId();

                switch (field) {
                    case "name" -> name = value;
                    case "body" -> body = readJsonBody(existing.getName(), false);
                    case "command" -> commandStr = value;
                    default -> {
                        System.err.println("Unknown command field: " + field);
                        return;
                    }
                }

                cliCommandService.updateCommand(
                        id,
                        name,
                        commandStr,
                        body,
                        newFolderId
                );
            }

            case "folder" -> {
                if (!"name".equals(field)) {
                    System.err.println("Folder can only update name");
                    return;
                }
                cliFolderService.updateFolder(id, value);
            }

            default -> System.err.println("Unknown update target: " + target);
        }
    }
}