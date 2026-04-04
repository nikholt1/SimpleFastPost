package org.example.simplefastpost.service;

import org.example.simplefastpost.model.Headers;
import org.example.simplefastpost.repository.HeaderRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HeaderServiceImpl implements HeaderService {


    private final HeaderRepository headerRepository;

    public HeaderServiceImpl(HeaderRepository headerRepository) {
        this.headerRepository = headerRepository;
    }
    @Override
    public Headers createNewHeader(Headers header) {
        try {
            return headerRepository.save(header);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public Headers getHeaderById(Long id) {
        try {
            Headers header = headerRepository.findById(id).get();
            return header;

        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public List<Headers> getAllHeaders() {
        try {
            return headerRepository.findAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Headers> getHeadersByCommandId(Long commandId) {
        List<Headers> allHeaders = getAllHeaders();
        List<Headers> filteredHeaders = new ArrayList<>();

        for (Headers header : allHeaders) {
            if (header.getCommandId() == commandId) {
                filteredHeaders.add(header);
            }
        }

        return filteredHeaders;
    }

    @Override
    public void deleteHeaderById(Long id) {
        try {
            headerRepository.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void deleteAllHeaders() {
        try {
            headerRepository.deleteAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
