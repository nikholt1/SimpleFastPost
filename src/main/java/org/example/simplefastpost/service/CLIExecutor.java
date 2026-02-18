package org.example.simplefastpost.service;


import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Scanner;


@Component
public class CLIExecutor {

    public CLIExecutor() {

    }

    public void execute(String[] rawArgs) {
        if (rawArgs.length == 0) {
            System.out.println("Usage: -m METHOD -u URL");
            return;
        }

        String method = "GET";
        String url = "";


        for (int i = 0; i < rawArgs.length; i++) {
            if (rawArgs[i].equalsIgnoreCase("-m") && i + 1 < rawArgs.length) {
                method = rawArgs[i + 1].toUpperCase();
            }
            if (rawArgs[i].equalsIgnoreCase("-u") && i + 1 < rawArgs.length) {
                url = rawArgs[i + 1];
            }
        }

        if (url.isEmpty()) {
            System.err.println("Error: URL not provided. Use -u <URL>");
            return;
        }

        String body = null;
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
            sendRequest(method, url, body);
        } catch (Exception e) {
            System.err.println("Failed to send request:");
            e.printStackTrace();
        }
    }

    private void sendRequest(String method, String url, String body) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));

        switch (method) {
            case "POST":
            case "PUT":
            case "PATCH":
            case "DELETE":
                if (body != null && !body.isEmpty()) {
                    builder.method(method, HttpRequest.BodyPublishers.ofString(body))
                            .header("Content-Type", "application/json");
                } else {
                    builder.method(method, HttpRequest.BodyPublishers.noBody());
                }
                break;
            default:
                builder.GET();
        }

        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        System.out.println("\nRESPONSE:");
        System.out.println("Status: " + response.statusCode());
        System.out.println(response.body());
    }
}
