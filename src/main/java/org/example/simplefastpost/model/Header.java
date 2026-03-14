package org.example.simplefastpost.model;

public class Header {

    private Long id;
    private Long commandId;
    private String key;
    private String value;


    public Header(Long id, Long commandId, String key, String value) {
        this.id = id;
        this.commandId = commandId;
        this.key = key;
        this.value = value;
    }

    public Header() {}
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
