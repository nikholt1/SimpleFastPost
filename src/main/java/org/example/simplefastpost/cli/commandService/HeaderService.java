package org.example.simplefastpost.cli.commandService;

import org.example.simplefastpost.cli.commandRepo.HeaderRepository;
import org.example.simplefastpost.model.Headers;

import java.sql.SQLException;
import java.util.List;

public class HeaderService {

    public HeaderRepository headerRepository;

    public HeaderService(HeaderRepository headerRepository) {
        this.headerRepository = headerRepository;
    }

    public void createHeader(Headers header) throws SQLException {
        headerRepository.createHeader(header);
    }

    public Headers getHeader(Long headerId) throws SQLException {
        return headerRepository.getHeader(headerId);
    }

    public List<Headers> getAllHeadersFromCommandId(Long commandId) throws SQLException {
        return  headerRepository.getAllHeadersFromCommandId(commandId);
    }

    public void updateHeader(Headers header) throws SQLException {
        headerRepository.updateHeader(header);
    }
    public void deleteHeader(Headers header) throws SQLException {
        headerRepository.deleteHeader(header);
    }

}
