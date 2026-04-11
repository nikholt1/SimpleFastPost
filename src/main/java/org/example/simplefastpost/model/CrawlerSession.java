package org.example.simplefastpost.model;


import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class CrawlerSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private LocalDateTime created;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CrawlerEntity> crawlerEntities = new ArrayList<>();

    public CrawlerSession() {}

    public CrawlerSession(String title) {
        this.title = title;
        this.created = LocalDateTime.now();
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

    public List<CrawlerEntity> getCrawlerEntities() {
        return crawlerEntities;
    }

    public void setCrawlerEntities(List<CrawlerEntity> crawlerEntities) {
        this.crawlerEntities = crawlerEntities;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }
}
