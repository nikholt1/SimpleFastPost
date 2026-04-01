package org.example.simplefastpost.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.List;

@Entity
public class CrawlerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;


    // if null == baseParent otherwise its a child, that can have a child
    private Long parentId;
    // ("endpoint" if endpoint and "website" if html page)
    private String type;

    private String URL;
    private List<String> connections;


    // if endpoint
    private String method;
    private String response;

    private int processTime;


    public CrawlerEntity() {}

    public CrawlerEntity(String title, Long parentId, String type, String URL, List<String> connections, String method, String response, int processTime) {
        this.title = title;
        this.parentId = parentId;
        this.type = type;
        this.URL = URL;
        this.connections = connections;
        this.method = method;
        this.response = response;
        this.processTime = processTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public List<String> getConnections() {
        return connections;
    }

    public void setConnections(List<String> connections) {
        this.connections = connections;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public int getProcessTime() {
        return processTime;
    }

    public void setProcessTime(int processTime) {
        this.processTime = processTime;
    }
}
