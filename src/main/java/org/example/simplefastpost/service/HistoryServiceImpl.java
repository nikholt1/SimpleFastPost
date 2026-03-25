package org.example.simplefastpost.service;


import org.example.simplefastpost.model.History;
import org.example.simplefastpost.repository.HistoryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HistoryServiceImpl implements HistoryService {

    private final HistoryRepository historyRepository;

    public HistoryServiceImpl(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Override
    public History createNewHistoryEntity(History history) {
        try {
            return historyRepository.save(history);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public History getHistoryById(Long id) {
        try {
            History history = historyRepository.findById(id).get();
            return history;
        }  catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<History> getAllHistoryEntities() {
        try {
            return   historyRepository.findAll();
        }  catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public List<History> getHistoryByCommandId(Long commandId) {
        List<History> allHistory =  getAllHistoryEntities();
        List<History> filteredHistory = new ArrayList<>();
        for (History history : allHistory) {
            if (history.getCommandId().equals(commandId)) {
                filteredHistory.add(history);
            }
        }
        return filteredHistory;
    }

    @Override
    public History updateHistoryEntity(History history, Long id) {
        try {
            return  historyRepository.save(history);
        }  catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void deleteAllHistory() {
        try {
            historyRepository.deleteAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteHistoryEntityById(Long id) {
        try {
            historyRepository.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
