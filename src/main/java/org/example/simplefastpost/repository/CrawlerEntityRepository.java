package org.example.simplefastpost.repository;

import org.example.simplefastpost.model.Command;
import org.example.simplefastpost.model.CrawlerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrawlerEntityRepository extends JpaRepository<CrawlerEntity, Long> {
}
