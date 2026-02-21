package org.example.simplefastpost.cli;

import org.example.simplefastpost.cli.RequestController.RequestController;
import org.example.simplefastpost.cli.commandRepo.CLICommandRepository;
import org.example.simplefastpost.cli.commandService.CLICommandService;
import org.example.simplefastpost.cli.folderRepo.CLIFolderRepository;
import org.example.simplefastpost.cli.folderService.CLIFolderService;
import org.example.simplefastpost.model.Command;
import org.example.simplefastpost.model.Folder;
import org.example.simplefastpost.service.FolderService;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;


public class CLIExecutor {


    private String DB_URL = "";
    public CLICommandRepository cliCommandRepository = null;
    public CLIFolderRepository cliFolderRepository = null;
    public CLICommandService cliCommandService = null;
    public CLIFolderService cliFolderService = null;

    private final RequestController requestController = new RequestController();

    public CLIExecutor(String DB_URL) {
        System.out.println(DB_URL + " in CLIExecutor");
        this.DB_URL = DB_URL;
        this.cliCommandRepository = new CLICommandRepository(DB_URL);
        this.cliFolderRepository = new CLIFolderRepository(DB_URL);
        this.cliCommandService = new CLICommandService(cliCommandRepository);
        this.cliFolderService = new CLIFolderService(cliFolderRepository);
    }

