package org.example.simplefastpost.cli.commandService;

import org.example.simplefastpost.cli.commandRepo.HeaderRepository;
import org.example.simplefastpost.model.Header;

import java.sql.SQLException;
import java.util.List;

public class HeaderService {

    public HeaderRepository headerRepository;

    public HeaderService(HeaderRepository headerRepository) {
        this.headerRepository = headerRepository;
    }

    public void createHeader(Header header) throws SQLException {
        headerRepository.createHeader(header);
    }

    public Header getHeader(Long headerId) throws SQLException {
        return headerRepository.getHeader(headerId);
    }

    public List<Header> getAllHeadersFromCommandId(Long commandId) throws SQLException {
        return  headerRepository.getAllHeadersFromCommandId(commandId);
    }

    public void updateHeader(Header header) throws SQLException {
        headerRepository.updateHeader(header);
    }
    public void deleteHeader(Header header) throws SQLException {
        headerRepository.deleteHeader(header);
    }

}
