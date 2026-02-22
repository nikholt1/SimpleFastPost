package org.example.simplefastpost.model;


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

    private Long folderId;

    @ManyToOne
    @JoinColumn(name = "folder_id", nullable = true)
    private Folder folder;

    public Command() {}
    public Command(String name, String command, String body) {
        this.name = name;
        this.command = command;
        this.body = body;
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
