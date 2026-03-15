package org.example.simplefastpost.service;

import org.example.simplefastpost.model.Headers;

import java.util.List;

public interface HeaderService {
    Headers createNewHeader(Headers header);
    Headers getHeaderById(Long id);
    List<Headers> getAllHeaders();
    List<Headers> getHeadersByCommandId(Long commandId);
    void deleteHeaderById(Long id);
    void deleteAllHeaders();

}
