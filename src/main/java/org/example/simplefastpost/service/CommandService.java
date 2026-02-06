package org.example.simplefastpost.service;

import org.example.simplefastpost.model.Command;
import org.example.simplefastpost.repository.CommandRepository;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class CommandService {
    private final CommandRepository commandRepository;

    public CommandService(CommandRepository commandRepository) {
        this.commandRepository = commandRepository;
    }


    public Command createCommand(Command command) {
        return commandRepository.save(command);
    }

    public List<Command> findAll() {
        return commandRepository.findAll();
    }
    public Command findById(Long id) {
        Optional<Command> commandOptional = commandRepository.findById(id);
        if (commandOptional.isEmpty()) {
            System.out.println("Command with id " + id + " not found");
        }
        return commandRepository.findById(id).get();
    }


}
