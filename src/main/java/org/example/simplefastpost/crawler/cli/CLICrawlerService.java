package org.example.simplefastpost.crawler.cli;

import org.example.simplefastpost.model.CrawlerEntity;
import org.example.simplefastpost.model.CrawlerSession;

import java.util.List;

public class CLICrawlerService {

    private final CLICrawlerRepository repo;

    public CLICrawlerService(CLICrawlerRepository repo) {
        this.repo = repo;
    }



    public CrawlerSession createSession(String title) {
        CrawlerSession session = new CrawlerSession(title);
        repo.createSession(session);
        return session;
    }

    public CrawlerEntity createEntity(
            CrawlerSession session,
            CrawlerEntity parent,
            String title,
            String type,
            String url,
            List<String> connections,
            String method,
            String response,
            int processTime
    ) {
        CrawlerEntity entity = new CrawlerEntity();

        entity.setTitle(title);
        entity.setSession(session);
        entity.setParent(parent);
        entity.setType(type);
        entity.setUrl(url);
        entity.setConnections(connections);
        entity.setMethod(method);
        entity.setResponse(response);
        entity.setProcessTime(processTime);

        repo.createEntity(entity);

        // optional: maintain in-memory tree immediately
        session.getCrawlerEntities().add(entity);
        if (parent != null) {
            parent.getChildren().add(entity);
        }

        return entity;
    }



    public List<CrawlerSession> getAllSessions() {
        return repo.getAllSessions();
    }



    public void updateEntity(CrawlerEntity entity) {
        repo.updateEntity(entity);
    }



    public void deleteSession(CrawlerSession session) {
        repo.deleteSession(session.getId());
    }

    public void deleteEntity(CrawlerEntity entity) {
        repo.deleteEntity(entity.getId());
    }
}