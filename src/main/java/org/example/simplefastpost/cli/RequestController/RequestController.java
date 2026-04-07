package org.example.simplefastpost.cli.RequestController;

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

public class RequestController {

    private final HistoryService historyService;

    public RequestController(HistoryService historyService) {
        this.historyService = historyService;
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

    public boolean sendTestRequest(List<Headers> headers, String method, String url, Command command) throws Exception {

        String body = command.getBody();
        Long commandId = command.getId();

        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("No URL provided for command: " + command.getName());
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));

        // Add headers
        if (headers != null) {
            for (Headers h : headers) {
                if (h.getKey() != null && !h.getKey().isBlank()) {
                    builder.header(h.getKey(), h.getValue() != null ? h.getValue() : "");
                }
            }
        }

        int expectedStatus = command.getExpected_status();
        String expectedResponse = command.getExpected_response();

        // Set HTTP method and body
        switch (method.toUpperCase()) {
            case "POST":
            case "PUT":
            case "PATCH":
            case "DELETE":
                if (body != null && !body.isEmpty()) {
                    builder.method(method.toUpperCase(), HttpRequest.BodyPublishers.ofString(body))
                            .header("Content-Type", "application/json");
                } else {
                    builder.method(method.toUpperCase(), HttpRequest.BodyPublishers.noBody());
                }
                break;
            default:
                builder.GET();
        }

        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        int actualStatus = response.statusCode();
        String actualBody = response.body();

        System.out.println("\nRESPONSE:");
        System.out.println("Status: " + actualStatus);
        System.out.println(actualBody);

        // ===== TEST LOGIC =====
        boolean statusMatch = (actualStatus == expectedStatus);
        boolean bodyMatch = true;
        if (expectedResponse != null && !expectedResponse.isBlank()) {
            bodyMatch = actualBody != null && actualBody.contains(expectedResponse);
        }

        boolean passed = statusMatch && bodyMatch;

        // ===== OUTPUT =====
        if (passed) {
            System.out.println("✅ TEST PASSED");
        } else {
            System.out.println("❌ TEST FAILED");
            if (!statusMatch) {
                System.out.println("  Expected status: " + expectedStatus + " but got: " + actualStatus);
            }
            if (!bodyMatch) {
                System.out.println("  Expected response to contain: " + expectedResponse);
            }
        }

        // ===== HISTORY CREATION (optional) =====
        // We will still save history but do NOT update response or status columns
        History history = new History();
        history.setCreated(LocalDateTime.now());
        history.setUrl(url);
        history.setResponse(response.body());
        history.setStatus(String.valueOf(response.statusCode()));
        history.setAction(null);
        history.setCommandId(commandId);
        // maybe list of headers?
        historyService.createHistoryEntity(history);
        return passed;
    }
}