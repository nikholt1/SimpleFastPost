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
        Path jarPath = getCurrentProgram();

        ProcessBuilder pb = new ProcessBuilder(
                "java",
                "-jar",
                jarPath.toString(),
                "--api"
        )
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD);

        Process process = pb.start();

        Long pid = process.pid();
        LocalDateTime startedAt = LocalDateTime.now();

        APIEntity apiEntity = new APIEntity(pid, startedAt);
        apiRepository.createNewInstance(apiEntity);
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
//        APIEntity apiEntity = apiRepository.getInstanceByPid(pid);

        ProcessHandle.of(pid).ifPresent(ProcessHandle::destroy);

        apiRepository.deleteByPid(pid);
    }


}
