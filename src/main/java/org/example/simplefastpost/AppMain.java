package org.example.simplefastpost;

import org.example.simplefastpost.cli.CLIExecutor;
import org.example.simplefastpost.cli.OSService.OSService;
import org.example.simplefastpost.cli.apiRepository.ApiRepository;
import org.example.simplefastpost.cli.apiService.ApiService;
import org.example.simplefastpost.cli.config.CLIconfig;
import org.example.simplefastpost.setup.ApplicationSetup;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import java.util.List;
import java.util.Map;

@Component
public class AppMain {

    public static void main(String[] args) throws Exception {

        ApplicationSetup applicationSetup = new ApplicationSetup();
        CLIconfig cliConfig = new CLIconfig();
        ApiRepository apiRepository = new ApiRepository(cliConfig.getDBUrl());
        OSService osService = new OSService();
        ApiService apiService = new ApiService(cliConfig, apiRepository, osService);

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }

        List<String> argList = Arrays.asList(args);
        boolean isApiMode = argList.contains("--api");
        boolean isDetached = argList.contains("-d");

        int port = 8080;
        int portIndex = argList.indexOf("-p");
        if (portIndex != -1 && portIndex + 1 < argList.size()) {
            try {
                port = Integer.parseInt(argList.get(portIndex + 1));
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number, using default 8080");
            }
        }

        if (isApiMode) {

            if (isDetached) {
                System.out.println("Starting API in detached mode on port " + port);

                apiService.startApiDaemon(args);

            } else {
                System.out.println("Starting API on port " + port);

                SpringApplication app = new SpringApplication(SimpleFastPostApplication.class);

                app.setDefaultProperties(Map.of("server.port", port));
                app.run(args);
            }

        } else {
            CLIExecutor cli = new CLIExecutor();

            applicationSetup.initialize(cliConfig.getDBUrl());

            try {
                cli.execute(args);
            } catch (Exception e) {
                System.err.println("CLI execution failed: " + e.getMessage());
                System.out.println("No URL to connect to");
            }
        }
    }
}