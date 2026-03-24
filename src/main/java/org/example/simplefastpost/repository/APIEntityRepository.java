package org.example.simplefastpost.repository;

import org.example.simplefastpost.model.APIEntity;
import org.example.simplefastpost.model.Command;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface APIEntityRepository extends JpaRepository<APIEntity, Long> {
}
