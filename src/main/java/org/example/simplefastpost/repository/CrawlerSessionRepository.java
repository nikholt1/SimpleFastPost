package org.example.simplefastpost.repository;


import org.example.simplefastpost.model.Command;
import org.example.simplefastpost.model.CrawlerSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrawlerSessionRepository extends JpaRepository<CrawlerSession, Long> {
}
