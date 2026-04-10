package org.example.simplefastpost.cli.RequestController;

import org.example.simplefastpost.cli.commandService.HeaderService;
import org.example.simplefastpost.cli.historyService.HistoryService;
import org.example.simplefastpost.model.Command;
import org.example.simplefastpost.model.Headers;
import org.example.simplefastpost.model.History;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
public class RequestController {

    private final HistoryService historyService;
    private final HeaderService headerService;

    public RequestController(HistoryService historyService, HeaderService headerService) {
        this.historyService = historyService;
        this.headerService = headerService;

    }

    public void sendRequest(String method, String url, String body, List<Headers> headers, Long commandId) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));

        // Add all headers from the list
        if (headers != null) {
            for (Headers h : headers) {
                if (h.getKey() != null && !h.getKey().isBlank()) {
                    builder.header(h.getKey(), h.getValue() != null ? h.getValue() : "");
                }
            }
        }

        // Set the HTTP method and body
        switch (method.toUpperCase()) {
            case "POST":
            case "PUT":
            case "PATCH":
            case "DELETE":
                if (body != null && !body.isEmpty()) {
                    builder.method(method.toUpperCase(), HttpRequest.BodyPublishers.ofString(body))
                            .header("Content-Type", "application/json"); // default content type
                } else {
                    builder.method(method.toUpperCase(), HttpRequest.BodyPublishers.noBody());
                }
                break;
            default:
                builder.GET();
        }

        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());

        System.out.println("\nRESPONSE:");
        System.out.println("Status: " + response.statusCode());
        System.out.println(response.body());

        History history = new History();
        history.setCreated(LocalDateTime.now());
        history.setUrl(url);
        history.setResponse(response.body());
        history.setStatus(String.valueOf(response.statusCode()));
        history.setAction(null);
        history.setCommandId(commandId);
        // maybe list of headers?
        historyService.createHistoryEntity(history);
    }

    public void sendSavedRequest(Command command) {

    }

    public void sendRequests(List<Command> commands) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        int failCount = 0;
        ObjectMapper mapper = new ObjectMapper();

        for (Command command : commands) {

            if (command.getCommand().contains("run")) {
                System.err.println("Skipping recursive command: " + command.getName());
                continue;
            }

            String commandStr = command.getCommand();
            String method = null;
            String url = null;

            String[] parts = commandStr.split("\\s+");
            for (int k = 0; k < parts.length; k++) {
                if (parts[k].equals("-m") && k + 1 < parts.length) {
                    method = parts[k + 1];
                }
                if (parts[k].equals("-u") && k + 1 < parts.length) {
                    url = parts[k + 1];
                }
            }

            if (method == null || url == null) {
                System.err.println("Skipping command (invalid format): " + command.getName());
                continue;
            }

            String body = command.getBody();
            HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));

            List<Headers> headers = headerService.getAllHeadersFromCommandId(command.getId());
            if (headers != null) {
                for (Headers h : headers) {
                    if (h.getKey() != null && !h.getKey().isBlank()) {
                        builder.header(h.getKey(), h.getValue() != null ? h.getValue() : "");
                    }
                }
            }

            switch (method.toUpperCase()) {
                case "POST":
                case "PUT":
                case "PATCH":
                case "DELETE":
                    if (body != null && !body.isEmpty()) {
                        builder.method(method.toUpperCase(), HttpRequest.BodyPublishers.ofString(body))
                                .header("Content-Type", "application/json");
                    } else {
                        // Even if no body, set Content-Type
                        builder.method(method.toUpperCase(), HttpRequest.BodyPublishers.noBody())
                                .header("Content-Type", "application/json");
                    }
                    break;
                default:
                    builder.GET();
            }

            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());

            System.out.println("\nCOMMAND: " + command.getName());
            System.out.println("Status: " + response.statusCode());
            System.out.println(response.body());

            // Save history
            History history = new History();
            history.setCreated(LocalDateTime.now());
            history.setUrl(url);
            history.setResponse(response.body());
            history.setStatus(String.valueOf(response.statusCode()));
            history.setAction(null);
            history.setCommandId(command.getId());
            historyService.createHistoryEntity(history);

            // Compare expected status
            boolean statusMatch = command.getExpected_status() == null ||
                    command.getExpected_status().trim().equals(String.valueOf(response.statusCode()).trim());

            // Compare expected response
            boolean responseMatch;
            if (command.getExpected_response() == null) {
                responseMatch = true;
            } else {
                String expected = command.getExpected_response().trim();
                String actual = response.body().trim();

                // Try JSON comparison if both look like JSON
                if ((expected.startsWith("{") && expected.endsWith("}")) ||
                        (expected.startsWith("[") && expected.endsWith("]"))) {
                    try {
                        JsonNode expectedJson = mapper.readTree(expected);
                        JsonNode actualJson = mapper.readTree(actual);
                        responseMatch = expectedJson.equals(actualJson);
                    } catch (Exception e) {
                        // fallback to string comparison
                        responseMatch = expected.equals(actual);
                    }
                } else {
                    responseMatch = expected.equals(actual);
                }
            }

            if (!statusMatch || !responseMatch) {
                System.err.println("Test failed for command: " + command.getName());
                System.err.println("Expected Status: " + command.getExpected_status() + ", Actual: " + response.statusCode());
                System.err.println("Expected Response: \n" + command.getExpected_response() + "\nActual: \n" + response.body());
                failCount++;
            } else {
                System.out.println("Test passed for command: " + command.getName());
            }
        }

        System.out.println("\\u001B[48;5;149m" + "SUMMARY: Total commands = " + commands.size() +
                ", Passed = " + (commands.size() - failCount) +
                ", Failed = " + failCount + "\u001B[0m");
        System.exit(failCount > 0 ? 1 : 0);
    }
}