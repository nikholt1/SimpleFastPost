package org.example.simplefastpost.cli.historyService;

import org.example.simplefastpost.cli.historyRepository.HistoryRepository;
import org.example.simplefastpost.model.History;

import java.util.Collections;
import java.util.List;

public class HistoryService {

    private final HistoryRepository historyRepository;

    public HistoryService(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public History createHistoryEntity(History history) {
        try {
            return historyRepository.createNewHistoryEntity(history);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating history", e);
        }
    }

    public History getHistoryById(Long id) {
        try {
            return historyRepository.getHistoryById(id);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching history", e);
        }
    }

    public List<History> getAllHistory() {
        try {
            return historyRepository.getAllHistoryEntities();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching history list", e);
        }
    }

    public List<History> getHistoryByCommandId(Long commandId) {
        try {
            return historyRepository.getHistoryByCommandId(commandId);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching history by commandId", e);
        }
    }

    public History updateHistoryEntity(Long id, History history) {
        try {
            return historyRepository.updateHistoryEntity(history, id);
        } catch (Exception e) {
            throw new RuntimeException("Error updating history", e);
        }
    }

    public boolean deleteHistoryById(Long id) {
        try {
            historyRepository.deleteHistoryEntityById(id);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error deleting history", e);
        }
    }

    public boolean deleteAllHistory() {
        try {
            historyRepository.deleteAllHistory();
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error deleting all history", e);
        }
    }

}
