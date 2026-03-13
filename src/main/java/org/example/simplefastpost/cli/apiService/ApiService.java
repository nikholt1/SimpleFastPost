package org.example.simplefastpost.cli.apiService;

import org.example.simplefastpost.AppMain;
import org.example.simplefastpost.cli.OSService.OSService;
import org.example.simplefastpost.cli.apiRepository.ApiRepository;
import org.example.simplefastpost.cli.config.CLIconfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

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
        return Paths.get(
                AppMain.class
                        .getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .toURI()
        );
    }

    public Path getDataDir() throws Exception {
        Path dataDir = cliConfig.getDataDir();
        Files.createDirectories(dataDir);
        return dataDir;
    }
    /// TODO: run program with "--api" flag only


    public void startApiDaemon() throws Exception {
        Path program = getCurrentProgram();

        Process process = new ProcessBuilder(
                program.toString(),
                "--api"
        )
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start();

        Long pid = process.pid();
        LocalDateTime startedAt = LocalDateTime.now();

        // apiRepository.saveApiRun();

    }
    /// TODO: save database instance pid or id in database

    /// TODO: list all entities in database

    /// TODO: stop all instances in database

    /// TODO: stop instance by id
    public void stopApiDaemon(Long id) throws Exception {
//        Long pid = apiRepository.getAPIById();
        Long pid = null;
        ProcessHandle.of(pid).ifPresent(ProcessHandle::destroy);

//        apiRepository.deleteApiById(id);

    }
    /// TODO: restart instance by id

}
