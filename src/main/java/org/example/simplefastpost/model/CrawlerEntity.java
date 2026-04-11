package org.example.simplefastpost.model;


import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class CrawlerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private CrawlerSession session;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private CrawlerEntity parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<CrawlerEntity> children = new ArrayList<>();

    private String type;
    private String url;

    @ElementCollection
    private List<String> connections = new ArrayList<>();

    private String method;

    @Column(columnDefinition = "TEXT")
    private String response;

    private int processTime;

    public CrawlerEntity() {}

    // ✅ helper
    public Long getParentId() {
        return parent != null ? parent.getId() : null;
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

    public CrawlerSession getSession() {
        return session;
    }

    public void setSession(CrawlerSession session) {
        this.session = session;
    }

    public CrawlerEntity getParent() {
        return parent;
    }

    public void setParent(CrawlerEntity parent) {
        this.parent = parent;
    }

    public List<CrawlerEntity> getChildren() {
        return children;
    }

    public void setChildren(List<CrawlerEntity> children) {
        this.children = children;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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


    // getters + setters
}