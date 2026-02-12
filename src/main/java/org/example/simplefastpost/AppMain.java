package org.example.simplefastpost;

import org.example.simplefastpost.service.CLIExecutor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;

public class AppMain {

    public static void main(String[] args) {
        boolean isApiMode = Arrays.asList(args).contains("--api");

        if (isApiMode) {
            // Start Spring Boot (API mode)
            SpringApplication.run(SimpleFastPostApplication.class, args);
        } else {
            // CLI mode: create CLIExecutor directly, pass args
            CLIExecutor cli = new CLIExecutor(); // pass null if JPA not needed
            try {
                cli.execute(args); // <-- passes all CLI arguments here
            } catch (Exception e) {
                System.err.println("CLI execution failed: " + e.getMessage());
                // e.printStackTrace(); // optional for debugging
            }
        }
    }
}
