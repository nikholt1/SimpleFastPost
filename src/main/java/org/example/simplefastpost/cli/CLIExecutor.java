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
import org.example.simplefastpost.cli.syncService.HttpSyncService;
import org.example.simplefastpost.cli.syncService.PushService;
import org.example.simplefastpost.model.Command;
import org.example.simplefastpost.model.Folder;
import org.example.simplefastpost.model.Headers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;


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
    private final RequestController requestController = new RequestController();
    private final CLIconfig clIconfig = new CLIconfig();
    private final HeaderService headerService;
    private final HeaderRepository headerRepository;

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
        String updateTarget = null; // "command" or "folder"
        String updateField = null;  // name, body, etc.
        String updateValue = null;

        for (; i < rawArgs.length; i++) {
            String arg = rawArgs[i];

            if (arg.equalsIgnoreCase("-m") && i + 1 < rawArgs.length) {
                method = rawArgs[++i].toUpperCase();
            } else if (arg.equalsIgnoreCase("-u") && i + 1 < rawArgs.length) {
                url = rawArgs[++i];
            } else if (arg.equalsIgnoreCase("-sv")) {
                saveMode = true;
            } else if (arg.equalsIgnoreCase("-f") && i + 1 < rawArgs.length) {
                folderName = rawArgs[++i];
            } else if ((arg.equalsIgnoreCase("-cmd") || arg.equalsIgnoreCase("-scmd"))
                    && i + 1 < rawArgs.length) {
                commandName = rawArgs[++i];
            } else if (arg.equalsIgnoreCase("--help")) {
                helpMode = true;
            } else if (arg.equalsIgnoreCase("--version")) {
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
                case "api":
                    if (rawArgs.length < 2) {
                        System.out.println("Usage: api [ls|kill <pid>]");
                        return; // exit method after showing usage
                    }

                    String subCommand = rawArgs[1];

                    if (subCommand.equals("ls")) {
                        // List all running API instances from the database
                        apiService.listAllInstances();
                        return; // done, exit method
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
                        return; // exit after handling kill
                    } else {
                        System.out.println("Unknown api subcommand: " + subCommand);
                        return; // exit after unknown subcommand
                    }
                case "push":
                    String findRoot = System.getProperty("user.dir");
                    Path fileToFind = Path.of(findRoot, "sfp.http");

                    if (!Files.exists(fileToFind)) {
                        System.out.println("File not found in root directory");
                    }
                    int runner = pushService.pushFile(fileToFind);
                    System.out.println("Found " + runner + " commands to build in root .http file");

                    return;
                case "sync":
                    String root = System.getProperty("user.dir");
                    Path file = Path.of(root, "sfp.http");

                    if (!Files.exists(file)) {
                        System.out.println("File not found in root directory");
                    }

                    int run = syncService.syncFile(file);
                    System.out.println("Found " + run + " commands to build from .http file to root");
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

                        for (Command c : commands) {

                            if (c.getCommand().contains("run")) {
                                System.err.println("Skipping recursive command: " + c.getName());
                                continue;
                            }

                            System.out.println("> " + c.getCommand());

                            execute(tokenize(c.getCommand()), depth + 1, true);
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

                    execute(tokenize(cmd.getCommand()), depth + 1, true);
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

                        // FOLDER MODE: delete -f <folderName>
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

                            // collect remaining args as part of the value
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

                            // 1️⃣ Get command (must return something even if multiple exist)
                            Command command = cliCommandService.getCommandByName(targetName);
                            if (command == null) {
                                System.err.println("Command not found: " + targetName);
                                return;
                            }

                            id = command.getId();
                            folderId = command.getFolderId();

                            // 2️⃣ Special handling: --set folder:<folderName>
                            if ("folder".equalsIgnoreCase(updateField)) {

                                // Special case: folder:0 → unset folder
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
                                    return; // done
                                }

                                // Normal case: find folder by name
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
                                command.setFolderId(folderId); // update command object
                                cliCommandService.updateCommand(
                                        command.getId(),
                                        command.getName(),
                                        command.getCommand(),
                                        command.getBody(),
                                        folderId
                                ); // persist change

                                System.out.println("Updated folderId of command '" + targetName + "' to folder '" + updateValue + "' (id=" + folderId + ")");
                                return; // done
                            }
                            // HEADER UPDATE MODE (unchanged)
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
                    return;


            }
        }

        /* SAVE MODE */
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

            requestController.sendRequest(method, url, body, headersToAdd);
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
        requestController.sendRequest(method, url, body, headersToAdd);
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