package org.example.simplefastpost.service;

import org.example.simplefastpost.model.Headers;

import java.util.List;

public interface HeaderService {
    void createNewHeader(Headers header);
    Headers getHeaderById(Long id);
    List<Headers> getAllHeaders();
    List<Headers> getHeadersByCommandId(int commandId);
    void deleteHeaderById(Long id);
    void deleteAllHeaders();

}
