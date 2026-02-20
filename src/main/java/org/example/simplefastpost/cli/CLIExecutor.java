package org.example.simplefastpost.cli;

import org.example.simplefastpost.cli.RequestController.RequestController;
import org.example.simplefastpost.cli.commandRepo.CLICommandRepository;
import org.example.simplefastpost.cli.commandService.CLICommandService;
import org.example.simplefastpost.cli.folderRepo.CLIFolderRepository;
import org.example.simplefastpost.cli.folderService.CLIFolderService;
import org.example.simplefastpost.service.FolderService;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Scanner;


public class CLIExecutor {


    private String DB_URL = "";
    public CLICommandRepository cliCommandRepository = new CLICommandRepository(DB_URL);
    public CLIFolderRepository cliFolderRepository = new CLIFolderRepository(DB_URL);
    public CLICommandService cliCommandService = new CLICommandService(cliCommandRepository);
    public CLIFolderService cliFolderService = new CLIFolderService(cliFolderRepository);

    private final RequestController requestController = new RequestController();

    public CLIExecutor(String DB_URL) {
        this.DB_URL = DB_URL;
    }

    public void execute(String[] rawArgs) {
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
                    } else {
                        System.out.println("Listing all folders");
                        // TODO: look up all folders and commands in root

                    }
                    return;

                case "run":
                    if (commandName == null || commandName.isBlank()) {
                        System.err.println("Error: run requires -scmd <commandName>");
                        return;
                    }
                    if (folderName != null) {
                        System.out.println("Running command '" + commandName + "' from folder '" + folderName + "'");
                        // TODO: lookup command in DB using folderName + commandName

                    } else {
                        System.out.println("Running command '" + commandName + "'");
                        // TODO: lookup command in DB using commandName
                    }
                    return;

                case "mkdir":
                    if (folderName == null || folderName.isBlank()) {
                        System.err.println("Error: mkdir requires -f <folderName>");
                        return;
                    }
                    System.out.println("Creating folder: " + folderName);
                    // TODO: create folder in DB
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


}
