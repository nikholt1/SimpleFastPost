package org.example.simplefastpost.cli.RequestController;

import org.example.simplefastpost.model.Command;
import org.example.simplefastpost.model.Headers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class RequestController {


    public RequestController() {

    }
    public void sendRequest(String method, String url, String body, List<Headers> headers) throws Exception {
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
    }
    public void sendSavedRequest(Command command) {

    }
}
