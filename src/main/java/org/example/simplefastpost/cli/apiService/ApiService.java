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


    public void startApiDaemon() throws Exception {
        Path jarPath = getCurrentProgram();
        Path dataDir = getDataDir();  // ensure data dir exists
        Path logFile = dataDir.resolve("api-detached.log");

        // Make sure log file exists
        if (!Files.exists(logFile)) {
            Files.createFile(logFile);
        }

        ProcessBuilder pb = new ProcessBuilder(
                "java",
                "-jar",
                jarPath.toString(),
                "--api"
        )
                .redirectOutput(logFile.toFile())  // write stdout to log file
                .redirectError(logFile.toFile());  // write stderr to same log file

        Process process = pb.start();

        Long pid = process.pid();
        LocalDateTime startedAt = LocalDateTime.now();

        APIEntity apiEntity = new APIEntity(pid, startedAt);
        apiRepository.createNewInstance(apiEntity);

        System.out.println("Detached API started with PID " + pid + ". Logs are at: " + logFile.toAbsolutePath());
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
            // Windows: use taskkill
            new ProcessBuilder("taskkill", "/PID", pid.toString(), "/F")
                    .inheritIO() // optional, to see output/errors
                    .start()
                    .waitFor();
        } else {
            // Linux/macOS: use ProcessHandle
            ProcessHandle.of(pid).ifPresent(ProcessHandle::destroy);
        }

        // Remove from database in all cases
        apiRepository.deleteByPid(pid);
    }


}
