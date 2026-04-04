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

    private final HistoryService  historyService;
    public RequestController(HistoryService historyService) {
        this.historyService = historyService;
    }
    public void sendRequest(String method, String url, String body, List<Headers> headers, Long commandId) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));

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
}
