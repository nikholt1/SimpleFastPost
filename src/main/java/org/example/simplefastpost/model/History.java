package org.example.simplefastpost.model;


import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "command_id")
    private Long commandId;


    private LocalDateTime created;
    private String response;
    private String url;
    private String status;
    private String action; //not necessary
    public History() {
    }

    public History(Long id, Long commandId, LocalDateTime created, String response, String url) {
        this.id = id;
        this.commandId = commandId;
        this.created = created;
        this.response = response;
        this.url = url;
    }


    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    //    id INTEGER PRIMARY KEY AUTOINCREMENT,
//    command_id INTEGER,
//    created TEXT DEFAULT (datetime('now','localtime'))
//    response TEXT,
//    url TEXT
}
