package org.example.simplefastpost.repository;

import org.example.simplefastpost.model.APIEntity;
import org.example.simplefastpost.model.Command;
import org.springframework.data.jpa.repository.JpaRepository;

public interface APIEntityRepository extends JpaRepository<APIEntity, Long> {
}
