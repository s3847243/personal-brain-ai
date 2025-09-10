package com.example.personalbrain.ingestion.queue.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record EmbeddingJob(UUID chunkId, UUID userId, UUID documentId, String text,    LocalDateTime createdAt,
    String tags) implements Serializable {}
