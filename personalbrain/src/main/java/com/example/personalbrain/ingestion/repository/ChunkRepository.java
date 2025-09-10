package com.example.personalbrain.ingestion.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.personalbrain.ingestion.model.Chunk;

public interface ChunkRepository extends JpaRepository<Chunk, UUID> {

    List<Chunk> findByDocumentId(UUID documentId);
    void deleteByDocumentId(UUID documentId); // bulk delete
    List<Chunk> findByUserId(UUID userId);

    List<Chunk> findByUserIdAndCreatedAtBetween(UUID userId, java.time.LocalDateTime start, java.time.LocalDateTime end);
    @Query("select c.id from Chunk c where c.documentId = :documentId")
    List<UUID> findIdsByDocumentId(UUID documentId);
}
