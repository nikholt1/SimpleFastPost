package org.example.simplefastpost.cli.RequestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RequestController {


    public RequestController() {

    }
    public void sendRequest(String method, String url, String body) throws Exception {
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
