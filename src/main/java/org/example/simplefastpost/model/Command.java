package org.example.simplefastpost.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
public class Command {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String command;

    @Column(length = 2000)
    private String body;

    @Column(name = "folder_id", insertable = false, updatable = false)
    private Long folderId;

    @ManyToOne
    @JoinColumn(name = "folder_id")
    @JsonBackReference
    private Folder folder;

    private String expected_response;
    private String expected_status;

    public Command() {}
    public Command(String name, String command, String body) {
        this.name = name;
        this.command = command;
        this.body = body;
    }

    public Command(String name, String command, String body, String expected_response, String expected_status) {
        this.name = name;
        this.command = command;
        this.body = body;
        this.expected_response = expected_response;
        this.expected_status = expected_status;
    }

    public String getExpected_status() {
        return expected_status;
    }

    public void setExpected_status(String expected_status) {
        this.expected_status = expected_status;
    }

    public String getExpected_response() {
        return expected_response;
    }

    public void setExpected_response(String expected_response) {
        this.expected_response = expected_response;
    }

    public Long getFolderId() {
        return folderId;
    }

    public void setFolderId(Long folderId) {
        this.folderId = folderId;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }


}
