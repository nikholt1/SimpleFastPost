package org.example.simplefastpost.cli.apiService;

import org.example.simplefastpost.AppMain;
import org.example.simplefastpost.cli.OSService.OSService;
import org.example.simplefastpost.cli.apiRepository.ApiRepository;
import org.example.simplefastpost.cli.config.CLIconfig;
import org.example.simplefastpost.model.APIEntity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ApiService {


    private final CLIconfig cliConfig;
    private final ApiRepository apiRepository;
    private final OSService osService;
    public ApiService(CLIconfig cliConfig, ApiRepository apiRepository,  OSService osService) {
        this.cliConfig = cliConfig;
        this.apiRepository = apiRepository;
        this.osService = osService;
    }

    /// TODO: getPath to the current running program
    public Path getCurrentProgram() throws Exception {
        Path currentDir = Paths.get(".").toAbsolutePath().normalize();
        Path jarPath = currentDir.resolve("SimpleFastPost.jar");

        if (!Files.exists(jarPath) || !Files.isRegularFile(jarPath)) {
            throw new RuntimeException(
                    "Detached mode requires SimpleFastPost.jar in current folder. Checked: " + jarPath
            );
        }
        return jarPath;
    }

    public Path getDataDir() throws Exception {
        Path dataDir = cliConfig.getDataDir();
        Files.createDirectories(dataDir);
        return dataDir;
    }
    /// TODO: run program with "--api" flag only


    public void startApiDaemon(String[] originalArgs) throws Exception {

//        Path jarPath = getCurrentProgram();

        Path dataDir = getDataDir();
        Path logFile = dataDir.resolve("api-detached.log");
        Path jarPath = dataDir.resolve("SimpleFastPost.jar");

        if (!Files.exists(logFile)) {
            Files.createFile(logFile);
        }

        List<String> command = new ArrayList<>();

        command.add("java");
        command.add("-jar");
        command.add(jarPath.toString());

        boolean hasApiFlag = false;
        boolean hasPort = false;

        for (int i = 0; i < originalArgs.length; i++) {
            String arg = originalArgs[i];

            if (arg.equals("-d")) {
                continue;
            }

            if (arg.equals("--api")) {
                hasApiFlag = true;
            }

            if (arg.equals("-p")) {
                hasPort = true;

                command.add(arg);

                if (i + 1 < originalArgs.length) {
                    command.add(originalArgs[i + 1]);
                    i++;
                }
                continue;
            }

            command.add(arg);
        }


        if (!hasApiFlag) {
            command.add("--api");
        }

        if (!hasPort) {
            command.add("-p");
            command.add("8080");
        }

        ProcessBuilder pb = new ProcessBuilder(command)
                .redirectOutput(logFile.toFile())
                .redirectError(logFile.toFile());

        Process process = pb.start();

        Long pid = process.pid();
        LocalDateTime startedAt = LocalDateTime.now();

        APIEntity apiEntity = new APIEntity(pid, startedAt);
        apiRepository.createNewInstance(apiEntity);

        System.out.println("Detached API started with PID " + pid +
                ". Logs are at: " + logFile.toAbsolutePath());
    }


    /// TODO: list all entities in database
    public void listAllInstances() throws SQLException {
        List<APIEntity> entities = apiRepository.listAllInstances();
        for (APIEntity entity : entities) {
            System.out.println(entity.getpId() + "     " + entity.getCreated());
        }

    }


    /// TODO: stop instance by id
    public void stopApiDaemon(Long pid) throws Exception {
        OSService.OSType os = OSService.getOperatingSystemType();

        if (os == OSService.OSType.WINDOWS) {
            new ProcessBuilder("taskkill", "/PID", pid.toString(), "/F")
                    .inheritIO()
                    .start()
                    .waitFor();
        } else {

            ProcessHandle.of(pid).ifPresent(ProcessHandle::destroy);
        }


        apiRepository.deleteByPid(pid);
    }


}
