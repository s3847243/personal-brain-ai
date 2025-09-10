package com.example.personalbrain.ingestion.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.personalbrain.ingestion.model.Document;

public interface DocumentRepository extends JpaRepository<Document,UUID>{
    List<Document> findByUserId(UUID userId);

}
