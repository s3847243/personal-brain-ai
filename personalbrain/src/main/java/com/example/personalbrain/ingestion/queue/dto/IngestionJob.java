package com.example.personalbrain.ingestion.queue.dto;

import java.io.Serializable;
import java.util.UUID;

public record IngestionJob(UUID documentId, UUID userId, String storageKey) implements Serializable {}

