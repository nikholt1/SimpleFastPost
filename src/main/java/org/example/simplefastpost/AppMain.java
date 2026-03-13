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
        CLIconfig cliConfig = new CLIconfig();
        ApiRepository apiRepository = new ApiRepository(cliConfig.getDBUrl());
        OSService osService = new OSService();
        ApiService apiService = new ApiService(cliConfig, apiRepository, osService);

        String DB_URL = "jdbc:sqlite:../sfpDB.db";
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }
        ApplicationSetup applicationSetup = new ApplicationSetup();
        applicationSetup.initialize(DB_URL);

        List<String> argList = Arrays.asList(args);
        boolean isApiMode = argList.contains("--api");
        boolean isDetached = argList.contains("-d");

        // default port
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
                // Start API in a detached/background process
                System.out.println("Starting API in detached mode on port " + port);

                apiService.startApiDaemon(); // spawns new process and saves PID
            } else {
                // Foreground: run Spring Boot API normally
                System.out.println("Starting API on port " + port);

                SpringApplication app = new SpringApplication(SimpleFastPostApplication.class);

                // Set port dynamically
                app.setDefaultProperties(Map.of("server.port", port));
                app.run(args);
            }


        } else {
            // CLI mode: create CLIExecutor directly, pass args
            CLIExecutor cli = new CLIExecutor();

            try {
                cli.execute(args);
            } catch (Exception e) {
                System.err.println("CLI execution failed: " + e.getMessage());
            }
        }
    }
}