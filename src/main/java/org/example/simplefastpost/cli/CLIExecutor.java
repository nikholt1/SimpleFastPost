package org.example.simplefastpost.cli;

import org.example.simplefastpost.cli.RequestController.RequestController;
import org.example.simplefastpost.cli.commandRepo.CLICommandRepository;
import org.example.simplefastpost.cli.commandService.CLICommandService;
import org.example.simplefastpost.cli.folderRepo.CLIFolderRepository;
import org.example.simplefastpost.cli.folderService.CLIFolderService;
import org.example.simplefastpost.model.Command;
import org.example.simplefastpost.model.Folder;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;


public class CLIExecutor {

    private static final int MAX_RECURSION_DEPTH = 5;

    private final String DB_URL;

    private final CLICommandRepository cliCommandRepository;
    private final CLIFolderRepository cliFolderRepository;
    private final CLICommandService cliCommandService;
    private final CLIFolderService cliFolderService;
    private boolean replayMode = false;
    private final RequestController requestController = new RequestController();

    public CLIExecutor(String DB_URL) {
        this.DB_URL = DB_URL;
        this.cliCommandRepository = new CLICommandRepository(DB_URL);
        this.cliFolderRepository = new CLIFolderRepository(DB_URL);
        this.cliCommandService = new CLICommandService(cliCommandRepository);
        this.cliFolderService = new CLIFolderService(cliFolderRepository);
    }

    public void execute(String[] rawArgs) throws Exception {
        execute(rawArgs, 0, false);
    }

    private void execute(String[] rawArgs, int depth, boolean replayMode) throws Exception {
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
                System.out.println("sfp version: 0.0.1 beta");
                return;
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
                        cmds.forEach(c ->
                                System.out.println(c.getName() + " -> " + c.getCommand()));
                    } else {
                        cliFolderService.listFolders();
                        cliCommandService.listCommands();
                    }
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
                    if (commandName == null) {
                        System.err.println("run requires -scmd <commandName>");
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

                case "update":
                    if (rawArgs.length < 3) {
                        System.err.println("Usage: update <command|folder> <name> [options]");
                        return;
                    }

                    updateTarget = rawArgs[1].toLowerCase();
                    String targetName = rawArgs[2];

                    Long id = null;
                    Long folderId = null;

                    // Parse flags
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
//                            Command cmdS = resolveCommand(targetName, null);

                            Command command = cliCommandService.getCommandByName(targetName);
                            if (command == null) {
                                System.err.println("Command not found: " + targetName);
                                return;
                            }
                            id = command.getId();
                            folderId = command.getFolderId();
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

            requestController.sendRequest(method, url, body);
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
        requestController.sendRequest(method, url, body);
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
--api                                   start api

-d                                      detached mode

COMMANDS:
  ls                                    List all folders and root commands

  ls -f <folder>                        List commands inside a folder

  mkdir -f <folder>                     Create a new folder

  run -scmd <command> [-f <folder>]     Execute a saved command
                                        Prevents recursive execution

  update command <commandName> --set <field>:<value>
            Update a saved command

      Fields:
        name        Rename the command
        body        Edit JSON body interactively
        command     Replace the raw command string

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