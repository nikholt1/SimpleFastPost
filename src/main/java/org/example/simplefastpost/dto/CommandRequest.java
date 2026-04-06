package org.example.simplefastpost.dto;

import java.util.Map;

public class CommandRequest {
    private String method; // GET, POST, etc.
    private String url;
    private Map<String, String> headers;
    private String body;
    private String response;
    private int status;
    private String expected_response;
    private int expected_status;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getExpected_response() {
        return expected_response;
    }

    public void setExpected_response(String expected_response) {
        this.expected_response = expected_response;
    }

    public int getExpected_status() {
        return expected_status;
    }

    public void setExpected_status(int expected_status) {
        this.expected_status = expected_status;
    }

    // Getters and setters
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}
