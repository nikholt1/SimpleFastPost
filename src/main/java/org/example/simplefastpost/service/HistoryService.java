package org.example.simplefastpost.service;

import org.example.simplefastpost.model.History;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface HistoryService {
    History createNewHistoryEntity(History history);
    History getHistoryById(Long id);

    List<History> getAllHistoryEntities();
    List<History> getHistoryByCommandId(Long commandId);

    History updateHistoryEntity(History history, Long id);

    void deleteAllHistory();
    void deleteHistoryEntityById(Long id);

}
