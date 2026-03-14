package org.example.simplefastpost.repository;


import org.example.simplefastpost.model.Headers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HeaderRepository extends JpaRepository<Headers, Long> {
}
