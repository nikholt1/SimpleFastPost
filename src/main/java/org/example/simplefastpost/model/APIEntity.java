package org.example.simplefastpost.model;

import java.time.LocalDateTime;

public class APIEntity {
    private Long id;
    private Long pId;
    private LocalDateTime created;

    public APIEntity(Long  pId, LocalDateTime created) {
        this.pId = pId;
        this.created = created;
    }
    public APIEntity(Long id, Long pId, LocalDateTime created) {
        this.id = id;
        this.pId = pId;
        this.created = created;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getpId() {
        return pId;
    }

    public void setpId(Long pId) {
        this.pId = pId;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }
}
