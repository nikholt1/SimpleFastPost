package org.example.simplefastpost.repository;

import org.example.simplefastpost.model.Command;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommandRepository extends JpaRepository<Command, Long> {

}