    public void execute(String[] rawArgs) throws SQLException {
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
        boolean runMode = false;
        boolean folderAndListMode = false;
        boolean helpMode = false;
        String commandName = null;
        String folderName = null;
        String method = "GET";
        String url = "";
        String body = null;


        for (; i < rawArgs.length; i++) {
            String arg = rawArgs[i];

            if (arg.equalsIgnoreCase("-m") && i + 1 < rawArgs.length) {
                method = rawArgs[i + 1].toUpperCase();
                i++;
            } else if (arg.equalsIgnoreCase("-u") && i + 1 < rawArgs.length) {
                url = rawArgs[i + 1];
                i++;
            } else if (arg.equalsIgnoreCase("-sv")) {
                saveMode = true;
            } else if (arg.equalsIgnoreCase("-f") && i + 1 < rawArgs.length) {
                folderName = rawArgs[i + 1];
                i++;
            } else if ((arg.equalsIgnoreCase("-cmd") || arg.equalsIgnoreCase("-scmd") || arg.equalsIgnoreCase("-r"))
                    && i + 1 < rawArgs.length) {
                commandName = rawArgs[i + 1];
                if (arg.equalsIgnoreCase("-scmd") || arg.equalsIgnoreCase("-r")) {
                    runMode = true;
                } else {
                    saveMode = true;
                }
                i++;
            } else if (arg.equalsIgnoreCase("--help")) {
                helpMode = true;
                System.out.println("\n=== SimpleFastPost CLI Help ===\n");
                System.out.println("Commands:");
                System.out.println("  help                     - Show this help message");
                System.out.println("  ls [-f <folderName>]     - List all folders or commands in a folder");
                System.out.println("  mkdir -f <folderName>    - Create a new folder");
                System.out.println("  run -scmd <commandName> [-f <folderName>]  - Run a saved command");
                System.out.println("\nHTTP Options (for save or direct requests):");
                System.out.println("  -m <METHOD>              - HTTP method: GET, POST, PUT, PATCH, DELETE");
                System.out.println("  -u <URL>                 - URL to send the request to");
                System.out.println("  -sv                      - Save the command instead of executing immediately");
                System.out.println("  -cmd <commandName>       - Name for the command to save");
                System.out.println("  -f <folderName>          - Folder to save or run the command from");
                System.out.println("\nWhen entering POST, PUT, PATCH, DELETE, you will be prompted for JSON input.");
                System.out.println("Type '-s' to finish entering JSON (can be left empty for DELETE).\n");
                System.out.println("Examples:");
                System.out.println("  -m GET -u https://jsonplaceholder.typicode.com/posts/1 -sv -cmd commandName -f foldername");
                System.out.println("  run -scmd commandName\n");
            } else {
                System.out.println("Unknown flag or value: " + arg);
            }
        }


        if (primaryCommand != null) {
            switch (primaryCommand) {

                case "ls":
                    folderAndListMode = true;
                    if (folderName != null) {
                        System.out.println("Listing commands in folder: " + folderName);
                        // TODO: Lookup command in folder
                        List<Command> commands = cliFolderRepository.getCommandsInFolder(folderName);
                        for (Command cmd : commands) {
                            System.out.println(cmd.getName() + " " + cmd.getCommand() + " " + cmd.getBody());
                        }
                    } else {
                        System.out.println("Listing all folders and parentless commands...");
                        // TODO: look up all folders and commands in root
                        cliFolderService.listFolders();
                        cliCommandService.listCommands();
                    }
                    return;

                case "run":
                    if (commandName == null || commandName.isBlank()) {
                        System.err.println("Error: run requires -scmd <commandName>");
                        return;
                    }

                    Command cmd = null;

                    if (folderName != null) {
                        List<Command> commands = cliFolderRepository.getCommandsInFolder(folderName);
                        for (Command c : commands) {
                            if (cmd.getCommand().contains("run -scmd")) {
                                System.err.println("Refusing to run recursive command.");
                                return;
                            }
                            if (c.getName().equalsIgnoreCase(commandName)) {
                                cmd = c;
                                break;
                            }
                        }
                    } else {
                        // TODO: lookup command by name only
                        if (commandName == null || commandName.isBlank()) {
                            System.err.println("Error: run requires -scmd <commandName>");
                            return;
                        }

                        List<Command> rootCommands = cliCommandRepository.getRootCommands();
                        for (Command c :  rootCommands) {
                            if (c.getName().equalsIgnoreCase(commandName)) {
                                cmd = c;
                                break;
                            }
                        }

                        List<Folder> folders = cliFolderRepository.getAllFolders();
                        for (Folder f : folders) {
                            for(Command c : f.getCommands()) {
                                if (c.getName().equalsIgnoreCase(commandName)) {
                                    cmd = c;
                                    break;
                                }
                            }
                        }
                    }

                    if (cmd == null) {
                        System.err.println("Command not found: " + commandName);
                        return;
                    }

                    System.out.println("Re-running saved command:");
                    System.out.println("> " + cmd.getCommand());

                    String[] savedArgs = tokenize(cmd.getCommand());
                    execute(savedArgs);
                    return;

                case "mkdir":
                    if (folderName == null || folderName.isBlank()) {
                        System.err.println("Error: mkdir requires -f <folderName>");
                        return;
                    }
                    System.out.println("Creating folder: " + folderName);
                    // TODO: create folder in DB
                    cliFolderService.createFolder(folderName);
                    System.out.println("Folder created: " + folderName);
                    return;

                default:
                    break;
            }
        }


        if (saveMode) {
            if (commandName == null || commandName.isBlank()) {
                System.err.println("Error: -cmd <commandName> is required when using -sv");
                return;
            }


            if (List.of("POST", "PUT", "PATCH", "DELETE").contains(method)) {
                System.out.println("Enter JSON line by line for command '" + commandName + "'. Type '-s' to finish:");
                Scanner scanner = new Scanner(System.in);
                StringBuilder sb = new StringBuilder();
                while (true) {
                    String line = scanner.nextLine();
                    if (line.trim().equals("-s")) break;
                    sb.append(line).append("\n");
                }
                body = sb.toString().trim();
                System.out.println("Captured JSON for command '" + commandName + "':\n" + body);
            }


            if (folderName != null) {
                System.out.println("Saving command '" + commandName + "' into folder '" + folderName + "'");
                System.out.println("Method: " + method + ", URL: " + url);
            } else {
                System.out.println("Saving command '" + commandName + "'");
                System.out.println("Method: " + method + ", URL: " + url);
            }

            // TODO: save command info (method, url, body, folderName) into DB
            String command = "-m " + method + " -u " + url;
            Long folderId = null;
            List<Folder> folders = cliFolderService.getAllFolders();
            for (Folder f : folders) {
                if (f.getProjectName().equalsIgnoreCase(commandName)) {
                    folderId = f.getId();
                }
            }
            cliCommandService.createCommand(commandName, command, body, folderId);

            System.out.println("Running saved command '" + commandName + "'...");
            try {
                requestController.sendRequest(method, url, body);
            } catch (Exception e) {
                System.err.println("Failed to run saved command:");
                e.printStackTrace();
            }

            return;
        }
        if (helpMode) {
            helpMode = false;
            return;
        }

        if (url.isEmpty()) {
            System.err.println("Error: URL not provided. Use -u <URL>");
            return;
        }

        if (List.of("POST", "PUT", "PATCH", "DELETE").contains(method)) {
            System.out.println("Enter JSON line by line. Type '-s' to send:");
            Scanner scanner = new Scanner(System.in);
            StringBuilder sb = new StringBuilder();
            while (true) {
                String line = scanner.nextLine();
                if (line.trim().equals("-s")) break;
                sb.append(line).append("\n");
            }
            body = sb.toString().trim();
        }

        try {
            requestController.sendRequest(method, url, body);
        } catch (Exception e) {
            System.err.println("Failed to send request:");
            e.printStackTrace();
        }
    }
    private String[] tokenize(String command) {
        return command.trim().split("\\s+");
    }

}
